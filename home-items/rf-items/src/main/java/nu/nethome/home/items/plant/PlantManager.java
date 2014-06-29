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

package nu.nethome.home.items.plant;


import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

@Plugin
@HomeItemType("Hardware")
public class PlantManager extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"PlantManager\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"TankLevel\" Type=\"String\" Get=\"getTankLevel\"  Unit=\"%\" />"
            + "  <Attribute Name=\"SoilMoisture\" Type=\"String\" Get=\"getSoilMoisture\"  Unit=\"%\" />"
            + "  <Attribute Name=\"Address\" Type=\"String\" Get=\"getPumpSwitchAddress\" 	Set=\"setPumpSwitchAddress\" />"
            + "  <Attribute Name=\"Button\" Type=\"String\" Get=\"getPumpSwitchButton\" 	Set=\"setPumpSwitchButton\" />"
            + "  <Attribute Name=\"MoistureItem\" 	Type=\"Item\" Get=\"getMoistureItem\" 	Set=\"setMoistureItem\" />"
            + "  <Attribute Name=\"WaterCount\" 	Type=\"Item\" Get=\"getWaterCount\" 	Init=\"setWaterCount\" />"
            + "  <Attribute Name=\"TankVolume\" Type=\"String\" Get=\"getTankVolume\" 	Set=\"setTankVolume\" />"
            + "  <Action Name=\"arm\" 	Method=\"arm\" />"
            + "  <Action Name=\"water\" 	Method=\"water\" Default=\"true\" />"
            + "  <Action Name=\"tankFilled\" 	Method=\"tankFilled\" />"
            + "</HomeItem> ");

    private static final int COMMAND_REPEAT_PAUSE = 5;
    private static final int ON_COMMAND = 1;
    private static final int PUMP_RUNNING_PAUSE = 10;
    private static final int OFF_COMMAND = 0;
    private static final int SHORT_PAUSE = 1;
    private static final int INTER_DISPENSE_PAUSE = 20;
    private static final int MAX_ARM_TIME_MINUTES = 24 * 60;
    private static final String HUMIDITY_ATTRIBUTE = "Humidity";

    private String moistureItem = "";
    private boolean isDispensing = false;
    private int armedMinuteCounter = 0;
    private int pumpSwitchAddress = 0;
    private int pumpSwitchButton = 1;
    private boolean lowBattery;
    private String latestUpdate;
    private int waterCount = 0;
    private int tankVolume = 1;

    public PlantManager() {
    }

    public synchronized void water() {
        if (!isDispensing && (armedMinuteCounter > 0)) {
            isDispensing = true;
            armedMinuteCounter = 0;
            Thread runner = new Thread(new Runnable() {
                public void run() {
                    dispenseWater();
                }
            }, "watering thread");
            runner.start();
        }
    }

    public synchronized void arm() {
        if (!isDispensing) {
            if (armedMinuteCounter > 0) {
                armedMinuteCounter = 0;
            } else {
                armedMinuteCounter = MAX_ARM_TIME_MINUTES;
            }
        }
    }

    private void dispenseWater() {
        sendCommand(OFF_COMMAND);       // If the receiver is already in an on state, turn it off
        pause(SHORT_PAUSE);             // Wait for signal to be transmitted
        sendCommand(ON_COMMAND);        // Send the on command which should trigger the pump to start pumping
        pause(COMMAND_REPEAT_PAUSE);    // Pause a while to get past any radio interference...
        sendCommand(ON_COMMAND);        // ...and resend the on signal
        pause(PUMP_RUNNING_PAUSE);      // Wait a bit longer so the pump engine has finished to avoid the radio interference from it
        sendCommand(OFF_COMMAND);       // send the off signal to reset the receiver to its of state
        pause(COMMAND_REPEAT_PAUSE);    // Pause a while to get past any radio interference...
        sendCommand(OFF_COMMAND);       // ...and resend the off signal
        waterCount++;
        pause(INTER_DISPENSE_PAUSE);    // Wait a bit longer, so it is not possible to trigger the watering to often
        isDispensing = false;
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("MinuteEvent") && (armedMinuteCounter > 0)) {
            armedMinuteCounter--;
            return true;
        }
        return false;
    }

    private double calculateTankLevel(int rawValue) {
        return rawValue;
    }


    private void pause(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public void sendCommand(int command) {
        Event ev = server.createEvent("NexaL_Message", "");
        ev.setAttribute("Direction", "Out");
        ev.setAttribute("NexaL.Address", pumpSwitchAddress);
        ev.setAttribute("NexaL.Button", pumpSwitchButton);
        ev.setAttribute("NexaL.Command", command);
        server.send(ev);
    }

    public String getState() {
        if (isDispensing) {
            return "Watering";
        } else if (armedMinuteCounter > 0) {
            return "Armed";
        }
        return "Idle";
    }

    public String getModel() {
        return MODEL;
    }

    public String getPumpSwitchAddress() {
        return Integer.toString(pumpSwitchAddress);
    }

    public void setPumpSwitchAddress(String pumpSwitchAddress) {
        this.pumpSwitchAddress = Integer.parseInt(pumpSwitchAddress);
    }

    public String getPumpSwitchButton() {
        return Integer.toString(pumpSwitchButton);
    }

    public void setPumpSwitchButton(String pumpSwitchButton) {
        this.pumpSwitchButton = Integer.parseInt(pumpSwitchButton);
    }

    public String getBatteryLevel() {
        return lowBattery ? "10" : "100";
    }

    public String getLatestUpdate() {
        return latestUpdate;
    }

    public String getSoilMoisture() {
        HomeItemProxy item = null;
        if (this.isActivated()) {
            item = server.openInstance(moistureItem);
        }
        if (item != null) {
            return item.getAttributeValue(HUMIDITY_ATTRIBUTE);
        }
        return "";
    }

    public String getMoistureItem() {
        return moistureItem;
    }

    public void setMoistureItem(String moistureItem) {
        this.moistureItem = moistureItem;
    }

    public String getTankVolume() {
        return Integer.toString(tankVolume);
    }

    public void setTankVolume(String tankVolume) {
        this.tankVolume = Integer.parseInt(tankVolume);
    }

    public String getWaterCount() {
        return Integer.toString(waterCount);
    }

    public void setWaterCount(String tankVolume) {
        this.waterCount = Integer.parseInt(tankVolume);
    }

    public String getTankLevel() {
        return Integer.toString(100 - waterCount * 100/ tankVolume);
    }

    public void tankFilled() {
        waterCount = 0;
    }
}
