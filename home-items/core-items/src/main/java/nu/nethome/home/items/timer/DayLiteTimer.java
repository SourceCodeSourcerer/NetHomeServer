/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNetHome is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author Peter Lagerhem - Started: 2010-10-10
 */
package nu.nethome.home.items.timer;

import com.jtheory.jdring.AlarmEntry;
import com.jtheory.jdring.AlarmListener;
import com.jtheory.jdring.AlarmManager;
import com.jtheory.jdring.PastDateException;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.timer.FlexableAlarm.AlarmItem;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItemAdapter2;
import nu.nethome.home.item.annotation.HomeItemAction;
import nu.nethome.home.item.annotation.HomeItemAttribute;

/**
 * <p>
 * This homeitem class can help assigning intervals over days in several ways,
 * with possibilities for randomness and sun-rise and sun-set. An interval is in
 * effect two alarms which triggers an onCommand and and offCommand.
 * </p>
 *
 * <p>
 * <h2>Comments</h2>
 * Inherits the {@link WeekTimer} homeItem class so that already implemented
 * logic can be reused.
 * </p>
 *
 * <p>
 * <h2>Command syntax</h2>
 * A specific command format is supported, see {@link FlexableAlarm}.
 * </p>
 *
 * @see FlexableAlarm
 * @see AlarmItem
 */
@Plugin
@HomeItemType("Timers")
public class DayLiteTimer extends HomeItemAdapter2 implements HomeItem {

    private static Logger logger = Logger.getLogger(DayLiteTimer.class
            .getName());

    protected LinkedList<AlarmEntry> mondayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> tuesdayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> wednesdayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> thursdayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> fridayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> saturdayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> sundayAlarms = new LinkedList<AlarmEntry>();
    protected AlarmManager alarmManager;
    protected CommandLineExecutor executor;

    // Public attributes
    @HomeItemAttribute(value = "Timezone identifiers", get = "getTimezoneIdentifier", items = "getTimezoneIdentifierItems")
    public String timezoneIdentifier = "Europe/Stockholm";
    @HomeItemAttribute
    protected String mondays = "";
    @HomeItemAttribute
    protected String tuesdays = "";
    @HomeItemAttribute
    protected String wednesdays = "";
    @HomeItemAttribute
    protected String thursdays = "";
    @HomeItemAttribute
    protected String fridays = "";
    @HomeItemAttribute
    protected String saturdays = "";
    @HomeItemAttribute
    protected String sundays = "";
    @HomeItemAttribute(value = "OnCommand", type = "Command", get = "getOnCommand", set = "setOnCommand")
    protected String m_OnCommand = "";
    @HomeItemAttribute(value = "OffCommand", type = "Command", get = "getOffCommand", set = "setOffCommand")
    protected String m_OffCommand = "";

    protected String latLong = "59.225527,18.000718"; // Default as example
    protected String exceptions = "";

    protected FlexableAlarm flexableAlarm = null;
    protected AlarmEntry m_recalcAlarm;

//    @HomeItemAttribute(value = "Sunrise Today")
//    protected String sunriseToday = "";
//    @HomeItemAttribute(value = "Sunset Today")
//    protected String sunsetToday = "";

    @HomeItemAttribute(value = "state", get = "getState", init = "setState", def = "true")
    private boolean timerActive = false;
    private boolean timerIsOn = false;

    @HomeItemAttribute(value = "Timer Today")
    private String todayStartEnd = "";

    /**
     * @return Returns the m_OnCommand.
     */
    public String getOnCommand() {
        return m_OnCommand;
    }

    /**
     * @param OnCommand The m_OnCommand to set.
     */
    public void setOnCommand(String OnCommand) {
        m_OnCommand = OnCommand;
    }

   /**
     * @return Returns the m_OffCommand.
     */
    public String getOffCommand() {
        return m_OffCommand;
    }

    /**
     * @param OffCommand The m_OffCommand to set.
     */
    public void setOffCommand(String OffCommand) {
        m_OffCommand = OffCommand;
    }

