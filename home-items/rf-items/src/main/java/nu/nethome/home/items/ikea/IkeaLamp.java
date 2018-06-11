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

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import static nu.nethome.home.items.ikea.IkeaGateway.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationInfo = IkeaLamp.IkeaCreationInfo.class)
public class IkeaLamp extends HomeItemAdapter implements HomeItem {

    protected static final String ONOFF = "5850";
    protected static final String DIMMER = "5851";
    private static final int MAX_UPDATE_WAIT_TIME_SECONDS = 10;
    private static final int MS_PER_S = 1000;
    protected static final int NOT_SET = -1;
    protected static final String LIGHT = "3311";
    protected static final String DEVICE = "3";

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
                    !hasColor(e);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Ikea lamp %d: \"%s\"",e.getAttributeInt(IKEA_NODE_ID), e.getAttribute(IkeaGateway.IKEA_NODE_NAME));
        }
    }

    protected static boolean hasColor(Event event) {
        JSONObject node = new JSONObject(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
        JSONArray lights = node.getJSONArray(LIGHT);
        JSONObject light = lights.getJSONObject(0);
        return light.has("5706") && !light.getString("5706").isEmpty();
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"IkeaLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Identity\" Type=\"String\" Get=\"getLampId\" 	Set=\"setLampId\" />"
            + "  <Attribute Name=\"LampModel\" Type=\"String\" Get=\"getLampModel\" 	Init=\"setLampModel\" />"
            + "  <Attribute Name=\"Version\" Type=\"String\" Get=\"getLampVersion\" 	Init=\"setLampVersion\" />"
            + "  <Attribute Name=\"Brightness\" Type=\"String\" Get=\"getCurrentBrightness\"  Unit=\"%\"/>"
            + "  <Attribute Name=\"OnBrightness\" Type=\"String\" Get=\"getBrightness\" 	Set=\"setBrightness\" />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Attribute Name=\"DimStep\" Type=\"String\" Get=\"getDimStep\" 	Set=\"setDimStep\" />"
            + "  <Attribute Name=\"RefreshInterval\" Type=\"String\" Get=\"getRefreshInterval\"  Set=\"setRefreshInterval\"  Unit=\"Minutes\"/>"
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

    private String lampId = "";
    private int onBrightness = NOT_SET;
    private int currentBrightness = 100;
    protected int colorTemperature = NOT_SET;
    protected boolean isOn;
    private String dimLevel1 = "0";
    private String dimLevel2 = "33";
    private String dimLevel3 = "66";
    private String dimLevel4 = "100";
    private int dimStep = 10;
    private String lampModel = "";
    private String lampVersion = "";
    private int refreshInterval = 10;
    private int refreshCounter = 0;

    public IkeaLamp() {
    }

    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if ((event.isType(IkeaGateway.IKEA_NODE_MESSAGE) || event.isType(IkeaGateway.IKEA_MESSAGE)) &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute(IkeaGateway.IKEA_NODE_ID).equals(lampId)) {
            updateAttributes(event);
            return true;
        } else if ((event.isType("MinuteEvent") && (++refreshCounter >= refreshInterval) && isActivated())) {
            refreshCounter = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sleepRandomTime();
                    fetchCurrentState();
                }
            }).run();
            return true;
        }
        return handleInit(event);
    }

    private void sleepRandomTime() {
        try {
            Thread.sleep((Integer.parseInt(lampId) % MAX_UPDATE_WAIT_TIME_SECONDS) * MS_PER_S);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    private void updateAttributes(Event event) {
        JSONObject node = new JSONObject(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE));
        JSONObject info = node.getJSONObject(DEVICE);
        this.lampModel = info.getString("1");
        this.lampVersion = info.getString("3");
        JSONArray lights = node.getJSONArray(LIGHT);
        JSONObject light = lights.getJSONObject(0);
        this.isOn = light.has(ONOFF) && light.getInt(ONOFF) != 0;
        if (light.has(DIMMER)) {
            this.currentBrightness = ikeaTopercent(light.getInt(DIMMER));
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        lampId = event.getAttribute(IkeaGateway.IKEA_NODE_ID);
        updateAttributes(event);
        return true;
    }

    protected void sendOnCommand(int brightness, int temperature) {
        Event ev = createEvent();
        ev.setAttribute(IkeaGateway.IKEA_METHOD, "PUT");
        JSONObject light = createLightObject(brightness, temperature);
        ev.setAttribute(IkeaGateway.IKEA_BODY, String.format("{\"" + LIGHT + "\":[%s]}", light.toString()));
        server.send(ev);
        isOn = true;
    }

    protected JSONObject createLightObject(int brightness, int temperature) {
        JSONObject light = new JSONObject();
        light.put(ONOFF, 1);
        if (brightness != NOT_SET) {
            light.put(DIMMER, percentToIkea(brightness));
            currentBrightness = brightness;
        }
        return light;
    }

    public String fetchCurrentState() {
        Event ev = createEvent();
        ev.setAttribute(IkeaGateway.IKEA_METHOD, "GET");
        ev.setAttribute(IkeaGateway.IKEA_NODE_ID, lampId);
        server.send(ev);
        return "";
    }

    private int percentToIkea(int brightness) {
        return (brightness * 254) / 100;
    }
    private int ikeaTopercent(int brightness) {
        return (brightness * 100) / 254;
    }

    protected void sendOffCommand() {
        Event ev = createEvent();
        ev.setAttribute(IkeaGateway.IKEA_METHOD, "PUT");
        ev.setAttribute(IkeaGateway.IKEA_BODY, "{\"" + LIGHT + "\":[{\"" + ONOFF + "\":0}]}");
        server.send(ev);
        isOn = false;
    }

    private Event createEvent() {
        Event ev = server.createEvent(IkeaGateway.IKEA_MESSAGE, "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute(IkeaGateway.IKEA_RESOURCE, "/15001/" + lampId);
        return ev;
    }

    public void on() {
        sendOnCommand(onBrightness, colorTemperature);
        isOn = true;
    }

    public void off() {
        sendOffCommand();
        isOn = false;
    }

    public void toggle() {
        if (isOn) {
            off();
        } else {
            on();
        }
    }

    public void setBrightness(String level) {
        if (level.length() == 0) {
            onBrightness = NOT_SET;
        } else {
            int newDimLevel = Integer.parseInt(level);
            if ((newDimLevel >= 0) && (newDimLevel <= 100) && (newDimLevel != onBrightness)) {
                onBrightness = newDimLevel;
                if (isOn) {
                    on();
                }
            }
        }
    }

    public String getBrightness() {
        return onBrightness == NOT_SET ? "" : Integer.toString(onBrightness);
    }

    public String getLampId() {
        return lampId;
    }

    public void setLampId(String lampId) {
        this.lampId = lampId;
    }

    public String getState() {
        return isOn ? "On" : "Off";
    }

    public boolean isOn() {
        return isOn;
    }

    public String getLampModel() {
        return lampModel;
    }

    public void setLampModel(String lampModel) {
        this.lampModel = lampModel;
    }

    public String getLampVersion() {
        return lampVersion;
    }

    public void setLampVersion(String lampVersion) {
        this.lampVersion = lampVersion;
    }

    public void dim1() {
        sendOnCommand(dimLevel1);
    }

    protected void sendOnCommand(String dimAndColor) {
        int dimLevel;
        if (!dimAndColor.isEmpty()) {
            dimLevel = Integer.parseInt(dimAndColor.trim());
        } else  {
            dimLevel = NOT_SET;
        }
        sendOnCommand(dimLevel, colorTemperature);
    }

    public void dim2() {
        sendOnCommand(dimLevel2);
    }

    public void dim3() {
        sendOnCommand(dimLevel3);
    }

    public void dim4() {
        sendOnCommand(dimLevel4);
    }

    public String getDimLevel1() {
        return dimLevel1;
    }

    public void setDimLevel1(String dimLevel1) {
        this.dimLevel1 = dimLevel1;
    }

    public String getDimLevel2() {
        return dimLevel2;
    }

    public void setDimLevel2(String dimLevel2) {
        this.dimLevel2 = dimLevel2;
    }

    public String getDimLevel3() {
        return dimLevel3;
    }

    public void setDimLevel3(String dimLevel3) {
        this.dimLevel3 = dimLevel3;
    }

    public String getDimLevel4() {
        return dimLevel4;
    }

    public void setDimLevel4(String dimLevel4) {
        this.dimLevel4 = dimLevel4;
    }

    public void bright() {
        currentBrightness += dimStep;
        if (currentBrightness > 100) {
            currentBrightness = 100;
        }
        sendOnCommand(currentBrightness, colorTemperature);
    }

    public void dim() {
        currentBrightness -= dimStep;
        if (currentBrightness < 1) {
            currentBrightness = 1;
        }
        sendOnCommand(currentBrightness, colorTemperature);
    }

    public String getCurrentBrightness() {
        return Integer.toString(currentBrightness);
    }

    public String getDimStep() {
        return Integer.toString(dimStep);
    }

    public void setDimStep(String dimStep) {
        this.dimStep = Integer.parseInt(dimStep);
    }

    public String getRefreshInterval() {
        return getIntAttribute(refreshInterval);
    }

    public void setRefreshInterval(String refreshInterval) throws IllegalValueException {
        this.refreshInterval = setIntAttribute(refreshInterval, 1, Integer.MAX_VALUE);
    }
}

