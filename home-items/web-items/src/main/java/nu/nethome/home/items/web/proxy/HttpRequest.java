package nu.nethome.home.items.web.proxy;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequest {
    public final String sessionToken;
    public final String method;
    public final String body;
    public final String url;
    public final String[] headers;
    public final String loginCredential;

    public HttpRequest(JSONObject json) {
        url = json.getString("url");
        final JSONArray headers1 = json.getJSONArray("headers");
        String[] h = new String[headers1.length()];
        for (int i = 0; i < h.length; i++) {
            h[i] = headers1.getString(i);
        }
        headers = h;
        loginCredential = json.getString("loginCredential");
        if (json.has("sessionToken")) {
            sessionToken = json.getString("sessionToken");
        } else {
            sessionToken = "";
        }
        method = json.getString("method");
        body = json.getString("body");
    }

    public HttpRequest(String url, String[] headers, String loginCredential, String sessionToken) {
        this(url, headers, loginCredential, sessionToken, "GET", "");
    }

    public HttpRequest(String url, String[] headers, String loginCredential, String sessionToken, String method, String body) {
        this.url = url;
        this.headers = headers;
        this.loginCredential = loginCredential;
        this.sessionToken = sessionToken;
        this.method = method;
        this.body = body;
    }

    private HttpRequest() {
        url = "";
        headers = new String[0];
        loginCredential = "";
        sessionToken = "";
        method = "GET";
        body = "";
    }

    public static HttpRequest empty() {
        return new HttpRequest();
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        jsonObject.put("headers", new JSONArray(headers));
        jsonObject.put("loginCredential", loginCredential);
        jsonObject.put("sessionToken", sessionToken);
        jsonObject.put("method", method);
        jsonObject.put("body", body);
        return jsonObject;
    }

    public boolean isProxyRequest() {
        return !url.isEmpty();
    }

    public boolean isAuthenticationRequest() {
        return !loginCredential.isEmpty() && !isProxyRequest();
    }
}
