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
package nu.nethome.home.items.timer;

import com.jtheory.jdring.AlarmEntry;
import com.jtheory.jdring.AlarmListener;
import com.jtheory.jdring.AlarmManager;
import com.jtheory.jdring.PastDateException;
import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.LinkedList;
import java.util.logging.Logger;
import nu.nethome.home.item.annotation.HomeItemAction;
import nu.nethome.home.item.HomeItemAdapter2;
import nu.nethome.home.item.annotation.HomeItemAttribute;

/**
 *
 * LampTimer
 *
 * @author Stefan
 */
@Plugin
@HomeItemType("Timers")
public class WeekTimer extends HomeItemAdapter2 implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WeekTimer\" Category=\"Timers\" >"
            + "  <Attribute Name=\"WeekDayTimes\" Type=\"String\" Get=\"getWeekDayTimes\" 	Set=\"setWeekDayTimes\" />"
            + "  <Attribute Name=\"WeekEndTimes\" Type=\"String\" Get=\"getWeekEndTimes\" 	Set=\"setWeekEndTimes\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Action Name=\"On\" 	Method=\"on\" />"
            + "  <Action Name=\"Off\" 	Method=\"off\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WeekTimer.class.getName());
    protected AlarmManager alarmManager;
    protected LinkedList<AlarmEntry> weekDayAlarms = new LinkedList<AlarmEntry>();
    protected LinkedList<AlarmEntry> weekEndAlarms = new LinkedList<AlarmEntry>();
    protected CommandLineExecutor executor;

    protected static final int weekDays[] = {2, 3, 4, 5, 6};
    protected static final int weekEndDays[] = {1, 7};
    protected static final int empty[] = {-1};

    // Public attributes
    @HomeItemAttribute
    protected String weekDayTimes = "";
    @HomeItemAttribute(value = "WeekEndTimes", get = "getWeekEndTimes", set = "setWeekEndTimes")
    protected String m_WeekEndTimes = "";
    @HomeItemAttribute(value = "OnCommand", type = "Command", get = "getOnCommand", set = "setOnCommand")
    protected String m_OnCommand = "";
    @HomeItemAttribute(value = "OffCommand", type = "Command", get = "getOffCommand", set = "setOffCommand")
    protected String m_OffCommand = "";

    protected String m_Active = "";

    public WeekTimer() {
        alarmManager = new AlarmManager();
    }

    /* (non-Javadoc)
     * @see ssg.home.HomeItem#getModel()
     */
    public String getModelOld() {
        return MODEL;
    }

    /* Activate the instance
     * @see ssg.home.HomeItem#activate()
     */
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        alarmManager.removeAllAlarmsAndStop();
    }

    protected void performCommand(String commandString) {
        String result = executor.executeCommandLine(commandString);
        if (!result.startsWith("ok")) {
            logger.warning(result);
        }
    }

    /**
     * @return Returns the weekDayTimes.
     */
    public String getWeekDayTimes() {
        return weekDayTimes;
    }

    /**
     * @param WeekDayTimes The weekDayTimes to set.
     */
    public void setWeekDayTimes(String WeekDayTimes) {
        // Set the string
        weekDayTimes = WeekDayTimes.trim();
        // Transform the string to alarm entries
        calculateAlarmEntries(weekDayAlarms, WeekDayTimes, weekDays);
    }

    public void calculateAlarmEntries(LinkedList<AlarmEntry> alarms, String timePeriodsString, int weekDays[]) {
        // First remove all on alarm entries
        while (alarms.size() > 0) {
            alarmManager.removeAlarm(alarms.remove());
        }
        if (timePeriodsString.isEmpty()) {
            return;
        }
        // Scan through the string and add all the on time alarms
        String timePeriods[] = timePeriodsString.split(",");
        for (int i = 0; i < timePeriods.length; i++) {
            try {
                // Split start and end time
                String period[] = timePeriods[i].split("-");
                if (period.length != 2) {
                    throw new IllegalValueException("Bad date format", timePeriodsString);
                }
			// Start processing Start Time
                // Split minute and hour
                String times[] = period[0].split(":");
                if (times.length != 2) {
                    throw new IllegalValueException("Bad date format", timePeriodsString);
                }
                int onMinutes[] = new int[1];
                int onHours[] = new int[1];
                onMinutes[0] = Integer.parseInt(times[1]);
                onHours[0] = Integer.parseInt(times[0]);
                logger.fine(String.format("Adding on alarm entry: %d:%d", onHours[0], onMinutes[0]));
                AlarmEntry onEntry = alarmManager.addAlarm("On Alarm ", onMinutes, onHours, empty, empty, weekDays, -1, new AlarmListener() {
                    public void handleAlarm(AlarmEntry entry) {
                        performCommand(m_OnCommand);
                    }
                });
                alarms.add(onEntry);

			// Start processing End Time
                // Split minute and hour
                times = period[1].split(":");
                if (times.length != 2) {
                    throw new IllegalValueException("Bad date format", timePeriodsString);
                }
                int offMinutes[] = new int[1];
                int offHours[] = new int[1];
                offMinutes[0] = Integer.parseInt(times[1]);
                offHours[0] = Integer.parseInt(times[0]);
                logger.fine(String.format("Adding off alarm entry: %d:%d", offHours[0], offMinutes[0]));
                AlarmEntry offEntry = alarmManager.addAlarm("Off Alarm ", offMinutes, offHours, empty, empty, weekDays, -1, new AlarmListener() {
                    public void handleAlarm(AlarmEntry entry) {
                        performCommand(m_OffCommand);
                    }
                });
                alarms.add(offEntry);
            } catch (PastDateException | IllegalValueException e) {
                logger.warning("Bad date entered in WeekTimer: " + timePeriodsString);
            }
        }
    }

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
     * @return Returns the m_WeekEndTimes.
     */
    public String getWeekEndTimes() {
        return m_WeekEndTimes;
    }

    /**
     * @param WeekEndTimes The m_WeekEndTimes to set.
     */
    public void setWeekEndTimes(String WeekEndTimes) {
        // Set the string
        m_WeekEndTimes = WeekEndTimes.trim();
        // Transform the string to alarm entries
        calculateAlarmEntries(weekEndAlarms, WeekEndTimes, weekEndDays);
    }

    /**
     * @return Returns the active.
     */
    public String getActive() {
        return m_Active;
    }

    /**
     * @param Active The active to set.
     */
    public void setActive(String Active) {
        m_Active = Active;
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

    @HomeItemAction
    public void on() {
        performCommand(m_OnCommand);
    }

    @HomeItemAction
    public void off() {
        performCommand(m_OffCommand);
    }
}
