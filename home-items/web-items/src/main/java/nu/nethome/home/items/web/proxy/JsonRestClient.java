/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class JsonRestClient {

    private static final int READ_TIMEOUT = 60000;

    private static final SSLSocketFactory factory = getLetsEncryptTrustedSocketFactory();

    public JSONResponse get(String baseUrl, String resource, JSONObject argument) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "GET", "");
    }

    public JSONResponse put(String baseUrl, String resource, JSONObject argument) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "PUT", "");
    }

    public JSONResponse post(String baseUrl, String resource, JSONObject argument, String sessionId) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "POST", sessionId);
    }

    private JSONResponse performRequest(String baseUrl, String resource, String body, String method, String sessionId) throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream wr = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();
        String line;
        URL serverAddress;

        try {
            serverAddress = new URL(baseUrl + resource);

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            fixTrustIssue(connection);
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/json");
            if (!sessionId.isEmpty()) {
                connection.setRequestProperty("id", sessionId);
            }
            connection.setUseCaches(false);
            if (body.length() > 0) {
                byte[] data = body.getBytes("UTF8");
                connection.setRequestProperty("Content-Length", Integer.toString(data.length));
                wr = new DataOutputStream(connection.getOutputStream());
                wr.write(data);
                wr.flush();
                wr.close();
                wr = null;
            }
            connection.connect();
            if (connection.getResponseCode() <= 299) {
                rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else if (connection.getErrorStream() != null){
                rd = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            if (rd != null) {
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
            }

        } finally {
            if (rd != null) {
                rd.close();
            }
            if (wr != null) {
                wr.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONResponse(sb.toString(), connection.getResponseCode());
    }

    /**
     * Since the LetsEncrypt CA certificate is currently not among the trusted certificates
     * in the standard java trust store, I have done this fix to allow this certificate to be
     * trusted in this connection.
     *
     * There is a more general solution which I might concider:
     * https://stackoverflow.com/questions/24555890/using-a-custom-truststore-in-java-as-well-as-the-default-one
     *
     * @param connection
     */
    private void fixTrustIssue(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(factory);
        }
    }

    private static SSLSocketFactory getLetsEncryptTrustedSocketFactory() {
        ByteArrayInputStream fis = new ByteArrayInputStream(LetsEcryptCA_X3_pem.getBytes());
        X509Certificate ca = null;
        try {
            ca = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(fis);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry(Integer.toString(1), ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String LetsEcryptCA_X3_pem =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIEkjCCA3qgAwIBAgIQCgFBQgAAAVOFc2oLheynCDANBgkqhkiG9w0BAQsFADA/\n" +
            "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
            "DkRTVCBSb290IENBIFgzMB4XDTE2MDMxNzE2NDA0NloXDTIxMDMxNzE2NDA0Nlow\n" +
            "SjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxIzAhBgNVBAMT\n" +
            "GkxldCdzIEVuY3J5cHQgQXV0aG9yaXR5IFgzMIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
            "AQ8AMIIBCgKCAQEAnNMM8FrlLke3cl03g7NoYzDq1zUmGSXhvb418XCSL7e4S0EF\n" +
            "q6meNQhY7LEqxGiHC6PjdeTm86dicbp5gWAf15Gan/PQeGdxyGkOlZHP/uaZ6WA8\n" +
            "SMx+yk13EiSdRxta67nsHjcAHJyse6cF6s5K671B5TaYucv9bTyWaN8jKkKQDIZ0\n" +
            "Z8h/pZq4UmEUEz9l6YKHy9v6Dlb2honzhT+Xhq+w3Brvaw2VFn3EK6BlspkENnWA\n" +
            "a6xK8xuQSXgvopZPKiAlKQTGdMDQMc2PMTiVFrqoM7hD8bEfwzB/onkxEz0tNvjj\n" +
            "/PIzark5McWvxI0NHWQWM6r6hCm21AvA2H3DkwIDAQABo4IBfTCCAXkwEgYDVR0T\n" +
            "AQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwfwYIKwYBBQUHAQEEczBxMDIG\n" +
            "CCsGAQUFBzABhiZodHRwOi8vaXNyZy50cnVzdGlkLm9jc3AuaWRlbnRydXN0LmNv\n" +
            "bTA7BggrBgEFBQcwAoYvaHR0cDovL2FwcHMuaWRlbnRydXN0LmNvbS9yb290cy9k\n" +
            "c3Ryb290Y2F4My5wN2MwHwYDVR0jBBgwFoAUxKexpHsscfrb4UuQdf/EFWCFiRAw\n" +
            "VAYDVR0gBE0wSzAIBgZngQwBAgEwPwYLKwYBBAGC3xMBAQEwMDAuBggrBgEFBQcC\n" +
            "ARYiaHR0cDovL2Nwcy5yb290LXgxLmxldHNlbmNyeXB0Lm9yZzA8BgNVHR8ENTAz\n" +
            "MDGgL6AthitodHRwOi8vY3JsLmlkZW50cnVzdC5jb20vRFNUUk9PVENBWDNDUkwu\n" +
            "Y3JsMB0GA1UdDgQWBBSoSmpjBH3duubRObemRWXv86jsoTANBgkqhkiG9w0BAQsF\n" +
            "AAOCAQEA3TPXEfNjWDjdGBX7CVW+dla5cEilaUcne8IkCJLxWh9KEik3JHRRHGJo\n" +
            "uM2VcGfl96S8TihRzZvoroed6ti6WqEBmtzw3Wodatg+VyOeph4EYpr/1wXKtx8/\n" +
            "wApIvJSwtmVi4MFU5aMqrSDE6ea73Mj2tcMyo5jMd6jmeWUHK8so/joWUoHOUgwu\n" +
            "X4Po1QYz+3dszkDqMp4fklxBwXRsW10KXzPMTZ+sOPAveyxindmjkW8lGy+QsRlG\n" +
            "PfZ+G6Z6h7mjem0Y+iWlkYcV4PIWL1iwBi8saCbGS5jN2p8M+X+Q7UNKEkROb3N6\n" +
            "KOqkqm57TH2H3eDJAkSnh6/DNFu0Qg==\n" +
            "-----END CERTIFICATE-----";

}
