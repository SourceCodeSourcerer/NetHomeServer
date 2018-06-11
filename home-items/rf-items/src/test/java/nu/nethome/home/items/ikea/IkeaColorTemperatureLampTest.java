package nu.nethome.home.items.ikea;

import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.items.util.TstHomeService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static nu.nethome.home.items.ikea.IkeaColorTemperatureLamp.COLOR_X;
import static nu.nethome.home.items.ikea.IkeaGateway.IKEA_BODY;
import static nu.nethome.home.items.ikea.IkeaLamp.LIGHT;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *
 */
public class IkeaColorTemperatureLampTest {

    private IkeaColorTemperatureLamp lamp;
    private TstHomeService service;
    private LocalHomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        service = new TstHomeService();
        lamp = new IkeaColorTemperatureLamp();
        lamp.activate(service);
        proxy = new LocalHomeItemProxy(lamp);
    }

    @Test
    public void warmOnDimSetsTempTo50WhenDim50() throws Exception {
        proxy.setAttributeValue("OnBrightness", "50");
        proxy.setAttributeValue("ColorTemperature", "0");
        proxy.setAttributeValue("WarmDim", "true");

        proxy.callAction("on");

        JSONObject light = getLightJsonFromLastEvent();
        assertThat(proxy.getAttributeValue("WarmDim"), is("True"));
        assertThat(light.getInt(COLOR_X), is(IkeaColorTemperatureLamp.percentToX(50)));
    }

    @Test
    public void warmOnDimSetsTempTo99WhenDim1() throws Exception {
        proxy.setAttributeValue("OnBrightness", "1");
        proxy.setAttributeValue("ColorTemperature", "0");
        proxy.setAttributeValue("WarmDim", "true");

        proxy.callAction("on");

        JSONObject light = getLightJsonFromLastEvent();
        assertThat(light.getInt(COLOR_X), is(IkeaColorTemperatureLamp.percentToX(99)));
    }

    @Test
    public void tempUnaffectedWhenNoWarmDim() throws Exception {
        proxy.setAttributeValue("OnBrightness", "50");
        proxy.setAttributeValue("ColorTemperature", "25");
        proxy.setAttributeValue("WarmDim", "false");

        proxy.callAction("on");

        JSONObject light = getLightJsonFromLastEvent();
        assertThat(light.getInt(COLOR_X), is(IkeaColorTemperatureLamp.percentToX(25)));
    }

    private JSONObject getLightJsonFromLastEvent() {
        JSONObject object = new JSONObject(service.sentEvents.get(service.sentEvents.size() - 1).getAttribute(IKEA_BODY));
        return object.getJSONArray(LIGHT).getJSONObject(0);
    }
}