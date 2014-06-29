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

/**
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "FooGadgetPulse_Message")
public class FooGadgetPulse extends FooGadgetLegacy implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FooGadgetPulse\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Power\" Type=\"String\" Get=\"getPower\" Default=\"true\" Unit=\"kW\" />"
            + "  <Attribute Name=\"EnergyToday\" Type=\"String\" Get=\"getEnergyToday\"  Unit=\"kWh\" />"
            + "  <Attribute Name=\"EnergyYesterday\" Type=\"String\" Get=\"getEnergyYesterday\" Unit=\"kWh\" />"
            + "  <Attribute Name=\"EnergyThisWeek\" Type=\"String\" Get=\"getEnergyThisWeek\" Unit=\"kWh\" />"
            + "  <Attribute Name=\"EnergyLastWeek\" Type=\"String\" Get=\"getEnergyLastWeek\" Unit=\"kWh\" />"
            + "  <Attribute Name=\"TotalEnergy\" Type=\"String\" Get=\"getTotalEnergy\" Unit=\"kWh\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" Unit=\"s\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"PulsesPerKwh\" Type=\"String\" Get=\"getEnergyK\" 	Set=\"setEnergyK\" />"
            + "  <Attribute Name=\"TotalSavedPulses\" Type=\"Hidden\" Get=\"getTotalSavedPulses\" Init=\"setTotalSavedPulses\" />"
            + "  <Attribute Name=\"LastSeenPulses\" Type=\"Hidden\" Get=\"getLastSeenPulseCounter\" Init=\"setLastSeenPulseCounter\" />"
            + "  <Attribute Name=\"DayState\" Type=\"Hidden\" Get=\"getDayState\" Init=\"setDayState\" />"
            + "  <Attribute Name=\"WeekState\" Type=\"Hidden\" Get=\"getWeekState\" Init=\"setWeekState\" />"
            + "</HomeItem> ");
    public static final String FOO_GADGET_PULSE_MESSAGE = "FooGadgetPulse_Message";
    public static final long FOO_GADGET_COUNTER_RANGE = (101L << 12);

    private long lastSeenPulseCounter;
    private String currentValue = "";

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isFooGadgetPulseEvent(event)) {
            return handleFooGadgetPulseEvent(event);
        } else if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            return handleMinuteEvent();
        }
        return false;
    }

    private boolean isFooGadgetPulseEvent(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(FOO_GADGET_PULSE_MESSAGE);
    }

    protected boolean handleFooGadgetPulseEvent(Event event) {
        long timeForPreviousUpdate = latestUpdateOrCreation.getTime();
        long valueAfterPreviousUpdate = getTotalPulses();
        latestUpdateOrCreation = getCurrentTime();
        long currentCounter = getPulseCounter(event);
        if (currentCounter < lastSeenPulseCounter) {
            handleWrappedCounter(currentCounter);
        }
        lastSeenPulseCounter = currentCounter;
        if (!hasBeenUpdated) {
            InitializeAtFirstUpdate();
        } else {
            long pulsesSincePreviousUpdate = getTotalPulses() - valueAfterPreviousUpdate;
            double hoursSinceLastUpdate = (latestUpdateOrCreation.getTime() - timeForPreviousUpdate) / (1000.0 * 60.0 * 60.0);
            currentValue = String.format(ENERGY_FORMAT, (pulsesSincePreviousUpdate / (pulsesPerKWh * hoursSinceLastUpdate)));
        }
        return true;
    }

    private void handleWrappedCounter(long currentCounter) {
        totalSavedPulses += FOO_GADGET_COUNTER_RANGE;
    }

    private long getPulseCounter(Event event) {
        return event.getAttributeInt("FooGadgetPulse.Pulses");
    }

    public String getPower() {
        return currentValue;
    }

    protected long getTotalPulses() {
        return totalSavedPulses + lastSeenPulseCounter;
    }

    public String getLastSeenPulseCounter() {
        return Long.toString(lastSeenPulseCounter);
    }

    public void setLastSeenPulseCounter(String lastSeenPulseCounter) {
        this.lastSeenPulseCounter = Long.parseLong(lastSeenPulseCounter);
    }
}
