/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.hue;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 */
public class PhilipsHueBridgeTest {

    public static final String USER_NAME = "test";
    PhilipsHueBridge api;
    JsonRestClient restClient;

    @Before
    public void setUp() throws IOException {
        restClient = mock(JsonRestClient.class);
        api = new PhilipsHueBridge(restClient, "1.1.1.1");
        when(restClient.put(any(String.class), any(String.class), any(JSONObject.class))).thenReturn(new JSONData("{}"));

    }

    @Test
    public void canTurnLampOn() throws Exception {
        LightState onState = new LightState(254, 0, 254);
        api.setLightState(USER_NAME, "2", onState);
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient, times(1)).put(eq("http://1.1.1.1"), eq("/api/test/lights/2/state"), captor.capture());
        assertThat(captor.getValue().getBoolean("on"), is(true));
    }

    @Test
    public void canTurnLampOff() throws Exception {
        LightState off = new LightState();
        api.setLightState(USER_NAME, "2", off);
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient, times(1)).put(eq("http://1.1.1.1"), eq("/api/test/lights/2/state"), captor.capture());
        assertThat(captor.getValue().getBoolean("on"), is(false));
    }

    private static final String ERROR_RESULT = "{\n" +
            "        \"error\": {\n" +
            "            \"type\": 201 ,\n" +
            "            \"address\": \"/resource/parameteraddress\",\n" +
            "            \"description\": \"An Error\"\n" +
            "        }\n" +
            "    }";

    @Test(expected = HueProcessingException.class)
    public void turnLampOffDetectsError() throws Exception {
        when(restClient.put(any(String.class), any(String.class), any(JSONObject.class))).thenReturn(new JSONData(ERROR_RESULT));
        LightState off = new LightState();
        api.setLightState(USER_NAME, "2", off);
    }


    private static String LAMP_REST_RESPONSE = "{\n" +
            "    \"state\": {\n" +
            "        \"on\": false,\n" +
            "        \"bri\": 254,\n" +
            "        \"hue\": 0,\n" +
            "        \"sat\": 17,\n" +
            "        \"xy\": [\n" +
            "            0.3804,\n" +
            "            0.3768\n" +
            "        ],\n" +
            "        \"ct\": 248,\n" +
            "        \"alert\": \"none\",\n" +
            "        \"effect\": \"none\",\n" +
            "        \"colormode\": \"hs\",\n" +
            "        \"reachable\": true\n" +
            "    },\n" +
            "    \"type\": \"Extended color light\",\n" +
            "    \"name\": \"Soffbordet\",\n" +
            "    \"modelid\": \"LCT001\",\n" +
            "    \"swversion\": \"66009663\",\n" +
            "    \"pointsymbol\": {\n" +
            "        \"1\": \"none\",\n" +
            "        \"2\": \"none\",\n" +
            "        \"3\": \"none\",\n" +
            "        \"4\": \"none\",\n" +
            "        \"5\": \"none\",\n" +
            "        \"6\": \"none\",\n" +
            "        \"7\": \"none\",\n" +
            "        \"8\": \"none\"\n" +
            "    } }";

    @Test
    public void canGetLamp() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(LAMP_REST_RESPONSE));
        Light result = api.getLight(USER_NAME, "2");
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/lights/2"), any(JSONObject.class));
        assertThat(result.getState().isOn(), is(false));
        assertThat(result.getState().getBrightness(), is(254));
        assertThat(result.getState().getHue(), is(0));
        assertThat(result.getState().getSaturation(), is(17));
        assertThat(result.getModelid(), is("LCT001"));
        assertThat(result.getName(), is("Soffbordet"));
        assertThat(result.getSwversion(), is("66009663"));
        assertThat(result.getType(), is("Extended color light"));
    }

    private static String LAMP_LUX_REST_RESPONSE = "{\n" +
            "    \"state\": {\n" +
            "        \"on\": true,\n" +
            "        \"bri\": 127,\n" +
            "        \"alert\": \"none\",\n" +
            "        \"reachable\": true\n" +
            "    },\n" +
            "    \"type\": \"Dimmable light\",\n" +
            "    \"name\": \"Lux Lamp 1\",\n" +
            "    \"modelid\": \"LWB004\",\n" +
            "    \"swversion\": \"66011639\",\n" +
            "    \"pointsymbol\": {\n" +
            "        \"1\": \"none\",\n" +
            "        \"2\": \"none\",\n" +
            "        \"3\": \"none\",\n" +
            "        \"4\": \"none\",\n" +
            "        \"5\": \"none\",\n" +
            "        \"6\": \"none\",\n" +
            "        \"7\": \"none\",\n" +
            "        \"8\": \"none\"\n" +
            "    }\n" +
            "}";

    @Test
    public void canGetHueLuxLamp() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(LAMP_LUX_REST_RESPONSE));
        Light result = api.getLight(USER_NAME, "2");
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/lights/2"), any(JSONObject.class));
        assertThat(result.getState().isOn(), is(true));
        assertThat(result.getState().getBrightness(), is(127));
        assertThat(result.getState().getHue(), is(0));
        assertThat(result.getState().getSaturation(), is(0));
        assertThat(result.getModelid(), is("LWB004"));
        assertThat(result.getName(), is("Lux Lamp 1"));
        assertThat(result.getSwversion(), is("66011639"));
        assertThat(result.getType(), is("Dimmable light"));
    }

    private static String LAMP_LIST_REST_RESPONSE = "{\n" +
            "    \"1\": {\n" +
            "        \"name\": \"Bedroom\"\n" +
            "    },\n" +
            "    \"2\": {\n" +
            "        \"name\": \"Kitchen\"\n" +
            "    }\n" +
            "}";

    @Test
    public void canListLamps() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(LAMP_LIST_REST_RESPONSE));
        List<LightId> result = api.listLights(USER_NAME);
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/lights"), any(JSONObject.class));
        assertThat(result.size(), is(2));
        LightId id1 = new LightId("1", "Bedroom");
        LightId id2 = new LightId("2", "Kitchen");
        assertThat(result, hasItems(id1, id2));
    }

    private static final String REGISTER_USER_OK = "[{\"success\":{\"username\": \"1234567890\"}}]";

    @Test
    public void canListNoLamps() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData("{}"));
        List<LightId> result = api.listLights(USER_NAME);
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/lights"), any(JSONObject.class));
        assertThat(result.size(), is(0));
    }

    private static String SENSOR_LIST_REST_RESPONSE = "{\n" +
            "    \"1\": {\n" +
            "        \"state\": {\n" +
            "            \"daylight\": false,\n" +
            "            \"lastupdated\": \"none\"\n" +
            "        },\n" +
            "        \"config\": {\n" +
            "            \"on\": true,\n" +
            "            \"long\": \"none\",\n" +
            "            \"lat\": \"none\",\n" +
            "            \"sunriseoffset\": 30,\n" +
            "            \"sunsetoffset\": -30\n" +
            "        },\n" +
            "        \"name\": \"Daylight\",\n" +
            "        \"type\": \"Daylight\",\n" +
            "        \"modelid\": \"PHDL00\",\n" +
            "        \"manufacturername\": \"Philips\",\n" +
            "        \"swversion\": \"1.0\"\n" +
            "    },\n" +
            "    \"2\": {\n" +
            "        \"state\": {\n" +
            "            \"buttonevent\": 18,\n" +
            "            \"lastupdated\": \"2014-11-23T14:53:25\"\n" +
            "        },\n" +
            "        \"config\": {\n" +
            "            \"on\": true\n" +
            "        },\n" +
            "        \"name\": \"ZGP Switch 1\",\n" +
            "        \"type\": \"ZGPSwitch\",\n" +
            "        \"modelid\": \"ZGPSWITCH\",\n" +
            "        \"manufacturername\": \"none\",\n" +
            "        \"uniqueid\": \"00:00:00:00:00:42:81:4d-f2\"\n" +
            "    }\n" +
            "}";

    @Test
    public void canListNoSensors() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData("{}"));
        List<Sensor> result = api.listSensors(USER_NAME);
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/sensors"), any(JSONObject.class));
        assertThat(result.size(), is(0));
    }

    @Test
    public void canListSensors() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(SENSOR_LIST_REST_RESPONSE));
        List<Sensor> result = api.listSensors(USER_NAME);
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/test/sensors"), any(JSONObject.class));
        assertThat(result.size(), is(2));
        int zgpIndex = (result.get(0).getId().equals("2")) ? 0 : 1;
        int dayLightIndex = (zgpIndex == 0) ? 1 : 0;
        assertThat(result.get(dayLightIndex).getId(), is("1"));
        assertThat(result.get(zgpIndex).getId(), is("2"));
        assertThat(result.get(zgpIndex).getName(), is("ZGP Switch 1"));
        assertThat(result.get(zgpIndex).getType(), is("ZGPSwitch"));
        assertThat(result.get(zgpIndex).getModelid(), is("ZGPSWITCH"));
    }

    @Test
    public void canRegisterUser() throws Exception {
        when(restClient.post(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(REGISTER_USER_OK));
        String result = api.registerUser("Test");
        verify(restClient, times(1)).post(eq("http://1.1.1.1"), eq("/api"), any(JSONObject.class));
        assertThat(result, is("1234567890"));
    }

    private static final String CONFIG_RESPONSE = "{\n" +
            "    \"proxyport\": 0,\n" +
            "    \"UTC\": \"2012-10-29T12:00:00\",\n" +
            "    \"name\": \"Smartbridge 1\",\n" +
            "    \"swupdate\": {\n" +
            "        \"updatestate\":1,\n" +
            "         \"url\": \"www.meethue.com/patchnotes/1453\",\n" +
            "         \"text\": \"This is a software update\",\n" +
            "         \"notify\": false\n" +
            "     },\n" +
            "    \"whitelist\": {\n" +
            "        \"1234567890\": {\n" +
            "            \"last use date\": \"2010-10-17T01:23:20\",\n" +
            "            \"create date\": \"2010-10-17T01:23:20\",\n" +
            "            \"name\": \"iPhone Web 1\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"swversion\": \"01003542\",\n" +
            "    \"proxyaddress\": \"none\",\n" +
            "    \"mac\": \"00:17:88:00:00:00\",        \n" +
            "    \"linkbutton\": false,\n" +
            "    \"ipaddress\": \"192.168.1.100\",\n" +
            "    \"netmask\": \"255.255.0.0\",\n" +
            "    \"gateway\": \"192.168.0.1\",\n" +
            "    \"dhcp\": false\n" +
            "}";

    @Test
    public void canGetConfiguration() throws Exception {
        when(restClient.get(anyString(), anyString(), any(JSONObject.class))).thenReturn(new JSONData(CONFIG_RESPONSE));
        HueConfig result = api.getConfiguration("stefanstromberg");
        verify(restClient, times(1)).get(eq("http://1.1.1.1"), eq("/api/stefanstromberg/config"), any(JSONObject.class));
        assertThat(result.getName(), is("Smartbridge 1"));
        assertThat(result.getMac(), is("00:17:88:00:00:00"));
        assertThat(result.getSwVersion(), is("01003542"));
    }
}