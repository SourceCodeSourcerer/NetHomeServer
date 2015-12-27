package nu.nethome.home.items;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.usb4java.*;

import java.util.logging.Logger;

@Plugin
@HomeItemType(value = "Ports")
public class UsbScanner extends HomeItemAdapter {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"UsbScanner\" Category=\"Ports\" >"
            + "  <Attribute Name=\"ScanReplies\" Type=\"String\" Get=\"getScanReplies\" Default=\"true\" />"
            + "  <Action Name=\"scan\" 	Method=\"scan\" />"
            + "</HomeItem> ");
    public static final String USB_REPORT_TYPE = "USB_Report";
    public static final String VENDOR_ID = "VID";
    public static final String PRODUCT_ID = "PID";
    public static final String EVENT = "Event";
    private Context context = new Context();
    private static Logger logger = Logger.getLogger(UsbScanner.class.getName());
    private volatile boolean hotPlugActive = false;
    private HotplugCallbackHandle callbackHandle;

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate() {
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) {
            logger.warning("UsbScanner could not open USB library");
            return;
        }
        registerHotPlug();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("ReportItems")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    scan();
                }
            }).start();
            return true;
        }
        return false;
    }



    private void registerHotPlug() {
        if (!LibUsb.hasCapability(LibUsb.CAP_HAS_HOTPLUG)) {
            logger.fine("libusb doesn't support hotplug on this system");
            return;
        }

        hotPlugActive = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                callbackLoop();
            }
        });
        thread.start();

        // Register the hotplug callback
        callbackHandle = new HotplugCallbackHandle();
        int result = LibUsb.hotplugRegisterCallback(null,
                LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED
                        | LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                LibUsb.HOTPLUG_ENUMERATE,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                new HotplugCallback() {
                    @Override
                    public int processEvent(Context context, Device device, int event, Object userData) {
                        return hotPlugCallback(device, event);
                    }
                }, null, callbackHandle);
        if (result != LibUsb.SUCCESS) {
            hotPlugActive = false;
            logger.warning("UsbScanner is unable to register hotplug callback");
        }
    }

    private int hotPlugCallback(Device device, int event) {
        String eventType = (event == LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED) ? "Added" : "Removed";
        DeviceDescriptor descriptor = new DeviceDescriptor();
        int result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result == LibUsb.SUCCESS) {
            reportDevice(descriptor, eventType);
        }
        return 0;
    }

    private void callbackLoop() {
        while (hotPlugActive) {
            int result = LibUsb.handleEventsTimeout(null, 1000000);
            if (result != LibUsb.SUCCESS) {
                logger.warning("UsbScanner unable to handle events");
                return;
            }
        }
    }

    @Override
    public void stop() {
        if (hotPlugActive) {
            hotPlugActive = false;
            LibUsb.hotplugDeregisterCallback(null, callbackHandle);
        }
        LibUsb.exit(context);
        super.stop();
    }

    public String scan() {
        DeviceList list = new DeviceList();
        try {
            int result = LibUsb.getDeviceList(context, list);
            if (result < 0) throw new LibUsbException("Unable to read device list", result);
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                if (descriptor.bDeviceClass() != 9) {
                    reportDevice(descriptor, "Present");
                }
            }
        } catch (LibUsbException e) {
            logger.warning("UsbScanner: " + e.getMessage());
        } finally {
            LibUsb.freeDeviceList(list, true);
        }
        return "";
    }

    private void reportDevice(DeviceDescriptor descriptor, String event) {
        Event UsbEvent = server.createEvent(USB_REPORT_TYPE, "");
        UsbEvent.setAttribute(EVENT, event);
        UsbEvent.setAttribute(VENDOR_ID, String.format("%04X", descriptor.idVendor()));
        UsbEvent.setAttribute(PRODUCT_ID, String.format("%04X", descriptor.idProduct()));
        UsbEvent.setAttribute("Direction", "In");
        server.send(UsbEvent);
    }
}
