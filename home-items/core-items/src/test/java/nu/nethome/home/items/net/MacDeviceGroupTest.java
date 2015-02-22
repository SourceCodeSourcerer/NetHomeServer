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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MacDeviceGroupTest {
    public static final String MAC1 = "e1:f4:c6:09:2c:26";
    public static final String MAC2 = "d2:f4:c6:09:2c:26";
    public static final String MAC3 = "e3:f4:c6:09:2c:26";
    public static final String OTHER_MAC = "00:f4:c6:09:2c:26";
    public static final String ALL_MACS = MAC1 + "," + MAC2 + "," + MAC3;
    private HomeService server;
    private InternalEvent sentEvent;
    private Event receivedEvent;
    private MacDeviceGroup macDeviceGroup;
    private LocalHomeItemProxy itemProxy;
    private HomeItemProxy fooItem;

    @Before
    public void setUp() throws Exception {
        macDeviceGroup = new MacDeviceGroup();
        itemProxy = new LocalHomeItemProxy(macDeviceGroup);
        itemProxy.setAttributeValue("MacAddresses", ALL_MACS);
        itemProxy.setAttributeValue("ActionWhilePresent", "call,foo,present");
        itemProxy.setAttributeValue("ActionWhileAllAbsent", "call,foo,absent");
        itemProxy.setAttributeValue("ActionOnPresent", "call,foo,onPresent");
        itemProxy.setAttributeValue("ActionOnAllAbsent", "call,foo,onAbsent");
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
        receivedEvent = mock(Event.class);
        doReturn(ArpScanner.ARP_SCAN_MESSAGE).when(receivedEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        macDeviceGroup.activate(server);

        fooItem = mock(HomeItemProxy.class);
        doReturn(fooItem).when(server).openInstance("foo");
    }

    @Test
    public void canSetMacAddresses() throws Exception {
        itemProxy.setAttributeValue("MacAddresses", ALL_MACS);
        assertThat(itemProxy.getAttributeValue("MacAddresses"), is(ALL_MACS));
    }


    @Test
    public void performsActionWhileAllAbsentWhenEmpty() throws Exception {
        doReturn("").when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDeviceGroup.receiveEvent(receivedEvent);

        verify(fooItem).callAction("absent");
    }

    @Test
    public void performsActionWhileAllAbsentWhenOtherMac() throws Exception {
        doReturn(OTHER_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDeviceGroup.receiveEvent(receivedEvent);

        verify(fooItem).callAction("absent");
    }

    @Test
    public void performsActionWhileSomePresentWhenSomeMacs() throws Exception {
        doReturn(MAC1 + "," + MAC2).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDeviceGroup.receiveEvent(receivedEvent);

        verify(fooItem, times(1)).callAction("present");
        verify(fooItem, times(0)).callAction("absent");
    }

    @Test
    public void performsActionWhileSomePresentWhenAllMacs() throws Exception {
        doReturn(ALL_MACS).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);

        macDeviceGroup.receiveEvent(receivedEvent);

        verify(fooItem, times(1)).callAction("present");
        verify(fooItem, times(0)).callAction("absent");
    }

    @Test
    public void showsPresentState() throws Exception {
        assertThat(itemProxy.getAttributeValue("State"), is(""));

        doReturn(ALL_MACS).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        assertThat(itemProxy.getAttributeValue("State"), is("Present"));

        doReturn(OTHER_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        assertThat(itemProxy.getAttributeValue("State"), is("Absent"));
    }

    @Test
    public void callsOnAbsent() throws Exception {
        doReturn(OTHER_MAC + "," + MAC1).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        verify(fooItem, times(0)).callAction("onPresent");
        verify(fooItem, times(0)).callAction("onAbsent");

        doReturn(OTHER_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        verify(fooItem, times(1)).callAction("onAbsent");
        verify(fooItem, times(0)).callAction("onPresent");
    }

    @Test
    public void callsOnPresent() throws Exception {
        doReturn(OTHER_MAC).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        verify(fooItem, times(0)).callAction("onPresent");
        verify(fooItem, times(0)).callAction("onAbsent");

        doReturn(OTHER_MAC + "," + MAC2).when(receivedEvent).getAttribute(Event.EVENT_VALUE_ATTRIBUTE);
        macDeviceGroup.receiveEvent(receivedEvent);
        verify(fooItem, times(1)).callAction("onPresent");
        verify(fooItem, times(0)).callAction("onAbsent");
    }
}
