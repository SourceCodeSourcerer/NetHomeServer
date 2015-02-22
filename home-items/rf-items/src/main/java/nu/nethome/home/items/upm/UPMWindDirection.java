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
import java.util.Date;
import java.util.logging.Logger;

/**
 * Presents and logs wind direction values received by an UPM-wind meter. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive UPM messages from the hardware devices.
 *
 * @author Stefan, modified by Fredric
 */
@Plugin
@HomeItemType("Gauges")
public class UPMWindDirection extends HomeItemAdapter implements HomeItem, ValueItem {

    private final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"UPMWindDirection\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"WindDirection\" 	Type=\"String\" Get=\"getValue\" Default=\"true\" />"
            + "  <Attribute Name=\"BatteryStatus\" 	Type=\"String\" Get=\"getLowBattery\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "</HomeItem> ");

    protected static final int WINDMETER_HOUSE_CODE = 10;
    protected static final int WINDMETER_DEVICE_CODE = 2;
    protected static final double WIND_DIR_CONV_FACTOR = 22.5; //Convert to deg.
    private static Logger logger = Logger.getLogger(UPMWindDirection.class.getName());
    protected ExtendedLoggerComponent m_WindDirectionLogger = new ExtendedLoggerComponent(this);

    // Public attributes
    protected double m_WindDirection = 0;
    protected String m_LastUpdate = "";
    protected int m_LowBattery = 0;

    public UPMWindDirection() {
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
                // Recalculate the wind direction to deg.
                m_WindDirection = event.getAttributeInt("UPM.Secondary") * WIND_DIR_CONV_FACTOR;
                int newBatteryStatus = event.getAttributeInt("UPM.LowBattery");
                if ((m_LowBattery == 0) && (newBatteryStatus != 0)) {
                    logger.warning("Low battery for " + name);
                }
                m_LowBattery = newBatteryStatus;
                logger.fine("WindDirection update: " + m_WindDirection);
                // Format and store the current time.
                SimpleDateFormat formatter
                        = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");
                Date currentTime = new Date();
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
        m_WindDirectionLogger.activate(server);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        m_WindDirectionLogger.stop();
    }

    /* (non-Javadoc)
     * @see ssg.home.items.ValueItem#getValue()
     */
    public String getValue() {
        // Format the value for printing
        Object arr[] = new Object[1];
        arr[0] = new Double(m_WindDirection);
        return String.format("%.1f", arr);
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
        return m_WindDirectionLogger.getFileName();
    }

    /**
     * @param LogFile The LogFile to set.
     */
    public void setLogFile(String LogFile) {
        m_WindDirectionLogger.setFileName(LogFile);
    }
}
