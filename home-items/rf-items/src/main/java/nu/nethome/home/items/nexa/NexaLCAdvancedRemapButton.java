/**
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Controls", creationEvents = "NexaL_Message")
public class NexaLCAdvancedRemapButton extends NexaLCRemapButton implements HomeItem {

    private static final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCAdvancedRemapButton\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" 	Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
            + "  <Attribute Name=\"PressOn\" Type=\"Command\" Get=\"getPressOnCommand\" 	Set=\"setPressOnCommand\" />"
            + "  <Attribute Name=\"ReleaseOn\" Type=\"Command\" Get=\"getReleaseOnCommand\" 	Set=\"setReleaseOnCommand\" />"
            + "  <Attribute Name=\"ReleaseLongOn\" Type=\"Command\" Get=\"getReleaseLongOnCommand\" 	Set=\"setReleaseLongOnCommand\" />"
            + "  <Attribute Name=\"PressOff\" Type=\"Command\" Get=\"getPressOffCommand\" 	Set=\"setPressOffCommand\" />"
            + "  <Attribute Name=\"ReleaseOff\" Type=\"Command\" Get=\"getReleaseOffCommand\" 	Set=\"setReleaseOffCommand\" />"
            + "  <Attribute Name=\"ReleaseLongOff\" Type=\"Command\" Get=\"getReleaseLongOffCommand\" 	Set=\"setReleaseLongOffCommand\" />"
            + "  <Attribute Name=\"HoldOffTime\" Type=\"StringList\" Get=\"getHoldOffTime\" 	Set=\"setHoldOffTime\" >"
            + "     <item>0</item> <item>100</item> <item>150</item> <item>200</item> <item>300</item> <item>400</item>  <item>500</item> </Attribute>"
            + "  <Attribute Name=\"LongPressTime\" Type=\"StringList\" Get=\"getLongPressTime\" 	Set=\"setLongPressTime\" >"
            + "     <item>0</item> <item>500</item> <item>700</item> <item>1000</item> <item>1500</item> <item>2000</item>  <item>2500</item> </Attribute>"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(NexaLCAdvancedRemapButton.class.getName());
    private long longPressTime = 1000;
    private String releaseLongOnCommand = "";
    private String releaseOnCommand = "";
    private String pressOnCommand = "";
    private String releaseLongOffCommand = "";
    private String releaseOffCommand = "";
    private String pressOffCommand = "";

    public NexaLCAdvancedRemapButton() {
    }

    @Override
    protected void processEvent(Event event){
        if (!isInHoldOff() && getHoldOffTime().length() != 0) {
            actOnEvent(event);
        }
        super.processEvent(event);
    }
    
    @Override
    protected void actOnEvent(Event event) {
        if (event.getAttribute("NexaL.Command").equals("1")) {
            chooseOnCommand();
        } else {
            chooseOffCommand();
        }
    }

    private void chooseOnCommand() {
        if (isInHoldOff()) {
            long pressTime = System.currentTimeMillis() - getHoldOffStart();
            if (pressTime > longPressTime) {
                performCommand(releaseLongOnCommand);
            } else {
                performCommand(releaseOnCommand);
            }
        } else {
            performCommand(pressOnCommand);
        }
    }

    private void chooseOffCommand() {
        if (isInHoldOff()) {
            long pressTime = System.currentTimeMillis() - getHoldOffStart();
            if (pressTime > longPressTime) {
                performCommand(releaseLongOffCommand);
            } else {
                performCommand(releaseOffCommand);
            }
        } else {
            performCommand(pressOffCommand);
        }
    }

    public String getModel() {
        return m_Model;
    }

    public String getLongPressTime() {
        return Long.toString(longPressTime);
    }

    public void setLongPressTime(String longPressTime) {
        this.longPressTime = Long.parseLong(longPressTime);
    }

    public String getReleaseLongOnCommand() {
        return releaseLongOnCommand;
    }

    public void setReleaseLongOnCommand(String releaseLongOnCommand) {
        this.releaseLongOnCommand = releaseLongOnCommand;
    }

    public String getReleaseOnCommand() {
        return releaseOnCommand;
    }

    public void setReleaseOnCommand(String releaseOnCommand) {
        this.releaseOnCommand = releaseOnCommand;
    }

    public String getPressOnCommand() {
        return pressOnCommand;
    }

    public void setPressOnCommand(String pressOnCommand) {
        this.pressOnCommand = pressOnCommand;
    }

    public String getReleaseLongOffCommand() {
        return releaseLongOffCommand;
    }

    public void setReleaseLongOffCommand(String releaseLongOffCommand) {
        this.releaseLongOffCommand = releaseLongOffCommand;
    }

    public String getReleaseOffCommand() {
        return releaseOffCommand;
    }

    public void setReleaseOffCommand(String releaseOffCommand) {
        this.releaseOffCommand = releaseOffCommand;
    }

    public String getPressOffCommand() {
        return pressOffCommand;
    }

    public void setPressOffCommand(String pressOffCommand) {
        this.pressOffCommand = pressOffCommand;
    }
}
