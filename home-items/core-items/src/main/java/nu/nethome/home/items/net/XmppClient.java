package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.session.TcpConnection;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.PresenceListener;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.compress.model.CompressionMethod;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import static nu.nethome.home.items.net.Message.*;

@Plugin
@HomeItemType("Ports")
public class XmppClient extends HomeItemAdapter {

    public static final String XMPP_PREFIX = "xmpp:";
    private final String model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"XmppClient\" Category=\"Ports\" >"
            + "  <Attribute Name=\"Status\" Type=\"String\" Get=\"getStatus\" Default=\"true\" />"
            + "  <Attribute Name=\"Domain\" Type=\"String\" Get=\"getDomain\" Set=\"setDomain\" />"
            + "  <Attribute Name=\"UserName\" Type=\"String\" Get=\"getUserName\" Set=\"setUserName\" />"
            + "  <Attribute Name=\"Password\" Type=\"Password\" Get=\"getPassword\" Set=\"setPassword\" />"
            + "  <Attribute Name=\"Resource\" Type=\"String\" Get=\"getResource\" Set=\"setResource\" />"
            + "  <Attribute Name=\"AcceptedSenders\" Type=\"String\" Get=\"getAcceptedSenders\" Set=\"setAcceptedSenders\" />"
            + "  <Attribute Name=\"MaxMessagesPerDay\" Type=\"String\" Get=\"getMaxMessagesPerDay\" Set=\"setMaxMessagesPerDay\" />"
            + "  <Attribute Name=\"UseSSL\" Type=\"Boolean\" Get=\"getUseSSL\" Set=\"setUseSSL\" />"
            + "  <Attribute Name=\"TrustAnyCertificate\" Type=\"Boolean\" Get=\"getTrustAnyCertificate\" Set=\"setTrustAnyCertificate\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" />"
            + "</HomeItem> ");

    private XmppSession session;
    private String domain = "jabber.se";
    private String userName = "";
    private String password = "";
    private String resource = "";
    private Set<String> acceptedSenders = new HashSet<>();
    private String status = "Not Connected";
    private int maxMessagesPerDay = 50;
    private int messagesSentToday = 0;
    private int currentDay;
    private boolean useSSL = true;
    private boolean trustAnyCertificate = true;

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void activate() {
        startSession();
    }

    public void reconnect() {
        stopSession();
        startSession();
    }

    @Override
    public void stop() {
        stopSession();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isOutgoingMessage(event) && (session != null) && session.isConnected() && (messagesSentToday++ < maxMessagesPerDay)) {
            return processMessage(event.getAttribute(TO), event.getAttribute(BODY), event.getAttribute(SUBJECT));
        } else if (isNewDay(event)) {
            currentDay = getDayOfYear();
            messagesSentToday = 0;
        }
        return false;
    }

    private boolean isNewDay(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE) && (currentDay != getDayOfYear());
    }

    private boolean isOutgoingMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(MESSAGE_TYPE) &&
                event.getAttribute(DIRECTION).equals(OUT_BOUND);
    }

    private boolean processMessage(String to, String body, String subject) {
        if (body.isEmpty() && subject.isEmpty()) {
            return false;
        }
        boolean hasSent = false;
        for (String recipient : to.split(",")) {
            if (recipient.toLowerCase().startsWith(XMPP_PREFIX)) {
                rocks.xmpp.core.stanza.model.client.Message message = new rocks.xmpp.core.stanza.model.client.Message(
                        Jid.valueOf(recipient.substring(XMPP_PREFIX.length())),
                        rocks.xmpp.core.stanza.model.client.Message.Type.CHAT,
                        body);
                if (!subject.isEmpty()) {
                    message.setSubject(subject);
                }
                session.send(message);
                hasSent = true;
            }
        }
        return hasSent;
    }

    private void startSession() {
        try {
            session = createSession();
            status = "Connected";
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.contains("certification path")) {
                message = "untrusted certificate";
            }
            status = "Failed to connect: " + message;
        } catch (LoginException e) {
            status = "Failed to login: " + e.getMessage();
        }
    }

    private void stopSession() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                // Failed to close
            } finally {
                session = null;
                status = "Not Connected";
            }
        }
    }

    XmppSession createSession() throws IOException, LoginException {
        TcpConnectionConfiguration.Builder builder = TcpConnectionConfiguration.builder()
                .hostname(this.domain)
                .port(5222)
                .compressionMethod(CompressionMethod.ZLIB)
                .secure(useSSL);
        if (trustAnyCertificate) {
            builder = builder.sslContext(trustAnyCertificateSslContext());
        }
        TcpConnectionConfiguration tcpConfiguration = builder.build();
        XmppSession newSession = createBabblerXmppSession(domain, tcpConfiguration);
        listenForPresenceChanges(newSession);
        listenForMessages(newSession);
        login(newSession);
        return newSession;
    }

    void login(XmppSession newSession) throws IOException, LoginException {
        newSession.connect();
        newSession.login(userName, password, resource);
        newSession.send(new Presence());
    }

    XmppSession createBabblerXmppSession(String domain, TcpConnectionConfiguration connectionConfiguration) {
        return new XmppSession(domain, connectionConfiguration);
    }

    void listenForMessages(XmppSession session) {
        session.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                handleMessageEvent(e);
            }
        });
    }

    void listenForPresenceChanges(XmppSession session) {
        session.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                if (e.isIncoming()) {
                    handlePresenceEvent(e);
                }
            }
        });
    }

    SSLContext trustAnyCertificateSslContext() throws IOException {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Could not configure trust", e);
        }
        try {
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    void handleMessageEvent(MessageEvent event) {
        if (event.isIncoming() && isAnAcceptedSender(event.getMessage().getFrom().toString())) {
            Event homeEvent = server.createEvent(Message.MESSAGE_TYPE, "");
            homeEvent.setAttribute(Message.DIRECTION, Message.IN_BOUND);
            if (event.getMessage().getFrom() != null) {
                homeEvent.setAttribute(Message.FROM, XMPP_PREFIX + event.getMessage().getFrom().toString());
            }
            if (event.getMessage().getSubject() != null) {
                homeEvent.setAttribute(Message.SUBJECT, event.getMessage().getSubject());
            }
            if (event.getMessage().getBody() != null) {
                homeEvent.setAttribute(Message.BODY, event.getMessage().getBody());
            }
            server.send(homeEvent);
        }
    }

    private boolean isAnAcceptedSender(String sender) {
        if (acceptedSenders.isEmpty()) {
            return true;
        }
        String senderWithoutResource;
        if (sender.contains("/")) {
            senderWithoutResource = sender.substring(0, sender.indexOf("/"));
        } else {
            senderWithoutResource = sender;
        }
        for (String acceptedSender : acceptedSenders) {
            if (acceptedSender.contains("/") && acceptedSender.equals(sender)) {
                return true;
            } else if (!acceptedSender.contains("/") && acceptedSender.equals(senderWithoutResource)) {
                return true;
            }
        }
        return false;
    }

    private void handlePresenceEvent(PresenceEvent event) {
        // Not yet handled
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getStatus() {
        if (session != null) {
            return session.getStatus().name().toLowerCase();
        }
        return status;
    }

    public String getAcceptedSenders() {
        String result = "";
        String separator = "";
        for (String s : acceptedSenders) {
            result += separator + s;
            separator = ",";
        }
        return result;
    }

    public void setAcceptedSenders(String acceptedSenders) {
        HashSet<String> senders = new HashSet<>();
        if (!acceptedSenders.isEmpty()) {
            senders.addAll(Arrays.asList(acceptedSenders.split(",")));
        }
        this.acceptedSenders = senders;
    }

    public void setMaxMessagesPerDay(String maxMessagesPerDay) {
        this.maxMessagesPerDay = Integer.parseInt(maxMessagesPerDay);
    }

    public String getMaxMessagesPerDay() {
        return Integer.toString(maxMessagesPerDay);
    }

    public int getDayOfYear() {
        return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }

    public String getUseSSL() {
        return useSSL ? "Yes" : "No";
    }

    public void setUseSSL(String useSSL) {
        this.useSSL = useSSL.equalsIgnoreCase("Yes") || useSSL.equalsIgnoreCase("True");
    }

    public String getTrustAnyCertificate() {
        return trustAnyCertificate ? "Yes" : "No";
    }

    public void setTrustAnyCertificate(String trustAnyCertificate) {
        this.trustAnyCertificate = trustAnyCertificate.equalsIgnoreCase("Yes") || trustAnyCertificate.equalsIgnoreCase("True");
    }
}
