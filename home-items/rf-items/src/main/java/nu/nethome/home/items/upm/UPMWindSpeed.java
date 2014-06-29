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

package nu.nethome.home.items.upm;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Presents and logs wind speed values received by an UPM-wind meter. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive UPM messages from the hardware devices.
 *
 * @author Stefan, modified by Fredric
 */
@Plugin
@HomeItemType("Gauges")
public class UPMWindSpeed extends HomeItemAdapter implements HomeItem, ValueItem {

	private final String m_Model = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"UPMWindSpeed\" Category=\"Gauges\" >"
			+ "  <Attribute Name=\"WindSpeed\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"m/s\" />"
			+ "  <Attribute Name=\"WindSpeedMean\" 	Type=\"String\" Get=\"getWindSpeedMean\" Unit=\"m/s\" />"
			+ "  <Attribute Name=\"WindSpeedGust\" 	Type=\"String\" Get=\"getWindSpeedGust\" Unit=\"m/s\" />"
			+ "  <Attribute Name=\"BatteryStatus\" 	Type=\"String\" Get=\"getLowBattery\" />"
			+ "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
			+ "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
			+ "</HomeItem> "); 
	
	protected static final int WINDMETER_HOUSE_CODE = 10;
	protected static final int WINDMETER_DEVICE_CODE = 2;
	/* wind speed mean value shall be calculated as mean speed for 10 minutes and wind gust is
	 * the highest measured wind speed in 10 minutes according to
	 * meteorology standards. (actually, a gust has to be over 10 knots to be regarded as a gust but
	 * this isn't handled here)*/
	protected static final int WIND_SPEED_MEAN_TIME = 10;
	protected static final double WIND_SPEED_CONV_FACTOR = 3.6;
	private static Logger logger = Logger.getLogger(UPMWindSpeed.class.getName());
	protected LoggerComponent m_WindSpeedLogger = new LoggerComponent(this);
	protected int m_LogInterval = 10;

	// Public attributes
	protected double m_WindSpeed = 0;
	protected double m_WindSpeedMean = 0;
	protected double m_WindSpeedGust = 0;
	protected String m_LastUpdate = "";
	protected int m_LowBattery = 0;

	public class storedWindSpeed{
		Calendar timeStamp;
		double windSpeed = 0;
		protected storedWindSpeed() {}
		public storedWindSpeed(Calendar time, double wind){
			timeStamp = time;
			windSpeed = wind;
		}
	}
	
	static LinkedList<storedWindSpeed> m_WindSpeedlist = new LinkedList<storedWindSpeed>();

	public UPMWindSpeed() {
	}

	/* (non-Javadoc)
	 * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
	 */
	public boolean receiveEvent(Event event) {
		// Check if the event is an UPM_Message and in that case check if it is
		// intended for this thermometer (by House Code and Device Code).
		// See http://wiki.nethome.nu/doku.php/events#upm_message
		if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("UPM_Message")) {
			if (event.getAttributeInt("UPM.HouseCode") == WINDMETER_HOUSE_CODE &&
					event.getAttributeInt("UPM.DeviceCode") == WINDMETER_DEVICE_CODE) {
				// Recalculate the raw wind speed value to m/s
				m_WindSpeed = event.getAttributeInt("UPM.Primary")/WIND_SPEED_CONV_FACTOR;
                int newBatteryStatus = event.getAttributeInt("UPM.LowBattery");
                if ((m_LowBattery == 0) && (newBatteryStatus != 0)) {
                    logger.warning("Low battery for " + name);
                }
                m_LowBattery = newBatteryStatus;
				logger.fine("WindSpeed update: " + m_WindSpeed + " m/s");
		        // Format and store the current time.
		        SimpleDateFormat formatter
		            = new SimpleDateFormat ("HH:mm:ss yyyy.MM.dd ");
		        Date currentTime = new Date();
		        handleWindSpeedStat(currentTime, m_WindSpeed);
		        m_LastUpdate = formatter.format(currentTime);
                return true;
			}
		}
		return false;
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
		// Activate the logger component
		m_WindSpeedLogger.setInterval(Integer.toString(m_LogInterval));
		m_WindSpeedLogger.activate();
	}

	/**
	 * HomeItem method which stops all object activity for program termination
	 */
	public void stop() {
		m_WindSpeedLogger.stop();
	}

	/* (non-Javadoc)
	 * @see ssg.home.items.ValueItem#getValue()
	 */
	public String getValue() {
		// Format the value for printing
		Object arr[] = new Object[1];
		arr[0] = new Double(m_WindSpeed);
		return String.format("%.1f", arr);
	}
	
	/**
	 * @return Wind speed mean for 10 minutes (meteorology wind speed)
	 */
	public String getWindSpeedMean(){
		return String.format("%.1f", m_WindSpeedMean);
	}
	
	/**
	 * @return Wind speed gust. Highest wind speed for last 10 minutes
	 */
	public String getWindSpeedGust(){
		return String.format("%.1f", m_WindSpeedGust);
	}
	
	/**
	 * @return the LastUpdate
	 */
	public String getLastUpdate() {
		return m_LastUpdate;
	}

	/**
	 * @param lastUpdate the LastUpdate to set
	 */
	public void setLastUpdate(String lastUpdate) {
		m_LastUpdate = lastUpdate;
	}
	/**
	 * @return Returns the Low Battery warning status.
	 */
	public String getLowBattery() {
		if (m_LowBattery == 0) {
			return "Ok";
		}
		return "Low Battery";
	}
	/**
	 * @return Returns the LogFile.
	 */
	public String getLogFile() {
		return m_WindSpeedLogger.getFileName();
	}
	/**
	 * @param LogFile The LogFile to set.
	 */
	public void setLogFile(String LogFile) {
		m_WindSpeedLogger.setFileName(LogFile);
	}
	
	/**
	 * @param time Current time
	 * @param newSpeed Current wind speed
	 */
	private void handleWindSpeedStat(Date time, double newSpeed){
		double tmpMean = 0;
		double tmpGaust = 0;
		Calendar timeStamp = Calendar.getInstance();
		timeStamp.setTime(time);
		storedWindSpeed newItem = new storedWindSpeed(timeStamp, newSpeed);
        
		m_WindSpeedlist.add(newItem);
        /* Remove obsolete items e.g older than WIND_SPEED_MEAN_TIME minutes */
        Calendar tmpCal = (Calendar) timeStamp.clone();
        tmpCal.add(Calendar.MINUTE, -WIND_SPEED_MEAN_TIME);
        while(m_WindSpeedlist.peek().timeStamp.before(tmpCal)){
        	m_WindSpeedlist.removeFirst();
        }
        /* calculate new mean value and gust value*/
        for(storedWindSpeed sw : m_WindSpeedlist){
        	tmpMean += sw.windSpeed;
        	if(sw.windSpeed > tmpGaust){
        		tmpGaust = sw.windSpeed;
        	}
        }
        
        m_WindSpeedMean = tmpMean/m_WindSpeedlist.size();
        m_WindSpeedGust = tmpGaust;
	}
}
