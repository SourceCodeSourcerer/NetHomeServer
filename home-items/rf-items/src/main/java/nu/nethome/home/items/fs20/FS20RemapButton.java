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

package nu.nethome.home.items.fs20;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.RemapButton;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;


/**
 * Receives messages from a FS20-button, for example a wall switch and
 * performs a configurable action corresponding to the button press.
 * Actions are specified as command line commands.
 * 
 * @author Stefan Str�mberg
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value="Controls", creationEvents = FHZ1000PcPort.EVENT_TYPE_FS20_EVENT)
public class FS20RemapButton extends RemapButton implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"

			+ "<HomeItem Class=\"FS20RemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"DeviceCode\" Type=\"String\" Get=\"getDeviceCode\" 	Set=\"setDeviceCode\" />"
            + "  <Attribute Name=\"FHZ1000PcPort\" 	Type=\"String\" Get=\"getFHZ1000PcPort\" 		Set=\"setFHZ1000PcPort\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Attribute Name=\"BrightCommand\" Type=\"Command\" Get=\"getBrightCommand\" 	Set=\"setBrightCommand\" />"
            + "  <Attribute Name=\"DimCommand\" Type=\"Command\" Get=\"getDimCommand\" 	Set=\"setDimCommand\" />"
            + "  <Attribute Name=\"ToggleCommand\" Type=\"Command\" Get=\"getToggleCommand\" 	Set=\"setToggleCommand\" />"
            + "  <Attribute Name=\"DimLoopCommand\" Type=\"Command\" Get=\"getDimLoopCommand\" 	Set=\"setDimLoopCommand\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" />"
            + "  <Action Name=\"dimLoop\" 	Method=\"dimLoop\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

	  // Public attributes
	  protected String houseCode = "11111124";
	  protected String deviceCode = "1112";
	  protected String fhz1000PcPort = "FHZ1000PcPort";

	  protected String brightCommand = "";
	  protected String dimCommand = "";
	  protected String toggleCommand = "";
	  protected String dimLoopCommand = "";
	  
	public FS20RemapButton() {
	}

	public boolean receiveEvent(Event event) {
		// Check the events and see if they affect our current state.
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(FHZ1000PcPort.EVENT_TYPE_FS20_EVENT) &&
                event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE).equals(houseCode) &&
				event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE).equals(deviceCode)) {
		    processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        houseCode = event.getAttribute(FHZ1000PcPort.EVENT_HOUSECODE_ATTRIBUTE);
        deviceCode = event.getAttribute(FHZ1000PcPort.EVENT_DEVICECODE_ATTRIBUTE);
        return true;
    }

	public void actOnEvent(Event event) {
        byte command = (byte)event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE);
		switch (command) {
		case FHZ1000PcPort.COMMAND_ON:
			on();
			break;
		case FHZ1000PcPort.COMMAND_DIM_UP:
			bright();
			break;
		case FHZ1000PcPort.COMMAND_OFF:
			off();
			break;
		case FHZ1000PcPort.COMMAND_DIM_DOWN:
			dim();
			break;
		case FHZ1000PcPort.COMMAND_TOGGLE:
			toggle();
			break;
		case FHZ1000PcPort.COMMAND_DIM_LOOP:
			dimLoop();
			break;
		}
	}

	public String getModel() {
		return MODEL;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getHouseCode() {
		return houseCode;
	}

	public void setHouseCode(String houseCode) {
		this.houseCode = houseCode;
	}
	
	public String getFHZ1000PcPort() {
		return fhz1000PcPort;
	}

	public void setFHZ1000PcPort(String port) {
		fhz1000PcPort = port;
	}

	public void bright() {
		performCommand(brightCommand);
	}

	public void dim() {
		performCommand(dimCommand);
	}

	public void toggle() {
		performCommand(toggleCommand);
	}

	public void dimLoop() {
		performCommand(dimLoopCommand);
	}

	public String getBrightCommand() {
		return brightCommand;
	}

	public void setBrightCommand(String brightCommand) {
		this.brightCommand = brightCommand;
	}

	public String getDimCommand() {
		return dimCommand;
	}

	public void setDimCommand(String dimCommand) {
		this.dimCommand = dimCommand;
	}

	public String getDimLoopCommand() {
		return dimLoopCommand;
	}

	public void setDimLoopCommand(String dimLoop) {
		dimLoopCommand = dimLoop;
	}

	public String getToggleCommand() {
		return toggleCommand;
	}

	public void setToggleCommand(String toggleCommand) {
		this.toggleCommand = toggleCommand;
	}
}
