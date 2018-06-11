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

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.HomeService;
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
public class Scene extends HomeItemAdapter implements HomeItem {

	private final static String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"Scene\"  Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
			+ "  <Attribute Name=\"Delay\" Type=\"String\" Get=\"getDelay\" 	Set=\"setDelay\" />"
			+ "  <Attribute Name=\"Command1\" Type=\"Command\" Get=\"getCommand1\" 	Set=\"setCommand1\" />"
			+ "  <Attribute Name=\"Command2\" Type=\"Command\" Get=\"getCommand2\" 	Set=\"setCommand2\" />"
			+ "  <Attribute Name=\"Command3\" Type=\"Command\" Get=\"getCommand3\" 	Set=\"setCommand3\" />"
			+ "  <Attribute Name=\"Command4\" Type=\"Command\" Get=\"getCommand4\" 	Set=\"setCommand4\" />"
			+ "  <Attribute Name=\"Command5\" Type=\"Command\" Get=\"getCommand5\" 	Set=\"setCommand5\" />"
			+ "  <Attribute Name=\"Command6\" Type=\"Command\" Get=\"getCommand6\" 	Set=\"setCommand6\" />"
			+ "  <Action Name=\"Action\" Method=\"action\" Default=\"true\" />"
            + "  <Action Name=\"Enable\" 	Method=\"enableScene\" />"
            + "  <Action Name=\"Disable\" 	Method=\"disableScene\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(Scene.class.getName());
    protected boolean activeState = true;
	private CommandLineExecutor commandExecutor;
	

	// Public attributes
	private long commandDelay = 0; // Delay in ms
	private String command1 = "";
	private String command2 = "";
	private String command3 = "";
	private String command4 = "";
	private String command5 = "";
	private String command6 = "";

	public Scene() {
	}
	
	public String getModel() {
		return MODEL;
	}

	public void activate(HomeService server) {
        super.activate(server);
        commandExecutor = new CommandLineExecutor(server, true);
	}

	public void stop() {
	}

    public String getState(){
        return activeState ? "Enabled" : "Disabled";
    }

    public void setState(String state) {
        activeState = state.compareToIgnoreCase("disabled") != 0;
    }

    public void action() {
        if (!activeState) {
            return;
        }
		try {
			performCommand(command1);
			if (command2.length() != 0) Thread.sleep(commandDelay);
			performCommand(command2);
			if (command3.length() != 0) Thread.sleep(commandDelay);
			performCommand(command3);
			if (command4.length() != 0) Thread.sleep(commandDelay);
			performCommand(command4);
			if (command5.length() != 0) Thread.sleep(commandDelay);
			performCommand(command5);
			if (command6.length() != 0) Thread.sleep(commandDelay);
			performCommand(command6);
		}
		catch (InterruptedException i) {
        	// Do Dinada
		}
	}
	
	protected void performCommand(String commandString) {
		String result = commandExecutor.executeCommandLine(commandString);
		if (!result.startsWith("ok")) {
			logger.warning(result);
		}
	}

    public String enableScene() {
        activeState = true;
        return "";
    }
	  
    public String disableScene() {
        activeState = false;
        return "";
    }

	public String getDelay() {
		return Double.toString(commandDelay / 1000.0);
	}
	public void setDelay(String Delay) {
		commandDelay = Math.round(Double.parseDouble(Delay) * 1000);
	}

	public String getCommand2() {
		return command2;
	}
	public void setCommand2(String Command2) {
		command2 = Command2;
	}
	public String getCommand1() {
		return command1;
	}
	public void setCommand1(String Command1) {
		command1 = Command1;
	}	
	public String getCommand3() {
		return command3;
	}
	public void setCommand3(String Command3) {
		command3 = Command3;
	}	
	public String getCommand4() {
		return command4;
	}
	public void setCommand4(String Command4) {
		command4 = Command4;
	}	
	public String getCommand5() {
		return command5;
	}
	public void setCommand5(String Command5) {
		command5 = Command5;
	}
	public String getCommand6() {
		return command6;
	}
	public void setCommand6(String Command6) {
		command6 = Command6;
	}
}
