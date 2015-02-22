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

package nu.nethome.home.items.deltronic;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.RemapButton;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value="Controls", creationEvents = "Deltronic_Message")
public class DeltronicRemapButton extends RemapButton implements HomeItem {

    private final static String MODEL = ("<?xml version = \"1.0\"?> \n"

            + "<HomeItem Class=\"DeltronicRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Button\" 	Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(DeltronicRemapButton.class.getName());

    // Public attributes
    private int itemButton = 0;
    private int itemAddress = 0;

    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Deltronic_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt("Deltronic.Button") == itemButton) &&
                (event.getAttributeInt("Deltronic.Address") == itemAddress)) {

            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        itemAddress = event.getAttributeInt("Deltronic.Address");
        itemButton = event.getAttributeInt("Deltronic.Button");
        return true;
    }

    @Override
    public void actOnEvent(Event event) {
        if (event.getAttribute("Deltronic.Command").equals("1")) {
            this.on();
        } else {
            this.off();
        }
    }

    public String getModel() {
        return MODEL;
    }

    /**
     * @return Returns the deviceCode.
     */

    @SuppressWarnings("UnusedDeclaration")
    public String getAddress() {
        return Integer.toString(itemAddress);
    }

    /**
     * @param address The Address to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAddress(String address) {
        try {
            int result = Integer.parseInt(address);
            if ((result >= 0) && (result < 64)) {
                itemAddress = result;
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    /**
     * @return Returns the m_Button.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getButton() {
        if ((itemButton >= 0) && (itemButton <= 3)) {
            return Character.toString("ABCD".charAt(itemButton));
        }
        return "A";
    }

    /**
     * @param button The m_Button to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setButton(String button) {
        String upperButton = button.toUpperCase();
        if ((upperButton.length() == 1) && (upperButton.compareTo("A") >= 0) &&
                (upperButton.compareTo("D") <= 0)) {
            itemButton = (int) upperButton.charAt(0) - (int) 'A';
        }
    }
}
