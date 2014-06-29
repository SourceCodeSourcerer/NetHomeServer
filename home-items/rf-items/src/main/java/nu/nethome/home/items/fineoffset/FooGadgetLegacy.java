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
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "FooGadgetLegacy_Message")
public class FooGadgetLegacy extends HomeItemAdapter implements HomeItem, ValueItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FooGadgetLegacy\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Power\" Type=\"String\" Get=\"getPower\" Default=\"true\" />"
            + "  <Attribute Name=\"EnergyToday\" Type=\"String\" Get=\"getEnergyToday\" />"
            + "  <Attribute Name=\"EnergyYesterday\" Type=\"String\" Get=\"getEnergyYesterday\" />"
            + "  <Attribute Name=\"EnergyThisWeek\" Type=\"String\" Get=\"getEnergyThisWeek\" />"
            + "  <Attribute Name=\"EnergyLastWeek\" Type=\"String\" Get=\"getEnergyLastWeek\" />"
            + "  <Attribute Name=\"TotalEnergy\" Type=\"String\" Get=\"getTotalEnergy\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"PulsesPerKwh\" Type=\"String\" Get=\"getEnergyK\" 	Set=\"setEnergyK\" />"
            + "  <Attribute Name=\"LostSamples\" Type=\"String\" Get=\"getLostSamples\" Init=\"setLostSamples\" />"
            + "  <Attribute Name=\"TotalSavedPulses\" Type=\"Hidden\" Get=\"getTotalSavedPulses\" Set=\"setTotalSavedPulses\" />"
            + "  <Attribute Name=\"DayState\" Type=\"Hidden\" Get=\"getDayState\" Set=\"setDayState\" />"
            + "  <Attribute Name=\"WeekState\" Type=\"Hidden\" Get=\"getWeekState\" Set=\"setWeekState\" />"
            + "</HomeItem> ");

    public static final int MINUTES_PER_HOUR = 60;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");

    public static final String ENERGY_FORMAT = "%.2f";
    protected Logger logger = Logger.getLogger(FooGadgetLegacy.class.getName());
    private LoggerComponent energyLoggerComponent = new LoggerComponent(this);
    protected Date latestUpdateOrCreation = getCurrentTime();
    private Date latestValueSampleTime = getCurrentTime();
    private long latestValueSamplePulses;
    protected long totalSavedPulses = 0;

    private long latestPulseSample = 0;
    private PeriodCounter dayPeriod;
    private PeriodCounter weekPeriod;

    // Public attributes
    protected double pulsesPerKWh = 1000.0;
    private int latestSampleCounter;
    protected boolean hasBeenUpdated;
    private long lostSamples;

    public FooGadgetLegacy() {
        dayPeriod = new PeriodCounter() {
            @Override
            public long getNewPeriod() {
                return getPresentDay();
            }
        };
        weekPeriod = new PeriodCounter() {
            @Override
            public long getNewPeriod() {
                return getPresentWeek();
            }
        };
    }

    @Override
    public void activate() {
        energyLoggerComponent.activate();
    }

    @Override
    public void stop() {
        energyLoggerComponent.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isFooGadgetLegacyEvent(event)) {
            return handleFooGadgetEvent(event);
        } else if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            return handleMinuteEvent();
        }
        return false;
    }

    private boolean isFooGadgetLegacyEvent(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals("FooGadgetLegacy_Message");
    }

    protected boolean handleFooGadgetEvent(Event event) {
        latestUpdateOrCreation = getCurrentTime();
        int counter = getSampleCounter(event);
        if (counter != latestSampleCounter) {
            addNewSample(event, counter);
        }
        if (!hasBeenUpdated) {
            InitializeAtFirstUpdate();
        }
        return true;
    }

    protected boolean handleMinuteEvent() {
        if (hasBeenUpdated) {
            dayPeriod.updateCounter(getTotalPulses());
            weekPeriod.updateCounter(getTotalPulses());
        }
        return true;
    }

    private int getPresentDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentTime());
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    private int getPresentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCurrentTime());
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    protected void InitializeAtFirstUpdate() {
        latestValueSamplePulses = getTotalPulses();
        latestValueSampleTime = getCurrentTime();
        dayPeriod.updateCounter(getTotalPulses());
        weekPeriod.updateCounter(getTotalPulses());
        hasBeenUpdated = true;
    }

    Date getCurrentTime() {
        return new Date();
    }

    private void addNewSample(Event event, int counter) {
        if (hasBeenUpdated) {
            int sampleCounterDiff = counter - latestSampleCounter;
            sampleCounterDiff += (sampleCounterDiff < 0) ? 100 : 0;
            lostSamples += sampleCounterDiff - 1;
        }
        latestSampleCounter = counter;
        latestPulseSample = getPulseSample(event);
        totalSavedPulses += latestPulseSample;
    }

    private int getSampleCounter(Event event) {
        return event.getAttributeInt("FooGadgetLegacy.Counter");
    }

    private long getPulseSample(Event event) {
        return event.getAttributeInt("FooGadgetLegacy.Energy");
    }

    public String getModel() {
        return MODEL;
    }

    public String getPower() {
        if (!hasBeenUpdated) {
            return "";
        }
        return String.format(ENERGY_FORMAT, (latestPulseSample / pulsesPerKWh) * MINUTES_PER_HOUR);
    }

    public String getEnergyK() {
        return Double.toString(pulsesPerKWh);
    }

    public void setEnergyK(String EnergyK) {
        this.pulsesPerKWh = Double.parseDouble(EnergyK);
    }

    public String getTotalEnergy() {
        return String.format(ENERGY_FORMAT, getTotalPulses() / pulsesPerKWh);
    }

    protected long getTotalPulses() {
        return totalSavedPulses;
    }

    public String getTotalSavedPulses() {
        return Long.toString(totalSavedPulses);
    }

    public void setTotalSavedPulses(String totalSavedPulses) {
        this.totalSavedPulses = Long.parseLong(totalSavedPulses);
    }

    public String getLastUpdate() {
        return hasBeenUpdated ? dateFormatter.format(latestUpdateOrCreation) : "";
    }

    public String getLogFile() {
        return energyLoggerComponent.getFileName();
    }

    public void setLogFile(String logfile) {
        energyLoggerComponent.setFileName(logfile);
    }

    public String getTimeSinceUpdate() {
        return Long.toString((getCurrentTime().getTime() - latestUpdateOrCreation.getTime()) / 1000);
    }

    @Override
    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        Date sampleTime = getCurrentTime();
        long currentEnergy = getTotalPulses();
        double timeSinceLastSampleHours = (sampleTime.getTime() - latestValueSampleTime.getTime()) / (1000.0 * 60.0 * 60.0);
        double energySinceLastSampleKWh = (currentEnergy - latestValueSamplePulses) / pulsesPerKWh;
        latestValueSampleTime = sampleTime;
        latestValueSamplePulses = currentEnergy;
        return String.format(ENERGY_FORMAT, energySinceLastSampleKWh / timeSinceLastSampleHours);
    }

    public String getLostSamples() {
        return Long.toString(lostSamples);
    }

    public void setLostSamples(String lostSamples) {
        this.lostSamples = Long.parseLong(lostSamples);
    }

    public String getEnergyToday() {
        if (!hasBeenUpdated) {
            return "";
        }
        return String.format(ENERGY_FORMAT, dayPeriod.getPulsesDuringPeriod(getTotalPulses()) / pulsesPerKWh);
    }

    public String getEnergyYesterday() {
        if (dayPeriod.getPulsesDuringLastPeriod() == 0) {
            return "";
        }
        return String.format(ENERGY_FORMAT, dayPeriod.getPulsesDuringLastPeriod() / pulsesPerKWh);
    }

    public String getEnergyThisWeek() {
        if (!hasBeenUpdated) {
            return "";
        }
        return String.format(ENERGY_FORMAT, weekPeriod.getPulsesDuringPeriod(getTotalPulses()) / pulsesPerKWh);
    }

    public String getEnergyLastWeek() {
        if (weekPeriod.getPulsesDuringLastPeriod() == 0) {
            return "";
        }
        return String.format(ENERGY_FORMAT, weekPeriod.getPulsesDuringLastPeriod() / pulsesPerKWh);
    }

    public String getDayState() {
        return dayPeriod.getState();
    }

    public void setDayState(String dayState) {
        dayPeriod.setState(dayState);
    }

    public String getWeekState() {
        return weekPeriod.getState();
    }

    public void setWeekState(String dayState) {
        weekPeriod.setState(dayState);
    }
}
