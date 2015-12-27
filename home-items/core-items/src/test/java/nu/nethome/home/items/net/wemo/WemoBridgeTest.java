package nu.nethome.home.items.net.wemo;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.items.UPnPScanner;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 */
public class WemoBridgeTest {

    public static final String CURRENT_LOCATION = "http://192.168.1.17:49153/setup.xml";
    public static final String BRIDGE_UDN = "uuid:Bridge-1_0-231447B0100DE4";
    private InternalEvent event;
    private WemoBridge wemoBridge;
    private WemoBridgeSoapClient soapClient;
    private HomeService homeService;

    @Before
    public void setUp() throws Exception {
        soapClient = mock(WemoBridgeSoapClient.class);
        event = createUPnP_Event(CURRENT_LOCATION);
        wemoBridge = spy(new WemoBridge());
        doReturn(soapClient).when(wemoBridge).getSoapClient();
        homeService = mock(HomeService.class);
        doReturn(new InternalEvent("WemoLight_Message")).when(homeService).createEvent("WemoLight_Message", "");
    }

    private InternalEvent createUPnP_Event(String currentLocation) {
        InternalEvent newEvent =  new InternalEvent(UPnPScanner.UPN_P_CREATION_MESSAGE);
        newEvent.setAttribute(UPnPScanner.DEVICE_TYPE, WemoBridge.BELKIN_WEMO_BRIDGE_DEVICE);
        newEvent.setAttribute(UPnPScanner.MODEL_NAME, "Bridge");
        newEvent.setAttribute(UPnPScanner.LOCATION, currentLocation);
        newEvent.setAttribute(UPnPScanner.SERIAL_NUMBER, "231447B0100DE4");
        newEvent.setAttribute(UPnPScanner.FRIENDLY_NAME, "WeMo Bridge");
        newEvent.setAttribute(UPnPScanner.UDN, BRIDGE_UDN);
        newEvent.setAttribute("Direction", "In");
        return newEvent;
    }

    @Test
    public void modelIsValidXML() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(new WemoBridge().getModel().getBytes());
        InputSource source = new InputSource(byteStream);

        parser.parse(source);
    }

    @Test
    public void recognizesCreationEvent() throws Exception {
        assertThat(new WemoBridge.WemoCreationInfo().canBeCreatedBy(event), is(true));
    }

    @Test
    public void creationEventToThisDeviceUpdatesLocation() throws Exception {
        wemoBridge.setDeviceURL("Foo");
        wemoBridge.setUDN(BRIDGE_UDN);

        assertThat(wemoBridge.receiveEvent(event), is(true));

        assertThat(wemoBridge.getDeviceURL(), is(CURRENT_LOCATION));
    }

    @Test
    public void settingURLConfiguresClient() throws Exception {
        wemoBridge.setDeviceURL("Foo");
        verify(soapClient, times(1)).setWemoURL("Foo");
    }

    @Test
    public void canListDevices() throws Exception, WemoException {
        doReturn(new InternalEvent("WemoLight_Message")).when(homeService).createEvent(anyString(), anyString());
        wemoBridge.activate(homeService);
        BridgeDevice device = new BridgeDevice(1, "ID", "Name", "", "FW", "xx", "1,128:0,0:0,,");
        doReturn(Arrays.asList(device)).when(soapClient).getEndDevices(anyString());

        wemoBridge.reportAllDevices();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(homeService, times(1)).send(captor.capture());

        assertThat(captor.getValue().getAttribute(Event.EVENT_TYPE_ATTRIBUTE), is("WemoLight_Message"));
        assertThat(captor.getValue().getAttribute("DeviceID"), is("ID"));
        assertThat(captor.getValue().getAttribute("FriendlyName"), is("Name"));
        assertThat(captor.getValue().getAttribute("FirmwareVersion"), is("FW"));
        assertThat(captor.getValue().getAttribute("CapabilityIDs"), is("xx"));
        assertThat(captor.getValue().getAttribute("OnState"), is("1"));
        assertThat(captor.getValue().getAttribute("Brightness"), is("128"));
    }
}
