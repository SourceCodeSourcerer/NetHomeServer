package nu.nethome.home.items.fineoffset;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.items.util.TstEvent;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class FooGadgetPulseTest {

    public static final int COUNTER_RANGE = 413696;
    private FooGadgetPulse energyMeter;
    private Date now;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.MAY, 25, 23, 30);
        now = calendar.getTime();
        energyMeter = new FooGadgetPulse(){
            @Override
            Date getCurrentTime() {
                return now;
            }
        };
        energyMeter.setEnergyK("1");
        proxy = new LocalHomeItemProxy(energyMeter);
    }

    @Test
    public void canGetValuesAfterOneInitialUpdate() throws Exception {
        pushValue(50);
        assertThat(proxy.getAttributeValue("Power"), is(""));
        assertThat(proxy.getAttributeValue("EnergyToday"), is("0,00"));
        assertThat(proxy.getAttributeValue("EnergyThisWeek"), is("0,00"));
        assertThat(proxy.getAttributeValue("TotalEnergy"), is("50,00"));
    }

    @Test
    public void canGetValuesAfterTwoUpdates() throws Exception {
        pushValue(50);
        passTime(1);
        pushValue(100);
        assertThat(proxy.getAttributeValue("Power"), is("3000,00")); // 60 * 50
        assertThat(proxy.getAttributeValue("EnergyToday"), is("50,00"));
        assertThat(proxy.getAttributeValue("EnergyThisWeek"), is("50,00"));
        assertThat(proxy.getAttributeValue("TotalEnergy"), is("100,00"));
    }

    @Test
    public void canGetValuesAfterWrapAround() throws Exception {
        pushValue(COUNTER_RANGE - 50);
        passTime(1);
        pushValue(50);
        assertThat(proxy.getAttributeValue("Power"), is("6000,00")); // 60 * 50
        assertThat(proxy.getAttributeValue("EnergyToday"), is("100,00"));
        assertThat(proxy.getAttributeValue("EnergyThisWeek"), is("100,00"));
        assertThat(proxy.getAttributeValue("TotalEnergy"), is("413746,00")); // 413696 + 50
    }

    private void passTime(int minutes) {
        Event event = new TstEvent(HomeService.MINUTE_EVENT_TYPE);
        for (int i = 0; i < minutes; i++) {
            now = new Date(now.getTime() + 1000 * 60);
            energyMeter.receiveEvent(event);
        }
    }

    private void pushValue(int counter) {
        Event currEvent = new TstEvent("FooGadgetPulse_Message");
        currEvent.setAttribute("FooGadgetPulse.Pulses", counter);
        energyMeter.receiveEvent(currEvent );
    }
}
