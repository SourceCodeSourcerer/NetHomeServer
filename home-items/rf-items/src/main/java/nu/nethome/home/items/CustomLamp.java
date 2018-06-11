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

package nu.nethome.home.items;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Represents a switch (typically connected to a lamp) which is controlled by custom
 * commands. The commands could for example be external shell scripts, which operates home made
 * relays where the onCommand could be: "exec /home/serial1_1 on" which would execute the
 * shell script "serial1_1".
 * <br>
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Lamps")
public class CustomLamp extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"CustomLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" Set=\"setOffCommand\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(CustomLamp.class.getName());
    private CommandLineExecutor commandExecutor;

    // Public attributes
    private boolean state = false;
    private String onCommand = "";
    private String offCommand = "";

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        commandExecutor = new CommandLineExecutor(server, true);
    }

    public String getState() {
        if (state) {
            return "On";
        }
        return "Off";
    }

    public void on() {
        logger.fine("Switching on " + name);
        commandExecutor.executeCommandLine(onCommand);
        state = true;
    }

    public void off() {
        logger.fine("Switching off " + name);
        commandExecutor.executeCommandLine(offCommand);
        state = false;
    }

    public void toggle() {
        logger.fine("Toggling " + name);
        if (state) {
            off();
        } else {
            on();
        }
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
}
