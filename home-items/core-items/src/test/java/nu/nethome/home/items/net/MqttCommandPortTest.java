package nu.nethome.home.items.net;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class MqttCommandPortTest {

    private static final String TEST_ITEM_NAME = "testItem";
    private static final String ATTRIBUTE = "attribute";
    private static final String ATTRIBUTE_SEPARATOR = "_";
    private MqttCommandPort mqttCommandPort;
    private HomeService service;
    private HomeItemProxy testItem;

    @Before
    public void setUp() throws Exception {
        mqttCommandPort = new MqttCommandPort();
        service = mock(HomeService.class);
        mqttCommandPort.activate(service);
        testItem = mock(HomeItemProxy.class);
        doReturn(testItem).when(service).openInstance(TEST_ITEM_NAME);

    }

    @Test
    public void findsCorrectHomeItemForActionWithOneLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME);

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void findsCorrectHomeItemForActionWithThreeLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test/more/levels");
        receiveMqttMessage("On", "test/more/levels/" + TEST_ITEM_NAME);

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void findsCorrectHomeItemForAttributeWithThreeLevelTopic() throws Exception {
        mqttCommandPort.setTopic("test/more/levels");
        receiveMqttMessage("On", "test/more/levels/" + TEST_ITEM_NAME + ATTRIBUTE_SEPARATOR + ATTRIBUTE);

        verify(service).openInstance(TEST_ITEM_NAME);
    }

    @Test
    public void callsCorrectAction() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME);

        verify(testItem).callAction("On");
    }

    @Test
    public void setsAttributeValue() throws Exception {
        mqttCommandPort.setTopic("test");
        receiveMqttMessage("On", "test/" + TEST_ITEM_NAME + ATTRIBUTE_SEPARATOR + ATTRIBUTE);

        verify(testItem).setAttributeValue(ATTRIBUTE, "On");
    }

    static final String separators[] = {"\\", "_", "A" , ",", ".", ";", ":", "!", "#", "¤", "%", "&", "(", ")", "=", "?", "z", "5", "_", "|", "ö", "'", "\"" };

    @Test
    public void handlesDifferentAttributeSeparators() throws Exception {
        mqttCommandPort.setTopic("test");
        int i = 0;
        for (String s : separators) {
            mqttCommandPort.setAttributeSeparator(s);
            receiveMqttMessage("On", "test/" + TEST_ITEM_NAME + s + ATTRIBUTE);
            i++;
            verify(testItem, times(i)).setAttributeValue(ATTRIBUTE, "On");
        }
    }

    static final String badSeparators[] = {"/", "aa", ""};

    @Test
    public void handlesBadAttributeSeparators() throws Exception {
        mqttCommandPort.setTopic("test");
        int i = 0;
        for (String s : badSeparators) {
            try {
                mqttCommandPort.setAttributeSeparator(s);
                assertThat(true, is(false));
            } catch (IllegalValueException e) {
                assertThat(e.getValue(), is(s));
            }
        }
    }

    private void receiveMqttMessage(String value, String topic) {
        Event event = new InternalEvent("Mqtt_Message");
        event.setAttribute("Mqtt.Topic", topic);
        event.setAttribute("Mqtt.Message", value);
        event.setAttribute("Direction", "In");
        mqttCommandPort.receiveEvent(event);
    }
}