/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.ikea;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import static nu.nethome.home.items.ikea.IkeaGateway.IKEA_NODE_ID;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationInfo = IkeaColorTemperatureLamp.IkeaCreationInfo.class)
public class IkeaColorTemperatureLamp extends IkeaLamp implements HomeItem {

    protected static final String COLOR_X = "5709";
    protected static final String COLOR_Y = "5710";
    private static final int X_MIN = 24930;
    private static final int X_MAX = 33135;
    private static final int Y_MIN = 24694;
    private static final int Y_MAX = 27211;

    public static class IkeaCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {IkeaGateway.IKEA_NODE_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.isType(IkeaGateway.IKEA_NODE_MESSAGE) &&
                    (e.getAttributeInt(IkeaGateway.IKEA_NODE_TYPE) == 2) &&
                    hasColor(e);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Ikea CT Lamp %d: \"%s\"",e.getAttributeInt(IKEA_NODE_ID), e.getAttribute(IkeaGateway.IKEA_NODE_NAME));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"IkeaColorTemperatureLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getLampId\" 	Set=\"setLampId\" />"
            + "  <Attribute Name=\"LampModel\" Type=\"String\" Get=\"getLampModel\" 	Init=\"setLampModel\" />"
            + "  <Attribute Name=\"Version\" Type=\"String\" Get=\"getLampVersion\" 	Init=\"setLampVersion\" />"
            + "  <Attribute Name=\"Brightness\" Type=\"String\" Get=\"getCurrentBrightness\"  Unit=\"%\" />"
            + "  <Attribute Name=\"OnBrightness\" Type=\"String\" Get=\"getBrightness\" 	Set=\"setBrightness\" />"
            + "  <Attribute Name=\"ColorTemperature\" Type=\"String\" Get=\"getColor\" 	Set=\"setColor\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Attribute Name=\"DimStep\" Type=\"String\" Get=\"getDimStep\" 	Set=\"setDimStep\" />"
            + "  <Attribute Name=\"RefreshInterval\" Type=\"String\" Get=\"getRefreshInterval\"  Set=\"setRefreshInterval\"  Unit=\"Minutes\" />"
            + "  <Attribute Name=\"WarmDim\" Type=\"Boolean\" Get=\"getWarmOnDim\" 	Set=\"setWarmOnDim\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "  <Action Name=\"Update\" 	Method=\"fetchCurrentState\" />"
            + "</HomeItem> ");

    private boolean warmOnDim;

    public IkeaColorTemperatureLamp() {
    }

    public String getModel() {
        return MODEL;
    }

    public String getColor() {
        return colorTemperature == NOT_SET ? "" : Integer.toString(colorTemperature);
    }

    public void setColor(String color) {
        if (color.length() == 0) {
            colorTemperature = NOT_SET;
        } else {
            int newColorTemperature = Integer.parseInt(color);
            if ((newColorTemperature >= 0) && (newColorTemperature <= 100) && (newColorTemperature != colorTemperature)) {
                colorTemperature = newColorTemperature;
                if (isOn) {
                    on();
                }
            }
        }
    }

    protected JSONObject createLightObject(int brightness, int temperature) {
        JSONObject light = super.createLightObject(brightness, temperature);
        if (temperature != NOT_SET) {
            int temp;
            if (warmOnDim) {
                temp = ((100-temperature) * (100 - brightness)) / 100 + temperature;
            } else {
                temp = temperature;
            }
            light.put(COLOR_X, percentToX(temp));
            light.put(COLOR_Y, percentToY(temp));
        }
        return light;
    }

    protected static int percentToX(int temperature) {
        return X_MIN + (temperature * (X_MAX - X_MIN)) / 100;
    }
    protected static int percentToY(int temperature) {
        return Y_MIN + (temperature * (Y_MAX - Y_MIN)) / 100;
    }


    protected void sendOnCommand(String dimAndColor) {
        int dimLevel;
        int colorTemperature = this.colorTemperature;

        String[] colourParts = dimAndColor.split(":");
        if (colourParts.length == 2) {
            dimLevel = Integer.parseInt(colourParts[0].trim());
            colorTemperature = Integer.parseInt(colourParts[1].trim());
        } else if (colourParts.length == 1  && !dimAndColor.isEmpty()) {
            dimLevel = Integer.parseInt(colourParts[0].trim());
        } else  {
            dimLevel = 0;
        }
        sendOnCommand(dimLevel, colorTemperature);
    }

    public String getWarmOnDim() {
        return getBooleanAttribute(warmOnDim);
    }

    public void setWarmOnDim(String warmOnDim) {
        this.warmOnDim = setBooleanAttribute(warmOnDim);
    }
}

