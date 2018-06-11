package nu.nethome.home.items.net;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class MqttCommanderTest {
    private static final String COMMAND = "c1";
    private MqttCommander mqttCommander;
    private HomeService service;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        service = mock(HomeService.class);
        doReturn(new InternalEvent("")).when(service).createEvent(anyString(), anyString());
        mqttCommander = new MqttCommander();
        mqttCommander.activate(service);
        proxy = new LocalHomeItemProxy(mqttCommander);
    }

    @Test
    public void modelIsValidXML() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(new MqttCommander().getModel().getBytes());
        InputSource source = new InputSource(byteStream);

        parser.parse(source);
    }

    @Test
    public void canSendCommand1() throws Exception {
        canSendCommand("1");
    }

    @Test
    public void canSendCommand2() throws Exception {
        canSendCommand("2");
    }

    @Test
    public void canSendCommand3() throws Exception {
        canSendCommand("3");
    }

    @Test
    public void canSendCommand4() throws Exception {
        canSendCommand("4");
    }

    public void canSendCommand(String number) throws Exception {
        proxy.setAttributeValue("Command" + number, COMMAND);
        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
        proxy.callAction("Command" + number);
        verify(service).send(argument.capture());
        assertThat(argument.getValue().getAttribute(MqttClient.MQTT_MESSAGE), is(COMMAND));
    }

    @Test
    public void canSendQoS() throws Exception {
        canSendParameter("QOS", MqttClient.MQTT_QOS, "2");
    }

    @Test
    public void canSendTopic() throws Exception {
        canSendParameter("Topic", MqttClient.MQTT_TOPIC, "Foo/file/fum");
    }

    @Test
    public void canSendMqttClient() throws Exception {
        canSendParameter("MqttClient", "Mqtt.Client", "Foo");
    }

    public void canSendParameter(String attributeName, String eventAttribute, String value) throws Exception {
        proxy.setAttributeValue(attributeName, value);
        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
        proxy.callAction("Command1");
        verify(service).send(argument.capture());
        assertThat(argument.getValue().getAttribute(eventAttribute), is(value));
    }
}