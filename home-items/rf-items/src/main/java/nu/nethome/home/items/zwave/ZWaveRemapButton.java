package nu.nethome.home.items.zwave;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 *
 */
@Plugin
@HomeItemType(value = "Controls", creationEvents = "ZWave_Message")
public class ZWaveRemapButton extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"InstanceId\" 	Type=\"String\" Get=\"getInstanceId\" 	Set=\"setInstanceId\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");
    public static final int APPLICATION_COMMAND_HANDLER = 4;

    private static Logger logger = Logger.getLogger(ZWaveRemapButton.class.getName());

    // Public attributes
    private boolean isEnabled = true;
    private int instanceId;
    private String onCommand = "";
    private String offCommand = "";
    CommandLineExecutor commandExecutor;

    @Override
    public void activate() {
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state. // 0004000607600D00052001FF41
        if (isEnabled && event.isType("ZWave_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttribute(ZWaveController.ZWAVE_TYPE).equals("Request")) &&
                (event.getAttributeInt(ZWaveController.ZWAVE_MESSAGE_TYPE) == APPLICATION_COMMAND_HANDLER) &&
                (getHexValueAt(event.getAttribute("Value"), 8) == instanceId)) {
            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    private void processEvent(Event event) {
        boolean isOn = getHexValueAt(event.getAttribute("Value"), 11) > 0;
        commandExecutor.executeCommandLine(isOn ? onCommand : offCommand);
    }

    private int getHexValueAt(String hexDataString, int index) {
        int i = Integer.parseInt(hexDataString.substring(index * 2, index * 2 + 2), 16);
        return i;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    public String getInstanceId() {
        return Integer.toString(instanceId);
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = Integer.parseInt(instanceId);
    }

    public String getOnCommand() {
        return onCommand;
    }

    public void setOnCommand(String onCommand) {
        this.onCommand = onCommand;
    }

    public String getOffCommand() {
        return offCommand;
    }

    public void setOffCommand(String offCommand) {
        this.offCommand = offCommand;
    }

    public String getState() {
        return isEnabled ? "Enabled" : "Disabled";
    }

    public void setState(String state) {
        isEnabled = state.equalsIgnoreCase("Enabled");
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

}


