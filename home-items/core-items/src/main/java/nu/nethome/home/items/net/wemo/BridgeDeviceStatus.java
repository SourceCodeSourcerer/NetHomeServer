package nu.nethome.home.items.net.wemo;

import nu.nethome.home.items.net.wemo.soap.LightSoapClient;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

public class BridgeDeviceStatus implements LightSoapClient.Argument{
    public static final String CAPABILITIES = "10006,10008,30008,30009,3000A";
    public static final String ON_OFF_CAPABILITY = "10006";
    public static final String DIM_CAPABILITY = "10008";
    private final String deviceID;
    private final String capabilityIDs;
    private final String currentState;
    private final int onState;
    private final int brightness;

    public BridgeDeviceStatus(String deviceID, String capabilityIDs, String currentState) {
        this.deviceID = deviceID;
        this.capabilityIDs = capabilityIDs;
        this.currentState = currentState;
        String stateParts[] = currentState.split(",");
        if (stateParts.length >= 2) {
            if (stateParts[0].equals("1")) {
                onState = 1;
            } else {
                onState = 0;
            }
            String brightParts[] = stateParts[1].split(":");
            brightness = Integer.parseInt(brightParts[0]);
        } else {
            onState = -1;
            brightness = -1;
        }
    }

    public BridgeDeviceStatus(String deviceID, boolean isOn, int dimLevel) {
        this.deviceID = deviceID;
        String capabilities = ON_OFF_CAPABILITY;
        String state = isOn ? "1" : "0";
        if (isOn) {
            capabilities += "," + DIM_CAPABILITY;
            state += String.format(",%d:0", dimLevel);
        }
        this.capabilityIDs = capabilities;
        this.currentState = state;
        onState = isOn ? 1 : 0;
        brightness = dimLevel;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getCapabilityIDs() {
        return capabilityIDs;
    }

    public String getCurrentRawState() {
        return currentState;
    }

    public int getOnState() {
        return onState;
    }

    public int getBrightness() {
        return brightness;
    }

    @Override
    public void addAsChild(SOAPElement parent) throws SOAPException {
        parent.addChildElement("DeviceStatusList")
                .addTextNode(String.format(DEVICE_STATUS_LIST,
                        deviceID,
                        getCapabilityIDs(),
                        getCurrentRawState()));
    }

    static final String DEVICE_STATUS_LIST = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<DeviceStatusList>" +
            "<DeviceStatus>" +
            "<IsGroupAction>NO</IsGroupAction>" +
            "<DeviceID available=\"YES\">%s</DeviceID>" +
            "<CapabilityID>%s</CapabilityID>" +
            "<CapabilityValue>%s</CapabilityValue>" +
            "</DeviceStatus>" +
            "</DeviceStatusList>";
}
