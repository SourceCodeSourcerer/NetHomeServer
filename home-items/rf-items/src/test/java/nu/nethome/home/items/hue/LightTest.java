package nu.nethome.home.items.hue;

import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class LightTest {

    private static final String OsramSmartPlusExample = " {\n" +
            "\"state\": {\n" +
            "\"on\": true,\n" +
            "\"alert\": \"select\",\n" +
            "\"mode\": \"homeautomation\",\n" +
            "\"reachable\": true\n" +
            "},\n" +
            "\"swupdate\": {\n" +
            "\"state\": \"notupdatable\",\n" +
            "\"lastinstall\": null\n" +
            "},\n" +
            "\"type\": \"On/Off plug-in unit\",\n" +
            "\"name\": \"Byrå Hall\",\n" +
            "\"modelid\": \"Plug 01\",\n" +
            "\"manufacturername\": \"OSRAM\",\n" +
            "\"capabilities\": {\n" +
            "\"streaming\": {\n" +
            "\"renderer\": false,\n" +
            "\"proxy\": false\n" +
            "}\n" +
            "},\n" +
            "\"uniqueid\": \"7c:b0:3e:aa:00:a3:b6:3e-03\",\n" +
            "\"swversion\": \"V1.04.12\"\n" +
            "}\n" +
            "}";

    @Test
    public void OsramSmartPlus() throws Exception {
        Light light = new Light(new JSONObject(OsramSmartPlusExample));

        assertThat(light.getState().isOn(), is(true));
        assertThat(light.getState().getBrightness(), is(100));
        assertThat(light.getName(), is("Byrå Hall"));
        assertThat(light.getModelid(), is("Plug 01"));
        assertThat(light.getType(), is("On/Off plug-in unit"));
        assertThat(light.getSwversion(), is("V1.04.12"));
        assertThat(light.getState().hasHueSat(), is(false));
        assertThat(light.getState().hasColorTemperature(), is(false));

    }

}