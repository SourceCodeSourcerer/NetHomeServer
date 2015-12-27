package nu.nethome.home.items.net.wemo;

import nu.nethome.home.items.net.wemo.soap.LightSoapClient;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.util.*;

public class WemoInsightSwitchClient extends LightSoapClient {
    public static final String BASICEVENT1_SERVICE_URL = "/upnp/control/basicevent1";
    public static final String BASICEVENT_NAMESPACE = "urn:Belkin:service:basicevent:1";
    public static final String SET_BINARY_STATE = "SetBinaryState";
    public static final String GET_BINARY_STATE = "GetBinaryState";
    public static final String INSIGHT1_SERVICE_URL = "/upnp/control/insight1";
    public static final String INSIGHT1_NAMESPACE = "urn:Belkin:service:insight:1";
    public static final String GET_INSIGHT_PARAMS = "GetInsightParams";

    private String wemoURL;

    public WemoInsightSwitchClient(String wemoURL) {
        super(500, 1000);
        this.wemoURL = wemoURL;
    }

    public void setOnState(boolean isOn) throws WemoException {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new StringArgument("BinaryState", isOn ? "1" :  "0"));
        try {
            sendRequest(BASICEVENT_NAMESPACE, wemoURL + BASICEVENT1_SERVICE_URL, SET_BINARY_STATE, arguments);
        } catch (SOAPException|IOException e) {
            throw new WemoException(e);
        }
    }

    public boolean getOnState() throws WemoException {
        try {
            List<Argument> args = new ArrayList<>();
            Map<String, String> result = sendRequest(BASICEVENT_NAMESPACE, wemoURL + BASICEVENT1_SERVICE_URL, GET_BINARY_STATE, args);
            return !result.values().iterator().next().equals("0");
        } catch (SOAPException|IOException e) {
            throw new WemoException(e);
        }
    }

    public InsightState getInsightParameters() throws WemoException {
        List<Argument> args = new ArrayList<>();
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
