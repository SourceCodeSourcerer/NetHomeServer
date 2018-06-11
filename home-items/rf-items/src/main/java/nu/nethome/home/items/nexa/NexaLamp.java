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

package nu.nethome.home.items.nexa;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 *
 * Represents a switch (typically connected to a lamp) which is controlled by 
 * the NEXA RF protocol. The NexaLamp requires a port which can send NEXA protocol
 * messages as RF signals. This is typically done with the
 * {@see nu.nethome.home.items.audio.AudioProtocolTransmitter}.
 *
 * <br>
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "Nexa_Message")
public class NexaLamp extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"HouseCode\" Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"TransmissionRepeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "</HomeItem> ");

    private static final String PROTOCOL_NAME = "Nexa_Message";
    private static final String HOUSE_CODE_NAME = "Nexa.HouseCode";
    private static final String BUTTON_NAME = "Nexa.Button";
    private static final String COMMAND_NAME = "Nexa.Command";
	private static final String LOWEST_HOUSE_CODE_CHAR = "A";
	private static final int LOWEST_HOUSE_CODE = (int)LOWEST_HOUSE_CODE_CHAR.charAt(0);
	private static final String HIGHEST_HOUSE_CODE_CHAR = "P";

	protected Logger logger = Logger.getLogger(NexaLamp.class.getName());

	// Public attributes
	private boolean state = false;
    private String lampHouseCode = LOWEST_HOUSE_CODE_CHAR;
    protected int lampButton = 1;
    protected int repeats = 0;

	public NexaLamp() {
	}

	public boolean receiveEvent(Event event) {
		// Check if this is an inward event directed to this instance
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(getProtocolName()) &&
				event.getAttribute("Direction").equals("In") &&
				(event.getAttributeInt(getHouseCodeName()) == ((int) lampHouseCode.charAt(0)) - LOWEST_HOUSE_CODE) &&
				(event.getAttributeInt(getButtonName()) == (lampButton))) {
			// In that case, update our state accordingly
			state = (event.getAttributeInt(getCommandName()) == 1);
            return true;
		} else {
		    return handleInit(event);
        }
	}

    @Override
    protected boolean initAttributes(Event event) {
        lampHouseCode = "" + (char)(event.getAttributeInt(getHouseCodeName()) + LOWEST_HOUSE_CODE);
        lampButton = event.getAttributeInt(getButtonName());
        return true;
    }

    public String getModel() {
		return MODEL;
	}

	/**
	 * @return Returns the m_State.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getState() {
		if (state) {
			return "On";
		}
		return "Off";
	}

	/**
	 * @return Returns the m_Button.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getButton() {
		return Integer.toString(lampButton);
	}

	/**
	 * @param button The m_Button to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setButton(String button) {
		int buttonNum = Integer.parseInt(button);
		lampButton = ((buttonNum > 0) && (buttonNum < 17)) ? buttonNum : lampButton;
	}

	/**
	 * @return Returns the houseCode.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public String getHouseCode() {
		return lampHouseCode;
	}
	/**
	 * @param houseCode The houseCode to set.
	 */
	@SuppressWarnings("UnusedDeclaration")
    public void setHouseCode(String houseCode) {
		houseCode = houseCode.toUpperCase();
		lampHouseCode = ((houseCode.length() == 1) && (houseCode.compareTo(LOWEST_HOUSE_CODE_CHAR) >= 1) &&
				(houseCode.compareTo(HIGHEST_HOUSE_CODE_CHAR) <= 0)) ? houseCode : lampHouseCode;
	}

	public void sendCommand(int command) {
		Event ev = server.createEvent(getProtocolName(), "");
		ev.setAttribute("Direction", "Out");
		ev.setAttribute(getHouseCodeName(),  ((int) lampHouseCode.charAt(0)) - LOWEST_HOUSE_CODE);
		ev.setAttribute(getButtonName(), lampButton);
		ev.setAttribute(getCommandName(), command);
        if (repeats > 0) {
            ev.setAttribute("Repeat", repeats);
        }
		server.send(ev);
	}

	@SuppressWarnings("UnusedDeclaration")
    public void on() {
		logger.fine("Switching on " + name);
		sendCommand(1);
		state = true;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void off() {
		logger.fine("Switching off " + name);
		sendCommand(0);
		state = false;
	}

	@SuppressWarnings("UnusedDeclaration")
    public void toggle() {
		logger.fine("Toggling " + name);
		state = !state;
		sendCommand(state ? 1 : 0);
	}

    protected String getProtocolName() {
        return PROTOCOL_NAME;
    }

    protected String getHouseCodeName() {
        return HOUSE_CODE_NAME;
    }

    protected String getButtonName() {
        return BUTTON_NAME;
    }

    protected String getCommandName() {
        return COMMAND_NAME;
    }

    public String getRepeats() {
        if (repeats == 0) {
            return "";
        }
        return Integer.toString(repeats);
    }

    public void setRepeats(String repeats) {
        if (repeats.length() == 0) {
            this.repeats = 0;
        } else {
            int newRepeats = Integer.parseInt(repeats);
            if ((newRepeats >= 0) && (newRepeats <= 50)) {
                this.repeats = newRepeats;
            }
        }
    }

}
