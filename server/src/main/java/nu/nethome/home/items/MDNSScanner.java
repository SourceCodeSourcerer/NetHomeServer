package nu.nethome.home.items;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

@Plugin
@HomeItemType(value = "Ports")
public class MDNSScanner extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MDNSScanner\" Category=\"Ports\" >"
            + "  <Attribute Name=\"ScanReplies\" Type=\"String\" Get=\"getScanReplies\" Default=\"true\" />"
            + "  <Attribute Name=\"AutoScanInterval\" Type=\"String\" Get=\"getScanInterval\" Set=\"setScanInterval\" />"
            + "  <Action Name=\"scan\" 	Method=\"scan\" />"
            + "</HomeItem> ");

    public static final String MDNS_CREATION_MESSAGE = "mDNS_Creation_Message";
    public static final String MDNS_SCAN_MESSAGE = "mDNS_Scan";
    public static final String MDNS_SERVICE_TYPE = "ServiceType";
    public static final String MDNS_LOCATION = "Location";
    public static final String MDNS_PORT = "Port";
    public static final String MDNS_SERVICE_NAME = "ServiceName";
    private static final String SERVICE_NAME = "_coap._udp.local.";
    private static final long SCAN_TIMEOUT = 1000L;

    private static Logger logger = Logger.getLogger(MDNSScanner.class.getName());
    private int replies = 0;
    private int scanInterval = 60;
    private int minutesUntilNextScan = 2;
    private JmDNS jmdns;

    @Override
    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("ReportItems") || event.isType(MDNS_SCAN_MESSAGE) || isTimeForAutoScan(event)) {
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
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            logger.warning("Could not get host address in MDNSScanner: " + e.getMessage());
        } catch (IOException e) {
            logger.warning("Failed to create mDNS-Scanner: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (jmdns != null) {
            try {
                jmdns.close();
            } catch (IOException e) {
                logger.info("Failed closing mDNS scanner");
            }
        }
        super.stop();
    }

    public String getScanReplies() {
        return Integer.toString(replies);
    }

    public String scan() {
        if (jmdns != null) {
            replies = 0;
            for (ServiceInfo serviceInfo : jmdns.list(SERVICE_NAME, SCAN_TIMEOUT)) {
                reportService(serviceInfo);
                replies++;
            }
        }
        return "";
    }

    private void reportService(ServiceInfo service) {
        Event deviceEvent = server.createEvent(MDNS_CREATION_MESSAGE, "");
        deviceEvent.setAttribute(MDNS_SERVICE_TYPE, service.getType());
        deviceEvent.setAttribute(MDNS_SERVICE_NAME, service.getName());
        deviceEvent.setAttribute(MDNS_LOCATION, service.getHostAddresses()[0]);
        deviceEvent.setAttribute(MDNS_PORT, service.getPort());
        deviceEvent.setAttribute("Direction", "In");
        server.send(deviceEvent);
    }

    public String getScanInterval() {
        return Integer.toString(scanInterval);
    }

    public void setScanInterval(String scanInterval) {
        this.scanInterval = Integer.parseInt(scanInterval);
        minutesUntilNextScan = this.scanInterval;
    }
}
