package nu.nethome.home.items.net.wemo;

import nu.nethome.home.items.net.wemo.soap.LightSoapClient;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WemoInsightSwitchClient extends LightSoapClient {
    public static final String BASICEVENT1_SERVICE_URL = "/upnp/control/basicevent1";
    public static final String BASICEVENT_NAMESPACE = "urn:Belkin:service:basicevent:1";
    public static final String SET_BINARY_STATE = "SetBinaryState";
    public static final String INSIGHT1_SERVICE_URL = "/upnp/control/insight1";
    public static final String INSIGHT1_NAMESPACE = "urn:Belkin:service:insight:1";
    public static final String GET_INSIGHT_PARAMS = "GetInsightParams";

    private String wemoURL;

    public WemoInsightSwitchClient(String wemoURL) {
        this.wemoURL = wemoURL;
    }

    public void setOnState(boolean isOn) throws WemoException {
        Map<String, String> args =  new HashMap<>();
        args.put("BinaryState", isOn ? "1" :  "0");
        try {
            sendRequest(BASICEVENT_NAMESPACE, wemoURL + BASICEVENT1_SERVICE_URL, SET_BINARY_STATE, args);
        } catch (SOAPException|IOException e) {
            throw new WemoException(e);
        }
    }

    public InsightState getInsightParameters() throws WemoException {
        Map<String, String> args =  new HashMap<>();
        try {
            Map<String, String> stringStringMap = sendRequest(INSIGHT1_NAMESPACE, wemoURL + INSIGHT1_SERVICE_URL, GET_INSIGHT_PARAMS, args);
            return new InsightState(stringStringMap.values().iterator().next());
        } catch (SOAPException|IOException e) {
            throw new WemoException(e);
        }
    }

    public String getWemoURL() {
        return wemoURL;
    }

    public void setWemoURL(String wemoURL) {
        this.wemoURL = wemoURL;
    }
}
