package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class MacDeviceTest {
    public static final String CORRECT_MAC = "e4:f4:c6:09:2c:26";
    public static final String WRONG_MAC = "dd:f4:c6:09:2c:26";
    private HomeService server;
    private InternalEvent sentEvent;
    private Event receivedEvent;
    private MacDevice macDevice;
    private LocalHomeItemProxy itemProxy;
    private HomeItemProxy fooItem;

    @Before
    public void setUp() throws Exception {
        macDevice = new MacDevice();
        itemProxy = new LocalHomeItemProxy(macDevice);
        itemProxy.setAttributeValue("MacAddress", CORRECT_MAC);
        itemProxy.setAttributeValue("ActionWhilePresent", "call,foo,present");
        itemProxy.setAttributeValue("ActionWhileAbsent", "call,foo,absent");
        itemProxy.setAttributeValue("ActionOnPresent", "call,foo,onPresent");
        itemProxy.setAttributeValue("ActionOnAbsent", "call,foo,onAbsent");
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
        receivedEvent = mock(Event.class);
        doReturn(ArpScanner.ARP_SCAN_MESSAGE).when(receivedEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        macDevice.activate(server);

        fooItem = mock(HomeItemProxy.class);
        doReturn(fooItem).when(server).openInstance("foo");
    }

    @Test
    public void canSetMacAddress() throws Exception {
        itemProxy.setAttributeValue("MacAddress", CORRECT_MAC);
        assertThat(itemProxy.getAttributeValue("MacAddress"), is(CORRECT_MAC));
    }

    @Test
    public void performsActionWhileNotPresentWhenEmpty() throws Exception {
        doReturn("").when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDevice.receiveEvent(receivedEvent);

        verify(fooItem).callAction("absent");
    }

    @Test
    public void performsActionWhileNotPresentWhenWrongMacs() throws Exception {
        doReturn(WRONG_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDevice.receiveEvent(receivedEvent);

        verify(fooItem, times(1)).callAction("absent");
        verify(fooItem, times(0)).callAction("present");
    }

    @Test
    public void performsActionWhilePresentWhenRightMac() throws Exception {
        doReturn(CORRECT_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDevice.receiveEvent(receivedEvent);

        verify(fooItem, times(1)).callAction("present");
        verify(fooItem, times(0)).callAction("absent");
    }

    @Test
    public void showsPresentState() throws Exception {
        assertThat(itemProxy.getAttributeValue("State"), is(""));

        doReturn(CORRECT_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDevice.receiveEvent(receivedEvent);
        assertThat(itemProxy.getAttributeValue("State"), is("Present"));

        doReturn(WRONG_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDevice.receiveEvent(receivedEvent);
        assertThat(itemProxy.getAttributeValue("State"), is("Absent"));
    }

    @Test
    public void callsOnAbsent() throws Exception {
        doReturn(CORRECT_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDevice.receiveEvent(receivedEvent);
        verify(fooItem, times(0)).callAction("onPresent");
        verify(fooItem, times(0)).callAction("onAbsent");

        doReturn(WRONG_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDevice.receiveEvent(receivedEvent);
        verify(fooItem, times(1)).callAction("onAbsent");
        verify(fooItem, times(0)).callAction("onPresent");
    }

}
