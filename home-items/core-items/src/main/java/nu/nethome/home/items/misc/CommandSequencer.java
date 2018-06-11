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

package nu.nethome.home.items.misc;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;


/**
 * 
 * Scene
 * 
 * @author Stefan
 */
@SuppressWarnings("unused")
@Plugin
@HomeItemType("Controls")
public class CommandSequencer extends Scene implements HomeItem {

	private final static String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"CommandSequencer\"  Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
			+ "  <Attribute Name=\"Command1\" Type=\"Command\" Get=\"getCommand1\" 	Set=\"setCommand1\" />"
			+ "  <Attribute Name=\"Command2\" Type=\"Command\" Get=\"getCommand2\" 	Set=\"setCommand2\" />"
			+ "  <Attribute Name=\"Command3\" Type=\"Command\" Get=\"getCommand3\" 	Set=\"setCommand3\" />"
			+ "  <Attribute Name=\"Command4\" Type=\"Command\" Get=\"getCommand4\" 	Set=\"setCommand4\" />"
			+ "  <Attribute Name=\"Command5\" Type=\"Command\" Get=\"getCommand5\" 	Set=\"setCommand5\" />"
			+ "  <Attribute Name=\"Command6\" Type=\"Command\" Get=\"getCommand6\" 	Set=\"setCommand6\" />"
			+ "  <Attribute Name=\"LatestCommand\" Type=\"String\" Get=\"getLatestCommand\" 	Set=\"setLatestCommand\" />"
            + "  <Action Name=\"Next\" 	Method=\"next\" />"
            + "  <Action Name=\"Previous\" 	Method=\"previous\" />"
            + "  <Action Name=\"First\" 	Method=\"first\" />"
            + "  <Action Name=\"Last\" 	Method=\"last\" />"
            + "  <Action Name=\"Enable\" 	Method=\"enableScene\" />"
            + "  <Action Name=\"Disable\" 	Method=\"disableScene\" />"
			+ "</HomeItem> ");

    private static final int MIN_COMMAND = 1;
    private static final int MAX_COMMAND = 6;
	private static Logger logger = Logger.getLogger(CommandSequencer.class.getName());

	public String getModel() {
		return MODEL;
	}
	int latestCommand = 1;

    public String next() {
        int nextCommand = latestCommand + 1;
        if ((nextCommand > MAX_COMMAND) || (getCommand(nextCommand).isEmpty())) {
            nextCommand = latestCommand;
        }
        return performCommandN(nextCommand);
    }

    public String previous() {
        int nextCommand = latestCommand - 1;
        if ((nextCommand < MIN_COMMAND)) {
            nextCommand = latestCommand;
        }
        return performCommandN(nextCommand);
    }

    private String performCommandN(int nextCommand) {
        if (!activeState) {
            return "";
        }
        performCommand(getCommand(nextCommand));
        latestCommand = nextCommand;
        return "";
    }

    public String first() {
        return performCommandN(1);
    }

    public String last() {
        int i;
        for (i = MAX_COMMAND; (i > MIN_COMMAND) && getCommand(i).isEmpty(); i--);
        return performCommandN(i);
    }

    private String getCommand(int index) {
        switch (index) {
            case 1: return getCommand1();
            case 2: return getCommand2();
            case 3: return getCommand3();
            case 4: return getCommand4();
            case 5: return getCommand5();
            case 6: return getCommand6();
            default: return "";
        }
    }

    public String getLatestCommand() {
        return getIntAttribute(latestCommand);
    }

    public void setLatestCommand(String latestCommand) throws IllegalValueException {
        this.latestCommand = setIntAttribute(latestCommand, 1, 6);
    }
}
