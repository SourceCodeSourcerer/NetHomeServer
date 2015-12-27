package nu.nethome.home.items.oregon;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class OregonWindTest {

    private OregonWind oregonWind;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        oregonWind = new OregonWind();
        proxy = new LocalHomeItemProxy(oregonWind);
    }

    @Test
    public void canInterpreteEvent() throws Exception {

        proxy.setAttributeValue("Channel", "1");
        proxy.setAttributeValue("DeviceId", "107");

        InternalEvent event = new InternalEvent(OregonWind.OREGON_MESSAGE);
        event.setAttribute("Oregon.SensorId", 6548);
        event.setAttribute("Oregon.Channel", 1);
        event.setAttribute("Oregon.Id", 107);
        event.setAttribute("Oregon.LowBattery", 0);
        event.setAttribute("Oregon.Direction", 9);
        event.setAttribute("Oregon.Wind", 360);
        event.setAttribute("Oregon.AverageWind", 298);

        oregonWind.receiveEvent(event);

        assertThat(proxy.getAttributeValue("SensorModel"), is("1994"));
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("100"));
        assertThat(proxy.getAttributeValue("Direction"), is("S"));
        assertThat(proxy.getAttributeValue("Wind"), is("36,0"));
        assertThat(proxy.getAttributeValue("AverageWind"), is("29,8"));
    }

    @Test
    public void canInitAttributes() throws Exception {
        InternalEvent event = new InternalEvent(OregonWind.OREGON_MESSAGE);
        event.setAttribute("Oregon.SensorId", 6548);
        event.setAttribute("Oregon.Channel", 1);
        event.setAttribute("Oregon.Id", 107);
        event.setAttribute("Oregon.LowBattery", 0);
        event.setAttribute("Oregon.Direction", 9);
        event.setAttribute("Oregon.Wind", 360);
        event.setAttribute("Oregon.AverageWind", 298);

        oregonWind.initAttributes(event);

        assertThat(proxy.getAttributeValue("Channel"), is("1"));
        assertThat(proxy.getAttributeValue("DeviceId"), is("107"));
    }
}
