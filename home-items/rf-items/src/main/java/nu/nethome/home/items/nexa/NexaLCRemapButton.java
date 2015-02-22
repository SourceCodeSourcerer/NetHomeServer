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
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.RemapButton;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;


/**
 * Listens for commands for a specific Nexa Learning Code address and button.
 * When a command is received, the corresponding command is executed.
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Controls", creationEvents = "NexaL_Message")
public class NexaLCRemapButton extends RemapButton implements HomeItem {

    private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" 	Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item>  <item>500</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(NexaLCRemapButton.class.getName());

    // Public attributes
    protected int buttonAddress = 0;
    private int buttonId = 1;

    public NexaLCRemapButton() {
    }

    @Override
    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("NexaL_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt("NexaL.Address") == buttonAddress) &&
                (event.getAttributeInt("NexaL.Button") == buttonId)) {
            //Ok, this event affects us, act on it
            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        buttonAddress = event.getAttributeInt("NexaL.Address");
        buttonId = event.getAttributeInt("NexaL.Button");
        return true;
    }


    @Override
    protected void actOnEvent(Event event) {
        if (event.getAttribute("NexaL.Command").equals("1")) {
            this.on();
        } else {
            this.off();
        }
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return m_Model;
    }

    /**
     * @return Returns the button.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getButton() {
        return Integer.toString(buttonId);
    }

    /**
     * @param sbutton The Button to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setButton(String sbutton) {
        try {
            int button = Integer.parseInt(sbutton);
            if ((button > 0) && (button < 33)) {
                buttonId = button;
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    /**
     * @return Returns the m_Address.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
        return Integer.toString(buttonAddress);
    }

    /**
     * @param address The m_Address to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address) {
        try {
            int newAddress = Integer.parseInt(address);
            if ((newAddress >= 0) && (newAddress < 1 << 26)) {
                buttonAddress = newAddress;
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }
}
