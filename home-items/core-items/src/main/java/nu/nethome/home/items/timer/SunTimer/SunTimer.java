package nu.nethome.home.items.timer.SunTimer;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.util.*;

import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.SwitchTime;
import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.TIME_EXPRESSION_SEPARATOR;
import static nu.nethome.home.items.timer.SunTimer.TimeExpressionParser.TIME_PERIOD_SEPARATOR;

/**
 *
 */
@SuppressWarnings("UnusedDeclaration")
public class SunTimer extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"SunTimer\" Category=\"Timers\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Location: Lat,Long\" Type=\"String\" Get=\"getLatLong\" 	Set=\"setLatLong\" />"
            + "  <Attribute Name=\"Timer Today\" Type=\"String\" Get=\"getTodayStartEnd\" />"
            + "  <Attribute Name=\"Sunrise(R)\" Type=\"String\" Get=\"getSunriseToday\" />"
            + "  <Attribute Name=\"Sunset(S)\" Type=\"String\" Get=\"getSunsetToday\" />"
            + "  <Attribute Name=\"Variable A\" Type=\"String\" Get=\"getVariableA\" Set=\"setVariableA\" />"
            + "  <Attribute Name=\"Variable B\" Type=\"String\" Get=\"getVariableB\" Set=\"setVariableB\" />"
            + "  <Attribute Name=\"Variable C\" Type=\"String\" Get=\"getVariableC\" Set=\"setVariableC\" />"
            + "  <Attribute Name=\"Mondays\" Type=\"String\" Get=\"getMondays\" 	Set=\"setMondays\" />"
            + "  <Attribute Name=\"Tuesdays\" Type=\"String\" Get=\"getTuesdays\" 	Set=\"setTuesdays\" />"
            + "  <Attribute Name=\"Wednesdays\" Type=\"String\" Get=\"getWednesdays\" 	Set=\"setWednesdays\" />"
            + "  <Attribute Name=\"Thursdays\" Type=\"String\" Get=\"getThursdays\" 	Set=\"setThursdays\" />"
            + "  <Attribute Name=\"Fridays\" Type=\"String\" Get=\"getFridays\" 	Set=\"setFridays\" />"
            + "  <Attribute Name=\"Saturdays\" Type=\"String\" Get=\"getSaturdays\" 	Set=\"setSaturdays\" />"
            + "  <Attribute Name=\"Sundays\" Type=\"String\" Get=\"getSundays\" 	Set=\"setSundays\" />"
            + "  <Attribute Name=\"OnCommand\" Type=\"Command\" Get=\"getOnCommand\" 	Set=\"setOnCommand\" />"
            + "  <Attribute Name=\"OffCommand\" Type=\"Command\" Get=\"getOffCommand\" 	Set=\"setOffCommand\" />"
            + "  <Action Name=\"Enable timer\" 	Method=\"enableTimer\" />"
            + "  <Action Name=\"Disable timer\" 	Method=\"disableTimer\" />"
            + "</HomeItem> ");
    private static final Location DEFAULT_LOCATION = new Location("0.0", "0.0");
    private static final int DAYS_IN_A_WEEK = 7;
    public static final String REPEAT_STRING = "\"";

    private String[] weekDays = new String[DAYS_IN_A_WEEK];
    private List<SwitchTime> switchTimesToday = Collections.emptyList();
    private String onCommand = "";
    private String offCommand = "";
    private String latLong = "";
    private Map<String, String> variables = new HashMap<>();
    private CommandLineExecutor executor;
    private Timer timer;
    private SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(DEFAULT_LOCATION, TimeZone.getDefault());
    private int currentDay = 0;
    private boolean isEnabled = true;
    private boolean isOn = false;

    public SunTimer() {
        for (int i = 0; i < weekDays.length; i++) {
            weekDays[i] = "";
        }
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        createSunCalculator();
        applySwitchTimesForToday();
        currentDay = getDayToday();
    }

    private void createSunCalculator() {
        String[] latAndLong = latLong.split(",");
        Location location;
        if (latAndLong.length == 2) {
            location = new Location(latAndLong[0], latAndLong[1]);
        } else {
            location = DEFAULT_LOCATION;
        }
        sunriseSunsetCalculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());
    }

    @Override
    public void stop() {
        stopCurrentTimer();
        super.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        super.receiveEvent(event);
        if (event.isType(HomeService.MINUTE_EVENT_TYPE)) {
            int dayToday = getDayToday();
            if (dayToday != this.currentDay) {
                applySwitchTimesForToday();
                this.currentDay = dayToday;
            }
            return true;
        }
        return false;
    }

    private void stopCurrentTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    void applySwitchTimesForToday() {
        if (isEnabled) {
            try {
                variables.put("R", getSunriseToday());
                variables.put("S", getSunsetToday());
                switchTimesToday = TimeExpressionParser.parseExpression(getTodaysTimeExpression(), variables);
            } catch (TimeExpressionParser.TimeExpressionException e) {
                switchTimesToday = Collections.emptyList();
            }
            stopCurrentTimer();
            timer = createTimer();
            createTimerTasksForSwitchTimes();
        }
    }

    private void createTimerTasksForSwitchTimes() {
        Calendar time = getTime();
        long nowTime = time.getTimeInMillis();
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        long baseTime = time.getTimeInMillis();
        SwitchTime mostRecentSwitchTime = null;
        for (SwitchTime switchTime : switchTimesToday) {
            long currentSwitchTime = switchTime.value() * 1000 + baseTime;
            if (currentSwitchTime > nowTime) {
                timer.schedule(new SunTimerTask(switchTime.isOn()), new Date(currentSwitchTime));
            } else {
                mostRecentSwitchTime = switchTime;
            }
        }
        if (mostRecentSwitchTime != null) {
            executeOnOffCommand(mostRecentSwitchTime.isOn());
        }
    }

    private void executeOnOffCommand(boolean shouldBeOn) {
        executor.executeCommandLine(shouldBeOn ? onCommand : offCommand);
        isOn = shouldBeOn;
    }

    private String getTodaysTimeExpression() {
        int index = getDayToday() - 1;
        for (int i = 0; i < DAYS_IN_A_WEEK; i++) {
            if (!weekDays[index].equals(REPEAT_STRING)) {
                return weekDays[index];
            }
            index = (index + (DAYS_IN_A_WEEK - 1)) % DAYS_IN_A_WEEK;
        }
        return "";
    }

    public String disableTimer() {
        isEnabled = false;
        stopCurrentTimer();
        switchTimesToday = Collections.emptyList();
        return "";
    }

    public String getTodayStartEnd() {
        String result = "";
        boolean isFirst = true;
        boolean lastTimeIsOn = false;
        for (SwitchTime time : switchTimesToday) {
            if (!(lastTimeIsOn && !time.isOn()) && !isFirst) {
                result += TIME_EXPRESSION_SEPARATOR;
            }
            if (!time.isOn() && !lastTimeIsOn) {
                result += TIME_PERIOD_SEPARATOR;
            }
            result += time.valueAsTimeString();
            if (time.isOn()) {
                result += TIME_PERIOD_SEPARATOR;
            }
            lastTimeIsOn = time.isOn();
            isFirst = false;
        }
        return result;
    }

    int getDayToday() {
        return getTime().get(Calendar.DAY_OF_WEEK);
    }

    public String getSunriseToday() {
        return sunriseSunsetCalculator.getOfficialSunriseForDate(getTime());
    }

    public String getSunsetToday() {
        return sunriseSunsetCalculator.getOfficialSunsetForDate(getTime());
    }

    public String getMondays() {
        return weekDays[1];
    }

    public void setMondays(String times) {
        setTimeExpressionForDay(1, times);
    }

    private void setTimeExpressionForDay(int day, String times) {
        String oldValue = weekDays[day];
        weekDays[day] = times;
        if (isActivated() && !oldValue.equals(times)) {
            applySwitchTimesForToday();
        }
    }

    public String getTuesdays() {
        return weekDays[2];
    }

    public void setTuesdays(String times) {
        setTimeExpressionForDay(2, times);
    }

    public String getWednesdays() {
        return weekDays[3];
    }

    public void setWednesdays(String times) {
        setTimeExpressionForDay(3, times);
    }

    public String getThursdays() {
        return weekDays[4];
    }

    public void setThursdays(String times) {
        setTimeExpressionForDay(4, times);
    }

    public String getFridays() {
        return weekDays[5];
    }

    public void setFridays(String times) {
        setTimeExpressionForDay(5, times);
    }

    public String getSaturdays() {
        return weekDays[6];
    }

    public void setSaturdays(String times) {
        setTimeExpressionForDay(6, times);
    }

    public String getSundays() {
        return weekDays[0];
    }

    public void setSundays(String times) {
        setTimeExpressionForDay(0, times);
    }

    public String getOnCommand() {
        return onCommand;
    }

    public void setOnCommand(String onCommand) {
        this.onCommand = onCommand;
    }

    public String getOffCommand() {
        return offCommand;
    }

    public void setOffCommand(String offCommand) {
        this.offCommand = offCommand;
    }

    public String getVariableA() {
        String v = variables.get("A");
        return v != null ? v : "";
    }

    public void setVariableA(String value) {
        variables.put("A", value);
    }

    public String getVariableB() {
        String v = variables.get("B");
        return v != null ? v : "";
    }

    public void setVariableB(String value) {
        variables.put("B", value);
    }

    public String getVariableC() {
        String v = variables.get("C");
        return v != null ? v : "";
    }

    public void setVariableC(String value) {
        variables.put("C", value);
    }

    Timer createTimer() {
        return new Timer("SunTimer", true);
    }

    Calendar getTime() {
        return Calendar.getInstance();
    }

    public String getLatLong() {
        return latLong;
    }

    public void setLatLong(String latLong) {
        if (!latLong.equals(this.latLong)) {
            this.latLong = latLong;
            createSunCalculator();
        }
    }

    public String getState() {
        return isEnabled ? (isOn ? "On" : "Enabled") : "Disabled";
    }

    public void setState(String state) {
        isEnabled = !"Disabled".equalsIgnoreCase(state);
    }
    class SunTimerTask extends TimerTask {

        private final boolean on;

        SunTimerTask(boolean on) {
            this.on = on;
        }

        @Override
        public void run() {
            executeOnOffCommand(on);
        }
    }
}
