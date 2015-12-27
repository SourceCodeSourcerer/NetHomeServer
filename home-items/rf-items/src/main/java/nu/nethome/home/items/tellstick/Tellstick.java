/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.tellstick;

import nu.nethome.coders.encoders.Encoders;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Hardware")
public class Tellstick extends HomeItemAdapter implements HomeItem, ProtocolDecoderSink {

    public static final String RECIEVED_MESSAGE = "+W";
    public static final String RECIEVED_ACK = "+S";
    public static final String RECIEVED_ACK_EXTENDED = "+T";
    public static final int INTER_MESSAGE_DELAY = 50;
    public static final int TIMEOUT_MILLISECONDS = 5000;
    private static Logger logger = Logger.getLogger(Tellstick.class.getName());

    private static final String MODEL1 = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"TellstickDuo\"  Category=\"Hardware\"  Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />");

    private static final String MODEL2 = ("  <Attribute Name=\"FirmwareVersion\" Type=\"String\" Get=\"getFirmwareVersion\" />"
            + "  <Attribute Name=\"ReceivedMessages\" Type=\"String\" Get=\"getReceivedMessages\" />"
            + "  <Attribute Name=\"SentMessages\" Type=\"String\" Get=\"getSentMessages\" />"
            + "  <Action Name=\"reconnect\" Method=\"reconnect\" Default=\"true\" />"
            + "</HomeItem> ");

    private TellstickPort tellstick;
    private EncoderFactory encoderFactory;
    private Map<String, TellstickEventReceiver> eventReceivers = new HashMap<String, TellstickEventReceiver>();

    private volatile int receivedMessages = 0;
    private volatile int sentMessages = 0;
    private String portName = "COM14";
    private SendQueue sendQueue = new SendQueue(TIMEOUT_MILLISECONDS);
    private String receivedProtocols = "UPM,NexaL,Nexa,Oregon,FineOffset";
    private String transmittedProtocols;
    private Set<String> transmittedProtocolSet = new HashSet<String>();

    public Tellstick() {
        encoderFactory = new EncoderFactory(Encoders.getAllTypes());
        String separator = "";
        StringBuilder protocols = new StringBuilder();
        for (String name : encoderFactory.getProtocolNames()) {
            transmittedProtocolSet.add(name);
            protocols.append(separator);
            protocols.append(name);
            separator = ",";
        }
        transmittedProtocols = protocols.toString();
        addEventReceiver(new UPMEventReceiver(this));
        addEventReceiver(new NexaLEventReceiver(this));
        addEventReceiver(new NexaEventReceiver(this));
        addEventReceiver(new OregonEventReceiver(this));
        addEventReceiver(new FineOffsetEventReceiver(this));
        addEventReceiver(new RollerTrolEventReceiver(this));
    }

    private void addEventReceiver(TellstickEventReceiver eventReceiver) {
        eventReceivers.put(eventReceiver.getEventType(), eventReceiver);
    }

    private List<String> supportedReceivedProtocols() {
        List<String> result = new ArrayList<String>();
        for (TellstickEventReceiver receiver : eventReceivers.values()) {
            result.add(receiver.getProtocolName());
        }
        return result;
    }

    @Override
    public String getModel() {
        StringBuilder model = new StringBuilder();
        model.append(MODEL1);
        addReceivedProtocolsAttribute(model);
        addTransmittedProtocolsAttribute(model);
        addPortNameAttribute(model);
        model.append(MODEL2);
        return model.toString();
    }

    private void addReceivedProtocolsAttribute(StringBuilder model) {
        model.append("  <Attribute Name=\"ReceivedProtocols\" Type=\"Strings\" Get=\"getReceivedProtocols\" Set=\"setReceivedProtocols\" >");
        for (String protocol : supportedReceivedProtocols()) {
            model.append("<item>");
            model.append(protocol);
            model.append("</item>");
        }
        model.append("  </Attribute>");
    }

    private void addTransmittedProtocolsAttribute(StringBuilder model) {
        model.append("  <Attribute Name=\"TransmittedProtocols\" Type=\"Strings\" Get=\"getTransmittedProtocols\" Set=\"setTransmittedProtocols\" >");
        for (String protocol : encoderFactory.getProtocolNames()) {
            model.append("<item>");
            model.append(protocol);
            model.append("</item>");
        }
        model.append("  </Attribute>");
    }

    public boolean receiveEvent(Event event) {
        if (!event.getAttribute("Direction").equals("Out") || tellstick == null) {
            return false;
        }
        ProtocolEncoder foundEncoder = encoderFactory.getEncoder(event);
        if (foundEncoder != null && transmittedProtocolSet.contains(foundEncoder.getInfo().getName())) {
            try {
                Message parameters = encoderFactory.extractMessage(event);
                int repeat = calculateRepeat(event, foundEncoder);
                int rawMessage[] = foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED);
                RawMessage message = sendQueue.newMessage(new RawMessage(rawMessage, repeat));
                if (message != null) {
                    tellstick.sendCommand(message.getData(), message.getRepeat());
                    sentMessages++;
                }
                return true;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not send message to Tellstick", e);
                reconnect();
            }
        }
        return false;
    }

    private void addPortNameAttribute(StringBuilder model) {
        model.append("  <Attribute Name=\"PortName\" Type=\"StringList\" Get=\"getPortName\" Set=\"setPortName\" >");
        List<String> ports = TellstickPort.listSerialPorts();
        model.append("<item>");
        model.append(portName);
        model.append("</item>");
        for (String port : ports) {
            model.append("<item>");
            model.append(port);
            model.append("</item>");
        }
        model.append("</Attribute>");
    }

    private int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }

    @Override
    public void activate() {
        createTellstickPort();
    }

    void createTellstickPort() {
        try {
            tellstick = new TellstickPort(portName, new TellstickPort.Client() {
                @Override
                public void received(String message) {
                    receivedTellstickEvent(message);
                }
            });
        } catch (IOException e) {
            logger.log(Level.WARNING, "failed to open serial port to Tellstick: " + portName, e);
        }
    }

    @Override
    public void stop() {
        try {
            if (tellstick != null) {
                tellstick.stop();
                tellstick = null;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "failed to close serial port: " + portName, e);
        }
    }

    public void setReceivedProtocols(String protocols) {
        Set<String> protocolSet = new HashSet<String>();
        Collections.addAll(protocolSet, protocols.split(","));
        for (TellstickEventReceiver receiver : eventReceivers.values()) {
            receiver.setActive(protocolSet.contains(receiver.getProtocolName()));
        }
        this.receivedProtocols = protocols;
    }

    public String getReceivedProtocols() {
        return receivedProtocols;
    }

    public void setTransmittedProtocols(String transmittedProtocols) {
        transmittedProtocolSet.clear();
        Collections.addAll(transmittedProtocolSet, transmittedProtocols.split(","));
        this.transmittedProtocols = transmittedProtocols;
    }

    public String getTransmittedProtocols() {
        return transmittedProtocols;
    }

    public void receivedTellstickEvent(String message) {
        receivedMessages++;
        logger.fine("Received from Tellstick: " + message);
        if (message.equals(TellstickPort.ERROR_MESSAGE)) {
            logger.warning("Error in serial communication, disconnecting");
            reconnect();
            return;
        }
        if (message.startsWith(RECIEVED_ACK) || message.startsWith(RECIEVED_ACK_EXTENDED)) {
            handleAck(message);
        } else if (message.startsWith(RECIEVED_MESSAGE)) {
            handleReceivedMessage(message);
        }
    }

    private void handleReceivedMessage(String message) {
        sendQueue.flush();
        TellstickEvent event = new TellstickEvent(message);
        TellstickEventReceiver handler = getHandlerForEvent(event);
        if (handler != null) {
            handler.processEvent(event);
        }
    }

    private TellstickEventReceiver getHandlerForEvent(TellstickEvent event) {
        TellstickEventReceiver tellstickEventReceiver = eventReceivers.get(event.getEventProtocolModel());
        if (tellstickEventReceiver == null) {
            tellstickEventReceiver = eventReceivers.get(event.getEventProtocol());
        }
        return tellstickEventReceiver;
    }

    private void handleAck(String message) {
        try {
            Thread.sleep(INTER_MESSAGE_DELAY);
            RawMessage rawMessage = sendQueue.messageAcknowledge();
            if (message != null) {
                tellstick.sendCommand(rawMessage.getData(), rawMessage.getRepeat());
            }
        } catch (InterruptedException e) {
            // Silently ignore
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not send message to Tellstick", e);
        }
    }

    @Override
    public void parsedMessage(ProtocolMessage message) {
        // Build an internal event from the protocol message
        Event event = server.createEvent(message.getProtocol() + "_Message", "");
        // Specify message direction
        event.setAttribute("Direction", "In");
        // Add all protocol specific parameters as event attributes. Use protocol
        // name as prefix to avoid mix with standard attribute names
        for (FieldValue f : message.getFields()) {
            event.setAttribute(message.getProtocol() + "." + f.getName(), f.getStringValue());
        }
        // Send the event internally
        server.send(event);
    }

    @Override
    public void partiallyParsedMessage(String protocol, int bits) {
        //Do nothing
    }

    @Override
    public void reportLevel(int level) {
        //Do nothing
    }

    public void reconnect() {
        stop();
        activate();
    }

    public String getState() {
        return (getFirmwareVersion().length() > 0) ? "Connected" : "Not connected";
    }

    public String getReceivedMessages() {
        return Integer.toString(receivedMessages);
    }

    public String getSentMessages() {
        return Integer.toString(sentMessages);
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String m_PortName) {
        this.portName = m_PortName;
    }

    public String getFirmwareVersion() {
        return tellstick != null ? tellstick.getFirmwareVersion() : "";
    }
}
