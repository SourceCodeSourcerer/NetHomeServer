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

package nu.nethome.home.items.oregon;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Presents and logs temperature values received by an Oregon Scientific-temperature sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive UPM messages from the hardware devices.
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Thermometers", creationEvents = "Oregon_Message")
public class OregonThermometer extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"OregonThermometer\" Category=\"Thermometers\" >"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"°C\" />"
            + "  <Attribute Name=\"SensorModel\" 	Type=\"String\" Get=\"getSensorType\" />"
            + "  <Attribute Name=\"BatteryLevel\" 	Type=\"String\" Get=\"getBatteryLevel\"  Unit=\"%\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"Channel\" 	Type=\"String\" Get=\"getChannel\" 	Set=\"setChannel\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"K\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"M\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(OregonThermometer.class.getName());
    private LoggerComponent tempLoggerComponent = new LoggerComponent(this);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    // Public attributes
    private double temperature = 0;
    protected double constantK = 0.1;
    protected double constantM = 0;
    private String itemChannel = "";
    private String itemDeviceId = "";
    private Date latestUpdateOrCreation = new Date();
    protected boolean hasBeenUpdated = false;
    private boolean batteryIsLow = false;
    private String sensorType = "";

    public OregonThermometer() {
    }

    public boolean receiveEvent(Event event) {
        // Check if the event is an Oregon_Message and in that case check if it is
        // intended for this thermometer (by channel and device id).
        // See http://wiki.nethome.nu/doku.php/events#upm_message
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("Oregon_Message") &&
                (event.getAttribute("Oregon.Channel").equals(itemChannel) &&
                        (event.getAttribute("Oregon.Id").equals(itemDeviceId) || itemDeviceId.length() == 0))) {
            return handleEvent(event);
        } else {
            return handleInit(event);
        }
    }

    protected boolean handleEvent(Event event) {
        temperature = constantK * event.getAttributeInt("Oregon.Temp") + constantM;
        boolean newBatteryLevel = event.getAttributeInt("Oregon.LowBattery") != 0;
        if (!batteryIsLow && newBatteryLevel) {
            logger.warning("Low battery for " + name);
        }
        batteryIsLow = newBatteryLevel;
        sensorType = String.format("%4X", event.getAttributeInt("Oregon.SensorId"));
        logger.finer("Temperature update: " + temperature + " degrees");
        // Format and store the current time.
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
        return true;
    }

    @Override
    protected boolean initAttributes(Event event) {
        itemChannel = event.getAttribute("Oregon.Channel");
        itemDeviceId = event.getAttribute("Oregon.Id");
        return true;
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
        tempLoggerComponent.activate();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        tempLoggerComponent.stop();
    }

    public String getValue() {
        return hasBeenUpdated ? String.format("%.1f", temperature) : "";
    }

    /**
     * @return Returns the K.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getK() {
        return Double.toString(constantK);
    }

    /**
     * @param k The K to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setK(String k) {
        constantK = Double.parseDouble(k);
    }

    /**
     * @return Returns the M.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getM() {
        return Double.toString(constantM);
    }

    /**
     * @param m The M to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setM(String m) {
        constantM = Double.parseDouble(m);
    }

    /**
     * @return Returns the DeviceId.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getDeviceId() {
        return itemDeviceId;
    }

    /**
     * @param DeviceId The DeviceId to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDeviceId(String DeviceId) {
        itemDeviceId = DeviceId;
    }

    /**
     * @return Returns the Channel.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getChannel() {
        return itemChannel;
    }

    /**
     * @param Channel The Channel to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setChannel(String Channel) {
        itemChannel = Channel;
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

    public String getSensorType() {
        return sensorType;
    }
}
