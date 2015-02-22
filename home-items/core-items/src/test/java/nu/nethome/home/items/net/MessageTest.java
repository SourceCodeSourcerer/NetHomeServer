package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class MessageTest {

    public static final String TO = "to";
    public static final String SUBJECT = "subject";
    public static final String MESSAGE = "message";
    private HomeService server;
    private Event event;
    private Message message;
    private HomeItemProxy itemProxy;

    @Before
    public void setUp() throws Exception {
        event = new InternalEvent("Foo");
        server = mock(HomeService.class);
        doReturn(event).when(server).createEvent(anyString(), anyString());
        message = new Message();
        itemProxy = new LocalHomeItemProxy(message);
        itemProxy.setAttributeValue("To", TO);
        itemProxy.setAttributeValue("Subject", SUBJECT);
        itemProxy.setAttributeValue("Message", MESSAGE);
        doReturn(itemProxy).when(server).openInstance("Test Message");
        message.activate(server);
    }

    @Test
    public void SendsMessageEvent() throws Exception {

        itemProxy.callAction("Send");

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(server).send(captor.capture());
        Event event1 = captor.getValue();
        assertThat(event1.getAttribute(Message.TO), is(TO));
        assertThat(event1.getAttribute(Message.SUBJECT), is(SUBJECT));
        assertThat(event1.getAttribute(Message.BODY), is(MESSAGE));
        assertThat(event1.getAttribute(Message.DIRECTION), is(Message.OUT_BOUND));
    }

    @Test
    public void replacesItemReference() throws Exception {
        itemProxy.setAttributeValue("Message", "Test ${Test Message.Subject} End");

        sendMessageAndVerifyResult("Test subject End");
    }

    @Test
    public void replacesItemReferenceAtEnd() throws Exception {
        itemProxy.setAttributeValue("Message", "Test ${Test Message.Subject}");

        sendMessageAndVerifyResult("Test subject");
    }

    @Test
    public void replacesItemReferenceAtStart() throws Exception {
        itemProxy.setAttributeValue("Message", "${Test Message.Subject} End");

        sendMessageAndVerifyResult("subject End");
    }

    @Test
    public void replacesMultipleItemReferences() throws Exception {
        itemProxy.setAttributeValue("Message", "Test ${Test Message.Subject} End ${Test Message.To} ${Test Message.Subject} ${Test Message.To}");

        sendMessageAndVerifyResult("Test subject End to subject to");
    }

    @Test
    public void replacesMultipleStackedItemReferences() throws Exception {
        itemProxy.setAttributeValue("Message", "${Test Message.Subject}${Test Message.To}${Test Message.Subject}${Test Message.To}");

        sendMessageAndVerifyResult("subjecttosubjectto");
    }

    @Test
    public void handlesBadItemReference() throws Exception {
        itemProxy.setAttributeValue("Message", "Test ${Foo.Subject} End");

        sendMessageAndVerifyResult("Test  End");
    }

    @Test
    public void handlesInclompleteItemReferences() throws Exception {
        itemProxy.setAttributeValue("Message", "Test ${Test Message.Subject End");

        sendMessageAndVerifyResult("Test ${Test Message.Subject End");
    }

    private void sendMessageAndVerifyResult(String result) throws ExecutionFailure {
        itemProxy.callAction("Send");
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(server).send(captor.capture());
        Event event1 = captor.getValue();
        assertThat(event1.getAttribute(Message.BODY), is(result));
    }
}
