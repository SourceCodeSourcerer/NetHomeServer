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

package nu.nethome.home.items.nexa;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Lamps", creationEvents = "NexaL_Message")
public class NexaLCDimmer extends NexaLCLamp implements HomeItem {

    private static final double NEXA_DIM_LEVEL_K = (14D / 100D);
    private static final double NEXA_DIM_LEVEL_M = 0.5;

    // Public attributes
    private int onDimLevel = 0;
    private int dimLevel1 = 25;
    private int dimLevel2 = 50;
    private int dimLevel3 = 75;
    private int dimLevel4 = 100;
    private int currentDimLevel = 0;
    private int dimStep = 10;

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"NexaLCDimmer\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getAddress\" 	Set=\"setAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getButton\" 	Set=\"setButton\" />"
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

    @SuppressWarnings("UnusedDeclaration")
    public NexaLCDimmer() {
    }

    public String getModel() {
        return MODEL;
    }

    /**
     * Send the event to dim the device to the specified level. If the level is 0 the lamp
     * is turned off.
     *
     * @param dimLevel level in % of full power, 0 = off, 100 = full power
     */
    protected void sendDimCommand(int dimLevel) {
        if (dimLevel == 0) {
            off();
            currentDimLevel = 0;
        } else {
            Event ev = server.createEvent("NexaL_Message", "");
            ev.setAttribute("Direction", "Out");
            ev.setAttribute("NexaL.Address", lampAddress);
            ev.setAttribute("NexaL.Button", lampButton);
            ev.setAttribute("NexaL.Command", 1);
            ev.setAttribute("NexaL.DimLevel", percentDimToNexaDim(dimLevel));
            if (repeats > 0) {
                ev.setAttribute("Repeat", repeats);
            }
            server.send(ev);
            isOn = true;
            currentDimLevel = dimLevel;
        }
    }

    private int percentDimToNexaDim(int dimLevel) {
        return (int) (dimLevel * NEXA_DIM_LEVEL_K + NEXA_DIM_LEVEL_M);
    }

    @Override
    public void on() {
        if (onDimLevel > 0) {
            sendDimCommand(onDimLevel);
        } else {
            super.on();
        }
    }

    @Override
    public void off() {
        super.off();
    }

    /**
     * Dim to the pre set dim level 1
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim1() {
        sendDimCommand(dimLevel1);
    }

    /**
     * Dim to the pre set dim level 2
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim2() {
        sendDimCommand(dimLevel2);
    }

    /**
     * Dim to the pre set dim level 3
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim3() {
        sendDimCommand(dimLevel3);
    }

    /**
     * Dim to the pre set dim level 4
     */
    @SuppressWarnings("UnusedDeclaration")
    public void dim4() {
        sendDimCommand(dimLevel4);
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
        if (newDimLevel < 0) {
            return 0;
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
            onDimLevel = 0;
        } else {
            onDimLevel = stringToDimLevel(level);
            if (isOn) {
                sendDimCommand(onDimLevel);
            }
        }
    }

    public void dim() {
        sendDimCommand(toDimLevel(currentDimLevel - dimStep));
    }

    public void bright() {
        sendDimCommand(toDimLevel(currentDimLevel + dimStep));
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
}