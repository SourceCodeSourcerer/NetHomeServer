package nu.nethome.home.items.web.proxy;

import nu.nethome.home.impl.LocalHomeItemProxy;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HomeCloudConnectionTest {
    private static final String ACCOUNT = "ACCOUNT";
    private static final String KEY = "KEY";
    private static final String URL = "https://foo.fie/fum";
    private static final String SESSION_KEY = "asdfwqer";
    private static final int ACCOUNT_ID = 17;
    private static final String SERVER_ID = "SERVER_ID";
    private static final String POLL_URL = String.format(HomeCloudConnection.CLOUD_POLL_RESOURCE, SERVER_ID);
    private static final String USER_PASSWORD = "USER_PASSWORD";
    private static final String LOCAL_URL = "/foo/fum";
    private static final String SESSION_TOKEN = "453245-3453245-3453245";
    private static final String LOCAL_RESPONSE_BODY = "<html>BODY</html>";
    private HomeCloudConnection homeCloudConnection;
    private LocalHomeItemProxy proxy;
    private JsonRestClient restClient;
    private JSONResponse unauthorisedResponse;
    private ArgumentCaptor<JSONObject> jsonCaptor;
    private JSONResponse validLoginHttpRequest;

    @Before
    public void setUp() throws Exception {
        homeCloudConnection = spy(new HomeCloudConnection());
        doReturn(null).when(homeCloudConnection).performLocalRequest(any(HttpRequest.class));
        proxy = new LocalHomeItemProxy(homeCloudConnection);
        proxy.setAttributeValue("Account", ACCOUNT);
        proxy.setAttributeValue("AccountKey", KEY);
        proxy.setAttributeValue("ServiceURL", URL);
        proxy.setAttributeValue("UserPassword", USER_PASSWORD);
        restClient = mock(JsonRestClient.class);
        homeCloudConnection.jsonRestClient = restClient;
        homeCloudConnection.isRunning = true;
        unauthorisedResponse = new JSONResponse("", HttpURLConnection.HTTP_UNAUTHORIZED);
        doReturn(HttpResponse.empty()).when(homeCloudConnection).performLocalRequest(any(HttpRequest.class));
        jsonCaptor = ArgumentCaptor.forClass(JSONObject.class);
        String expectedHashString = calculatePasswordHash();
        validLoginHttpRequest = new JSONResponse(new HttpRequest("", new String[0], expectedHashString, "").toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(SESSION_TOKEN).when(homeCloudConnection).generateSessionToken();
    }

    @Test(expected = HomeCloudConnection.ConnectionException.class)
    public void throwsExceptionWhenLoginFailes() throws Exception {
        JSONResponse response = new JSONResponse("", HttpURLConnection.HTTP_UNAUTHORIZED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        homeCloudConnection.connectAndProxyCloudRequests();
    }

    @Test
    public void logsInToCloudUsingSpecifiedCredentials() throws Exception {
        doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        try {
            homeCloudConnection.connectAndProxyCloudRequests();
        } catch (Exception e) {

        }
        verify(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), jsonCaptor.capture(), eq(""));
        assertThat(jsonCaptor.getValue().get("account").toString(), is(ACCOUNT));
        assertThat(jsonCaptor.getValue().get("password").toString(), is(KEY));
    }

    @Test
    public void pollsForRequestWithSessionTokenAfterLogin() throws Exception {
        loginToCloud();
        verify(restClient).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));
    }

    private void loginToCloud() throws IOException {
        LoginResp resp = new LoginResp(SESSION_KEY, SERVER_ID);
        JSONResponse response = new JSONResponse(resp.toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        try {
            homeCloudConnection.connectAndProxyCloudRequests();
        } catch (Exception e) {
            System.out.print(e.toString());
        }
        verify(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
    }

    @Test
    public void rejectsProxyRequestWithoutClientLogin() throws Exception {
        JSONResponse response = new JSONResponse(new HttpRequest(LOCAL_URL, new String[0], "", "").toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(response).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));

        loginToCloud();

        verify(restClient, times(2)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));    // Polls cloud twice
        verify(homeCloudConnection, times(0)).performLocalRequest(any(HttpRequest.class));                  // Performs no local requests
    }

    @Test
    public void rejectsInvalidLoginToProxy() throws Exception {
        proxy.setAttributeValue("UserPassword", "ANOTHER_PASSWORD");
        doReturn(validLoginHttpRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));

        loginToCloud();

        verify(restClient, times(2)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));    // Polls cloud twice
        verify(homeCloudConnection, times(0)).performLocalRequest(any(HttpRequest.class));                  // Performs no local requests
        assertThat(jsonCaptor.getAllValues().get(1).get("sessionToken").toString(), is(""));
        assertThat(jsonCaptor.getAllValues().get(1).get("challenge").toString(), is(homeCloudConnection.currentChallenge));
    }

    @Test
    public void invalidLoginChangesChallenge() throws Exception {
        proxy.setAttributeValue("UserPassword", "ANOTHER_PASSWORD");
        doReturn(validLoginHttpRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));
        String oldChallenge = homeCloudConnection.currentChallenge;

        loginToCloud();

        verify(restClient, times(2)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));    // Polls cloud twice
        String newChallenge = jsonCaptor.getAllValues().get(1).get("challenge").toString();
        assertThat(newChallenge.length(), greaterThan(10));
        assertThat(newChallenge, not(is(oldChallenge)));
    }

    @Test
    public void acceptsValidLoginToProxy() throws Exception {
        doReturn(validLoginHttpRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));

        loginToCloud();

        verify(restClient, times(2)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));    // Polls cloud twice
        verify(homeCloudConnection, times(0)).performLocalRequest(any(HttpRequest.class));                  // Performs no local requests
        assertThat(jsonCaptor.getAllValues().get(1).get("sessionToken").toString().length(), greaterThan(10));
    }

    private String calculatePasswordHash() throws NoSuchAlgorithmException {
        String expectedCredential = ACCOUNT + USER_PASSWORD + homeCloudConnection.currentChallenge;
        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(expectedCredential.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }

    @Test
    public void validLoginToProxyChangesChallange() throws Exception {
        doReturn(validLoginHttpRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));
        String oldChallenge = homeCloudConnection.currentChallenge;

        loginToCloud();

        verify(restClient, times(2)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));    // Polls cloud twice
        String newChallenge = jsonCaptor.getAllValues().get(1).get("challenge").toString();
        assertThat(newChallenge.length(), greaterThan(10));
        assertThat(newChallenge, not(is(oldChallenge)));
    }

    static final String headers[] = {"UserAgent:Internet Explorer"};

    @Test
    public void forwardsProxyRequestWhenCorrectSession() throws Exception {
        JSONResponse proxyRequest = new JSONResponse(new HttpRequest(LOCAL_URL, headers, "", SESSION_TOKEN).toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(validLoginHttpRequest).doReturn(proxyRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));

        loginToCloud();

        verify(restClient, times(3)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(homeCloudConnection, times(1)).performLocalRequest(captor.capture());
        assertThat(captor.getValue().headers, is(headers));
        assertThat(captor.getValue().url, is(LOCAL_URL));
    }

    @Test
    public void returnsResultFromProxyRequestWhenCorrectSession() throws Exception {
        JSONResponse proxyRequest = new JSONResponse(new HttpRequest(LOCAL_URL, headers, "", SESSION_TOKEN).toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(validLoginHttpRequest).doReturn(proxyRequest).doReturn(unauthorisedResponse).when(restClient).post(eq(URL), eq(POLL_URL), any(JSONObject.class), eq(SESSION_KEY));
        HttpResponse localResponse = new HttpResponse(LOCAL_RESPONSE_BODY, headers, "");
        doReturn(localResponse).when(homeCloudConnection).performLocalRequest(any(HttpRequest.class));

        loginToCloud();

        verify(restClient, times(3)).post(eq(URL), eq(POLL_URL), jsonCaptor.capture(), eq(SESSION_KEY));
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(homeCloudConnection, times(1)).performLocalRequest(captor.capture());
        assertThat(jsonCaptor.getAllValues().get(2).get("body").toString(), is(LOCAL_RESPONSE_BODY));
        assertThat(jsonCaptor.getAllValues().get(2).getJSONArray("headers").get(0).toString(), is(headers[0]));
        assertThat(jsonCaptor.getAllValues().get(2).get("challenge").toString(), is(""));
    }
}