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
public class NexaLCRemapButton4 extends NexaLCRemapButton implements HomeItem {

    class Button {
        private int button;
        private String onCommand = "";
        private String offCommand = "";

        int getButton() {
            return button;
        }

        String getButtonString() {
            return Integer.toString(button);
        }

        void setButton(String button) {
            this.button = getIntValue(button, this.button);
        }

        String getOnCommand() {
            return onCommand;
        }

        void setOnCommand(String onCommand) {
            this.onCommand = onCommand;
        }

        String getOffCommand() {
            return offCommand;
        }

        void setOffCommand(String offCommand) {
            this.offCommand = offCommand;
        }

        private int getIntValue(String newValue, int oldValue) {
            try {
                int button = Integer.parseInt(newValue);
                if ((button > 0) && (button < 33)) {
                    return button;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
            return oldValue;
        }
    }

    private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" 	Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button1\" Type=\"String\" Get=\"getButton1\" 	Set=\"setButton1\" />"
            + "  <Attribute Name=\"Button2\" Type=\"String\" Get=\"getButton2\" 	Set=\"setButton2\" />"
            + "  <Attribute Name=\"Button3\" Type=\"String\" Get=\"getButton3\" 	Set=\"setButton3\" />"
            + "  <Attribute Name=\"Button4\" Type=\"String\" Get=\"getButton4\" 	Set=\"setButton4\" />"
            + "  <Attribute Name=\"OnCommand1\" Type=\"Command\" Get=\"getOnCommand1\" 	Set=\"setOnCommand1\" />"
            + "  <Attribute Name=\"OffCommand1\" Type=\"Command\" Get=\"getOffCommand1\" 	Set=\"setOffCommand1\" />"
            + "  <Attribute Name=\"OnCommand2\" Type=\"Command\" Get=\"getOnCommand2\" 	Set=\"setOnCommand2\" />"
            + "  <Attribute Name=\"OffCommand2\" Type=\"Command\" Get=\"getOffCommand2\" 	Set=\"setOffCommand2\" />"
            + "  <Attribute Name=\"OnCommand3\" Type=\"Command\" Get=\"getOnCommand3\" 	Set=\"setOnCommand3\" />"
            + "  <Attribute Name=\"OffCommand3\" Type=\"Command\" Get=\"getOffCommand3\" 	Set=\"setOffCommand3\" />"
            + "  <Attribute Name=\"OnCommand4\" Type=\"Command\" Get=\"getOnCommand4\" 	Set=\"setOnCommand4\" />"
            + "  <Attribute Name=\"OffCommand4\" Type=\"Command\" Get=\"getOffCommand4\" 	Set=\"setOffCommand4\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item>  <item>500</item> </Attribute>"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(NexaLCRemapButton4.class.getName());

    // Public attributes
    private Button[] buttons = new Button[4];

    public NexaLCRemapButton4() {
        for (int i = 0; i < 4; i++) {
            buttons[i] = new Button();
            buttons[i].setButton(Integer.toString(i + 1));
        }
    }

    @Override
    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("NexaL_Message") &&
                event.getAttribute("Direction").equals("In") &&
                (event.getAttributeInt("NexaL.Address") == buttonAddress) &&
                getButton(event.getAttributeInt("NexaL.Button")) != null) {
            //Ok, this event affects us, act on it
            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected void actOnEvent(Event event) {
        Button button = getButton(event.getAttributeInt("NexaL.Button"));
        if (event.getAttribute("NexaL.Command").equals("1")) {
            performCommand(button.getOnCommand());
        } else {
            performCommand(button.getOffCommand());
        }
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return m_Model;
    }

    public String getButton1() {
        return buttons[0].getButtonString();
    }

    public void setButton1(String sbutton) {
        buttons[0].setButton(sbutton);
    }

    public String getButton2() {
        return buttons[1].getButtonString();
    }

    public void setButton2(String sbutton) {
        buttons[1].setButton(sbutton);
    }

    public String getButton3() {
        return buttons[2].getButtonString();
    }

    public void setButton3(String sbutton) {
        buttons[2].setButton(sbutton);
    }

    public String getButton4() {
        return buttons[3].getButtonString();
    }

    public void setButton4(String sbutton) {
        buttons[3].setButton(sbutton);
    }

    public String getOnCommand1() {
        return buttons[0].getOnCommand();
    }

    public void setOnCommand1(String sbutton) {
        buttons[0].setOnCommand(sbutton);
    }

    public String getOffCommand1() {
        return buttons[0].getOffCommand();
    }

    public void setOffCommand1(String sbutton) {
        buttons[0].setOffCommand(sbutton);
    }

    public String getOnCommand2() {
        return buttons[1].getOnCommand();
    }

    public void setOnCommand2(String sbutton) {
        buttons[1].setOnCommand(sbutton);
    }

    public String getOffCommand2() {
        return buttons[1].getOffCommand();
    }

    public void setOffCommand2(String sbutton) {
        buttons[1].setOffCommand(sbutton);
    }

    public String getOnCommand3() {
        return buttons[2].getOnCommand();
    }

    public void setOnCommand3(String sbutton) {
        buttons[2].setOnCommand(sbutton);
    }

    public String getOffCommand3() {
        return buttons[2].getOffCommand();
    }

    public void setOffCommand3(String sbutton) {
        buttons[2].setOffCommand(sbutton);
    }

    public String getOnCommand4() {
        return buttons[3].getOnCommand();
    }

    public void setOnCommand4(String sbutton) {
        buttons[3].setOnCommand(sbutton);
    }

    public String getOffCommand4() {
        return buttons[3].getOffCommand();
    }

    public void setOffCommand4(String sbutton) {
        buttons[3].setOffCommand(sbutton);
    }

    private Button getButton(int buttonId) {
        Button result = null;
        for (Button button : buttons) {
            if (button.getButton() == buttonId) {
                return button;
            }
        }
        return null;
    }

}
