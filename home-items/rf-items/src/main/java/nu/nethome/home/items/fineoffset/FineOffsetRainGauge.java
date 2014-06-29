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

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Presents and logs rain values received by an FineOffset-rain sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive FineOffset messages from the hardware devices.
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationEvents = "FineOffset_Message")
public class FineOffsetRainGauge extends FineOffsetThermometer implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"FineOffsetRainGauge\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Rain1h\" Type=\"String\" Get=\"getRain1h\" Default=\"true\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"Rain24h\" Type=\"String\" Get=\"getRain24h\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"RainThisWeek\" Type=\"String\" Get=\"getRainWeek\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"RainLastWeek\" Type=\"String\" Get=\"getRainLastWeek\" Init=\"setRainLastWeek\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"TotalRain\" Type=\"String\" Get=\"getTotalRain\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"Temperature\" 	Type=\"String\" Get=\"getTemp\"  Unit=\"°C\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"RainK\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"TempK\" Type=\"String\" Get=\"getK\" 	Set=\"setK\" />"
            + "  <Attribute Name=\"TempM\" Type=\"String\" Get=\"getM\" 	Set=\"setM\" />"
            + "  <Attribute Name=\"TotalRainBase\" Type=\"Hidden\" Get=\"getTotalRainBase\" Init=\"setTotalRainBase\" />"
            + "  <Attribute Name=\"RainAtStartOfWeek\" Type=\"Hidden\" Get=\"getRainAtStartOfWeek\" Init=\"setRainAtStartOfWeek\" />"
            + "  <Attribute Name=\"CurrentWeekNumber\" Type=\"Hidden\" Get=\"getCurrentWeekNumber\" Init=\"setCurrentWeekNumber\" />"
            + "</HomeItem> ");
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;

    private CounterHistory minutesOfLastHour = new CounterHistory(MINUTES_PER_HOUR);
    private CounterHistory last24Hours = new CounterHistory(24);
    private int currentHour = 0;
    private int minuteCounter = MINUTES_PER_HOUR - 1;
    private int hourCounter = 0;
    private int dayCounter = 0;
    private long totalHours = 0;
    private long totalRainAtLastValue = 0;
    private long totalRainBase = 0;
    private long sensorTotalRain = 0;

    // Public attributes
    private double rainConstantK = 0.1;
    private long rainAtStartOfWeek;
    private int currentWeekNumber = -1;
    private String rainLastWeek = "";

    public FineOffsetRainGauge() {
        constantK = 0.1;
        logger = Logger.getLogger(FineOffsetRainGauge.class.getName());
    }

    @Override
    public boolean receiveEvent(Event event) {
        boolean result = super.receiveEvent(event);
        if (hasBeenUpdated && event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            pushValue();
        }
        return result;
    }

    private void pushValue() {
        minutesOfLastHour.addValue(getTotalRainInternal());
        if (++minuteCounter == MINUTES_PER_HOUR) {
            minuteCounter = 0;
            last24Hours.addValue(getTotalRainInternal());
        }
    }

    private int calculateCurrentWeekNumber() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    protected boolean handleEvent(Event event) {
        long oldSensorTotalRain = sensorTotalRain;
        sensorTotalRain = event.getAttributeInt("FineOffset.Rain");
        if (oldSensorTotalRain > sensorTotalRain) {
            totalRainBase += oldSensorTotalRain;
        }
        if (currentWeekNumber != calculateCurrentWeekNumber()) {
            currentWeekNumber = calculateCurrentWeekNumber();
            rainLastWeek = getRainWeek();
            rainAtStartOfWeek = getTotalRainInternal();
        }
        return super.handleEvent(event);
    }

    public String getModel() {
        return MODEL;
    }

    public String getRain1h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = minutesOfLastHour.differenceSince(getTotalRainInternal(), MINUTES_PER_HOUR) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getRain24h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = last24Hours.differenceSince(getTotalRainInternal(), 24) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getRainWeek() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = (getTotalRainInternal() - rainAtStartOfWeek) * rainConstantK;
        return String.format("%.1f", rain1h);
    }

    public String getRainLastWeek() {
        return rainLastWeek;
    }

    public void setRainLastWeek(String rainLastWeek) {
        this.rainLastWeek = rainLastWeek;
    }

    public String getValue() {
        if (!hasBeenUpdated) {
            return "";
        }
        long currentTotalRain = getTotalRainInternal();
        if (totalRainAtLastValue == 0) {
            totalRainAtLastValue = currentTotalRain;
        }
        double rain1h = (getTotalRainInternal() - totalRainAtLastValue) * rainConstantK;
        totalRainAtLastValue = currentTotalRain;
        return String.format("%.1f", rain1h);
    }

    public String getTotalRain() {
        return hasBeenUpdated ? String.format("%.1f", getTotalRainInternal() * rainConstantK) : "";
    }

    public String getRainK() {
        return Double.toString(rainConstantK);
    }

    public void setRainK(String rainK) {
        this.rainConstantK = Double.parseDouble(rainK);
    }

    public String getTotalRainBase() {
        return String.format("%.1f", totalRainBase * rainConstantK);
    }

    public void setTotalRainBase(String rainBase) {
        this.totalRainBase = (long)(Double.parseDouble(rainBase.replace(",", ".")) / rainConstantK);
    }

    protected long getTotalRainInternal() {
        return totalRainBase + sensorTotalRain;
    }

    public String getRainAtStartOfWeek() {
        return Long.toString(rainAtStartOfWeek);
    }

    public void setRainAtStartOfWeek(String rainAtStartOfWeek) {
        this.rainAtStartOfWeek = Long.parseLong(rainAtStartOfWeek);
    }

    public String getCurrentWeekNumber() {
        return Integer.toString(currentWeekNumber);
    }

    public void setCurrentWeekNumber(String currentWeekNumber) {
        this.currentWeekNumber = Integer.parseInt(currentWeekNumber);
    }
}
