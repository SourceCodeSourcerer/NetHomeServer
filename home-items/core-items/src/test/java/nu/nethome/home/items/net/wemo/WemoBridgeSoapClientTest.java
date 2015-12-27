package nu.nethome.home.items.net.wemo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class WemoBridgeSoapClientTest {

    private WemoBridgeSoapClient client;

    @Before
    public void setUp() throws Exception {
        client = new WemoBridgeSoapClient("http://192.168.1.5:49153");
        //client = new WemoBridgeSoapClient("http://127.0.0.1:49000");
    }

    @Ignore
    @Test
    public void testConnection() throws Exception, WemoException {
        List<BridgeDevice> endDevices = client.getEndDevices("uuid:Bridge-1_0-231447B0100DE4");
        int size = endDevices.size();
    }

    @Ignore
    @Test
    public void testConnection2() throws Exception, WemoException {
        List<BridgeDeviceStatus> statuses = client.getDeviceStatus("94103EA2B278CAD5");
        int size = statuses.size();
    }

    @Ignore
    @Test
    public void testConnection3() throws Exception, WemoException {
        boolean result = client.setDeviceStatus("94103EA2B278CAD5", true, 50);
        int size = result ? 1 : 0;
    }

    @Test
    public void canListDevices() throws Exception, WemoException {
        client = spy(client);
        Map<String, String> result = new HashMap<>();
        result.put("DeviceLists", DEVICE_LIST_RESPONSE);
        doReturn(result).when(client).sendRequest(anyString(), anyString(), anyString(), anyList());

        List<BridgeDevice> endDevices = client.getEndDevices("");
        assertThat(endDevices.size(), is(1));
        assertThat(endDevices.get(0).getDeviceIndex(), is(0));
        assertThat(endDevices.get(0).getDeviceID(), is("94103EA2B278CAD5"));
        assertThat(endDevices.get(0).getFriendlyName(), is("Lightbulb 01"));
        assertThat(endDevices.get(0).getFirmwareVersion(), is("7E"));
        assertThat(endDevices.get(0).getCapabilityIDs(), is("10006,10008,30008,30009,3000A"));
        assertThat(endDevices.get(0).getCurrentRawState(), is(",,,,"));
        assertThat(endDevices.get(0).getOnState(), is(WemoBridgeSoapClient.UNKNOWN));
        assertThat(endDevices.get(0).getBrightness(), is(WemoBridgeSoapClient.UNKNOWN));
    }

    static final String DEVICE_LIST_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<DeviceLists>\n" +
            "    <DeviceList>\n" +
            "        <DeviceListType>Paired</DeviceListType>\n" +
            "        <DeviceInfos>\n" +
            "            <DeviceInfo>\n" +
            "                <DeviceIndex>0</DeviceIndex>\n" +
            "                <DeviceID>94103EA2B278CAD5</DeviceID>\n" +
            "                <FriendlyName>Lightbulb 01</FriendlyName>\n" +
            "                <IconVersion>1</IconVersion>\n" +
            "                <FirmwareVersion>7E</FirmwareVersion>\n" +
            "                <CapabilityIDs>10006,10008,30008,30009,3000A</CapabilityIDs>\n" +
            "                <CurrentState>,,,,</CurrentState>\n" +
            "            </DeviceInfo>\n" +
            "        </DeviceInfos>\n" +
            "    </DeviceList>\n" +
            "</DeviceLists>";

    @Test
    public void canListDeviceStatuses() throws Exception, WemoException {
        client = spy(client);
        Map<String, String> result = new HashMap<>();
        result.put("DeviceStatusList", DEVICE_STATUS_RESPONSE);
        doReturn(result).when(client).sendRequest(anyString(), anyString(), anyString(), anyList());

        List<BridgeDeviceStatus> deviceStatus = client.getDeviceStatus("94103EA2B278CAD5");
        assertThat(deviceStatus.size(), is(1));
        assertThat(deviceStatus.get(0).getDeviceID(), is("94103EA2B278CAD5"));
        assertThat(deviceStatus.get(0).getCapabilityIDs(), is("10006,10008,30008,30009,3000A"));
        assertThat(deviceStatus.get(0).getOnState(), is(1));
        assertThat(deviceStatus.get(0).getBrightness(), is(68));
    }

    private static final String DEVICE_STATUS_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<DeviceStatusList>\n" +
            "<DeviceStatus>\n" +
            "    <IsGroupAction>NO</IsGroupAction>\n" +
            "    <DeviceID available=\"YES\">94103EA2B278CAD5</DeviceID>\n" +
            "    <CapabilityID>10006,10008,30008,30009,3000A</CapabilityID>\n" +
            "    <CapabilityValue>1,68:0,0:0,,</CapabilityValue>\n" +
            "</DeviceStatus>\n" +
            "</DeviceStatusList>";
}
