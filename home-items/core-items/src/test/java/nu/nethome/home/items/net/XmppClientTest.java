package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;

import javax.net.ssl.SSLContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class XmppClientTest {

    public static final String MESSAGE_SENDER = "a@b";
    XmppClient client;
    private HomeService server;
    private Event messageEvent;
    private XmppSession session;
    private LocalHomeItemProxy itemProxy;
    private Event minuteEvent;
    private rocks.xmpp.core.stanza.model.client.Message inMessage;
    private MessageEvent xmppMessageEvent;
    private InternalEvent event;
    private SSLContext sslContext;

    @Before
    public void setUp() throws Exception {
        event = new InternalEvent("Foo");
        client = spy(new XmppClient());
        itemProxy = new LocalHomeItemProxy(client);
        session = mock(XmppSession.class);
        doReturn(true).when(session).isConnected();
        doReturn(session).when(client).createBabblerXmppSession(anyString(), any(TcpConnectionConfiguration.class));
        sslContext = mock(SSLContext.class);
        doReturn(sslContext).when(client).trustAnyCertificateSslContext();
        doNothing().when(client).listenForMessages(any(XmppSession.class));
        doNothing().when(client).listenForPresenceChanges(any(XmppSession.class));
        doNothing().when(client).login(any(XmppSession.class));
        doReturn(1).when(client).getDayOfYear();
        server = mock(HomeService.class);
        doReturn(event).when(server).createEvent(anyString(), anyString());

        messageEvent = mock(Event.class);
        doReturn(Message.MESSAGE_TYPE).when(messageEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        doReturn("xmpp:a@b").when(messageEvent).getAttribute(Message.TO);
        doReturn("subject").when(messageEvent).getAttribute(Message.SUBJECT);
        doReturn("text").when(messageEvent).getAttribute(Message.BODY);
        doReturn(Message.OUT_BOUND).when(messageEvent).getAttribute(Message.DIRECTION);

        minuteEvent = mock(Event.class);
        doReturn(HomeService.MINUTE_EVENT_TYPE).when(minuteEvent).getAttribute(Event.EVENT_TYPE_ATTRIBUTE);

        inMessage = new rocks.xmpp.core.stanza.model.client.Message(
                Jid.valueOf("x@y/z"),
                rocks.xmpp.core.stanza.model.client.Message.Type.CHAT,
                "Text");
        inMessage.setSubject("subject");
        inMessage.setFrom(Jid.valueOf(MESSAGE_SENDER));
        xmppMessageEvent = new MessageEvent(new Object(), inMessage, true);
    }

    @Test
    public void sendsOutboundXmppMessage() throws Exception {
        client.activate(server);
        rocks.xmpp.core.stanza.model.client.Message value = sendMessage();

        assertThat(value.getTo().getDomain(), is("b"));
        assertThat(value.getTo().getLocal(), is("a"));
        assertThat(value.getBody(), is("text"));
        assertThat(value.getSubject(), is("subject"));
    }

    @Test
    public void sendsOutboundXmppMessageWithoutSubject() throws Exception {
        doReturn("").when(messageEvent).getAttribute(Message.SUBJECT);
        client.activate(server);
        rocks.xmpp.core.stanza.model.client.Message value = sendMessage();

        assertThat(value.getSubject(), is(nullValue()));
    }

    @Test
    public void doesNotSendOutboundXmppMessageWithoutSubjectAndBody() throws Exception {
        doReturn("").when(messageEvent).getAttribute(Message.SUBJECT);
        doReturn("").when(messageEvent).getAttribute(Message.BODY);
        client.activate(server);
        client.receiveEvent(messageEvent);

        verify(session, times(0)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }

    @Test
    public void doesNotSendInboundXmppMessage() throws Exception {
        doReturn(Message.IN_BOUND).when(messageEvent).getAttribute(Message.DIRECTION);
        client.activate(server);
        client.receiveEvent(messageEvent);

        verify(session, times(0)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }

    private rocks.xmpp.core.stanza.model.client.Message sendMessage() {
        client.receiveEvent(messageEvent);
        ArgumentCaptor<rocks.xmpp.core.stanza.model.client.Message> messageArgumentCaptor = ArgumentCaptor.forClass(rocks.xmpp.core.stanza.model.client.Message.class);
        verify(session).send(messageArgumentCaptor.capture());
        return messageArgumentCaptor.getValue();
    }

    @Test
    public void ignoresXmppMessageBeforeActivated() throws Exception {
        client.receiveEvent(messageEvent);
        verify(client, times(0)).createSession();
    }

    @Test
    public void ignoresXmppMessageWhenNotConnected() throws Exception {
        client.activate(server);
        doReturn(false).when(session).isConnected();
        client.receiveEvent(messageEvent);
        verify(session, times(0)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }

    @Test
    public void limitsNumberOfSentMessages() throws Exception {
        itemProxy.setAttributeValue("MaxMessagesPerDay", "5");
        client.activate(server);
        sendMessages(10);
        verify(session, times(5)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }

    @Test
    public void limitsNumberOfSentMessagesPerDay() throws Exception {
        itemProxy.setAttributeValue("MaxMessagesPerDay", "5");
        client.activate(server);
        sendMessages(10);
        doReturn(2).when(client).getDayOfYear();
        sendMessages(2);
        verify(session, times(5 + 2)).send(any(rocks.xmpp.core.stanza.model.client.Message.class));
    }

    private void sendMessages(int messages) {
        for (int i = 0; i < messages; i++) {
            client.receiveEvent(minuteEvent);
            client.receiveEvent(messageEvent);
        }
    }

    @Test
    public void makesMessageEventOfIncomingMessage() throws Exception {
        client.activate(server);
        client.handleMessageEvent(xmppMessageEvent);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);

        verify(server).createEvent(eq(Message.MESSAGE_TYPE), anyString());
        verify(server).send(captor.capture());
        Event sentEvent = captor.getValue();
        assertThat(sentEvent.getAttribute(Message.DIRECTION), is(Message.IN_BOUND));
        assertThat(sentEvent.getAttribute(Message.BODY), is(inMessage.getBody()));
        assertThat(sentEvent.getAttribute(Message.SUBJECT), is(inMessage.getSubject()));
        assertThat(sentEvent.getAttribute(Message.FROM), is(XmppClient.XMPP_PREFIX + inMessage.getFrom().toString()));
    }

    @Test
    public void doesNotMakeMessageEventOfOutgoingMessage() throws Exception {
        client.activate(server);
        xmppMessageEvent = new MessageEvent(new Object(), inMessage, false);

        client.handleMessageEvent(xmppMessageEvent);

        verify(server, times(0)).createEvent(anyString(), anyString());
        verify(server, times(0)).send(any(Event.class));
    }

    @Test
    public void doesNotMakeMessageEventForUnspecifiedSender() throws Exception {
        itemProxy.setAttributeValue("AcceptedSenders", "b@c,d@e");
        client.activate(server);
        client.handleMessageEvent(xmppMessageEvent);

        verify(server, times(0)).createEvent(anyString(), anyString());
        verify(server, times(0)).send(any(Event.class));
    }

    @Test
    public void makeMessageEventForSpecifiedSender() throws Exception {
        itemProxy.setAttributeValue("AcceptedSenders", "b@c," + MESSAGE_SENDER);
        client.activate(server);
        client.handleMessageEvent(xmppMessageEvent);

        verify(server, times(1)).createEvent(anyString(), anyString());
        verify(server, times(1)).send(any(Event.class));
    }

    @Test
    public void makeMessageEventForSpecifiedSenderIgnoresResourceIfNotSpecified() throws Exception {
        itemProxy.setAttributeValue("AcceptedSenders", "b@c," + MESSAGE_SENDER);
        inMessage.setFrom(Jid.valueOf(MESSAGE_SENDER + "/Resource"));
        client.activate(server);
        client.handleMessageEvent(xmppMessageEvent);

        verify(server, times(1)).createEvent(anyString(), anyString());
        verify(server, times(1)).send(any(Event.class));
    }

    @Test
    public void noMessageEventForSpecifiedSenderIfWringResource() throws Exception {
        itemProxy.setAttributeValue("AcceptedSenders", "b@c," + MESSAGE_SENDER + "/OtherResource");
        inMessage.setFrom(Jid.valueOf(MESSAGE_SENDER + "/Resource"));
        client.activate(server);
        client.handleMessageEvent(xmppMessageEvent);

        verify(server, times(0)).createEvent(anyString(), anyString());
        verify(server, times(0)).send(any(Event.class));
    }

    @Test
    public void turnsOnSSL() throws Exception {
        itemProxy.setAttributeValue("UseSSL", "true");
        client.activate(server);

        ArgumentCaptor<TcpConnectionConfiguration> capt = ArgumentCaptor.forClass(TcpConnectionConfiguration.class);
        verify(client).createBabblerXmppSession(anyString(), capt.capture());
        assertThat(capt.getValue().isSecure(), is(true));
    }

    @Test
    public void turnsOffSSL() throws Exception {
        itemProxy.setAttributeValue("UseSSL", "false");
        client.activate(server);

        ArgumentCaptor<TcpConnectionConfiguration> capt = ArgumentCaptor.forClass(TcpConnectionConfiguration.class);
        verify(client).createBabblerXmppSession(anyString(), capt.capture());
        assertThat(capt.getValue().isSecure(), is(false));
    }

    @Test
    public void canTrustAnyCertificate() throws Exception {
        itemProxy.setAttributeValue("TrustAnyCertificate", "true");
        client.activate(server);

        ArgumentCaptor<TcpConnectionConfiguration> capt = ArgumentCaptor.forClass(TcpConnectionConfiguration.class);
        verify(client).createBabblerXmppSession(anyString(), capt.capture());
        assertThat(capt.getValue().getSSLContext(), is(sslContext));
    }

    @Test
    public void canNotTrustAnyCertificate() throws Exception {
        itemProxy.setAttributeValue("TrustAnyCertificate", "false");
        client.activate(server);

        ArgumentCaptor<TcpConnectionConfiguration> capt = ArgumentCaptor.forClass(TcpConnectionConfiguration.class);
        verify(client).createBabblerXmppSession(anyString(), capt.capture());
        assertThat(capt.getValue().getSSLContext(), not(is(sslContext)));
    }
}
