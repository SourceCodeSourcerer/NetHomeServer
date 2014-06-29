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

import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;

/**
 * 
 * LmSensorsFan. This module is a part of the NetHomeServer. 
 * The LmSensorsFan is a fan RPM display which presents data from the built in 
 * sensors of a PC. This item works on Linux and uses the �sensors�-command 
 * to get the data from the system. The sensors-program and information on how to 
 * configure that can be obtained from http://www.lm-sensors.org. The implementation
 * is basically identical to LmSensorsThermometer, so it just inherits that.
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Gauges")
public class LmSensorsFan extends LmSensorsThermometer {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"LmSensorsFan\"  Category=\"Gauges\" >"
			+ "  <Attribute Name=\"Speed\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"rpm\" />"
			+ "  <Attribute Name=\"SensorName\"	Type=\"String\" Get=\"getSensorName\" 	Set=\"setSensorName\" />"
			+ "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
			+ "</HomeItem> "); 


	public LmSensorsFan() {
	}
	
	public String getModel() {
		return m_Model;
	}
}
