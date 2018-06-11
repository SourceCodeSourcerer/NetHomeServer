/*
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

package nu.nethome.home.items;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Presents and logs temperature values received as an event. The events may be sent by any kind of
 * receiver module which can receive temperature values from hardware devices.
 *
 * @author Stefan
 */
@SuppressWarnings("unused")
@Plugin
@HomeItemType(value = "Thermometers", creationEvents = "Temperature_Message")
public class GenericThermometer extends HomeItemAdapter implements HomeItem, ValueItem {

    protected static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"%s\" Category=\"%s\" >"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\" Unit=\"°C\" />"
            + "  <Attribute Name=\"BatteryLevel\" 	Type=\"String\" Get=\"getBatteryLevel\"  Unit=\"%%\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "%s"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"K\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"M\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "</HomeItem> ");

    protected static final String ADDRESS = ("  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />");

    protected static Logger logger = Logger.getLogger(GenericThermometer.class.getName());
    private ExtendedLoggerComponent tempLoggerComponent = new ExtendedLoggerComponent(this);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    // Public attributes
    private double temperature = 0;
    private double constantK = 0.1;
    private double constantM = 0;
    protected String address = "1";
    private Date latestUpdateOrCreation = new Date();
    private boolean hasBeenUpdated = false;
    private boolean batteryIsLow = false;

    public GenericThermometer() {
    }

    public String getModel() {
        return String.format(MODEL, "GenericThermometer", "Thermometers", ADDRESS);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("Temperature_Message") && event.getAttribute("Address").equals(address)) {
            boolean newBatteryLevel = event.getAttributeInt("LowBattery") != 0;
            update(event.getAttributeInt(Event.EVENT_VALUE_ATTRIBUTE), newBatteryLevel);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        address = event.getAttribute("Address");
        return true;
    }

    protected void update(int rawTemperature, boolean newBatteryLevel) {
        temperature = constantK * rawTemperature + constantM;
        if (!batteryIsLow && newBatteryLevel) {
            logger.warning("Low battery for " + name);
        }
        batteryIsLow = newBatteryLevel;
        logger.finer("Temperature update: " + temperature + " degrees");
        // Format and store the current time.
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
    }

    public void activate(HomeService server) {
        super.activate(server);
        // Activate the logger component
        tempLoggerComponent.activate(server);
    }

    public void stop() {
        tempLoggerComponent.stop();
    }

    public String getValue() {
        return hasBeenUpdated ? String.format("%.1f", temperature) : "";
    }

    public String getK() {
        return Double.toString(constantK);
    }

    public void setK(String k) {
        constantK = Double.parseDouble(k);
    }

    public String getM() {
        return Double.toString(constantM);
    }

    public void setM(String m) {
        constantM = Double.parseDouble(m);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String deviceCode) {
        address = deviceCode;
    }

    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
    }

    public String getBatteryLevel() {
        return batteryIsLow ? "10" : "100";
    }

    public String getLogFile() {
        return tempLoggerComponent.getFileName();
    }

    public void setLogFile(String logfile) {
        tempLoggerComponent.setFileName(logfile);
    }

    public String getTimeSinceUpdate() {
        return Long.toString((new Date().getTime() - latestUpdateOrCreation.getTime()) / 1000);
    }
}
