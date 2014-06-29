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
@HomeItemType(value = "Gauges", creationEvents = "FineOffset_Message")
public class FineOffsetHygrometer extends FineOffsetThermometer implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FineOffsetHygrometer\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Humidity\" 	Type=\"String\" Get=\"getValue\" Default=\"true\"  Unit=\"%\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"K\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"M\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "</HomeItem> ");

    // Public attributes
    private double humidity = 0;

    public FineOffsetHygrometer() {
        constantK = 1;
        logger = Logger.getLogger(FineOffsetHygrometer.class.getName());
    }

    @Override
    protected boolean handleEvent(Event event) {
        humidity = constantK * event.getAttributeInt("FineOffset.Moisture") + constantM;
        logger.finer("Humidity update: " + humidity + " degrees");
        return super.handleEvent(event);
    }

    public String getModel() {
        return MODEL;
    }

    public String getValue() {
        return hasBeenUpdated ? String.format("%.1f", humidity) : "";
    }
}
