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

package nu.nethome.home.items.fineoffset;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Presents and logs temperature values received by an FineOffset-temperature sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive FineOffset messages from the hardware devices.
 *
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Thermometers", creationEvents = "FineOffset_Message")
public class FineOffsetThermometer extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FineOffsetThermometer\" Category=\"Thermometers\" >"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"°C\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"K\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"M\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "</HomeItem> ");

    protected Logger logger = Logger.getLogger(FineOffsetThermometer.class.getName());
    private ExtendedLoggerComponent tempLoggerComponent = new ExtendedLoggerComponent(this);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    // Public attributes
    private double temperature = 0;
    protected double constantK = 0.1;
    protected double constantM = 0;
    private String itemDeviceId = "";
    private Date latestUpdateOrCreation = new Date();
    protected boolean hasBeenUpdated = false;

    public FineOffsetThermometer() {
    }

    public boolean receiveEvent(Event event) {
        // Check if the event is an FineOffset_Message and in that case check if it is
        // intended for this thermometer.
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("FineOffset_Message") &&
                (event.getAttribute("FineOffset.Identity").equals(itemDeviceId))) {
            return handleEvent(event);
        } else {
            return handleInit(event);
        }
    }

    protected boolean handleEvent(Event event) {
        temperature = constantK * event.getAttributeInt("FineOffset.Temp") + constantM;
        logger.finer("Temperature update: " + temperature + " degrees");
        // Format and store the current time.
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
        return true;
    }

    @Override
    protected boolean initAttributes(Event event) {
        itemDeviceId = event.getAttribute("FineOffset.Identity");
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
        tempLoggerComponent.activate(server);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        tempLoggerComponent.stop();
    }

    public String getValue() {
        return getTemp();
    }

    public String getTemp() {
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

    public String getDeviceId() {
        return itemDeviceId;
    }

    public void setDeviceId(String DeviceId) {
        itemDeviceId = DeviceId;
    }

    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
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
