package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Arrays;

public class HttpResponse {
    private static final HttpResponse EMPTY = new HttpResponse();
    public final String body;
    public final String[] headers;
    public final String challenge;
    public final String sessionToken;
    private final int responseCode;

    public HttpResponse(String body, String[] headers, String challenge) {
        this(body, headers, challenge, null, HttpURLConnection.HTTP_OK);
    }

    public HttpResponse(String body, String[] headers, String challenge, String sessionToken, int responseCode) {
        this.body = body;
        this.headers = headers;
        this.challenge = challenge;
        this.sessionToken = sessionToken;
        this.responseCode = responseCode;
    }

    private HttpResponse() {
        this.body = "";
        this.headers = new String[0];
        this.challenge = "";
        this.sessionToken = null;
        this.responseCode = HttpURLConnection.HTTP_OK;
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", body);
        jsonObject.put("headers", Arrays.asList(headers));
        jsonObject.put("challenge", challenge);
        jsonObject.put("responseCode", responseCode);
        if (sessionToken != null) {
            jsonObject.put("sessionToken", sessionToken);
        }
        return jsonObject;
    }

    public static HttpResponse empty() {
        return EMPTY;
    }

    public static HttpResponse challenge(String challenge) {
        return new HttpResponse("", new String[0], challenge, null, HttpURLConnection.HTTP_OK);
    }

    public static HttpResponse loginFailed(String challenge) {
        return new HttpResponse("", new String[0], challenge, "", HttpURLConnection.HTTP_OK);
    }

    public static HttpResponse loginSucceeded(String challenge, String sessionId) {
        return new HttpResponse("", new String[0], challenge, sessionId, HttpURLConnection.HTTP_OK);
    }

    public static HttpResponse unauthorized() {
        return new HttpResponse("", new String[0], "", "", HttpURLConnection.HTTP_UNAUTHORIZED);
    }
}
