/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.jeelink;

import nu.nethome.coders.decoders.*;
import nu.nethome.coders.encoders.Encoders;
import nu.nethome.coders.encoders.ShortBeepEncoder;
import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.UsbScanner;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;
import nu.nethome.util.ps.impl.ProtocolDecoderGroup;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * CULTransceiver ...
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Hardware", creationInfo = JeeLink.JeeLinkCreationInfo.class)
public class JeeLink extends HomeItemAdapter implements HomeItem, ProtocolDecoderSink {

    public static final String VID = "0403";
    public static final String PID = "6001";

    public static class JeeLinkCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UsbScanner.USB_REPORT_TYPE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return isJeeLinkUsbEvent(e) && !e.getAttribute(UsbScanner.EVENT).equals("Removed");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("JeeLink RF transmitter");
        }
    }

    private final String MODEL1 = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"JeeLink\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getConnected\" Default=\"true\" />");

    private final String MODEL2 = ("  <Attribute Name=\"FirmwareVersion\" Type=\"String\" Get=\"getFirmwareVersion\"  />"
            + "  <Attribute Name=\"SendCount\" Type=\"String\" Get=\"getSendCount\"  />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"PlayTestBeep\"		Method=\"playTestBeep\" />"
            + "  <Attribute Name=\"TestBeepFrequency\" Type=\"String\" Get=\"getTestBeepFrequency\" 	Set=\"setTestBeepFrequency\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(JeeLink.class.getName());
    private ProtocolDecoderGroup protocolDecoders = new ProtocolDecoderGroup();

    // Public attributes
    private int testBeepFrequency = 2000;
    JeeLinkProtocolPort port;
    private long sendCount = 0;
    private float modulationFrequency = 0;
    private EncoderFactory factory;
    private String portName = "COM2";
    private String lastErrorMessage = "Not Connected";


    public JeeLink() {
        // Create the Protocol-Decoders and add them to the decoder group
        Collection<Class<? extends ProtocolDecoder>> allTypes = Decoders.getAllTypes();
        for (Class<? extends ProtocolDecoder> decoder : allTypes) {
            try {
                protocolDecoders.add(decoder.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                // Ignore
            }
        }
        for (ProtocolDecoder decoder : protocolDecoders.getAllDecoders()) {
            decoder.setTarget(this);
        }
        factory = new EncoderFactory(Encoders.getAllTypes());
    }

    public boolean receiveEvent(Event event) {
        if (isJeeLinkUsbEvent(event)) {
            if (!event.getAttribute(UsbScanner.EVENT).equals("Present") && isActivated()) {
                reconnect();
            }
            return true;
        } else if (!event.getAttribute("Direction").equals("Out") || port == null) {
            return false;
        }
        ProtocolEncoder foundEncoder = factory.getEncoder(event);
        if (foundEncoder != null) {
            try {
                Message parameters = factory.extractMessage(event);
                int repeat = calculateRepeat(event, foundEncoder);
                int modulationFrequency = calculateModulationFrequency(event, foundEncoder, parameters);
                setModulationFrequencyOnPort(modulationFrequency);
                port.playMessage(foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED), repeat, 0);
                setModulationFrequency(getModulationFrequency());
                sendCount++;
                return true;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            } catch (PortException e) {
                logger.warning("Failed to send to serial port");
                closePort();
            }
        }
        return false;
    }

    private static boolean isJeeLinkUsbEvent(Event e) {
        return e.getAttribute(UsbScanner.VENDOR_ID).equals(VID) &&
                e.getAttribute(UsbScanner.PRODUCT_ID).equals(PID);
    }

    private int calculateModulationFrequency(Event event, ProtocolEncoder encoder, Message message) {
        if (event.getAttribute("Modulation").equals("On")) {
            return encoder.modulationFrequency(message);
        }
        return 0;
    }

    private int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return MODEL1 + getPortNameAttribute() + MODEL2;
    }

    private String getPortNameAttribute() {
        StringBuilder model = new StringBuilder();
        model.append("  <Attribute Name=\"SerialPort\" Type=\"StringList\" Get=\"getSerialPort\" Set=\"setSerialPort\" >");
        List<String> ports = JeeLinkProtocolPort.listAvailablePortNames();
        model.append("<item>");
        model.append(portName);
        model.append("</item>");
        for (String port : ports) {
            model.append("<item>");
            model.append(port);
            model.append("</item>");
        }
        model.append("</Attribute>");
        return model.toString();
    }


    public void activate(HomeService server) {
        super.activate(server);
        factory.addEncoderTypes(server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class));
        openPort();
    }

    private void openPort() {
        try {
            port = new JeeLinkProtocolPort(portName, protocolDecoders);
        } catch (PortException e) {
            logger.warning("Could not open serial port " + portName + " in " + name);
            lastErrorMessage = e.getMessage();
        }
    }

    private void closePort() {
        if (port != null) {
            port.close();
            port = null;
        }
        lastErrorMessage = "Not Connected";
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        closePort();
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        closePort();
        openPort();
    }

    /**
     * Play a short test beep. This is for test purposes
     */
    public void playTestBeep() {
        ShortBeepEncoder beep = new ShortBeepEncoder();
        beep.setFrequency(testBeepFrequency);
        beep.setDuration(0.05F);
        if (port != null) {
            try {
                port.playMessage(beep.encode(), 10, 0);
            } catch (PortException e) {
                // Failed to send - ignore
            }
        }
        sendCount++;
    }

    public String getSerialPort() {
        return portName;
    }

    public void setSerialPort(String serialPort) {
        if (!portName.equals(serialPort)) {
            portName = serialPort;
            closePort();
            if (isActivated()) {
                openPort();
            }
        }
    }

    public String getTestBeepFrequency() {
        return Integer.toString(testBeepFrequency);
    }

    public void setTestBeepFrequency(String TestBeepFrequency) {
        testBeepFrequency = Integer.parseInt(TestBeepFrequency);
    }

    public String getConnected() {
        if (port == null) {
            return lastErrorMessage;
        }
        return port.getReportedVersion().isEmpty() ? "Searching for firmware" : "Connected";
    }

    public void parsedMessage(ProtocolMessage message) {
        // TODO Auto-generated method stub

    }

    public void partiallyParsedMessage(String protocol, int bits) {
        // TODO Auto-generated method stub

    }

    public void reportLevel(int level) {
        // TODO Auto-generated method stub

    }

    public String getModulationFrequency() {
        return Float.toString(modulationFrequency);
    }

    public void setModulationFrequency(String newModulationFrequency) {
        float frequency = Float.parseFloat(newModulationFrequency);
        if ((frequency >= 10000) && (frequency <= 100000)) {
            modulationFrequency = frequency;
        } else {
            modulationFrequency = 0;
        }
        setModulationFrequencyOnPort(modulationFrequency);
    }

    private void setModulationFrequencyOnPort(float frequency) {
        int modulationPeriod;
        if ((frequency > 0)) {
            modulationPeriod = (int) (1 / (frequency * 375E-9 * 2.0));
        } else {
            modulationPeriod = 0;
        }
        port.setModulationOnPeriod(modulationPeriod);
        port.setModulationOffPeriod(modulationPeriod);
    }

    public String getSendCount() {
        return Long.toString(sendCount);
    }

    public String getFirmwareVersion() {
        return port != null ? port.getReportedVersion() : "";
    }
}
