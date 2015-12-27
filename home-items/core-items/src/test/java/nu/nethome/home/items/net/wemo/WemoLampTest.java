package nu.nethome.home.items.net.wemo;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static nu.nethome.home.items.net.wemo.WemoBridge.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class WemoLampTest {

    private WemoLamp wemoLamp;
    private HomeItemProxy proxy;
    private HomeService server;
    private InternalEvent event;

    @Before
    public void setUp() throws Exception {
        wemoLamp = new WemoLamp();
        proxy = new LocalHomeItemProxy(wemoLamp);
        server = mock(HomeService.class);
        event = new InternalEvent("Type");
        doReturn(event).when(server).createEvent(anyString(), anyString());
        wemoLamp.activate(server);
    }

    @Test
    public void idAttribute() throws Exception {
        assertThat(proxy.getAttributeValue("DeviceId"), is(""));
        proxy.setAttributeValue("DeviceId", "ABC");
        assertThat(proxy.getAttributeValue("DeviceId"), is("ABC"));
    }

    @Test
    public void canTurnOn() throws Exception {
        proxy.setAttributeValue("OnDimLevel", "50");
        proxy.setAttributeValue("DeviceId", "DI");
        assertThat(proxy.getAttributeValue("State"), is(""));
        proxy.callAction("on");

        assertThat(proxy.getAttributeValue("Level"), is("50"));
        verify(server).createEvent(WEMO_LIGHT_MESSAGE, "");
        assertThat(event.getAttribute(DEVICE_ID), is("DI"));
        assertThat(event.getAttribute(ON_STATE), is("1"));
        assertThat(event.getAttribute(BRIGHTNESS), is("127"));
        assertThat(event.getAttribute("Direction"), is("Out"));
        verify(server).send(event);
        assertThat(proxy.getAttributeValue("State"), is("On"));
    }

    @Test
    public void canTurnOff() throws Exception {
        proxy.setAttributeValue("DeviceId", "DI");
        assertThat(proxy.getAttributeValue("State"), is(""));
        proxy.callAction("off");

        verify(server).createEvent(WEMO_LIGHT_MESSAGE, "");
        assertThat(event.getAttribute(DEVICE_ID), is("DI"));
        assertThat(event.getAttribute(ON_STATE), is("0"));
        assertThat(event.getAttribute("Direction"), is("Out"));
        verify(server).send(event);
        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }

    @Test
    public void canToggle() throws Exception {
        proxy.setAttributeValue("DeviceId", "DI");
        assertThat(proxy.getAttributeValue("State"), is(""));

        proxy.callAction("toggle");

        verify(server).send(event);
        assertThat(event.getAttribute(ON_STATE), is("1"));

        proxy.callAction("toggle");

        verify(server, times(2)).send(event);
        assertThat(event.getAttribute(ON_STATE), is("0"));
    }

}
