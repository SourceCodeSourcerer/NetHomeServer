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
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "UPM_Message")
public class UPMSoilMoisture extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"UPMSoilMoisture\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Moisture\" 	Type=\"String\" Get=\"getValue\" Default=\"true\" Unit=\"%\" />"
            + "  <Attribute Name=\"BatteryLevel\" 	Type=\"String\" Get=\"getBatteryLevel\" Unit=\"%\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"DeviceCode\" Type=\"String\" Get=\"getDeviceCode\" 	Set=\"setDeviceCode\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"RawValue100\" Type=\"String\" Get=\"getRawValue100\" 	Set=\"setRawValue100\" />"
            + "  <Attribute Name=\"RawValue0\" Type=\"String\" Get=\"getRawValue0\" 	Set=\"setRawValue0\" />"
            + "  <Attribute Name=\"MaxRawValue\" Type=\"String\" Get=\"getMaxRawValue\" Init=\"setMaxRawValue\"  />"
            + "  <Attribute Name=\"MinRawValue\" Type=\"String\" Get=\"getMinRawValue\" Init=\"setMinRawValue\" />"
            + "  <Action Name=\"ResetMaxMin\" 	Method=\"resetMaxMin\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(UPMSoilMoisture.class.getName());
    private static final int LOWEST_POSSIBLE_RAW_VALUE = 0;
    private static final int HIGHEST_POSSIBLE_RAW_VALUE = 2000;
    private static final int DEFAULT_100_RAW_VALUE = 1936;
    private static final int DEFAULT_0_RAW_VALUE = 960;
    private ExtendedLoggerComponent tempLoggerComponent = new ExtendedLoggerComponent(this);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    // Public attributes
    private double moisture = 0;
    private int rawValue100 = DEFAULT_100_RAW_VALUE;
    private int rawValue0 = DEFAULT_0_RAW_VALUE;
    private int maxRawValue = LOWEST_POSSIBLE_RAW_VALUE;
    private int minRawValue = HIGHEST_POSSIBLE_RAW_VALUE;
    private String itemHouseCode = "2";
    private String itemDeviceCode = "1";
    private Date latestUpdateOrCreation = new Date();
    private boolean hasBeenUpdated = false;
    private boolean batteryIsLow = false;

    public UPMSoilMoisture() {
    }

    public boolean receiveEvent(Event event) {
        // Check if the event is an UPM_Message and in that case check if it is
        // intended for this item (by House Code and Device Code).
        // See http://wiki.nethome.nu/doku.php/events#upm_message
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("UPM_Message") &&
                event.getAttribute("UPM.HouseCode").equals(itemHouseCode) &&
                event.getAttribute("UPM.DeviceCode").equals(itemDeviceCode)) {
            maxRawValue = Math.max(maxRawValue, event.getAttributeInt("UPM.Primary"));
            minRawValue = Math.min(minRawValue, event.getAttributeInt("UPM.Primary"));
            moisture = calculateMoisture(event.getAttributeInt("UPM.Primary"));
            batteryIsLow = event.getAttributeInt("UPM.LowBattery") != 0;
            logger.finer("Moisture update: " + moisture + " %");
            // Format and store the current time.
            latestUpdateOrCreation = new Date();
            hasBeenUpdated = true;
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        itemHouseCode = event.getAttribute("UPM.HouseCode");
        itemDeviceCode = event.getAttribute("UPM.DeviceCode");
        return true;
    }

    private double calculateMoisture(int rawValue) {
        double result = (rawValue - rawValue0) * 100D / (rawValue100 - rawValue0);
        result = Math.max(0D, result);
        result = Math.min(100D, result);
        return result;
    }

    public String getModel() {
        return MODEL;
    }

    /* Activate the instance
      * @see ssg.home.HomeItem#activate()
      */
    public void activate(HomeService server) {
        super.activate(server);
        // Activate the logger component
        tempLoggerComponent.activate(server);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        tempLoggerComponent.stop();
    }

    public String getValue() {
        return hasBeenUpdated ? String.format("%.1f", moisture) : "";
    }

    public void resetMaxMin() {
        maxRawValue = LOWEST_POSSIBLE_RAW_VALUE;
        minRawValue = HIGHEST_POSSIBLE_RAW_VALUE;
    }

    /**
     * @return Returns the raw value seen at 100% moisture.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getRawValue100() {
        return Integer.toString(rawValue100);
    }

    /**
     * @param k The raw value seen at 100% moisture.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRawValue100(String k) {
        rawValue100 = Integer.parseInt(k);
    }

    /**
     * @return Returns the raw value seen at 100% moisture.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getRawValue0() {
        return Integer.toString(rawValue0);
    }

    /**
     * @param k The raw value seen at 100% moisture.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setRawValue0(String k) {
        rawValue0 = Integer.parseInt(k);
    }

    /**
     * @return Returns the DeviceCode.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDeviceCode() {
        return itemDeviceCode;
    }

    /**
     * @param deviceCode The DeviceCode to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDeviceCode(String deviceCode) {
        itemDeviceCode = deviceCode;
    }

    /**
     * @return Returns the HouseCode.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHouseCode() {
        return itemHouseCode;
    }

    /**
     * @param houseCode The HouseCode to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setHouseCode(String houseCode) {
        itemHouseCode = houseCode;
    }

    /**
     * @return the LastUpdate
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
    }

    /**
     * @return Returns the Low Battery warning status.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getBatteryLevel() {
        if (!hasBeenUpdated) {
            return "";
        }
        return batteryIsLow ? "10" : "100";
    }

    /**
     * @return Returns the LogFile.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getLogFile() {
        return tempLoggerComponent.getFileName();
    }

    /**
     * @param logfile The LogFile to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setLogFile(String logfile) {
        tempLoggerComponent.setFileName(logfile);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getTimeSinceUpdate() {
        return Long.toString((new Date().getTime() - latestUpdateOrCreation.getTime()) / 1000);
    }

    public String getMaxRawValue() {
        if (maxRawValue == LOWEST_POSSIBLE_RAW_VALUE) {
            return "";
        }
        return Integer.toString(maxRawValue);
    }

    public String getMinRawValue() {
        if (minRawValue == HIGHEST_POSSIBLE_RAW_VALUE) {
            return "";
        }
        return Integer.toString(minRawValue);
    }

    public void setMaxRawValue(String maxRawValue) {
        if (maxRawValue.length() == 0) {
            this.maxRawValue = LOWEST_POSSIBLE_RAW_VALUE;
        }
        this.maxRawValue = Integer.parseInt(maxRawValue);
    }

    public void setMinRawValue(String minRawValue) {
        if (minRawValue.length() == 0) {
            this.minRawValue = HIGHEST_POSSIBLE_RAW_VALUE;
        }
        this.minRawValue = Integer.parseInt(minRawValue);
    }
}
