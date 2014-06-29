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
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "UPM_Message")
public class UPMHygrometer extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"UPMHygrometer\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Humidity\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"%\" />"
            + "  <Attribute Name=\"BatteryStatus\" 	Type=\"String\" Get=\"getLowBattery\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"HouseCode\" 	Type=\"String\" Get=\"getHouseCode\" 	Set=\"setHouseCode\" />"
            + "  <Attribute Name=\"DeviceCode\" Type=\"String\" Get=\"getDeviceCode\" 	Set=\"setDeviceCode\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"K\" Type=\"String\" Get=\"getConstantK\" 	Set=\"setConstantK\" />"
            + "  <Attribute Name=\"M\" Type=\"String\" Get=\"getConstantM\" 	Set=\"setConstantM\" />"
            + "</HomeItem> ");
    private static final int BACKWARDS_COMPATIBILITY = 2;
    private static Logger logger = Logger.getLogger(UPMHygrometer.class.getName());
    private LoggerComponent moistureLoggerComponent = new LoggerComponent(this);

    // Public attributes
    private double humidity = 0;
    private String itemHouseCode = "2";
    private String itemDeviceCode = "1";
    private String lastUpdateString = "";
    private int lowBattery = 0;
    private double constantK = 0.5;
    private double constantM = 0;

    public UPMHygrometer() {
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
      */
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("UPM_Message") &&
                event.getAttributeInt("UPM.HouseCode") == Integer.parseInt(itemHouseCode) &&
                event.getAttributeInt("UPM.DeviceCode") == Integer.parseInt(itemDeviceCode)) {
            humidity = event.getAttributeInt("UPM.Secondary") * constantK  * BACKWARDS_COMPATIBILITY + constantM;
            boolean newBatteryLevel = event.getAttributeInt("UPM.LowBattery") != 0;
            if (lowBattery == 0 && newBatteryLevel) {
                logger.warning("Low battery for " + name);
            }
            lowBattery = newBatteryLevel ? 1 : 0;
            logger.finer("Hygrometer update: " + humidity + " %");
            // Format and store the current time.
            SimpleDateFormat formatter
                    = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");
            Date currentTime = new Date();
            lastUpdateString = formatter.format(currentTime);
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

    public String getModel() {
        return MODEL;
    }

    /* Activate the instance
      * @see ssg.home.HomeItem#activate()
      */
    public void activate() {
        moistureLoggerComponent.activate();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        moistureLoggerComponent.stop();
    }

    /**
     * @return Returns the humidity.
     */
    public String getValue() {
        return lastUpdateString.length() > 0 ? String.format("%.1f", humidity) : "";
    }

    /**
     * @return Returns the deviceCode.
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
        return lastUpdateString;
    }

    /**
     * @param lastUpdate the LastUpdate to set
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setLastUpdate(String lastUpdate) {
        lastUpdateString = lastUpdate;
    }

    /**
     * @return Returns the Low Battery warning status.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getLowBattery() {
        if (lowBattery == 0) {
            return "Ok";
        }
        return "Low Battery";
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getLogFile() {
        return moistureLoggerComponent.getFileName();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLogFile(String logfile) {
        moistureLoggerComponent.setFileName(logfile);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getConstantK() {
        return Double.toString(constantK);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setConstantK(String constantK) {
        this.constantK = Double.parseDouble(constantK);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getConstantM() {
        return Double.toString(constantM);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setConstantM(String constantM) {
        this.constantM = Double.parseDouble(constantM);
    }
}
