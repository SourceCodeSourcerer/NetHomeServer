package nu.nethome.home.items.zwave;

import jssc.SerialPortException;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.MessageProcessor;
import nu.nethome.zwave.PortException;
import nu.nethome.zwave.ZWavePort;
import nu.nethome.zwave.messages.AddNode;
import nu.nethome.zwave.messages.ApplicationCommand;
import nu.nethome.zwave.messages.MemoryGetId;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MessageAdaptor;
import nu.nethome.zwave.messages.framework.UndecodedMessage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class ZWaveController extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveController\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"PortName\" Type=\"StringList\" Get=\"getPortName\" Set=\"setPortName\" >"
            + "    %s </Attribute>"
            + "  <Attribute Name=\"HomeId\" Type=\"String\" Get=\"getHomeId\" />"
            + "  <Attribute Name=\"NodeId\" Type=\"String\" Get=\"getNodeId\" />"
            + "  <Action Name=\"RequestIdentity\" 	Method=\"requestIdentity\" Default=\"true\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"StartInclusion\"		Method=\"startInclusion\" />"
            + "  <Action Name=\"EndInclusion\"		Method=\"endInclusion\" />"
            + "</HomeItem> ");
    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";
    public static final String ZWAVE_EVENT_TYPE = "ZWave_Message";
    public static final String ZWAVE_COMMAND_CLASS = "ZWave.CommandClass";
    public static final String ZWAVE_COMMAND = "ZWave.Command";
    public static final String ZWAVE_NODE = "ZWave.Node";

    private static Logger logger = Logger.getLogger(ZWaveController.class.getName());

    private ZWavePort port;
    private String portName = "/dev/ttyAMA0";
    private int homeId = 0;
    private int nodeId = 0;

    public boolean receiveEvent(nu.nethome.home.system.Event event) {
        if (event.isType(ZWAVE_EVENT_TYPE) &&
                event.getAttribute("Direction").equals("Out") &&
                event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE).length() > 0 &&
                port != null && port.isOpen()) {
            byte[] message = Hex.hexStringToByteArray(event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            try {
                port.sendMessage(message);
            } catch (SerialPortException e) {
                logger.warning("Failed to send ZWave message: " + event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            }
            return true;
        }
        return false;
    }

    public String getModel() {
        return String.format(MODEL, getPortNames());
    }

    private String getPortNames() {
        StringBuilder model = new StringBuilder();
        List<String> ports = ZWavePort.listAvailablePortNames();
        model.append("<item>");
        model.append(portName);
        model.append("</item>");
        for (String port : ports) {
            model.append("<item>");
            model.append(port);
            model.append("</item>");
        }
        return model.toString();
    }


    @Override
    public void activate() {
        openPort();
    }

    private void openPort() {
        try {
            port = new ZWavePort(portName);
            port.setReceiver(new MessageProcessor() {
                @Override
                public UndecodedMessage.Message process(byte[] message) {
                    logger.fine("Receiving Message");
                    ZWaveController.this.receiveMessage(message);
                    return null;
                }
            });
            logger.fine("Created port");
            requestIdentity();
        } catch (PortException e) {
            logger.log(Level.WARNING, "Could not open ZWave port", e);
        }
    }

    public String requestIdentity() {
        try {
            port.sendMessage(new MemoryGetId.Request().encode());
        } catch (SerialPortException e) {
            logger.log(Level.WARNING, "Could not send ZWave initial message", e);
        }
        return "";
    }

    private void closePort() {
        logger.fine("Closing port");
        nodeId = 0;
        homeId = 0;
        if (port != null) {
            port.close();
            port = null;
        }
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        closePort();
        openPort();
    }

    public String startInclusion() {
        return sendRequest(new AddNode.Request(AddNode.Request.InclusionMode.ANY));
    }

    public String endInclusion() {
        return sendRequest(new AddNode.Request(AddNode.Request.InclusionMode.STOP));
    }

    private String sendRequest(MessageAdaptor request) {
        try {
            if (port != null && port.isOpen()) {
                port.sendMessage(request.encode());
            }
        } catch (SerialPortException e) {
            logger.log(Level.WARNING, "Could not send ZWave message", e);
        }
        return "";
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        closePort();
    }


    private void receiveFrameByte(byte frameByte) {
        String data = String.format("%02X", frameByte);
        logger.info(data);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, "FrameByte");
        event.setAttribute("Direction", "In");
        server.send(event);
    }

    private void receiveMessage(byte[] message) {
        String data = Hex.asHexString(message);
        logger.info(data);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int) message[1]) & 0xFF);
        event.setAttribute("Direction", "In");
        try {
            if (MessageAdaptor.decodeMessageId(message).messageId == ApplicationCommand.REQUEST_ID) {
                ApplicationCommand.Request request = new ApplicationCommand.Request(message);
                event.setAttribute(ZWAVE_NODE, request.node);
                Command command = request.command;
                if (command.getCommandClass() == MultiInstanceCommandClass.COMMAND_CLASS && command.getCommand() == MultiInstanceCommandClass.ENCAP_V2) {
                    MultiInstanceCommandClass.EncapsulationV2 encap = new MultiInstanceCommandClass.EncapsulationV2(command.encode());
                    event.setAttribute("ZWave.Endpoint", encap.instance);
                    command = encap.command;
                }
                event.setAttribute(ZWAVE_COMMAND_CLASS, command.getCommandClass());
                event.setAttribute(ZWAVE_COMMAND, command.getCommand());
            }
            server.send(event);
            if (MessageAdaptor.decodeMessageId(message).messageId == MemoryGetId.MEMORY_GET_ID) {
                MemoryGetId.Response memoryGetIdResponse = new MemoryGetId.Response(message);
                homeId = memoryGetIdResponse.homeId;
                nodeId = memoryGetIdResponse.nodeId;
            }
        } catch (DecoderException | IOException e) {
            logger.warning("Could not parse ZWave response:" + e.getMessage());
        }
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String serialPort) {
        if (!portName.equals(serialPort)) {
            portName = serialPort;
            closePort();
            if (isActivated()) {
                openPort();
            }
        }
    }

    public String getState() {
        if (port == null) {
            return "Disconnected";
        } else if (homeId == 0) {
            return "Connecting";
        } else {
            return "Connected";
        }
    }

    public String getHomeId() {
        return homeId != 0 ? Integer.toHexString(homeId) : "";
    }

    public String getNodeId() {
        return homeId != 0 ? Integer.toString(nodeId) : "";
    }
}
