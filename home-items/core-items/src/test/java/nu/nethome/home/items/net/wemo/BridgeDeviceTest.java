package nu.nethome.home.items.net.wemo;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class BridgeDeviceTest {

    static final String offState = "0,255:0,0:0,,";
    static final String onState = "1,128:0,0:0,,";
    static final String unknownState = ",,,,";

    @Test
    public void canDecodeStateOn() throws Exception {
        BridgeDevice device = new BridgeDevice(0, "", "", "", "", "", onState);
        assertThat(device.getOnState(), is(1));
    }

    @Test
    public void canDecodeStateOff() throws Exception {
        BridgeDevice device = new BridgeDevice(0, "", "", "", "", "", offState);
        assertThat(device.getOnState(), is(0));
    }

    @Test
    public void canDecodeUnknownState() throws Exception {
        BridgeDevice device = new BridgeDevice(0, "", "", "", "", "", unknownState);
        assertThat(device.getOnState(), is(-1));
    }

    @Test
    public void canDecodeBrightness() throws Exception {
        BridgeDevice device = new BridgeDevice(0, "", "", "", "", "", onState);
        assertThat(device.getBrightness(), is(128));
    }

    @Test
    public void canDecodeUnknownBrightness() throws Exception {
        BridgeDevice device = new BridgeDevice(0, "", "", "", "", "", unknownState);
        assertThat(device.getBrightness(), is(-1));
    }
}
