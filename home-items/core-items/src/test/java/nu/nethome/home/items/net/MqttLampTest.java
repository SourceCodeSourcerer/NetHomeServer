package nu.nethome.home.items.net;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
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
public class MqttLampTest {

    private static final String TEST_TOPIC = "test/topic";
    private static final String ON_MESSAGE = "OnMessage";
    private static final String OFF_MESSAGE = "OffMEssage";
    private MqttLamp mqttLamp;
    private HomeService service;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        service = mock(HomeService.class);
        doReturn(new InternalEvent("")).when(service).createEvent(anyString(), anyString());
        mqttLamp = new MqttLamp();
        mqttLamp.activate(service);
        proxy = new LocalHomeItemProxy(mqttLamp);
    }

    @Test
    public void modelIsValidXML() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(new MqttCommander().getModel().getBytes());
        InputSource source = new InputSource(byteStream);

        parser.parse(source);
    }

    @Test
    public void canSendOn() throws Exception {
        canSendParameter("OnMessage", "On", "Foo");
    }

    @Test
    public void canSendOff() throws Exception {
        canSendParameter("OffMessage", "Off", "Foo");
    }

    public void canSendParameter(String attributeName, String command, String value) throws Exception {
        proxy.setAttributeValue(attributeName, value);
        ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
        proxy.callAction(command);
        verify(service).send(argument.capture());
        assertThat(argument.getValue().getAttribute(MqttClient.MQTT_MESSAGE), is(value));
    }

    @Test
    public void canUpdateStateFromMessage() throws Exception {
        Event event = setUpForReceivingEvent(ON_MESSAGE);

        mqttLamp.receiveEvent(event);

        assertThat(proxy.getAttributeValue("State"), is("On"));
    }

    @Test
    public void canUpdateStateToOffFromMessage() throws Exception {
        Event event = setUpForReceivingEvent(OFF_MESSAGE);

        mqttLamp.on();
        mqttLamp.receiveEvent(event);

        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }

    private Event setUpForReceivingEvent(String message) throws IllegalValueException {
        proxy.setAttributeValue("Topic", TEST_TOPIC);
        proxy.setAttributeValue("OnMessage", ON_MESSAGE);
        proxy.setAttributeValue("OffMessage", OFF_MESSAGE);
        Event event = mock(Event.class);
        doReturn(true).when(event).isType(MqttClient.MQTT_MESSAGE_TYPE);
        doReturn("In").when(event).getAttribute("Direction");
        doReturn(TEST_TOPIC).when(event).getAttribute(MqttClient.MQTT_TOPIC);
        doReturn(message).when(event).getAttribute(MqttClient.MQTT_MESSAGE);
        return event;
    }

    @Test
    public void canToggle() throws Exception {
        proxy.callAction("Toggle");
        assertThat(proxy.getAttributeValue("State"), is("On"));
        proxy.callAction("Toggle");
        assertThat(proxy.getAttributeValue("State"), is("Off"));
    }
}