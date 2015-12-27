package nu.nethome.home.items.net.wemo;

/**
 *
 */
public class BridgeDevice {
    private final int deviceIndex;
    private final String friendlyName;
    private final String iconVersion;
    private final String firmwareVersion;
    private final BridgeDeviceStatus status;

    public BridgeDevice(int deviceIndex, String deviceID, String friendlyName, String iconVersion, String firmwareVersion, String capabilityIDs, String currentState) {
        this.status = new BridgeDeviceStatus(deviceID, capabilityIDs, currentState);
        this.deviceIndex = deviceIndex;
        this.friendlyName = friendlyName;
        this.iconVersion = iconVersion;
        this.firmwareVersion = firmwareVersion;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public String getDeviceID() {
        return status.getDeviceID();
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getIconVersion() {
        return iconVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getCapabilityIDs() {
        return status.getCapabilityIDs();
    }

    public String getCurrentRawState() {
        return status.getCurrentRawState();
    }

    public int getOnState() {
        return status.getOnState();
    }

    public int getBrightness() {
        return status.getBrightness();
    }
}
