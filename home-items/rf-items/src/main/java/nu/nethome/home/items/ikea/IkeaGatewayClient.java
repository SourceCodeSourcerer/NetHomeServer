package nu.nethome.home.items.ikea;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class IkeaGatewayClient {

    private static final long MESSAGE_TIMEOUT = 1000L;
    private final String NODES = "/15001";
    private static Logger logger = Logger.getLogger(IkeaGatewayClient.class.getName());

    private InMemoryPskStore pskStore;
    private CoapEndpoint dtlsEndpoint;
    private boolean isConnected;

    public void start() {
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        pskStore = new InMemoryPskStore();
        builder.setPskStore(pskStore);
        builder.setSupportedCipherSuites(new CipherSuite[]{CipherSuite.TLS_PSK_WITH_AES_128_CCM_8});
        DTLSConnector dtlsconnector = new DTLSConnector(builder.build(), null);
        try {
            dtlsEndpoint = new CoapEndpoint(dtlsconnector, NetworkConfig.getStandard());
            dtlsEndpoint.start();
            EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint);
        } catch (IOException e) {
            dtlsEndpoint = null;
        }
    }

    public void setRouterKey(InetSocketAddress peerAddress, String identity, byte[] key) {
        pskStore.addKnownPeer(peerAddress, identity, key);
    }

    public void stop() {
        if (dtlsEndpoint != null) {
            dtlsEndpoint.stop();
            dtlsEndpoint.destroy();
            dtlsEndpoint = null;
        }
    }

    public JSONData sendCoapRequest(String uri, String method, String body, boolean getResponse) {
        Request request = requestFromType(method);
        request.setURI(uri);
        if (!isGetRequest(request)) {
            request.setPayload(body);
        }
        request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);

        request.send();

        if (getResponse) {
            Response response;
            try {
                response = request.waitForResponse(MESSAGE_TIMEOUT);
                if (response != null &&
                        response.getPayloadSize() > 0 &&
                        CoAP.ResponseCode.isSuccess(response.getCode())) {
                    isConnected = true;
                    JSONData jsonData = new JSONData(response.getPayloadString());
                    return jsonData;
                } else {
                    isConnected = false;
                    if (response == null) {
                        logger.info("Timeout waiting for response from IKEA GW " + uri);
                    } else if (!CoAP.ResponseCode.isSuccess(response.getCode())) {
                        logger.info(String.format("Failed request for IKEA GW (%s), error: %s", uri, response.getCode().toString()));
                    } else if (response.getPayloadSize() == 0) {
                        logger.info("Failed GET request for IKEA GW, no response data: " + uri);
                    }
                    return null;
                }
            } catch (InterruptedException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isGetRequest(Request request) {
        return request.getCode() == CoAP.Code.GET;
    }

    public JSONData getJsonMessage(String uri) {
        return sendCoapRequest(uri, "GET", "", true);
    }

    public List<Integer> getNodeIds(String address, String port) {
        ArrayList<Integer> result = new ArrayList<>();
        JSONData nodelist = getJsonMessage(String.format("coaps://%s%s%s%s", address, port.isEmpty() ? "" : ":", port, NODES));
        if (nodelist != null && !nodelist.isObject()) {
            JSONArray jsonArray = nodelist.getArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getInt(i));
            }
        }
        return result;
    }

    public List<JSONObject> getNodes(String address, String port) {
        ArrayList<JSONObject> result = new ArrayList<>();
        for (int nodeId : getNodeIds(address, port)) {
            JSONData nodeInfo = getJsonMessage(String.format("coaps://%s%s%s%s/%d", address, port.isEmpty() ? "" : ":", port, NODES, nodeId));
            if (nodeInfo != null && nodeInfo.isObject()) {
                result.add(nodeInfo.getObject());
            }
        }
        return result;
    }

    private static Request requestFromType(String method) {
        if (method.equalsIgnoreCase("GET")) {
            return Request.newGet();
        } else if (method.equalsIgnoreCase("POST")) {
            return Request.newPost();
        } else if (method.equalsIgnoreCase("PUT")) {
            return Request.newPut();
        } else if (method.equalsIgnoreCase("DELETE")) {
            return Request.newDelete();
        } else {
            return Request.newGet();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
