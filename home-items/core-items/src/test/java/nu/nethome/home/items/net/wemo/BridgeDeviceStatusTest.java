package nu.nethome.home.items.net.wemo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class BridgeDeviceStatusTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void canCreateOffDimmedFull() {
        final String offState = "0";

        BridgeDeviceStatus status = new BridgeDeviceStatus("Foo", false, 255);

        assertThat(status.getOnState(), is(0));
        assertThat(status.getBrightness(), is(255));
        assertThat(status.getCurrentRawState(), is(offState));
        assertThat(status.getCapabilityIDs(), is("10006"));
    }

    @Test
    public void canCreateOnDimmedHalf() {
        final String onState = "1,128:0";

        BridgeDeviceStatus status = new BridgeDeviceStatus("Foo", true, 128);

        assertThat(status.getOnState(), is(1));
        assertThat(status.getBrightness(), is(128));
        assertThat(status.getCurrentRawState(), is(onState));
        assertThat(status.getCapabilityIDs(), is("10006,10008"));
    }
}


