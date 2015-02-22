package nu.nethome.home.items.fineoffset;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.items.util.TstEvent;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 */
public class FooGadgetLegacyTest {

    private FooGadgetLegacy energyMeter;
    private Date now;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        energyMeter = new FooGadgetLegacy(){
            @Override
            Date getCurrentTime() {
                return now;
            }
        };
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.MAY, 25, 23, 30);
        now = calendar.getTime();
        energyMeter.setEnergyK("1");
        proxy = new LocalHomeItemProxy(energyMeter);
    }

    @Test
    public void canGetCurrentPower() throws Exception {
        pushValue(1, 1, 1);
        assertThat(proxy.getAttributeValue("Power"), is("60,00"));
    }

    @Test
    public void canGetCurrentValue() throws Exception {
        for (int i = 0; i < 15; i++) {
            pushValue(1, i + 1, 1);
            passTime(1);
        }
        assertThat(energyMeter.getValue(), is("60,00"));
    }

    @Test
    public void canCountLostValues() throws Exception {
        assertThat(proxy.getAttributeValue("LostSamples"), is("0"));
        pushValue(1, 1, 1);
        assertThat(proxy.getAttributeValue("LostSamples"), is("0"));
        pushValue(1, 2, 1);
        assertThat(proxy.getAttributeValue("LostSamples"), is("0"));
        pushValue(1, 4, 1);
        assertThat(proxy.getAttributeValue("LostSamples"), is("0"));
        pushValue(1, 8, 1);
        assertThat(proxy.getAttributeValue("LostSamples"), is("2"));
        pushValue(1, 11, 1);
        assertThat(proxy.getAttributeValue("LostSamples"), is("3"));
    }

    @Test
    public void canGetTotalEnergy() throws Exception {
        assertThat(proxy.getAttributeValue("TotalEnergy"), is("0,00"));
        proxy.setAttributeValue("TotalSavedPulses", "10");
        assertThat(proxy.getAttributeValue("TotalEnergy"), is("10,00"));
    }

    @Test
    public void canGetEnergyToday() throws Exception {
        assertThat(proxy.getAttributeValue("EnergyToday"), is(""));
        pushValue(0, 1, 1);
        assertThat(proxy.getAttributeValue("EnergyToday"), is("1,00"));
        pushValue(1, 2, 1);
        assertThat(proxy.getAttributeValue("EnergyToday"), is("2,00"));
    }

    @Test
    public void canGetEnergyTomorrow() throws Exception {
        assertThat(proxy.getAttributeValue("EnergyToday"), is(""));
        pushValue(0, 1, 1);
        assertThat(proxy.getAttributeValue("EnergyToday"), is("1,00"));
        passTime(60); // Passes 00.00
        pushValue(1, 2, 1);
        assertThat(proxy.getAttributeValue("EnergyToday"), is("1,00"));
    }

    private void passTime(int minutes) {
        Event event = new TstEvent(HomeService.MINUTE_EVENT_TYPE);
        for (int i = 0; i < minutes; i++) {
            now = new Date(now.getTime() + 1000 * 60);
            energyMeter.receiveEvent(event);
        }
    }

    private void pushValue(int previous, int prevCounter, int current) {
        Event prevEvent = new TstEvent("FooGadgetLegacy_Message");
        prevEvent.setAttribute("FooGadgetLegacy.Energy", previous);
        prevEvent.setAttribute("FooGadgetLegacy.Counter", prevCounter);
        energyMeter.receiveEvent(prevEvent);
        Event currEvent = new TstEvent("FooGadgetLegacy_Message");
        currEvent .setAttribute("FooGadgetLegacy.Energy", current);
        currEvent .setAttribute("FooGadgetLegacy.Counter", (prevCounter + 1) % 100);
        energyMeter.receiveEvent(currEvent );
    }
}
