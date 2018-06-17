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

package nu.nethome.home.items.oregon;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.ExtendedLoggerComponent;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.item.ValueItem;
import nu.nethome.home.items.fineoffset.CounterHistory;
import nu.nethome.home.items.fineoffset.FineOffsetThermometer;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

/**
 * Presents and logs rain values received by an Oregon-rain sensor. The actual
 * values are received as events which may be sent by any kind of receiver module
 * which can receive Oregon messages from the hardware devices.
 *
 * @author Peter Lagerhem
 * Based on the FineOffsetRainGauge implementation by Stefan Strömberg
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Gauges", creationInfo = OregonRainGauge.OregonCreationInfo.class)
public class OregonRainGauge extends HomeItemAdapter implements HomeItem, ValueItem {

    public static class OregonCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {"Oregon_Message"};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.hasAttribute("Oregon.RainRate") || e.hasAttribute("Oregon.TotalRain");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Oregon Rain Sensor, Ch: %s, Id: %s",
                    e.getAttribute("Oregon.Channel"), e.getAttribute("Oregon.SensorId"));
        }
    }
    
    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"OregonRainGauge\" Category=\"Gauges\" >"
            + "  <Attribute Name=\"Rain1h\" Type=\"String\" Get=\"getRain1h\" Default=\"true\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"Rain24h\" Type=\"String\" Get=\"getRain24h\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"RainThisWeek\" Type=\"String\" Get=\"getRainWeek\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"RainLastWeek\" Type=\"String\" Get=\"getRainLastWeek\" Init=\"setRainLastWeek\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"TotalRain\" Type=\"String\" Get=\"getTotalRain\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"RainPerHour\" Type=\"String\" Get=\"getTotalRainPerHour\"  Unit=\"mm\" />"
            + "  <Attribute Name=\"TimeSinceUpdate\" 	Type=\"String\" Get=\"getTimeSinceUpdate\"  Unit=\"s\" />"
            + "  <Attribute Name=\"DeviceId\" Type=\"String\" Get=\"getDeviceId\" 	Set=\"setDeviceId\" />"
            + "  <Attribute Name=\"LogFile\" Type=\"String\" Get=\"getLogFile\" 	Set=\"setLogFile\" />"
            + "  <Attribute Name=\"LastUpdate\" Type=\"String\" Get=\"getLastUpdate\" />"
            + "  <Attribute Name=\"RainK\" Type=\"String\" Get=\"getRainK\" 	Set=\"setRainK\" />"
            + "  <Attribute Name=\"TotalRainBase\" Type=\"Hidden\" Get=\"getTotalRainBase\" Init=\"setTotalRainBase\" />"
            + "  <Attribute Name=\"RainAtStartOfWeek\" Type=\"Hidden\" Get=\"getRainAtStartOfWeek\" Init=\"setRainAtStartOfWeek\" />"
            + "  <Attribute Name=\"CurrentWeekNumber\" Type=\"Hidden\" Get=\"getCurrentWeekNumber\" Init=\"setCurrentWeekNumber\" />"
            + "</HomeItem> ");
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_MONTH = 24 * 31;
    public static final int MONTH_BUFFER_SIZE = HOURS_PER_MONTH + 1;
    public static final int HOUR_BUFFER_SIZE = MINUTES_PER_HOUR + 1;
    public static final int HOURS_PER_WEEK = 24 * 7;

    protected Logger logger = Logger.getLogger(FineOffsetThermometer.class.getName());
    private ExtendedLoggerComponent tempLoggerComponent = new ExtendedLoggerComponent(this);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd ");
    
    private CounterHistory minutesOfLastHour = new CounterHistory(MINUTES_PER_HOUR);
    private CounterHistory last24Hours = new CounterHistory(24);
    private int minuteCounter = MINUTES_PER_HOUR - 1;
    private long totalRainAtLastValue = 0;
    private long totalRainBase = 0;
    private long sensorTotalRain = 0;
    private long sensorRainRate = 0;

    // Public attributes
    private double rainConstantK = 0.01;
    private long rainAtStartOfWeek;
    private int currentWeekNumber = -1;
    private String rainLastWeek = "";
    private String itemChannel = "";
    private String itemSensorId = "";
    private Date latestUpdateOrCreation = new Date();
    private boolean hasBeenUpdated = false;
    
    public OregonRainGauge() {
        logger = Logger.getLogger(OregonRainGauge.class.getName());
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        // Activate the logger component
        tempLoggerComponent.activate(server);
    }

    @Override
    public void stop() {
        tempLoggerComponent.stop();
        super.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        boolean result = super.receiveEvent(event);
        handleEvent(event);
        if (hasBeenUpdated  && event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            pushValue();
        }
        return result;
    }

    public String getDeviceId() {
        return itemSensorId;
    }

    public void setDeviceId(String DeviceId) {
        itemSensorId = DeviceId;
    }

    public String getChannel() {
        return itemChannel;
    }

    public void setChannel(String Channel) {
        itemChannel = Channel;
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

    protected boolean handleEvent(Event event) {
//        long oldSensorTotalRain = sensorTotalRain;
        if (event.hasAttribute("Oregon.TotalRain")) {            
            sensorTotalRain = event.getAttributeInt("Oregon.TotalRain");
//            if (oldSensorTotalRain > sensorTotalRain) {
//                totalRainBase += oldSensorTotalRain;
//            }
        }

        if (event.hasAttribute("Oregon.RainRate")) {            
            sensorRainRate = event.getAttributeInt("Oregon.RainRate");
        }

        if (currentWeekNumber != calculateCurrentWeekNumber()) {
            currentWeekNumber = calculateCurrentWeekNumber();
            rainLastWeek = getRainWeek();
            rainAtStartOfWeek = getTotalRainInternal();
        }
        latestUpdateOrCreation = new Date();
        hasBeenUpdated = true;
        return true;
    }

    @Override
    protected boolean initAttributes(Event event) {
        itemChannel = event.getAttribute("Oregon.Channel");
        itemSensorId = event.getAttribute("Oregon.SensorId");
        return true;
    }

    public String getModel() {
        return MODEL;
    }

    public String getRain1h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = minutesOfLastHour.differenceSince(getTotalRainInternal(), MINUTES_PER_HOUR) * rainConstantK;
        return String.format("%.01f", rain1h);
    }

    public String getRain24h() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = last24Hours.differenceSince(getTotalRainInternal(), 24) * rainConstantK;
        return String.format("%.01f", rain1h);
    }

    public String getRainWeek() {
        if (!hasBeenUpdated) {
            return "";
        }
        double rain1h = (getTotalRainInternal() - rainAtStartOfWeek) * rainConstantK;
        return String.format("%.01f", rain1h);
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
        return String.format("%.01f", currentTotalRain * rainConstantK);

/*        
        if (totalRainAtLastValue == 0) {
            totalRainAtLastValue = currentTotalRain;
        }
        double rain1h = (getTotalRainInternal() - totalRainAtLastValue) * rainConstantK;
        totalRainAtLastValue = currentTotalRain;
        return String.format("%.01f", rain1h);
*/        
    }

    public String getTotalRain() {
        return hasBeenUpdated ? String.format("%.01f", getTotalRainInternal() * rainConstantK) : "";
    }

    /**
     * Returns the total rain amount per hour in mm (internally represented as inches by the hardware).
     * 
     * @return rain per hour
     */
    public String getTotalRainPerHour() {
        return hasBeenUpdated ? String.format("%.01f", getRainRateInternal() * rainConstantK) : "";
    }

    public String getRainK() {
        return Double.toString(rainConstantK);
    }

    public void setRainK(String rainK) {
        this.rainConstantK = Double.parseDouble(rainK);
    }

    public String getTotalRainBase() {
        return String.format("%.01f", totalRainBase * rainConstantK);
    }

    public void setTotalRainBase(String rainBase) {
        this.totalRainBase = (long)(Double.parseDouble(rainBase.replace(",", ".")) / rainConstantK);
    }

    protected long getTotalRainInternal() {
        return convertInchToMm(totalRainBase + sensorTotalRain);
    }

    protected long getRainRateInternal() {
        return convertInchToMm(sensorRainRate);
    }
    
    protected long convertInchToMm(long inch) {
        return (long)(inch / 0.039370f);
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
