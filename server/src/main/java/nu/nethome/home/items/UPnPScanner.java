package nu.nethome.home.items;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.util.logging.Logger;

@Plugin
@HomeItemType(value = "Ports")
public class UPnPScanner extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"UPnPScanner\" Category=\"Ports\" >"
            + "  <Attribute Name=\"ScanReplies\" Type=\"String\" Get=\"getScanReplies\" Default=\"true\" />"
            + "  <Attribute Name=\"AutoScanInterval\" Type=\"String\" Get=\"getScanInterval\" Set=\"setScanInterval\" />"
            + "  <Action Name=\"scan\" 	Method=\"scan\" />"
            + "</HomeItem> ");

    public static final String UPN_P_CREATION_MESSAGE = "UPnP_Creation_Message";
    public static final String UPN_P_SCAN_MESSAGE = "UPnP_Scan";
    public static final String DEVICE_TYPE = "DeviceType";
    public static final String LOCATION = "Location";
    public static final String SERIAL_NUMBER = "SerialNumber";
    public static final String FRIENDLY_NAME = "FriendlyName";
    public static final String MODEL_NAME = "ModelName";
    public static final String UDN = "UDN";

    private static Logger logger = Logger.getLogger(UPnPScanner.class.getName());
    private final ControlPoint controlPoint = new ControlPoint();
    private int replies = 0;
    private int scanInterval = 60;
    private int minutesUntilNextScan = 1;

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("ReportItems") || event.isType(UPN_P_SCAN_MESSAGE) || isTimeForAutoScan(event)) {
            scan();
            return true;
        }
        return false;
    }

    private boolean isTimeForAutoScan(Event event) {
        if (event.isType(HomeService.MINUTE_EVENT_TYPE)) {
            if (--minutesUntilNextScan <= 0) {
                minutesUntilNextScan = scanInterval;
                return true;
            }
        }
        return false;
    }

    @Override
    public void activate() {
        if (controlPoint.start()) {
            controlPoint.addDeviceChangeListener(new DeviceChangeListener() {
                @Override
                public void deviceAdded(Device device) {
                    reportDevice(device);
                }

                @Override
                public void deviceRemoved(Device device) {
                }
            });
        } else {
            logger.warning("Could not start UPnP scanner");
        }
    }

    @Override
    public void stop() {
        controlPoint.stop();
        super.stop();
    }

    private void reportDevice(Device device) {
        replies++;
        Event deviceEvent = server.createEvent(UPN_P_CREATION_MESSAGE, "");
        deviceEvent.setAttribute(DEVICE_TYPE, device.getDeviceType());
        deviceEvent.setAttribute(MODEL_NAME, device.getModelName());
        deviceEvent.setAttribute(LOCATION, device.getLocation());
        deviceEvent.setAttribute(SERIAL_NUMBER, device.getSerialNumber());
        deviceEvent.setAttribute(FRIENDLY_NAME, device.getFriendlyName());
        deviceEvent.setAttribute(UDN, device.getUDN());
        deviceEvent.setAttribute("Direction", "In");
        server.send(deviceEvent);
    }

    public String getScanReplies() {
        return Integer.toString(replies);
    }

    public String scan() {
        replies = 0;
        for (int i = 0; i < controlPoint.getDeviceList().size(); i++) {
            reportDevice(controlPoint.getDeviceList().getDevice(i));
        }
        controlPoint.search();
        return "";
    }

    public String getScanInterval() {
        return Integer.toString(scanInterval);
    }

    public void setScanInterval(String scanInterval) {
        this.scanInterval = Integer.parseInt(scanInterval);
        minutesUntilNextScan = this.scanInterval;
    }
}
