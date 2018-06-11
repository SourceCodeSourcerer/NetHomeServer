/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 * <p>
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 * <p>
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.web.proxy;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;


@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class HomeCloudConnection extends HomeItemAdapter implements Runnable, HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HomeCloudConnection\" Category=\"Ports\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"ServiceURL\" Type=\"String\" Get=\"getServiceURL\" Set=\"setServiceURL\" />"
            + "  <Attribute Name=\"LocalURL\" Type=\"String\" Get=\"getLocalURL\" Set=\"setLocalURL\" />"
            + "  <Attribute Name=\"Account\" Type=\"String\" Get=\"getAccount\" Set=\"setAccount\" />"
            + "  <Attribute Name=\"AccountKey\" Type=\"String\" Get=\"getAccountKey\" Set=\"setAccountKey\" />"
            + "  <Attribute Name=\"ServerNumber\" Type=\"String\" Get=\"getServerNumber\" Set=\"setServerNumber\" />"
            + "  <Attribute Name=\"ServerName\" Type=\"String\" Get=\"getServerName\" Set=\"setServerName\" />"
            + "  <Attribute Name=\"UserPassword\" Type=\"String\" Get=\"getPassword\" Set=\"setPassword\" />"
            + "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
            + "  <Action Name=\"Reconnect\" 	Method=\"reconnect\" />"
            + "</HomeItem> ");

    private static final int RETRY_INTERVAL_MS = 5000;
    static final String LOGIN_RESOURCE = "api/server-sessions";
    static final String CLOUD_POLL_RESOURCE = "api/servers/%s/poll";
    private static final String CLOUD_ACCOUNT = "Cloud-Account";
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    private static final int MAX_LOGIN_ATTEMPTS_LOCKOUT_MS = 2 * 60 * 1000;

    private String serviceURL = "https://cloud.opennethome.org/";
    private String localURL = "http://127.0.0.1:8020/";
    private String password = "";
    private String account = "0";
    String currentChallenge = UUID.randomUUID().toString();
    private String accountKey = "";
    private int serverNumber = 1;
    private String serverName = "No Name";
    private int messageCount = 0;
    private boolean accountKeyIsBad = false;
    private boolean doReconnect = false;

    private boolean connected = false;
    /*
     * Internal attributes
     */
    private static Logger logger = Logger.getLogger(HomeCloudConnection.class.getName());
    protected Thread listenThread;
    protected boolean isRunning = false;
    JsonRestClient jsonRestClient;
    private String pollResource;
    private String currentSessionToken;
    private int failedLoginAttempts;

    public HomeCloudConnection() {
        account = Integer.toString(new Random().nextInt(10000));
    }

    public String getModel() {
        return MODEL;
    }

    public void activate() {
        jsonRestClient = new JsonRestClient();
        isRunning = true;
        listenThread = new Thread(this, "ProxyListenThread");
        listenThread.start();
    }

    public void stop() {
        isRunning = false;
        super.stop();
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("Logout_Message") && event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).equals(account)) {
            currentSessionToken = "";
            return true;
        }
        return false;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setLocalURL(String localURL) {
        this.localURL = localURL;
    }

    public String getLocalURL() {
        return localURL;
    }

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    public void run() {
        while (isRunning) {
            try {
                connectAndProxyCloudRequests();
            } catch (ConnectionException | IOException | InterruptedException e) {
                logger.fine("Failed Communicating with cloud: " + e);
            }
            connected = false;
            if (isRunning & !doReconnect) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException e1) {
                    return;
                }
            }
            doReconnect = false;
        }
    }

    void connectAndProxyCloudRequests() throws InterruptedException, IOException, ConnectionException {
        if (accountKeyIsBad) {
            Thread.sleep(RETRY_INTERVAL_MS);
        } else {
            final LoginResp loginResp = loginToCloud(new LoginReq(account, accountKey, serverNumber, serverName));
            connected = true;
            HttpResponse lastHttpResponse = HttpResponse.challenge(currentChallenge);
            while (isRunning & !doReconnect) {
                lastHttpResponse = proxyHttpRequest(lastHttpResponse, loginResp.Id);
                if (failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                    logger.warning(Integer.toString(MAX_FAILED_LOGIN_ATTEMPTS) + " failed login attempts to " + this.getName() + ". Going temporary offline");
                    Thread.sleep(MAX_LOGIN_ATTEMPTS_LOCKOUT_MS);
                    failedLoginAttempts = 0;
                }
            }
            connected = false;
        }
    }

    private HttpResponse proxyHttpRequest(HttpResponse previousLocalHttpResponse, String sessionId) throws IOException, ConnectionException {
        final HttpRequest request = postPreviousResponseToCloudAndFetchNewRequest(previousLocalHttpResponse, sessionId);
        HttpResponse nextLocalHttpResponse;
        if (request.isProxyRequest()) {
            nextLocalHttpResponse = proxyIfAuthenticated(request);
        } else if (request.isAuthenticationRequest()) {
            nextLocalHttpResponse = verifyAuthenticationRequest(request.loginCredential);
        } else {
            nextLocalHttpResponse = HttpResponse.empty();
        }
        return nextLocalHttpResponse;
    }

    private HttpResponse proxyIfAuthenticated(HttpRequest request) throws IOException {
        HttpResponse nextLocalHttpResponse;
        if (isAuthenticated(request)) {
            nextLocalHttpResponse = performLocalRequest(request);
            messageCount++;
        } else {
            nextLocalHttpResponse = HttpResponse.unauthorized();
        }
        return nextLocalHttpResponse;
    }

    private boolean isAuthenticated(HttpRequest request) {
        return request.sessionToken.equals(this.currentSessionToken);
    }

    private HttpResponse verifyAuthenticationRequest(String loginCredential) throws ConnectionException {
        HttpResponse httpResponse;
        String expectedCredential = this.account + this.password + currentChallenge;
        updateChallenge();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ConnectionException("Could not get SHA-256");
        }
        byte[] hash = digest.digest(expectedCredential.getBytes(StandardCharsets.UTF_8));
        String expectedHashString = Hex.encodeHexString(hash);
        if (expectedHashString.equals(loginCredential)) {
            currentSessionToken = generateSessionToken();
            httpResponse = HttpResponse.loginSucceeded(currentChallenge, currentSessionToken);
            failedLoginAttempts = 0;
        } else {
            httpResponse = HttpResponse.loginFailed(currentChallenge);
            failedLoginAttempts++;
        }
        return httpResponse;
    }

    String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    private String updateChallenge() {
        currentChallenge = UUID.randomUUID().toString();
        return currentChallenge;
    }

    private LoginResp loginToCloud(LoginReq loginReq) throws IOException, ConnectionException {
        final JSONResponse result = jsonRestClient.post(serviceURL, LOGIN_RESOURCE, loginReq.toJson(), "");
        if (result.getResultCode() == HttpURLConnection.HTTP_CREATED) {
            LoginResp loginResp = new LoginResp(result.getObject());
            pollResource = String.format(CLOUD_POLL_RESOURCE, loginResp.server);
            return loginResp;
        } else if (result.getResultCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            accountKeyIsBad = true;
        }
        throw new ConnectionException("Could not login to cloud server, error code: " + result.getResultCode());
    }

    private HttpRequest postPreviousResponseToCloudAndFetchNewRequest(HttpResponse httpResponse, String sessionId) throws IOException, ConnectionException {
        final JSONResponse result = jsonRestClient.post(serviceURL, pollResource, httpResponse.toJson(), sessionId);
        if (result.getResultCode() == HttpURLConnection.HTTP_CREATED) {
            return new HttpRequest(result.getObject());
        } else if (result.getResultCode() == HttpURLConnection.HTTP_NO_CONTENT) {
            return HttpRequest.empty();
        }
        throw new ConnectionException("Got unexpected return code: " + result.getResultCode() + "from cloud server");
    }

    HttpResponse performLocalRequest(HttpRequest request) throws IOException {
        HttpResponse httpResponse;
        HttpURLConnection connection = (HttpURLConnection) new URL(localURL + request.url).openConnection();
        for (String header : request.headers) {
            String parts[] = header.split(":");
            connection.setRequestProperty(parts[0].trim(), parts[1].trim());
        }
        connection.setRequestMethod(request.method);
        connection.setRequestProperty(CLOUD_ACCOUNT, this.account);
        if (!request.body.isEmpty()) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(Base64.decodeBase64(request.body));
        }
        ByteArrayBuffer baf = new ByteArrayBuffer(50);
        int responseCode = connection.getResponseCode();
        try (InputStream response = responseCode < 300 ? connection.getInputStream() : connection.getErrorStream()) {
            BufferedInputStream bis = new BufferedInputStream(response);
            int read;
            int bufSize = 512;
            byte[] buffer = new byte[bufSize];
            while (true) {
                read = bis.read(buffer);
                if (read == -1) {
                    break;
                }
                baf.append(buffer, 0, read);
            }
        } catch (IOException e) {
            return HttpResponse.empty();
        }

        Map<String, List<String>> map = connection.getHeaderFields();
        List<String> headers = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue().get(0) != null && !entry.getKey().equalsIgnoreCase("Transfer-Encoding")) {
                headers.add(entry.getKey() + ":" + entry.getValue().get(0));
            }
        }
        httpResponse = new HttpResponse(new String(Base64.encodeBase64(baf.toByteArray())), headers.toArray(new String[headers.size()]), "", null, responseCode);
        return httpResponse;
    }

    public String getMessageCount() {
        return String.valueOf(messageCount);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerNumber() {
        return Integer.toString(serverNumber);
    }

    public void setServerNumber(String number) {
        this.serverNumber = Integer.parseInt(number);
    }

    public String getState() {
        if (accountKeyIsBad ) {
            return "Bad account key";
        }
        if (failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            return "Failed login lockout";
        }
        return connected ? "Connected" : "Not Connected";
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
        accountKeyIsBad = false;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        if (!serverName.equals(this.serverName) && connected) {
            doReconnect = true;
        }
        this.serverName = serverName;
    }

    public String reconnect() {
        accountKeyIsBad = false;
        doReconnect = true;
        return "";
    }

    class ConnectionException extends Exception {
        public ConnectionException(String message) {
            super(message);
        }
    }
}

