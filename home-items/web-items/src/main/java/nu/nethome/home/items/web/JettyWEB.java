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

package nu.nethome.home.items.web;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.items.web.rest.HomeServices;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JettyWEB
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("GUI")
public class JettyWEB extends HomeItemAdapter implements HomeItem, HomeWebServer {

    public static class HelloServlet extends HttpServlet {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello SimpleServlet</h1>");
            response.getWriter().println("session=" + request.getSession(true).getId());
            response.getWriter().println("Context Path: " + request.getRequestURI());
        }
    }

    protected class Registration {
        public Registration(String url, Servlet s) {
            URL = url;
            servlet = s;
        }

        public String URL;
        public Servlet servlet;
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"JettyWEB\" Category=\"GUI\" StartOrder=\"7\" >"
            + "  <Attribute Name=\"Port\" Type=\"String\" Get=\"getPort\" 	Set=\"setPort\" Default=\"true\" />"
            + "  <Attribute Name=\"MediaDirectory\" Type=\"String\" Get=\"getMediaDirectory\" 	Set=\"setMediaDirectory\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(JettyWEB.class.getName());
    protected Server WebServer;
    protected List<Registration> externalServlets = new LinkedList<Registration>();
    protected boolean isRunning = false;
    private String mediaDirectory = "../media";
    Context applicationsContext;

    // Public attributes
    protected int connectionPortNumber = 8080;

    public JettyWEB() {
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return MODEL;
    }

    /* Activate the instance
      * @see ssg.home.HomeItem#activate()
      */
    public void activate(HomeService server) {
        super.activate(server);
        try {
            WebServer = new Server(connectionPortNumber);

            // Create a collection of context handlers
            ContextHandlerCollection contexts = new ContextHandlerCollection();
            WebServer.setHandler(contexts);

   			// Create a default context for file access using the DefaultServlet
			Context defaultContext = new Context(contexts,"/media",Context.SESSIONS);
			defaultContext.setResourceBase(mediaDirectory);
			ServletHolder holder = new ServletHolder();
			holder.setInitParameter("dirAllowed", "false");
			holder.setServlet(new DefaultServlet());
			defaultContext.addServlet(holder, "/");

            // Create an application Servlet context and add the test servlet
            applicationsContext = new Context(contexts, "/", Context.SESSIONS);
            applicationsContext.addServlet(new ServletHolder(new HelloServlet()), "/test/*");

            // Create the resource servlet which supplies files
            ResourceServlet resourceHandler = new ResourceServlet("/web", "nu/nethome/home/items/web");
            applicationsContext.addServlet(new ServletHolder(resourceHandler), resourceHandler.getPathSpecification());

            // Create a graph Servlet
            applicationsContext.addServlet(new ServletHolder(new GraphServlet(server)), "/Graph");

            // Create rest Servlet
            HomeServices.setServer(server);
            ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
            servletHolder.setInitParameter("javax.ws.rs.Application", "nu.nethome.home.items.web.rest.HomeServices");
            applicationsContext.addServlet(servletHolder, "/rest/*");
            // applicationsContext.addFilter()
            // http://stackoverflow.com/questions/14390577/how-to-add-servlet-filter-with-embedded-jetty

            // Add all externally registered servlets
            for (Registration externalServlet : externalServlets) {
                applicationsContext.addServlet(new ServletHolder(externalServlet.servlet), externalServlet.URL);
            }

            WebServer.start();
            isRunning = true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to start " + name, e);
        }
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        try {
            WebServer.stop();
            WebServer.join();
            isRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerServlet(String URL, Servlet s) {
        externalServlets.add(new Registration(URL, s));
        if (isRunning) {
            applicationsContext.addServlet(new ServletHolder(s), URL);
        }
    }


    /**
     * @return Returns the m_Port.
     */
    public String getPort() {
        return Integer.toString(connectionPortNumber);
    }

    /**
     * @param Port The m_Port to set.
     */
    public void setPort(String Port) {
        connectionPortNumber = Integer.parseInt(Port);
    }

    public String getMediaDirectory() {
        return mediaDirectory;
    }

    public void setMediaDirectory(String mediaDirectory) {
        this.mediaDirectory = mediaDirectory;
    }
}


