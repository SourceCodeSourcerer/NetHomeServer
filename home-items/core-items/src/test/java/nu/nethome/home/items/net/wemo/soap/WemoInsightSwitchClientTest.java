package nu.nethome.home.items.net.wemo.soap;

import nu.nethome.home.items.net.wemo.InsightState;
import nu.nethome.home.items.net.wemo.WemoException;
import nu.nethome.home.items.net.wemo.WemoInsightSwitchClient;
import org.junit.Test;

/**
 *
 */
public class WemoInsightSwitchClientTest {
    @Test
    public void testSetOn() throws Exception, WemoException {
        WemoInsightSwitchClient wemoSwitch = new WemoInsightSwitchClient("http://192.168.1.16:49153");
        //WemoInsightSwitchClient wemoSwitch = new WemoInsightSwitchClient("http://127.0.0.1:49000");
//        InsightState insightParameters = wemoSwitch.getInsightParameters();
//         wemoSwitch.setOnState(true);
//        boolean onState = wemoSwitch.getOnState();
//        Thread.sleep(1000);
//        wemoSwitch.setOnState(false);
    }
}
