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
import nu.nethome.home.item.*;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


/**
 * 
 * ClassName
 * 
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Gauges")
public class ValueLogger extends HomeItemAdapter implements ValueItem, HomeItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ValueLogger\" Category=\"Gauges\"  >"
            + "  <Attribute Name=\"Value\" Type=\"String\" Get=\"getValue\" Default=\"true\" />"
            + "  <Attribute Name=\"ValueAction\" Type=\"String\" Get=\"getValueAction\" 	Set=\"setValueAction\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(ValueLogger.class.getName());
    protected CommandLineExecutor executor;

	// Public attributes
    private LoggerComponent valueLoggerComponent = new LoggerComponent(this);
    protected String valueAction = "get,OutThermometer,Temperature";

	public String getModel() {
		return m_Model;
	}

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        valueLoggerComponent.activate();
    }

    @Override
    public void stop() {
        valueLoggerComponent.stop();
    }

    public String getLogFile() {
        return valueLoggerComponent.getFileName();
    }

    public void setLogFile(String logfile) {
        valueLoggerComponent.setFileName(logfile);
    }

    public String getValueAction() {
        return valueAction;
    }

    public void setValueAction(String valueAction) {
        this.valueAction = valueAction;
    }

    @Override
    public String getValue() {
        String result = executor.executeCommandLine(getValueAction());
        String results[] = result.split(",");
        if (results.length != 3 || !results[0].equalsIgnoreCase("ok") || results[2].length() == 0) {
            return "";
        }
        return results[2].replace("%2C", ".");
    }
}

