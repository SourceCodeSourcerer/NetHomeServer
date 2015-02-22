package nu.nethome.home.items.timer;

import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@Plugin
@HomeItemType("Timers")
public class LampRepeater extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"LampRepeater\" Category=\"Timers\" >"
            + "  <Attribute Name=\"Repeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
            + "  <Attribute Name=\"Lamps\" Type=\"Items\" Get=\"getLamps\" 	Set=\"setLamps\" />"
            + "</HomeItem> ");
    public static final long PERIOD_MS = 1000 * 15;

    private static Logger logger = Logger.getLogger(IntervalTimer.class.getName());
    Map<Long, LampState> states = new HashMap<Long, LampState>();
    Timer timer;

    // Public attributes
    private String items = "";
    private int repeats = 3;

    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE)) {
            checkLampStates();
        }
        return true;
    }

    @Override
    public void activate() {
        timer = new Timer("LampRepeater", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkLampStates();
            }
        }, PERIOD_MS, PERIOD_MS);
    }

    private void checkLampStates() {
        String[] lampIds = items.split(",");
        for (String lampId : lampIds) {
            HomeItemProxy lamp = server.openInstance(lampId);
            if (lamp != null) {
                LampState lampState = findLampState(lamp);
                boolean lampIsOn = lamp.getAttributeValue("State").equalsIgnoreCase("on");
                if (lampIsOn != lampState.isOn()) {
                    lampState.triggerCounter(repeats);
                    lampState.setOn(lampIsOn);
                }
                if (lampState.decrementCounter() > 0) {
                    try {
                        lamp.callAction(lampIsOn ? "on" : "off");
                    } catch (ExecutionFailure executionFailure) {
                        // Ignore
                    }
                }
            }
        }
    }

    private LampState findLampState(HomeItemProxy lamp) {
        Long lampId = Long.parseLong(lamp.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE));
        LampState result = states.get(lampId);
        if (result == null) {
            result = new LampState(lampId);
            states.put(lampId, result);
        }
        return result;
    }

    public String getLamps() {
        return items;
    }

    public void setLamps(String items) {
        this.items = items;
    }

    public String getRepeats() {
        return Integer.toString(repeats);
    }

    public void setRepeats(String repeats) {
        this.repeats = Integer.parseInt(repeats);
    }

    private class LampState {
        final long lampId;
        private boolean on;
        private int repeatCounter;

        private LampState(long lampId) {
            this.lampId = lampId;
        }

        private boolean isOn() {
            return on;
        }

        private void setOn(boolean on) {
            this.on = on;
        }

        public void triggerCounter(int repeats) {
            repeatCounter = repeats;
        }

        public int decrementCounter() {
            int result = repeatCounter;
            if (repeatCounter > 0) {
                repeatCounter--;
            }
            return result;
        }
    }
}