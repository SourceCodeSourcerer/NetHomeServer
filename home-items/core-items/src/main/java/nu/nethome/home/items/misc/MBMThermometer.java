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
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;


/**
 * @author Stefan
 *
 */
@Plugin
@HomeItemType("Thermometers")
public class MBMThermometer extends HomeItemAdapter implements HomeItem, ValueItem {

	
	/**
	 * @author Stefan
	 * 
	 * Decodes a syslog message sent by MBM. The messages has the form:
	 * "<29>Nov  5 13:56:12 SSGSERVER MBM[Case]: C=31 LA=5 HA=70 L=27 H=32 A=30"	
	 */
	public class MBMSyslogMessage {
		protected String host = "";
		protected String sensor = "";
		protected double LowAlarm = 0.0;
		protected double highAlarm = 0.0;
		protected double lowestTemp = 0.0;
		protected double highestTemp = 0.0;
		protected double averageTemp = 0.0;
		protected double temperature = 0.0;
		MBMSyslogMessage(String logRow){
			// http://javaalmanac.com/egs/java.util.regex/ParseLine.html
			logRow = logRow.replace("  ", " ");
			logRow = logRow.replace("  ", " ");
			String[] fields = logRow.split(" ");
			if (fields.length < 10 || !(fields[4].subSequence(0, 4).equals("MBM["))) {
				logger.fine("MBMSyslogMessage failed to parse message: " + logRow);
				return;
			}
			host = fields[3];
			sensor = fields[4].substring(4, fields[4].length() - 2);
			temperature = Double.parseDouble(fields[5].substring(2));
			LowAlarm = Double.parseDouble(fields[6].substring(3));
			highAlarm = Double.parseDouble(fields[7].substring(3));
			lowestTemp = Double.parseDouble(fields[8].substring(2));
			highestTemp = Double.parseDouble(fields[9].substring(2));
			averageTemp = Double.parseDouble(fields[10].substring(2));
		}
		/**
		 * @return the AverageTemp
		 */
		public double getAverageTemp() {
			return averageTemp;
		}
		/**
		 * @return the HighAlarm
		 */
		public double getHighAlarm() {
			return highAlarm;
		}
		/**
		 * @return the HighestTemp
		 */
		public double getHighestTemp() {
			return highestTemp;
		}
		/**
		 * @return the Host
		 */
		public String getHost() {
			return host;
		}
		/**
		 * @return the LowAlarm
		 */
		public double getLowAlarm() {
			return LowAlarm;
		}
		/**
		 * @return the LowestTemp
		 */
		public double getLowestTemp() {
			return lowestTemp;
		}
		/**
		 * @return the m_Sensor
		 */
		public String getSensor() {
			return sensor;
		}
		/**
		 * @return the m_Temp
		 */
		public double getTemp() {
			return temperature;
		}

	}
	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"MBMThermometer\" Category=\"Thermometers\" >"
			+ "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"°C\" />"
			+ "  <Attribute Name=\"Port\" Type=\"String\" Get=\"getPort\" 	Set=\"setPort\" />"
			+ "  <Attribute Name=\"SensorName\"	Type=\"String\" Get=\"getSensorName\" 	Set=\"setSensorName\" />"
			+ "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
			+ "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
			+ "</HomeItem> "); 

	private static Logger logger = Logger.getLogger(MBMThermometer.class.getName());
	protected LoggerComponent m_TempLogger = new LoggerComponent(this);

	// Public attributes
	protected double temperature = 0;
	protected String port = "";
	protected String sensorName = "";
	protected String rawMessage = "";
	protected String lastUpdate = "";

	public MBMThermometer() {
	}
	
	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
	 */
	public boolean receiveEvent(Event event) {
		if (event.getAttribute(Event.EVENT_SENDER_ATTRIBUTE).equals(port)) {
			MBMSyslogMessage mess = new MBMSyslogMessage(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
			if (mess.getSensor().equals(sensorName)) {
				temperature = mess.getTemp();
				logger.finer("Received MBMMessage:" + event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
				logger.fine("MBM update: " + temperature + " Degrees");
		        // Format and store the current time.
		        SimpleDateFormat formatter
		            = new SimpleDateFormat ("HH:mm:ss yyyy.MM.dd ");
		        Date currentTime = new Date();
		        lastUpdate = formatter.format(currentTime);
                return true;
			}
		}
		return false;
	}

	public String getModel() {
		return m_Model;
	}

	public void activate() {
		m_TempLogger.activate();
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		m_TempLogger.stop();
	}

	/**
	 * @return Returns the m_PortAddress.
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @param port The m_PortAddress to set.
	 */
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * @return Returns the m_Temp.
	 */
	public String getValue() {
		Object arr[] = new Object[1];
		arr[0] = new Double(temperature);
		return String.format("%.1f", arr);
	}

	/**
	 * @return Returns the m_DeviceCode.
	 */
	public String getSensorName() {
		return sensorName;
	}
	/**
	 * @param SensorName The SensorName to set.
	 */
	public void setSensorName(String SensorName) {
		sensorName = SensorName;
	}
	/**
	 * @return Returns the last raw message.
	 */
	public String getRawMessage() {
		return rawMessage;
	}

	/**
	 * @return the time for the last update
	 */
	public String getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the LastUpdate to set
	 */
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public String getLogFile() {
		return m_TempLogger.getFileName();
	}
	
	public void setLogFile(String LogFile) {
		m_TempLogger.setFileName(LogFile);
	}
}
