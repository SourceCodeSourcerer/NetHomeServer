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

import nu.nethome.home.item.*;
import nu.nethome.util.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * 
 * LmSensorsThermometer. This module is a part of the NetHomeServer. 
 * The LmSensorsThermometer is a thermometer which presents data from the built in 
 * temperature sensors of a PC. This item works on Linux and uses the �sensors�-command 
 * to get the data from the system. The sensors-program and information on how to 
 * configure that can be obtained from http://www.lm-sensors.org
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Thermometers")
public class LmSensorsThermometer extends HomeItemAdapter implements HomeItem, ValueItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"LmSensorsThermometer\"  Category=\"Thermometers\" >"
			+ "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"°C\" />"
			+ "  <Attribute Name=\"SensorName\"	Type=\"String\" Get=\"getSensorName\" 	Set=\"setSensorName\" />"
			+ "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(LmSensorsThermometer.class.getName());
	public ExtendedLoggerComponent m_TempLogger = new ExtendedLoggerComponent(this);
	protected String m_ExecName = "sensors";

	// Public attributes
	protected double m_Value = 0.0;
	protected String m_SensorName = "";

	public LmSensorsThermometer() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#getModel()
	 */
	public String getModel() {
		return m_Model;
	}

	/* Activate the instance
	 * @see ssg.home.HomeItem#activate()
	 */
	public void activate() {
		m_TempLogger.activate(server);
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		m_TempLogger.stop();
	}

	/**
	 * @return Returns the m_Value.
	 */
	public String getValue() {
		try {
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec(m_ExecName);
			InputStream s = proc.getInputStream();
			Scanner scanner = new Scanner(s);
			String linePattern = m_SensorName + ":\\s*[+-]?[0-9.]*";
			String line = scanner.findWithinHorizon(linePattern, 0);
			if ((line != null) && (line.length() != 0)) {
				Scanner lineScanner = new Scanner(line);
				String value = lineScanner.findInLine("[0-9]+.?[0-9]+");
				if ((value != null) && (value.length() != 0)){
					return value;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			//NYI
		}
		return "0";
	}

	/**
	 * @return Returns the m_LogFile.
	 */
	public String getLogFile() {
		return m_TempLogger.getFileName();
	}
	/**
	 * @param LogFile The m_LogFile to set.
	 */
	public void setLogFile(String LogFile) {
		m_TempLogger.setFileName(LogFile);
	}
	/**
	 * @return Returns the m_SensorName.
	 */
	public String getSensorName() {
		return m_SensorName;
	}
	/**
	 * @param SensorName The m_SensorName to set.
	 */
	public void setSensorName(String SensorName) {
		m_SensorName = SensorName;
	}

	/**
	 * @param execName the m_ExecName to set
	 */
	public void setExecName(String execName) {
		m_ExecName = execName;
	}	

}
