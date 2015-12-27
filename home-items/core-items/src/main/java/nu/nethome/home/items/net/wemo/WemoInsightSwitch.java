package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.UPnPScanner;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Belkin wemo insight switch
 *
 * @author Stefan
 *         Todo: Move out soap client
 *         Todo: Handle lost connection
 *         Todo: timePeriod
 *         Todo: todayMW
 *         Todo: powerThresholdMW
 *         Todo: lastChange (??)
 *         Todo: subscribe for state changes
 */

@Plugin
@HomeItemType(value = "Lamps", creationInfo = WemoInsightSwitch.WemoCreationInfo.class)
public class WemoInsightSwitch extends WemoSwitch implements HomeItem {

    public static final int READ_RETRY_ATTEMPTS = 1;
    public static final String BELKIN_DEVICE_INSIGHT_URN = "urn:Belkin:device:insight:1";

    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UPN_P_CREATION_MESSAGE};

        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("DeviceType").equals(BELKIN_DEVICE_INSIGHT_URN);
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Insight Switch: \"%s\", SerialNr: %s", e.getAttribute("FriendlyName"), e.getAttribute("SerialNumber"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"SerialNumber\" Type=\"String\" Get=\"getSerialNumber\" 	Init=\"setSerialNumber\" />"
            + "  <Attribute Name=\"CurrentConsumption\" Type=\"String\" Get=\"getCurrentPowerConsumption\" 	Unit=\"W\" />"
            + "  <Attribute Name=\"LastOnTime\" Type=\"Duration\" Get=\"getLastOnTime\" />"
            + "  <Attribute Name=\"OnTimeToday\" Type=\"Duration\" Get=\"getOnTimeToday\" />"
            + "  <Attribute Name=\"TotalOnTime\" Type=\"Duration\" Get=\"getTotalOnTime\" />"
            + "  <Attribute Name=\"TotalConsumption\" Type=\"String\" Get=\"getTotalPowerConsumption\" 	Unit=\"kWh\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");
    public static final int TIME_TO_CACHE_STATE = 300;

    private static Logger logger = Logger.getLogger(WemoInsightSwitch.class.getName());
    private InsightState currentState;
    private long lastStateUpdate = 0;

    public String getModel() {
        return MODEL;
    }

    @Override
    protected String getDeviceType() {
        return BELKIN_DEVICE_INSIGHT_URN;
    }

    @Override
    public String getState() {
        updateCurrentState();
        if (currentState == null) {
            return "";
        } else if (currentState.getState() == InsightState.State.On) {
            return "On " + getCurrentPowerConsumption();
        } else if (currentState.getState() == InsightState.State.Idle) {
            return "Idle";
        }
        return "Off";
    }

    @Override
    protected void setOnStateWithRetry(boolean isOn) {
        super.setOnStateWithRetry(isOn);
        lastStateUpdate = 0;
    }

    @Override
    public void toggle() {
        logger.fine("Toggling " + name);
        updateCurrentState();
        if (currentState == null) {
            return;
        }
        if (currentState.getState() == InsightState.State.Off) {
            on();
        } else {
            off();
        }
    }

    public String getCurrentPowerConsumption() {
        updateCurrentState();
        return currentState != null ? String.format("%.1f", currentState.getCurrentConsumption()) : "";
    }

    public String getTotalPowerConsumption() {
        updateCurrentState();
        return currentState != null ? String.format("%.3f", currentState.getTotalConsumption()) : "";
    }

    public String getTotalOnTime() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getTotalOnTime()) : "";
    }

    public String getLastOnTime() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getLastOnTime()) : "";
    }

    public String getOnTimeToday() {
        updateCurrentState();
        return currentState != null ? String.format("%d", currentState.getOnTimeToday()) : "";
    }

    private void updateCurrentState() {
        if (System.currentTimeMillis() - lastStateUpdate > TIME_TO_CACHE_STATE) {
            for (int retry = 0; retry < READ_RETRY_ATTEMPTS; retry++) {
                try {
                    currentState = getInsightSwitch().getInsightParameters();
                    lastStateUpdate = System.currentTimeMillis();
                    return;
                } catch (WemoException e) {
                    logger.log(Level.FINE, "Failed to get from " + wemoDescriptionUrl, e);
                    currentState = null;
                    lastStateUpdate = System.currentTimeMillis(); // Treat this as an update too
                    if (retry == 0) {
                        server.send(server.createEvent(UPnPScanner.UPN_P_SCAN_MESSAGE, ""));
                    }
                }
            }
            logger.log(Level.FINE, String.format("Failed to get state in %s after %d retries", name, READ_RETRY_ATTEMPTS));
        }
    }
}
