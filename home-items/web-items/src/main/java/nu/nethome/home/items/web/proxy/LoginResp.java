package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginResp {
    public final String Id;
    public final String server;

    public LoginResp(String Id, String server) {
        this.Id = Id;
        this.server = server;
    }

    public LoginResp(JSONObject json) {
        Id = json.getString("id");
        server = json.getString("server");
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", Id);
        jsonObject.put("server", server);
        return jsonObject;
    }
}
