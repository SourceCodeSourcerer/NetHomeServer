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

import nu.nethome.home.system.Event;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class PhilipsHueBridge {

    public static final String WWW_MEETHUE_COM_API = "https://www.meethue.com";
    public static final String HUE_NUPNP = "/api/nupnp";

    private String url = "http://192.168.1.174";
    private String id = "";

    JsonRestClient client = new JsonRestClient();

    public PhilipsHueBridge(String url, String identity) {
        this.url = url;
        this.id = identity;
    }

    PhilipsHueBridge(JsonRestClient client, String address) {
        this.url = "http://" + address;
        this.client = client;
    }

    /**
     * Set the state of the specified lamp
     *
     * @param user  Registered user
     * @param lamp  Lamp identity
     * @param state New state of the lamp
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public void setLightState(String user, String lamp, LightState state) throws IOException, HueProcessingException {
        try {
            JSONObject stateParameter = new JSONObject();
            if (state.isOn()) {
                stateParameter.put("on", true);
                stateParameter.put("bri", state.getBrightness());
                if (state.getColorTemperature() > 0) {
                    stateParameter.put("ct", state.getColorTemperature());
                } else {
                    stateParameter.put("hue", state.getHue());
                    stateParameter.put("sat", state.getSaturation());
                }
            } else {
                stateParameter.put("on", false);
            }
            String resource = String.format("/api/%s/lights/%s/state", user, lamp);
            JSONData result = client.put(url, resource, stateParameter);
            checkForErrorResponse(result);
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    private void checkForErrorResponse(JSONData result) throws HueProcessingException {
        if (result.isObject() && result.getObject().has("error")) {
            JSONObject error = result.getObject().getJSONObject("error");
            throw new HueProcessingException(error.getString("description"), error.getInt("type"));
        }
    }

    /**
     * Get the state of a specified lamp
     *
     * @param user Registered user
     * @param lamp Lamp identity
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public Light getLight(String user, String lamp) throws IOException, HueProcessingException {
        try {
            String resource = String.format("/api/%s/lights/%s", user, lamp);
            JSONData result = client.get(url, resource, null);
            checkForErrorResponse(result);
            return new Light(result.getObject());
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    /**
     * List all lamps known to the bridge
     *
     * @param user Registered user
     * @return List of lamps
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public List<LightId> listLights(String user) throws HueProcessingException, IOException {
        try {
            String resource = String.format("/api/%s/lights", user);
            JSONData result = client.get(url, resource, null);
            checkForErrorResponse(result);
            List<LightId> list = new ArrayList<LightId>();
            for (String lampId : getFieldNames(result.getObject())) {
                list.add(new LightId(lampId, result.getObject().getJSONObject(lampId).getString("name")));
            }
            return list;
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    /**
     * List all sensors known to the bridge
     *
     * @param user Registered user
     * @return List of sensors
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public List<Sensor> listSensors(String user) throws HueProcessingException, IOException {
        try {
            String resource = String.format("/api/%s/sensors", user);
            JSONData result = client.get(url, resource, null);
            checkForErrorResponse(result);
            JSONObject sensors = result.getObject();
            List<Sensor> list = new ArrayList<>();
            Iterator<String> attributes = sensors.keys();
            while (attributes.hasNext()) {
                String id = attributes.next();
                list.add(new Sensor(id, sensors.getJSONObject(id)));
            }
            return list;
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    /**
     * Register a new user to the bridge. The button on the bridge must have been pressed within 30 seconds
     * for this operation to succeed.
     *
     * @param deviceType Name of device, should for example be app name
     * @param user       Username to register. May be left blank
     * @return The name of the registered user
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public String registerUser(String deviceType, String user) throws HueProcessingException, IOException {
        try {
            JSONObject parameter = new JSONObject();
            parameter.put("devicetype", deviceType);
            if (user != null && user.length() > 0) {
                parameter.put("username", user);
            }
            JSONData result = client.post(url, "/api", parameter);
            checkForErrorResponse(result);
            JSONObject resultObject = result.getArray().getJSONObject(0);
            JSONObject resultData = resultObject.getJSONObject("success");
            return resultData.getString("username");
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    /**
     * Get the bridge configuration
     *
     * @param user Registered user
     * @return the configuration
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public HueConfig getConfiguration(String user) throws IOException, HueProcessingException {
        try {
            String resource = String.format("/api/%s/config", user);
            JSONData jResult = client.get(url, resource, null);
            checkForErrorResponse(jResult);
            return new HueConfig(jResult.getObject());
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    private String[] getFieldNames(JSONObject object) {
        String[] result = JSONObject.getNames(object);
        return result == null ? new String[0] : result;
    }

    /**
     * List all hue bridges in this local network
     *
     * @return List of bridges
     * @throws IOException            If communication fails
     * @throws HueProcessingException If the command cannot be executed
     */
    public static List<PhilipsHueBridge> listLocalPhilipsHueBridges() throws HueProcessingException, IOException {
        try {
            JsonRestClient client = new JsonRestClient();
            JSONData result = client.get(WWW_MEETHUE_COM_API, HUE_NUPNP, null);
            List<PhilipsHueBridge> list = new ArrayList<PhilipsHueBridge>();
            for (int i = 0; i < result.getArray().length(); i++) {
                JSONObject id = result.getArray().getJSONObject(i);
                list.add(new PhilipsHueBridge("http://" + id.getString("internalipaddress"), id.getString("id")));
            }
            return list;
        } catch (JSONException e) {
            throw new HueProcessingException(e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }
}