package nu.nethome.home.items.net.wemo;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.LocalHomeItemProxy;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 */
public class WemoInsightSwitchTest {

    private WemoInsightSwitch wemoSwitch;
    private WemoInsightSwitchClient swithClient;
    private LocalHomeItemProxy itemProxy;

    @Before
    public void setUp() throws Exception {
        swithClient = mock(WemoInsightSwitchClient.class);
        wemoSwitch = spy(new WemoInsightSwitch());
        itemProxy = new LocalHomeItemProxy(wemoSwitch);
        doReturn(swithClient).when(wemoSwitch).getInsightSwitch();
    }

    @Test
    public void testGetModel() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(wemoSwitch.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }

    @Test
    public void canTurnSwitchOn() throws Exception, WemoException {
        itemProxy.callAction("on");
        verify(swithClient, times(1)).setOnState(true);
    }

    @Test
    public void canTurnSwitchOff() throws Exception, WemoException {
        itemProxy.callAction("off");
        verify(swithClient, times(1)).setOnState(false);
    }

    @Test
    public void canGetOffState() throws Exception, WemoException {
        InsightState state = new InsightState("0|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        doReturn(state).when(swithClient).getInsightParameters();
        assertThat(itemProxy.getAttributeValue("State"), is("Off"));
    }

    @Test
    public void canGetIdleState() throws Exception, WemoException {
        InsightState state = new InsightState("8|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        doReturn(state).when(swithClient).getInsightParameters();
        assertThat(itemProxy.getAttributeValue("State"), is("Idle"));
    }

    @Test
    public void canGetOnState() throws Exception, WemoException {
        InsightState state = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        doReturn(state).when(swithClient).getInsightParameters();
        char decimalSeparator = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
        assertThat(itemProxy.getAttributeValue("State"), is("On 105" + decimalSeparator + "7"));
    }

    @Test
    public void cachesState() throws Exception, WemoException {
        InsightState state = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        doReturn(state).when(swithClient).getInsightParameters();
        itemProxy.getAttributeValue("State");
        itemProxy.getAttributeValue("State");
        verify(swithClient, times(1)).getInsightParameters();
    }

    @Test
    public void cachesStateLessThan500Ms() throws Exception, WemoException {
        InsightState state = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        doReturn(state).when(swithClient).getInsightParameters();
        itemProxy.getAttributeValue("State");
        Thread.sleep(500);
        itemProxy.getAttributeValue("State");
        verify(swithClient, times(2)).getInsightParameters();
    }
}