    /**
     * @return the timezoneIdentifier
     */
    public String getTimezoneIdentifier() {
        return timezoneIdentifier;
    }

    /**
     * @param timezoneIdentifier the timezoneIdentifier to set
     */
    public void setTimezoneIdentifier(String timezoneIdentifier) {
        this.timezoneIdentifier = timezoneIdentifier;
        flexableAlarm.setTimeZoneIdentifier(timezoneIdentifier);
    }

    /**
     * Returns a list of strings containing the available timezones.
     *
     * @return String[]
     */
    public String[] getTimezoneIdentifierItems() {
        return TimeZone.getAvailableIDs();
    }

    /*
     * Activate the instance
     * 
     * @see ssg.home.HomeItem#activate()
     */
    @Override
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        if (timerActive) {
            timerActive = false;
            // Will set to true in method...
            try {
                enableTimer();
            } catch (ExecutionFailure e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns if the satellite is currently active (running) or not.
     *
     * @return Returns the active state, "Yes" or "No".
     */
    public String getState() {
        if (timerActive) {
            return timerIsOn ? "On" : "Enabled";
        }
        return "Disabled";
    }

    public void setState(String state) {
        timerActive = state.compareToIgnoreCase("disabled") != 0;
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        disableTimer();
        alarmManager.removeAllAlarmsAndStop();
    }

    /**
     * @return the mondays
     */
    public String getMondays() {
        return mondays;
    }

    public String getTodayStartEnd() {
        return todayStartEnd;
    }

    @HomeItemAttribute("Sunrise today")
    public String getSunriseToday() {
        try {
            return flexableAlarm.getSunRiseStringByCalendar(null);
            // return sunriseToday;
        } catch (ExecutionFailure ex) {
            Logger.getLogger(DayLiteTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @HomeItemAttribute("Sunset today")
    public String getSunsetToday()  {
        try {
            return flexableAlarm.getSunSetStringByCalendar(null);
//        return sunsetToday;
        } catch (ExecutionFailure ex) {
            Logger.getLogger(DayLiteTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * @param mondays the mondays to set
     * @throws Throwable
     * @throws IllegalValueException
     */
    public void setMondays(String mondays) throws IllegalValueException {
        this.mondays = mondays;

        // verify the command
        if (!commandAccepted(this.mondays)) {
            throw new IllegalValueException("The format of the command is wrong!", mondays);
        }
    }

    private boolean commandAccepted(String command) {
        if (command == null) {
            return true;
        }
        if (command.trim().length() == 0) {
            return true;
        }
        List<AlarmItem> alarmItemList = flexableAlarm.parseCommand(command);
        return (alarmItemList.size() > 0);
    }

    /**
     * @return the tuesdays
     */
    public String getTuesdays() {
        return tuesdays;
    }

    /**
     * @param mTuesdays the tuesdays to set
     * @throws Throwable
     */
    public void setTuesdays(String mTuesdays) throws IllegalValueException {
        tuesdays = mTuesdays;

        // verify the command
        if (!commandAccepted(tuesdays)) {
            throw new IllegalValueException("The format of the command is wrong!", mTuesdays);
        }
    }

    /**
     * @return the wednesdays
     */
    public String getWednesdays() {
        return wednesdays;
    }

    /**
     * @param wednesdays the wednesdays to set
     * @throws IllegalValueException
     */
    public void setWednesdays(String wednesdays) throws IllegalValueException {
        this.wednesdays = wednesdays;

        // verify the command
        if (!commandAccepted(this.wednesdays)) {
            throw new IllegalValueException("The format of the command is wrong!", wednesdays);
        }
    }

    /**
     * @return the thursdays
     */
    public String getThursdays() {
        return thursdays;
    }

    /**
     * @param thursdays the m_thursdays to set
     * @throws Throwable
     */
    public void setThursdays(String thursdays) throws IllegalValueException {
        this.thursdays = thursdays;

        // verify the command
        if (!commandAccepted(this.thursdays)) {
            throw new IllegalValueException("The format of the command is wrong!", thursdays);
        }
    }

    /**
     * @return the fridays
     */
    public String getFridays() {
        return fridays;
    }

    /**
     * @param fridays the m_fridays to set
     * @throws Throwable
     */
    public void setFridays(String fridays) throws IllegalValueException {
        this.fridays = fridays;

        // verify the command
        if (!commandAccepted(this.fridays)) {
            throw new IllegalValueException("The format of the command is wrong!", fridays);
        }
    }

    /**
     * @return the saturdays
     */
    public String getSaturdays() {
        return saturdays;
    }

    /**
     * @param saturdays the m_saturdays to set
     * @throws Throwable
     */
    public void setSaturdays(String saturdays) throws IllegalValueException {
        this.saturdays = saturdays;

        // verify the command
        if (!commandAccepted(this.saturdays)) {
            throw new IllegalValueException("The format of the command is wrong!", saturdays);
        }
    }

    /**
     * @return the sundays
     */
    public String getSundays() {
        return sundays;
    }

    /**
     * @param sundays the m_sundays to set
     * @throws Throwable
     */
    public void setSundays(String sundays) throws IllegalValueException {
        this.sundays = sundays;

        // verify the command
        if (!commandAccepted(this.sundays)) {
            throw new IllegalValueException("The format of the command is wrong!", sundays);
        }
    }

    /**
     * @return the latLong
     */
    @HomeItemAttribute("Location: Lat,Long")
    public String getLatLong() {
        return latLong;
    }

    /**
     * Sets the latitude and longitude coordinate formatted as
     * "latitude,longitude".
     *
     * @param latitudeLongitude
     */
    public void setLatLong(String latitudeLongitude) {
        latLong = latitudeLongitude;
        flexableAlarm.setLongitudeAndLatitude(latLong);
    }

    /**
     * @return the exceptions
     */
    public String getExceptions() {
        return exceptions;
    }

    /**
     * @param exceptions the exceptions to set
     */
    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public DayLiteTimer() {
        init();
    }

    public void init() {
        alarmManager = new AlarmManager();
        flexableAlarm = new FlexableAlarm(getLatLong(), timezoneIdentifier);
    }

    @HomeItemAction(name = "Enable timer")
    public void enableTimer() throws ExecutionFailure {
        if (timerActive) {
            return;
        }

        try {
            if (!calcAll()) {
                throw new ExecutionFailure("Can't start the timer due to errors!");
            }
            m_recalcAlarm = alarmManager.addAlarm("Recalc Alarms", 1, 0, -1,
                    -1, -1, -1, new AlarmListener() {
                        public void handleAlarm(AlarmEntry entry) {
                            calcAll();
                        }
                    });
            timerActive = true;
            logger.finer("DayLiteTimer service is started.");
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void performCommand(String commandString) {
        String result = executor.executeCommandLine(commandString);
        if (!result.startsWith("ok")) {
            logger.warning(result);
        }
    }
    
    @HomeItemAction(name = "Disable timer")
    public void disableTimer() {
        if (!timerActive) {
            return;
        }

        alarmManager.removeAlarm(m_recalcAlarm);
        removeAlarm(mondayAlarms);
        removeAlarm(tuesdayAlarms);
        removeAlarm(wednesdayAlarms);
        removeAlarm(thursdayAlarms);
        removeAlarm(fridayAlarms);
        removeAlarm(saturdayAlarms);
        removeAlarm(sundayAlarms);

        timerActive = false;
        todayStartEnd = "";
        logger.finer("DayLiteTimer service is stopped.");
    }

    private boolean calcAll() {
        logger.finer("Recalculating all alarms for all days of the week...");
        try {
            calculateAlarmEntries(mondayAlarms, mondays, 2);
            calculateAlarmEntries(tuesdayAlarms, tuesdays, 3);
            calculateAlarmEntries(wednesdayAlarms, wednesdays, 4);
            calculateAlarmEntries(thursdayAlarms, thursdays, 5);
            calculateAlarmEntries(fridayAlarms, fridays, 6);
            calculateAlarmEntries(saturdayAlarms, saturdays, 7);
            calculateAlarmEntries(sundayAlarms, sundays, 1);
            return true;
        } catch (ExecutionFailure e) {
            logger.warning(e.getMessage());
        }
        return false;
    }

    private void removeAlarm(LinkedList<AlarmEntry> alarms) {
        while (alarms.size() > 0) {
            alarmManager.removeAlarm(alarms.remove());
        }
    }

    public void calculateAlarmEntries(LinkedList<AlarmEntry> alarms,
            String timePeriodsString, int weekDay) throws ExecutionFailure {

        if (null == alarms) {
            throw new ExecutionFailure("Alarms parameter is null");
        }
        // First remove all alarm entries for this day (alarm list)
        while (alarms.size() > 0) {
            alarmManager.removeAlarm(alarms.remove());
        }

        if (timePeriodsString.length() == 0) {
            return;
        }

        List<AlarmItem> alarmItemList;
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        alarmItemList = flexableAlarm.parseCommand(timePeriodsString);
        if (alarmItemList.size() == 0) {
            throw new ExecutionFailure("The format of the command is wrong!");
        }

//        if (dayOfWeek == weekDay) {
//            sunriseToday = flexableAlarm.getSunRiseStringByCalendar(null);
//            sunsetToday = flexableAlarm.getSunSetStringByCalendar(null);
//        }
        String s = "";
        for (AlarmItem alarmItem : alarmItemList) {
            try {
                Calendar startsAt = alarmItem.startsAtCalendar();
                Calendar endsAt = alarmItem.endsAtCalendar();

                // If the start time is same as or after the end time, then we disregard the alarm item 
                if ((startsAt != null) && (endsAt != null) && (startsAt.after(endsAt) || startsAt.equals(endsAt))) {
                    continue;
                }

                if (startsAt != null) {
                    AlarmEntry onEntry = alarmManager.addAlarm("On Alarm ",
                            startsAt.get(Calendar.MINUTE),
                            startsAt.get(Calendar.HOUR_OF_DAY), -1, -1,
                            weekDay, -1, new AlarmListener() {
                                public void handleAlarm(AlarmEntry entry) {
                                    performCommand(m_OnCommand);
                                    timerIsOn = true;
                                }
                            });
                    alarms.add(onEntry);
                    logger.finer("Alarm set for day: " + weekDay
                            + " to perform onCommand at "
                            + alarmItem.startsAtString());
                    if (dayOfWeek == weekDay) {
                        if (s.length() > 0) {
                            s += ", ";
                        }
                        s += "On: ";
                        s += FlexableAlarm.calendarToString(startsAt);
                        todayStartEnd = s;
                    }
                }

                if (endsAt != null) {
                    AlarmEntry offEntry = alarmManager.addAlarm("Off Alarm ",
                            endsAt.get(Calendar.MINUTE),
                            endsAt.get(Calendar.HOUR_OF_DAY), -1, -1, weekDay,
                            -1, new AlarmListener() {
                                public void handleAlarm(AlarmEntry entry) {
                                    performCommand(m_OffCommand);
                                    timerIsOn = false;
                                }
                            });
                    alarms.add(offEntry);
                    logger.finer("Alarm set for day: " + weekDay
                            + " to perform offComand at "
                            + alarmItem.endsAtString());
                    if (dayOfWeek == weekDay) {
                        if (s.length() > 0) {
                            s += ", ";
                        }
                        s += "Off: ";
                        s += FlexableAlarm.calendarToString(endsAt);
                        todayStartEnd = s;
                    }
                }

            } catch (PastDateException x) {
                System.out
                        .println("'Past date exception', this is simply a warning meaning the indicated day and time has already been passed.");
            }
        }
    }

}
