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

package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import static nu.nethome.home.items.net.wemo.WemoBridge.*;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationInfo = WemoLamp.WemoCreationInfo.class)
public class WemoLamp extends HomeItemAdapter implements HomeItem {

    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {WEMO_LIGHT_MESSAGE};

        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.isType(WEMO_LIGHT_MESSAGE);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Lamp: \"%s\", DeviceId: %s", e.getAttribute(FRIENDLY_NAME), e.getAttribute(DEVICE_ID));
        }
    }

    private static final double DIM_LEVEL_K = (255D / 100D);

    private int onDimLevel = 100;
    private int dimLevel1 = 25;
    private int dimLevel2 = 50;
    private int dimLevel3 = 75;
    private int dimLevel4 = 100;
    private int currentDimLevel = 0;
    private int dimStep = 10;
    private String deviceId = "";
    private String firmwareVersion = "";
    private String friendlyName = "";
    private int onState = -1;

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoLamp\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"FirmwareVersion\" Type=\"String\" Get=\"getFirmwareVersion\" Init=\"setFirmwareVersion\" />"
            + "  <Attribute Name=\"FriendlyName\" Type=\"String\" Get=\"getFriendlyName\" Init=\"setFriendlyName\" />"
            + "  <Attribute Name=\"OnDimLevel\" Type=\"String\" Get=\"getOnDimLevel\" 	Set=\"setOnDimLevel\" />"
            + "  <Attribute Name=\"Level\" Type=\"String\" Get=\"getCurrentDimLevel\"  />"
            + "  <Attribute Name=\"DimLevel1\" Type=\"String\" Get=\"getDimLevel1\" 	Set=\"setDimLevel1\" />"
            + "  <Attribute Name=\"DimLevel2\" Type=\"String\" Get=\"getDimLevel2\" 	Set=\"setDimLevel2\" />"
            + "  <Attribute Name=\"DimLevel3\" Type=\"String\" Get=\"getDimLevel3\" 	Set=\"setDimLevel3\" />"
            + "  <Attribute Name=\"DimLevel4\" Type=\"String\" Get=\"getDimLevel4\" 	Set=\"setDimLevel4\" />"
            + "  <Attribute Name=\"DimStep\" Type=\"String\" Get=\"getDimStep\" 	Set=\"setDimStep\" />"
            + "  <Attribute Name=\"TransmissionRepeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"dim\" 	Method=\"dim\" />"
            + "  <Action Name=\"bright\" 	Method=\"bright\" />"
            + "  <Action Name=\"dim1\" 	Method=\"dim1\" />"
            + "  <Action Name=\"dim2\" 	Method=\"dim2\" />"
            + "  <Action Name=\"dim3\" 	Method=\"dim3\" />"
            + "  <Action Name=\"dim4\" 	Method=\"dim4\" />"
            + "  <Action Name=\"store\" 	Method=\"store\" />"
            + "</HomeItem> ");

    public String getModel() {
        return MODEL;
    }

    public boolean receiveEvent(Event event) {
        if (event.isType(WEMO_LIGHT_MESSAGE) &&
                event.getAttribute("Direction").equals("In") &&
                event.getAttribute(DEVICE_ID).equals(deviceId)) {
            updateAttributesFromEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    @Override
    protected boolean initAttributes(Event event) {
        deviceId = event.getAttribute(DEVICE_ID);
        updateAttributesFromEvent(event);
        return true;
    }

    private void updateAttributesFromEvent(Event event) {
        friendlyName = event.getAttribute(FRIENDLY_NAME);
        firmwareVersion = event.getAttribute(FIRMWARE_VERSION);
        onState = event.getAttributeInt(ON_STATE);
        int dimLevel = event.getAttributeInt(BRIGHTNESS);
        if (dimLevel > 0) {
            currentDimLevel = (int) (dimLevel / DIM_LEVEL_K);
        }
    }

    /**
     * Send the event to dim the device to the specified level. If the level is 0 the lamp
     * is turned off.
     *
     * @param dimLevel level in % of full power, 0 = off, 100 = full power
     */
    protected void sendLampCommand(boolean isOn, int dimLevel) {
        Event ev = server.createEvent(WEMO_LIGHT_MESSAGE, "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute(DEVICE_ID, deviceId);
        ev.setAttribute(ON_STATE, isOn ? "1" : "0");
        ev.setAttribute(BRIGHTNESS, percentDimToWemoDim(dimLevel));
        server.send(ev);
        this.onState = isOn ? 1 : 0;
        currentDimLevel = dimLevel;
    }

    private int percentDimToWemoDim(int dimLevel) {
        return (int) (dimLevel * DIM_LEVEL_K);
    }

    public void on() {
        currentDimLevel = onDimLevel;
        sendLampCommand(true, currentDimLevel);
    }

    public void off() {
        sendLampCommand(false, currentDimLevel);
    }

    public void toggle() {
        if (onState == 1) {
            off();
        } else {
            on();
        }
    }

    /**
     * Dim to the pre set dim level 1
     */
    public void dim1() {
        sendLampCommand(true, dimLevel1);
    }

    /**
     * Dim to the pre set dim level 2
     */
    public void dim2() {
        sendLampCommand(true, dimLevel2);
    }

    /**
     * Dim to the pre set dim level 3
     */
    public void dim3() {
        sendLampCommand(true, dimLevel3);
    }

    /**
     * Dim to the pre set dim level 4
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim4() {
        sendLampCommand(true, dimLevel4);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel1() {
        return Integer.toString(dimLevel1);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel1 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel1(String mDimLevel1) {
        dimLevel1 = stringToDimLevel(mDimLevel1);
    }

    private int stringToDimLevel(String level) {
        int newDimLevel = Integer.parseInt(level);
        return toDimLevel(newDimLevel);
    }

    private int toDimLevel(int newDimLevel) {
        if (newDimLevel < 1) {
            return 1;
        } else if (newDimLevel > 100) {
            return 100;
        }
        return newDimLevel;
    }


    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel2() {
        return Integer.toString(dimLevel2);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel2 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel2(String mDimLevel2) {
        dimLevel2 = stringToDimLevel(mDimLevel2);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel3() {
        return Integer.toString(dimLevel3);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel3 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel3(String mDimLevel3) {
        dimLevel3 = stringToDimLevel(mDimLevel3);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getDimLevel4() {
        return Integer.toString(dimLevel4);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param mDimLevel4 dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setDimLevel4(String mDimLevel4) {
        dimLevel4 = stringToDimLevel(mDimLevel4);
    }

    /**
     * Get the pre set dim level
     *
     * @return current dim level setting
     */
    public String getOnDimLevel() {
        if (onDimLevel == 0) {
            return "";
        }
        return Integer.toString(onDimLevel);
    }

    /**
     * Set the pre set dim level which can be used by the corresponding dim-action
     *
     * @param level dimLevel level in % of full power, 0 = off, 100 = full power
     */
    public void setOnDimLevel(String level) {
        if (level.length() == 0) {
            onDimLevel = 100;
        } else {
            onDimLevel = stringToDimLevel(level);
            if (onState == 1) {
                sendLampCommand(true, onDimLevel);
            }
        }
    }

    public void dim() {
        sendLampCommand(true, toDimLevel(currentDimLevel - dimStep));
    }

    public void bright() {
        sendLampCommand(true, toDimLevel(currentDimLevel + dimStep));
    }

    public void store() {
        onDimLevel = currentDimLevel;
    }

    public String getDimStep() {
        return Integer.toString(dimStep);
    }

    public void setDimStep(String dimStep) {
        this.dimStep = stringToDimLevel(dimStep);
    }

    public String getCurrentDimLevel() {
        return Integer.toString(currentDimLevel);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getState() {
        if (onState == 0) {
            return "Off";
        } else if (onState == 1) {
            return "On";
        } else {
            return "";
        }
    }
}