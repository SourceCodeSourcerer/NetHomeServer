package nu.nethome.home.items.zwave;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.zwave.Hex;
import nu.nethome.zwave.messages.ApplicationCommand;
import nu.nethome.zwave.messages.SendData;
import nu.nethome.zwave.messages.commandclasses.AssociatedNode;
import nu.nethome.zwave.messages.commandclasses.CommandArgument;
import nu.nethome.zwave.messages.commandclasses.MultiInstanceAssociationCommandClass;
import nu.nethome.zwave.messages.commandclasses.framework.Command;
import nu.nethome.zwave.messages.commandclasses.framework.CommandCode;
import nu.nethome.zwave.messages.commandclasses.framework.CommandProcessor;
import nu.nethome.zwave.messages.framework.DecoderException;
import nu.nethome.zwave.messages.framework.MessageAdaptor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Hardware")
public class ZWaveNodeExplorer extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ZWaveNodeExplorer\" Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"Node\" Type=\"String\" Get=\"getNode\" Set=\"setNode\" />"
            + "  <Attribute Name=\"Association\" Type=\"String\" Get=\"getAssociation\" Set=\"setAssociation\" />"
            + "  <Attribute Name=\"Associations\" Type=\"String\" Get=\"getAssociations\" />"
            + "  <Action Name=\"getAssociation\"		Method=\"fetchAssociation\" />"
            + "</HomeItem> ");
    public static final String ZWAVE_TYPE = "ZWave.Type";
    public static final String ZWAVE_MESSAGE_TYPE = "ZWave.MessageType";
    public static final String ZWAVE_EVENT_TYPE = "ZWave_Message";

    public static final int TRANSMIT_OPTIONS = SendData.TRANSMIT_OPTION_ACK |
            SendData.TRANSMIT_OPTION_AUTO_ROUTE | SendData.TRANSMIT_OPTION_EXPLORE;

    private static Logger logger = Logger.getLogger(ZWaveNodeExplorer.class.getName());

    private int node = 0;
    private int association = 0;
    private String associations = "";

    public boolean receiveEvent(nu.nethome.home.system.Event event) {
        if (event.isType(ZWAVE_EVENT_TYPE) &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE).length() > 0) {
            byte[] message = Hex.hexStringToByteArray(event.getAttribute(nu.nethome.home.system.Event.EVENT_VALUE_ATTRIBUTE));
            processZWaveMessage(message);
            return true;
        }
        return false;
    }

    private void processZWaveMessage(byte[] message){
        if (MessageAdaptor.decodeMessageId(message).messageId == ApplicationCommand.REQUEST_ID) {
            try {
                ApplicationCommand.Request command = new ApplicationCommand.Request(message, new CommandProcessor() {
                    @Override
                    public CommandCode getCommandCode() {
                        return null;
                    }

                    @Override
                    public Command process(byte[] commandData, CommandArgument argument) throws DecoderException {
                        return new MultiInstanceAssociationCommandClass.Report(commandData);
                    }
                });
                processAssociationReport(command.node, (MultiInstanceAssociationCommandClass.Report)command.command);
            } catch (DecoderException|IOException e) {
                logger.warning("Could not parse ZWave response:" + e.getMessage());
            }
        }
    }

    private void processAssociationReport(int node, MultiInstanceAssociationCommandClass.Report command) {
        if (node == this.node) {
            this.associations = "";
            String spacer = "";
            for (AssociatedNode aNode : command.nodes) {
                associations += spacer + aNode;
                spacer = ", ";
            }
        }
    }

    public String getModel() {
        return String.format(MODEL);
    }

    public String fetchAssociation() {
        associations = "";
        sendRequest(new SendData.Request((byte) node, new MultiInstanceAssociationCommandClass.Get(association), TRANSMIT_OPTIONS).encode());
        return "";
    }

    private void sendRequest(byte[] message) {
        String data = Hex.asHexString(message);
        logger.info(data);
        nu.nethome.home.system.Event event = server.createEvent(ZWAVE_EVENT_TYPE, data);
        event.setAttribute(ZWAVE_TYPE, message[0] == 0 ? "Request" : "Response");
        event.setAttribute(ZWAVE_MESSAGE_TYPE, ((int) message[1]) & 0xFF);
        event.setAttribute("Direction", "Out");
        server.send(event);
    }

    public String getNode() {
        return Integer.toString(node);
    }

    public void setNode(String node) {
        this.node = Integer.parseInt(node);
    }

    public String getAssociation() {
        return Integer.toString(association);
    }

    public void setAssociation(String association) {
        this.association = Integer.parseInt(association);
    }

    public String getAssociations() {
        return associations;
    }
}
