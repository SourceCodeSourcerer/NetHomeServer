package nu.nethome.home.items.oregon;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.util.TstEvent;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class OregonRainGaugeTest {

    OregonRainGauge rainGauge;
    HomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        rainGauge = new OregonRainGauge();
        proxy = new LocalHomeItemProxy(rainGauge);
    }

    @Test
    public void totalRainEmptyWithoutUpdates() throws Exception {
        assertThat(rainGauge.getTotalRain(), is(""));
        assertThat(proxy.getAttributeValue("TotalRain"), is(""));
    }

    @Test
    public void canGetTotalRain() throws Exception {
        pushTotalRainValue(894);
        assertThat(proxy.getAttributeValue("TotalRain"), is("227,1"));
    }

    @Test
    public void canGetTotalRainPerHour() throws Exception {
        pushRainRateValue(100);
        assertThat(proxy.getAttributeValue("RainPerHour"), is("25,4"));
    }

    @Test
    public void valuesEmptyWithoutUpdates() throws Exception {
        assertThat(rainGauge.getTotalRain(), is(""));
        assertThat(proxy.getAttributeValue("Rain1h"), is(""));
        assertThat(proxy.getAttributeValue("Rain24h"), is(""));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is(""));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Test
    public void canGetValueWith1update() throws Exception {
        pushRainRateValue(10);
        assertThat(proxy.getAttributeValue("Rain1h"), is("0,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("0,0"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("0,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is("0,0"));
    }

    @Ignore
    @Test
    public void canGetValueWith2updates() throws Exception {
        pushTotalRainValue(100);
        passTime(1);
        pushTotalRainValue(312);
        passTime(1);
        assertThat(proxy.getAttributeValue("Rain1h"), is("79,2"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("0,1"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("0,1"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Test
    public void canGetValueWith2updatesWithinHourSameRainRate() throws Exception {
        pushRainRateValue(100);
        passTime(1);
        pushRainRateValue(312);
        passTime(1);
        assertThat(proxy.getAttributeValue("RainPerHour"), is("79,2"));
    }

    @Test
    public void canGetValueWith2updatesWithinHourSameUpdatedTotalRainValue() throws Exception {
        pushTotalRainValue(100);
        passTime(1);
        pushTotalRainValue(312);
        passTime(1);
        assertThat(proxy.getAttributeValue("TotalRain"), is("79,2"));
    }

    @Ignore
    @Test
    public void canGetValueAfterManyUpdatesLessThan24h() throws Exception {
        pushRainRateValue(10);
        passTime(358);
        pushRainRateValue(20);
        passTime(1);
        pushRainRateValue(30);
        passTime(59);
        assertThat(proxy.getAttributeValue("Rain1h"), is("1,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("2,0"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("2,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Ignore
    @Test
    public void canGetValuesAfter1MonthOfRain() throws Exception {
        int value = 0;
        for (long i = 0; i < 60 * 24 * 31; i++) {
            passTime(1);
            pushRainRateValue(value++);
        }
        assertThat(proxy.getAttributeValue("Rain1h"), is("6,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("143,9"));
//        assertThat(proxy.getAttributeValue("RainThisWeek"), is("2,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Ignore
    @Test
    public void getLoggerValueWithoutUpdates() throws Exception {
        assertThat(rainGauge.getValue(), is(""));
    }

    @Test
    public void getFirstLoggerValue() throws Exception {
        pushTotalRainValue(100);
        assertThat(rainGauge.getValue(), is("25,4"));
    }

    @Ignore
    @Test
    public void LoggerValueIsDiffBetweenMeasurementPoints() throws Exception {
        pushRainRateValue(10);
        assertThat(rainGauge.getValue(), is("0,0"));
        pushRainRateValue(20);
        assertThat(rainGauge.getValue(), is("1,0"));
        pushRainRateValue(40);
        assertThat(rainGauge.getValue(), is("2,0"));
    }

    @Ignore
    @Test
    public void KeepsTotalRainIfSensorResets() throws Exception {
        pushRainRateValue(10);
        assertThat(rainGauge.getValue(), is("0,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("1,0"));
        pushRainRateValue(20);
        assertThat(rainGauge.getValue(), is("1,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("2,0"));
        pushRainRateValue(10);
        assertThat(rainGauge.getValue(), is("1,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("3,0"));
    }

    private void pushTotalRainValue(int totalRainValue) {
        Event event = new TstEvent("Oregon_Message");
        event.setAttribute("Oregon.TotalRain", totalRainValue);
        rainGauge.receiveEvent(event);
    }

    private void pushRainRateValue(int rainRateValue) {
        Event event = new TstEvent("Oregon_Message");
        event.setAttribute("Oregon.RainRate", rainRateValue);
        rainGauge.receiveEvent(event);
    }

    private void passTime(int minutes) {
        Event event = new TstEvent(HomeService.MINUTE_EVENT_TYPE);
        for (int i = 0; i < minutes; i++) {
            rainGauge.receiveEvent(event);
        }
    }
}
