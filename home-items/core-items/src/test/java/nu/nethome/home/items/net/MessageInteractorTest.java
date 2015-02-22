package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *
 */
public class MessageInteractorTest {

    private HomeService server;
    private InternalEvent sentEvent;
    private Event receivedEvent;
    private MessageInteractor interactor;
    private LocalHomeItemProxy itemProxy;

    @Before
    public void setUp() throws Exception {
        interactor = new MessageInteractor();
        itemProxy = new LocalHomeItemProxy(interactor);
        itemProxy.setAttributeValue("Reply", "Reply message");
        sentEvent = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(sentEvent).when(server).createEvent(anyString(), anyString());
        receivedEvent = mock(Event.class);
        doReturn(Message.MESSAGE_TYPE).when(receivedEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        doReturn("xmpp:a@b").when(receivedEvent).getAttribute(Message.TO);
        doReturn("subject").when(receivedEvent).getAttribute(Message.SUBJECT);
        doReturn("text").when(receivedEvent).getAttribute(Message.BODY);
        doReturn(Message.IN_BOUND).when(receivedEvent).getAttribute(Message.DIRECTION);
        interactor.activate(server);
    }

    @Test
    public void findsTriggerTextInMessageIgnoringCase() throws Exception {
        doReturn("text with tRiGgEr message").when(receivedEvent).getAttribute(Message.BODY);
        itemProxy.setAttributeValue("TriggerText", "Trigger");

        interactor.receiveEvent(receivedEvent);

        verify(server).send(sentEvent);
    }

    @Test
    public void findsTriggerTextInMessageAmongSeveral() throws Exception {
        doReturn("text with trigger message").when(receivedEvent).getAttribute(Message.BODY);
        itemProxy.setAttributeValue("TriggerText", "foo, fie, trigger,fum");

        interactor.receiveEvent(receivedEvent);

        verify(server).send(sentEvent);
    }

    @Test
    public void doesNothingIfNoTriggerText() throws Exception {
        doReturn("text without ").when(receivedEvent).getAttribute(Message.BODY);
        itemProxy.setAttributeValue("TriggerText", "foo, fie, trigger,fum");

        interactor.receiveEvent(receivedEvent);

        verify(server, times(0)).send(sentEvent);
    }
}
