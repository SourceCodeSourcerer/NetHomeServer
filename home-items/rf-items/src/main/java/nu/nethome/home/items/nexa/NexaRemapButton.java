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


/**
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Controls", creationEvents = "Nexa_Message")
public class NexaRemapButton extends RemapButton implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"StringList\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" >"
            + "     <item>A</item> <item>B</item> <item>C</item> <item>D</item> <item>E</item> <item>F</item> <item>G</item> <item>H</item> </Attribute>"
            + "  <Attribute Name=\"Button\" Type=\"StringList\" Get=\"getButton\" 	Set=\"setButton\" >"
            + "     <item>1</item> <item>2</item> <item>3</item> <item>4</item> <item>5</item> <item>6</item> <item>7</item> <item>8</item> </Attribute>"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    // Public attributes
    private int buttonHouseCode = 0;
    private int buttonNumber = 1;

    public NexaRemapButton() {
    }

    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Nexa_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt("Nexa.HouseCode") == buttonHouseCode) &&
                (event.getAttributeInt("Nexa.Button") == buttonNumber)) {

            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        buttonHouseCode = event.getAttributeInt("Nexa.HouseCode");
        buttonNumber = event.getAttributeInt("Nexa.Button");
        return true;
    }

    @Override
    protected void actOnEvent(Event event) {
        if (event.getAttribute("Nexa.Command").equals("1")) {
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
    public String getButton() {
        return Integer.toString(buttonNumber);
    }

    /**
     * @param deviceCode The deviceCode to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setButton(String deviceCode) {
        try {
            int result = Integer.parseInt(deviceCode);
            if ((result > 0) && (result <= 8)) {
                buttonNumber = result;
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    /**
     * @return Returns the houseCode.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHouseCode() {
        if ((buttonHouseCode >= 0) && (buttonHouseCode <= 7)) {
            return Character.toString("ABCDEFGH".charAt(buttonHouseCode));
        }
        return "A";
    }

    /**
     * @param houseCode The HouseCode to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setHouseCode(String houseCode) {
        String hc = houseCode.toUpperCase();
        if ((hc.length() == 1) && (hc.compareTo("A") >= 0) &&
                (hc.compareTo("H") <= 0)) {
            buttonHouseCode = (int) hc.charAt(0) - (int) 'A';
        }
    }
}
