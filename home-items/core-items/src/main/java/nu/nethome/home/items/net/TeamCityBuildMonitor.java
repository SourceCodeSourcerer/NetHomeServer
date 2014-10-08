/**
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

package nu.nethome.home.items.net;

/*
 * Copyright (C) 2012 Stefan Stromberg
 * 
 * Project: HomeManager
 *  
 * This source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 */


import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * TeamCityBuildMonitor
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class TeamCityBuildMonitor extends HomeItemAdapter implements HomeItem {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAIL_STATUS = "ERROR";
    private static final String BUILD_TYPES_URL_FRAGMENT = "/httpAuth/app/rest/buildTypes/id:";
    private static final String BUILDS_URL_FRAGMENT = "/builds/";


    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"TeamCityBuildMonitor\" Category=\"Ports\" >"
            + "  <Attribute Name=\"LatestResult\" Type=\"String\" Get=\"getLatestResult\"  Default=\"true\" />"
            + "  <Attribute Name=\"Status\" Type=\"String\" Get=\"getStatus\"  Init=\"setStatus\" />"
            + "  <Attribute Name=\"restURL\" Type=\"String\" Get=\"getrestURL\" 	Set=\"setrestURL\" />"
            + "  <Attribute Name=\"Builds\" Type=\"String\" Get=\"getBuilds\" 	Set=\"setBuilds\" />"
            + "  <Attribute Name=\"Username\" Type=\"String\" Get=\"getUsername\" 	Set=\"setUsername\" />"
            + "  <Attribute Name=\"Password\" Type=\"Password\" Get=\"getPassword\" 	Set=\"setPassword\" />"
            + "  <Attribute Name=\"SuccessAction\" Type=\"Command\" Get=\"getSuccessAction\" 	Set=\"setSuccessAction\" />"
            + "  <Attribute Name=\"FailAction\" Type=\"Command\" Get=\"getFailAction\" 	Set=\"setFailAction\" />"
            + "  <Attribute Name=\"InconclusiveAction\" Type=\"Command\" Get=\"getInconclusiveAction\" 	Set=\"setInconclusiveAction\" />"
            + "  <Action Name=\"CheckNow\" 	Method=\"checkAllBuildStatus\" />"
            + "  <Action Name=\"Activate\" 	Method=\"doActivate\" />"
            + "  <Action Name=\"Deactivate\" 	Method=\"doDeactivate\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(TeamCityBuildMonitor.class.getName());
    protected CommandLineExecutor executor;
    private boolean started = false;

    // Public attributes
    protected String latestResult = "";
    protected boolean activated = true;
    protected String restURL = "http://teamcity.hq.assaabloy.org/TeamCity";
    protected String builds = "bt1385";
    protected String successAction = "";
    protected String failAction = "";
    protected String inconclusiveAction = "";
    protected String username = "aastst";
    protected String password = "";

    public TeamCityBuildMonitor() {
    }

    /* Activate the instance
  * @see ssg.home.HomeItem#activate()
  */
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        started = true;
    }


    /* (non-Javadoc)
      * @see ssg.home.HomeItem#receiveEvent(ssg.home.Event)
      */
    public boolean receiveEvent(Event event) {
        if (event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(HomeService.MINUTE_EVENT_TYPE) && activated && started) {
            checkAllBuildStatus();
            return true;
        }
        return false;
    }

    public void checkAllBuildStatus() {
        List<String> builds = getBuildsAsList();

        for (String build : builds) {
            StringBuilder url = new StringBuilder(restURL);
            url.append(BUILD_TYPES_URL_FRAGMENT).append(build).append(BUILDS_URL_FRAGMENT);
            latestResult = "";
            try {
                latestResult = parseLatestBuildStatus(restGet(url.toString(), username, password));
                if (!latestResult.equalsIgnoreCase(SUCCESS_STATUS)) {
                    latestResult = "Error: " + build;
                    performCommand(failAction);
                    return;
                }
            } catch (Exception e) {
                latestResult = "Inconclusive: " + build;
                performCommand(inconclusiveAction);
                return;
            }
        }
        performCommand(successAction);
    }

    public String parseLatestBuildStatus(String builsStatusXML) throws Exception {
        Document document = parseXMLDocument(builsStatusXML);
        Node buildsNode = document.getFirstChild();
        NodeList builds = buildsNode.getChildNodes();
        return extractFirstBuildResult(builds);
    }

    private String extractFirstBuildResult(NodeList builds) throws Exception {
        String result = "";
        for (int i = 0 - 1; i < builds.getLength(); i++) {
            Node currentNode = builds.item(i);
            if (currentNode.getLocalName() != null && currentNode.getLocalName().equals("build")) {
                Node statusNode = currentNode.getAttributes().getNamedItem("status");
                if (statusNode != null) {
                    result = statusNode.getNodeValue();
                    break;
                } else {
                    throw new Exception("Could not find build status");
                }
            }
        }
        return result;
    }

    private Document parseXMLDocument(String builsStatusXML) throws Exception {
        DOMParser parser = new DOMParser();
        ByteArrayInputStream byteStream;
        try {
            byteStream = new ByteArrayInputStream(builsStatusXML.getBytes("UTF-8"));
            InputSource source = new InputSource(byteStream);
            parser.parse(source);
        } catch (UnsupportedEncodingException e1) {
            throw new Exception("Could not decode XML", e1);
        }
        return parser.getDocument();
    }

    protected void performCommand(String commandString) {
        String result = executor.executeCommandLine(commandString);
        if (!result.startsWith("ok")) {
            logger.warning(result);
        }
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return MODEL;
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        super.stop();
        started = false;
    }

    /**
     * @return Returns the restURL.
     */
    public String getrestURL() {
        return restURL;
    }

    /**
     * @param restURL The restURL to set.
     */
    public void setrestURL(String restURL) {
        this.restURL = restURL;
    }

    /**
     * @return Returns the failAction.
     */
    public String getFailAction() {
        return failAction;
    }

    /**
     * @param FailAction The failAction to set.
     */
    public void setFailAction(String FailAction) {
        failAction = FailAction;
    }

    /**
     * @return Returns the successAction.
     */
    public String getSuccessAction() {
        return successAction;
    }

    /**
     * @param SuccessAction The successAction to set.
     */
    public void setSuccessAction(String SuccessAction) {
        successAction = SuccessAction;
    }

    /**
     * @return Returns the inconclusiveAction.
     */
    public String getInconclusiveAction() {
        return inconclusiveAction;
    }

    /**
     * @param InconclusiveAction The inconclusiveAction to set.
     */
    public void setInconclusiveAction(String InconclusiveAction) {
        inconclusiveAction = InconclusiveAction;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param Username The username to set.
     */
    public void setUsername(String Username) {
        username = Username;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getLatestResult() {
        return latestResult;
    }

    public String getBuilds() {
        return builds;
    }

    public List<String> getBuildsAsList() {
        return Arrays.asList(builds.split(","));
    }

    public void setBuilds(String builds) {
        this.builds = builds;
    }

    public String getStatus() {
        return activated ? "Active" : "Deactivated";
    }

    public void setStatus(String status) {
        this.activated = status.equalsIgnoreCase("Active");
    }

    public void doDeactivate() {
        activated = false;
    }

    public void doActivate() {
        activated = true;
    }

    public String restGet(String url, String username, String password) throws Exception {
        HttpURLConnection connection = null;
        OutputStreamWriter wr;
        BufferedReader rd;
        StringBuilder sb = new StringBuilder();
        String line;
        URL serverAddress;

        try {
            serverAddress = new URL(url);

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/xml");

            if (username != null && username.length() > 0 && password != null) {
                String userPassword = username + ":" + password;
                String encodedAuthorization = new org.apache.commons.codec.binary.Base64().encodeAsString(userPassword.getBytes());
                connection.setRequestProperty("Authorization", "Basic " +
                        encodedAuthorization);
            }

            connection.connect();

            if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
                throw new Exception("Bad HTTP response code: " + connection.getResponseCode());
            }

            //read the result from the server
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return sb.toString();
    }
}



