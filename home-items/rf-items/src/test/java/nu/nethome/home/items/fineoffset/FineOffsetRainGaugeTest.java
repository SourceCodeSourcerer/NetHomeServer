package nu.nethome.home.items.fineoffset;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.util.TstEvent;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class FineOffsetRainGaugeTest {

    FineOffsetRainGauge rainGauge;
    HomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        rainGauge = new FineOffsetRainGauge();
        proxy = new LocalHomeItemProxy(rainGauge);
    }

    @Test
    public void totalRainEmptyWithoutUpdates() throws Exception {
        assertThat(rainGauge.getTotalRain(), is(""));
        assertThat(proxy.getAttributeValue("TotalRain"), is(""));
    }

    @Test
    public void canGetTotalRain() throws Exception {
        pushValue(10);
        assertThat(proxy.getAttributeValue("TotalRain"), is("1,0"));
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
        pushValue(10);
        assertThat(proxy.getAttributeValue("Rain1h"), is("0,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("0,0"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("0,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is("0,0"));
    }

    @Test
    public void canGetValueWith2updates() throws Exception {
        pushValue(10);
        passTime(1);
        pushValue(11);
        passTime(1);
        assertThat(proxy.getAttributeValue("Rain1h"), is("0,1"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("0,1"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("0,1"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Test
    public void canGetValueAfterManyUpdatesLessThan24h() throws Exception {
        pushValue(10);
        passTime(358);
        pushValue(20);
        passTime(1);
        pushValue(30);
        passTime(59);
        assertThat(proxy.getAttributeValue("Rain1h"), is("1,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("2,0"));
        assertThat(proxy.getAttributeValue("RainThisWeek"), is("2,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Test
    public void canGetValuesAfter1MonthOfRain() throws Exception {
        int value = 0;
        for (long i = 0; i < 60 * 24 * 31; i++) {
            passTime(1);
            pushValue(value++);
        }
        assertThat(proxy.getAttributeValue("Rain1h"), is("6,0"));
        assertThat(proxy.getAttributeValue("Rain24h"), is("143,9"));
//        assertThat(proxy.getAttributeValue("RainThisWeek"), is("2,0"));
//        assertThat(proxy.getAttributeValue("RainLastWeeks"), is(""));
    }

    @Test
    public void getLoggerValueWithoutUpdates() throws Exception {
        assertThat(rainGauge.getValue(), is(""));
    }

    @Test
    public void getFirstLoggerValue() throws Exception {
        pushValue(10);
        assertThat(rainGauge.getValue(), is("0,0"));
    }

    @Test
    public void LoggerValueIsDiffBetweenMeasurementPoints() throws Exception {
        pushValue(10);
        assertThat(rainGauge.getValue(), is("0,0"));
        pushValue(20);
        assertThat(rainGauge.getValue(), is("1,0"));
        pushValue(40);
        assertThat(rainGauge.getValue(), is("2,0"));
    }

    @Test
    public void KeepsTotalRainIfSensorResets() throws Exception {
        pushValue(10);
        assertThat(rainGauge.getValue(), is("0,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("1,0"));
        pushValue(20);
        assertThat(rainGauge.getValue(), is("1,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("2,0"));
        pushValue(10);
        assertThat(rainGauge.getValue(), is("1,0"));
        assertThat(proxy.getAttributeValue("TotalRain"), is("3,0"));
    }

    private void pushValue(int rainValue) {
        Event event = new TstEvent("FineOffset_Message");
        event.setAttribute("FineOffset.Rain", rainValue);
        rainGauge.receiveEvent(event);
    }

    private void passTime(int minutes) {
        Event event = new TstEvent(HomeService.MINUTE_EVENT_TYPE);
        for (int i = 0; i < minutes; i++) {
            rainGauge.receiveEvent(event);
        }
    }
}
