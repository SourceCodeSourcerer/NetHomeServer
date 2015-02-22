package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class LampMessageInteractorTest {

    private HomeService server;
    private InternalEvent sentEvent;
    private Event receivedEvent;
    private LampMessageInteractor interactor;
    private LocalHomeItemProxy itemProxy;
    private LocalHomeItemProxy lamp1;
    private LocalHomeItemProxy lamp2;

    @Before
    public void setUp() throws Exception {
        interactor = new LampMessageInteractor();
        itemProxy = new LocalHomeItemProxy(interactor);
        itemProxy.setAttributeValue("Reply", "Reply message");
        itemProxy.setAttributeValue("OnString", "turn on");
        itemProxy.setAttributeValue("OffString", "turn off");
        lamp1 = mock(LocalHomeItemProxy.class);
        doReturn("Window Lamp 1").when(lamp1).getAttributeValue(LocalHomeItemProxy.NAME_ATTRIBUTE);
        lamp2 = mock(LocalHomeItemProxy.class);
        doReturn("Bedroom Lamp").when(lamp2).getAttributeValue(LocalHomeItemProxy.NAME_ATTRIBUTE);
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
        receivedEvent = mock(Event.class);
        doReturn(Message.MESSAGE_TYPE).when(receivedEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        doReturn("xmpp:a@b").when(receivedEvent).getAttribute(Message.TO);
        doReturn("subject").when(receivedEvent).getAttribute(Message.SUBJECT);
        doReturn(Message.IN_BOUND).when(receivedEvent).getAttribute(Message.DIRECTION);
        doReturn(lamp1).when(server).openInstance("1");
        doReturn(lamp2).when(server).openInstance("2");
        interactor.activate(server);
    }

    @Test
    public void turnsOnLamp1WhenToldSo() throws Exception {
        itemProxy.setAttributeValue("Lamps", "1,2");
        doReturn("turn on Window Lamp 1").when(receivedEvent).getAttribute(Message.BODY);

        interactor.receiveEvent(receivedEvent);

        verify(lamp1).callAction("on");
    }

    @Test
    public void turnsOffLamp1WhenToldSo() throws Exception {
        itemProxy.setAttributeValue("Lamps", "1,2");
        doReturn("turn off bedroom Lamp").when(receivedEvent).getAttribute(Message.BODY);

        interactor.receiveEvent(receivedEvent);

        verify(lamp2).callAction("off");
    }

    @Test
    public void noActionWhenCommandDoesNotMatch() throws Exception {
        itemProxy.setAttributeValue("Lamps", "1,2");
        doReturn("turn o Window Lamp 1").when(receivedEvent).getAttribute(Message.BODY);

        interactor.receiveEvent(receivedEvent);

        verifyNoMoreInteractions(lamp1);
        verifyNoMoreInteractions(lamp2);
    }

    @Test
    public void noActionWhenItemNotSpecified() throws Exception {
        itemProxy.setAttributeValue("Lamps", "");
        doReturn("turn on Window Lamp 1").when(receivedEvent).getAttribute(Message.BODY);

        interactor.receiveEvent(receivedEvent);

        verifyNoMoreInteractions(lamp1);
        verifyNoMoreInteractions(lamp2);
    }

    @Test
    public void ReplacesItemNameVariable() throws Exception {
        itemProxy.setAttributeValue("Reply", "test #LAMP message");
        itemProxy.setAttributeValue("Lamps", "1,2");
        doReturn("turn off bedroom Lamp").when(receivedEvent).getAttribute(Message.BODY);

        interactor.receiveEvent(receivedEvent);

        assertThat(sentEvent.getAttribute(Message.BODY), is("test " + "Bedroom Lamp" + " message"));
    }
}
