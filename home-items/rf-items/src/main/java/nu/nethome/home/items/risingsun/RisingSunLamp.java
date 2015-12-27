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

package nu.nethome.home.items.risingsun;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.nexa.NexaLamp;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
* 
* Represents a switch (typically connected to a lamp) which is controlled by 
* the RisingSun RF protocol. The RisingSunLamp requires a port which can send RisingSun protocol
* messages as RF signals. This is typically done with the
* {@see nu.nethome.home.items.audio.AudioProtocolTransmitter}.
* 
*
* @author Stefan
*/
@Plugin
@HomeItemType(value = "Lamps", creationEvents = RisingSunLamp.RISING_SUN_PROTOCOL_NAME)
public class RisingSunLamp extends NexaLamp implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
  				+ "<HomeItem Class=\"RisingSunLamp\" Category=\"Lamps\" >"
  				+ "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
  				+ "  <Attribute Name=\"Channel\" Type=\"String\" Get=\"getChannel\" 	Set=\"setChannel\" />"
  				+ "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
  				+ "  <Action Name=\"on\" 	Method=\"on\" />"
  				+ "  <Action Name=\"off\" 	Method=\"off\" />"
  				+ "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
  				+ "</HomeItem> ");

    public static final String RISING_SUN_PROTOCOL_NAME = "RisingSun_Message";
    private static final String RISING_SUN_HOUSE_CODE_NAME = "RisingSun.Channel";
    private static final String RISING_SUN_BUTTON_NAME = "RisingSun.Button";
    private static final String RISING_SUN_COMMAND_NAME = "RisingSun.Command";

    private int lampChannel = 1;

	public RisingSunLamp() {
        super();
        logger = Logger.getLogger(RisingSunLamp.class.getName());
	}

    @Override
    public String getModel() {
        return MODEL;
    }

    public void sendCommand(int command) {
		Event ev = server.createEvent(getProtocolName(), "");
		ev.setAttribute("Direction", "Out");
		ev.setAttribute(getHouseCodeName(), lampChannel);
		ev.setAttribute(getButtonName(), lampButton);
		ev.setAttribute(getCommandName(), command);
		server.send(ev);
	}

	/**
	 * @return Returns the channel.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getChannel() {
		return Integer.toString(lampChannel);
	}
	
	/**
	 * @param channels The m_Button to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setChannel(String channels) {
		int channel = Integer.parseInt(channels);
		lampChannel = ((channel > 0) && (channel < 9)) ? channel : lampChannel;
	}

    protected String getProtocolName() {
        return RISING_SUN_PROTOCOL_NAME;
    }

    protected String getHouseCodeName() {
        return RISING_SUN_HOUSE_CODE_NAME;
    }

    protected String getButtonName() {
        return RISING_SUN_BUTTON_NAME;
    }

    protected String getCommandName() {
        return RISING_SUN_COMMAND_NAME;
    }
}
