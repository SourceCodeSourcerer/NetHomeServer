package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginReq {
    public final String account;
    public final String password;
    public final int serverNumber;
    public final String serverName;

    public LoginReq(String account, String password, int serverNumber, String serverName) {
        this.account = account;
        this.password = password;
        this.serverNumber = serverNumber;
        this.serverName = serverName;
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        jsonObject.put("server", serverNumber);
        jsonObject.put("serverName", serverName);
        return jsonObject;
    }
}
