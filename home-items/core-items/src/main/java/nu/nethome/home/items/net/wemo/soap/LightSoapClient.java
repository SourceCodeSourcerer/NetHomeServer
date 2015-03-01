package nu.nethome.home.items.net.wemo.soap;

import org.w3c.dom.Node;

import javax.xml.soap.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class LightSoapClient {
    public static final int DEFAULT_CONNECT_TIMEOUT = 700;
    public static final int DEFAULT_READ_TIMEOUT = 700;

    private static Logger logger = Logger.getLogger(LightSoapClient.class.getName());
    private int connectionTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    public LightSoapClient() {
    }

    public LightSoapClient(int connectionTimeout, int readTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
    }

    public Map<String, String> sendRequest(String nameSpace, String serverURI, String method, List<Argument> arguments) throws SOAPException, IOException {
        final String ns = "u";
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(ns, nameSpace);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(method, ns);
        for (Argument argument : arguments) {
            SOAPElement soapBodyElem1 = soapBodyElem.addChildElement(argument.name);
            soapBodyElem1.addTextNode(argument.value);
        }
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPACTION", "\"" + nameSpace + "#" + method + "\"");
        headers.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
        soapMessage.saveChanges();
        long startTime = System.currentTimeMillis();
        SOAPMessage response = sendRequest(serverURI, soapMessage);
        logger.info(String.format("Soap request to: %s took %d mS", serverURI, System.currentTimeMillis() - startTime));
        Map<String, String> result = new HashMap<>();
        SOAPBody soapBody1 = response.getSOAPBody();
        Iterator childElements = soapBody1.getChildElements();
        while (childElements.hasNext()) {
            Object element = childElements.next();
            if (element instanceof SOAPElement) {
                SOAPElement methodResponseElement = (SOAPElement) element;
                for (int i = 0; i < methodResponseElement.getChildNodes().getLength(); i++) {
                    Node parameterNode = methodResponseElement.getChildNodes().item(i);
                    if (parameterNode.getNodeType() == Node.ELEMENT_NODE && parameterNode.getChildNodes().getLength() == 1) {
                        result.put(parameterNode.getLocalName(), parameterNode.getChildNodes().item(0).getTextContent());
                    }
                }
            }
        }
        return result;
    }

    private SOAPMessage sendRequest(String url, SOAPMessage request) throws SOAPException, IOException {
        request.writeTo(System.out);
        SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
        URL endpoint = new URL(new URL(url), "", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                URL target = new URL(url.toString());
                URLConnection connection = target.openConnection();
                connection.setConnectTimeout(connectionTimeout);
                connection.setReadTimeout(readTimeout);
                return (connection);
            }
        });
        SOAPMessage soapResponse = soapConnection.call(request, endpoint);
        soapResponse.writeTo(System.out);
        soapConnection.close();
        return soapResponse;
    }

    public static class Argument {
        public final String name;
        public final String value;

        public Argument(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Argument argument = (Argument) o;

            if (!name.equals(argument.name)) return false;
            if (!value.equals(argument.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }
}