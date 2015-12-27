package nu.nethome.home.items.net.wemo;

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.UPnPScanner;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Belkin wemo switch
 *
 * @author Stefan
 */

@Plugin
@HomeItemType(value = "Lamps", creationInfo = WemoSwitch.WemoCreationInfo.class)
public class WemoSwitch extends HomeItemAdapter implements HomeItem {

    public static final String UPN_P_CREATION_MESSAGE = "UPnP_Creation_Message";
    public static final String BELKIN_DEVICE_TYPE_URN = "urn:Belkin:device:controllee:1";
    public static final int UPDATE_RETRY_ATTEMPTS = 4;
    public static final int RETRY_DELAY = 30000;
    private Timer retryTimer;
    private volatile boolean lastWantedOnState = false;


    public static class WemoCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {UPN_P_CREATION_MESSAGE};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return e.getAttribute("DeviceType").equals("urn:Belkin:device:controllee:1");
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Belkin Wemo Switch: \"%s\", SerialNr: %s",e.getAttribute("FriendlyName"), e.getAttribute("SerialNumber"));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"WemoInsightSwitch\" Category=\"Lamps\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"DeviceURL\" Type=\"String\" Get=\"getDeviceURL\" 	Set=\"setDeviceURL\" />"
            + "  <Attribute Name=\"SerialNumber\" Type=\"String\" Get=\"getSerialNumber\" 	Init=\"setSerialNumber\" />"
            + "  <Action Name=\"on\" 	Method=\"on\" />"
            + "  <Action Name=\"off\" 	Method=\"off\" />"
            + "  <Action Name=\"toggle\" 	Method=\"toggle\" Default=\"true\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(WemoSwitch.class.getName());
    protected WemoInsightSwitchClient insightSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");
    protected String wemoDescriptionUrl = "";

    protected String serialNumber = "";

    public String getModel() {
        return MODEL;
    }

    WemoInsightSwitchClient getInsightSwitch() {
        return insightSwitch;
    }

    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(UPN_P_CREATION_MESSAGE) &&
                event.getAttribute("DeviceType").equals(getDeviceType()) &&
                event.getAttribute("SerialNumber").equals(serialNumber)) {
            setDeviceURL(event.getAttribute("Location"));
            return true;
        }
        return handleInit(event);
    }

    protected String getDeviceType() {
        return BELKIN_DEVICE_TYPE_URN;
    }

    @Override
    protected boolean initAttributes(Event event) {
        setDeviceURL(event.getAttribute("Location"));
        serialNumber = event.getAttribute("SerialNumber");
        return true;
    }

    private String extractBaseUrl(String url) {
        int pos = url.indexOf("/", 9);
        if (pos > 0) {
            return url.substring(0, pos);
        }
        return url;
    }

    public String getState() {
        try {
            return insightSwitch.getOnState() ? "On" : "Off";
        } catch (WemoException e) {
            logger.log(Level.FINE, "Failed to get state in " + name, e);
            return "";
        }
    }

    public String getDeviceURL() {
        return wemoDescriptionUrl;
    }

    public void setDeviceURL(String url) {
        wemoDescriptionUrl = url;
        insightSwitch.setWemoURL(extractBaseUrl(url));
    }

    public void on() {
        logger.fine("Switching on " + name);
        setOnStateWithRetry(true);
    }

    public void off() {
        logger.fine("Switching off " + name);
        setOnStateWithRetry(false);
    }

    protected synchronized void setOnStateWithRetry(boolean isOn) {
        lastWantedOnState = isOn;
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer = null;
        }
        if (!setInternalOnState(isOn)) {
            server.send(server.createEvent(UPnPScanner.UPN_P_SCAN_MESSAGE, ""));
            retryTimer = new Timer("WemoSwithRetryTimer", true);
            retryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setInternalOnState(lastWantedOnState);
                }
            }, RETRY_DELAY);
        }
    }

    boolean setInternalOnState(boolean isOn) {
        for (int retry = 0; retry < UPDATE_RETRY_ATTEMPTS; retry++) {
            try {
                getInsightSwitch().setOnState(isOn);
                return true;
            } catch (WemoException e) {
                logger.log(Level.FINE, "Failed to set on state in " + wemoDescriptionUrl, e);
            }
        }
        logger.log(Level.INFO, String.format("Failed to set on state in %s after %d retries", name, UPDATE_RETRY_ATTEMPTS));
        return false;
    }


    public void toggle() {
        try {
            setOnStateWithRetry(!insightSwitch.getOnState());
        } catch (WemoException e) {
            logger.warning("Failed to contact Wemo device: " + e.getMessage());
        }
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
