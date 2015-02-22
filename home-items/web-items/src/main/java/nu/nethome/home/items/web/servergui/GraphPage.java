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

package nu.nethome.home.items.web.servergui;

import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GraphPage extends PortletPage {

    public static final int HOURS_24_IN_MS = 24 * 60 * 60 * 1000;
    public static final int DAYS_IN_WEEK = 7;
    public static final String LOG_REST_URL = "/rest/items/%s/log?start=%s&stop=%s";

    private class GraphPageArguments extends HomeGUIArguments {

        private Date startDate;
        private String range = "day";

        public GraphPageArguments(HttpServletRequest req) {
            super(req);
            String start = req.getParameter("start");
            if (start != null) {
                try {
                    startDate = logDateFormat.parse(start);
                } catch (ParseException e) {
                    // Ok
                }
            }
            if (startDate == null) {
                startDate = new Date();
            }
            String rangeParameter = req.getParameter("range");
            if (rangeParameter != null) {
                range = rangeParameter;
            }
        }

        public Date getStartDate() {
            return startDate;
        }

        public String getRange() {
            return range;
        }
    }

    private final HomeService server;
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");


    public GraphPage(String mLocalURL, HomeService server) {
        super(mLocalURL);
        this.server = server;
    }

    @Override
    public List<String> getCssFileNames() {
        return Arrays.asList("web/home/js/jquery.jqplot.min.css");
    }

    @Override
    public List<String> getJavaScriptFileNames() {
        return Arrays.asList("web/home/js/jquery-1.4.3.min.js",
                "web/home/js/jquery.jqplot.min.js",
                "web/home/js/plugins/jqplot.canvasTextRenderer.min.js",
                "web/home/js/plugins/jqplot.categoryAxisRenderer.min.js",
                "web/home/js/plugins/jqplot.cursor.min.js",
                "web/home/js/plugins/jqplot.dateAxisRenderer.min.js");
    }

    @Override
    public String getPageNameURL() {
        return "graphs";
    }

    public String getPageName() {
        return null; //"Graphs";
    }

    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        PrintWriter p = res.getWriter();
        GraphPageArguments pageArguments = new GraphPageArguments(req);
        p.println("<!--[if IE]><script language=\"javascript\" type=\"text/javascript\" src=\"web/home/js/excanvas.min.js\"></script><![endif]-->");
        p.println("<script type=\"text/javascript\" src=\"web/home/graph.js\"></script>");
        if (pageArguments.hasName()) {
            p.println("<div id=\"chart1\">Loading graph data, please wait...</div>");
            HomeItemProxy item = server.openInstance(pageArguments.getName());
            if (item == null) {
                return;
            }
            String graph_title;
            if (pageArguments.getRange().equals("week")) {
                graph_title = printWeekGraph(p, pageArguments, item);
            } else if (pageArguments.getRange().equals("month")) {
                graph_title = printMonthGraph(p, pageArguments, item);
            } else {
                graph_title = printDayGraph(p, pageArguments, item);
            }
        }
    }

    private String printWeekGraph(PrintWriter p, GraphPageArguments pageArguments, HomeItemProxy item) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(pageArguments.getStartDate());
        Date currentTime = pageArguments.getStartDate();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date startOfWeek = cal.getTime();
        Date previousWeek = new Date(startOfWeek.getTime() - HOURS_24_IN_MS * DAYS_IN_WEEK);
        Date nextWeek = new Date(startOfWeek.getTime() + HOURS_24_IN_MS * DAYS_IN_WEEK);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = new Date(cal.getTime().getTime() + HOURS_24_IN_MS * (DAYS_IN_WEEK - 1));
        String graphTitle = String.format("%s, Week starting %2$tA %2$tF", item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE), startOfWeek);
        printParameter(p, "graph_title", graphTitle);
        printParameter(p, "jsonurl", String.format(LOG_REST_URL,
                item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                logDateFormat.format(startOfWeek),
                logDateFormat.format(endOfDay)));
        printParameter(p, "tick_format", "%a %R");

        printGraphNavigationPanel(p, item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                startOfWeek, "week",
                "day", "Day", "month", "Month",
                previousWeek, nextWeek, currentTime);
        return graphTitle;
    }

    private void printGraphNavigationPanel(PrintWriter p, String itemName,
                                           Date periodStartDate, String range,
                                           String range1, String rangeLabel1, String range2, String rangeLabel2,
                                           Date previousDate,
                                           Date nextDate, Date selectedDate) {
        HomeUrlBuilder previousUrl = new HomeUrlBuilder(localURL)
                .addParameter("page", getPageNameURL())
                .addParameter("name", itemName)
                .addParameter("range", range)
                .addParameter("start", logDateFormat.format(previousDate));
        HomeUrlBuilder nextUrl = new HomeUrlBuilder(localURL)
                .addParameter("page", getPageNameURL())
                .addParameter("name", itemName)
                .addParameter("range", range)
                .addParameter("start", logDateFormat.format(nextDate));
        HomeUrlBuilder dayUrl = new HomeUrlBuilder(localURL)
                .addParameter("page", getPageNameURL())
                .addParameter("name", itemName)
                .addParameter("range", "day")
                .addParameter("start", logDateFormat.format(selectedDate));
        HomeUrlBuilder weekUrl = new HomeUrlBuilder(localURL)
                .addParameter("page", getPageNameURL())
                .addParameter("name", itemName)
                .addParameter("range", "week")
                .addParameter("start", logDateFormat.format(selectedDate));
        HomeUrlBuilder monthUrl = new HomeUrlBuilder(localURL)
                .addParameter("page", getPageNameURL())
                .addParameter("name", itemName)
                .addParameter("range", "month")
                .addParameter("start", logDateFormat.format(selectedDate));
        p.print("<div class=\"graphPanel\">");
        String dayClass = range.equals("day") ? "class=\"selected\" " :  "";
        String weekClass = range.equals("week") ? "class=\"selected\" " :  "";
        String monthClass = range.equals("month") ? "class=\"selected\" " :  "";
        p.print(String.format("<a id=\"prev\" href=\"%s\"><span>%s</span></a>", previousUrl.toString(), "Previous"));
        p.print(String.format("<a " + dayClass + " href=\"%s\"><span>%s</span></a>", dayUrl.toString(), "Day"));
        p.print(String.format("<a " + weekClass + " href=\"%s\"><span>%s</span></a>", weekUrl.toString(), "Week"));
        p.print(String.format("<a " + monthClass + " href=\"%s\"><span>%s</span></a>", monthUrl.toString(), "Month"));
        p.println(String.format("<a id=\"next\" href=\"%s\"><span>%s</span></a>", nextUrl.toString(), "Next"));
        p.println("</div>");
    }

    private String printMonthGraph(PrintWriter p, GraphPageArguments pageArguments, HomeItemProxy item) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(pageArguments.getStartDate());
        Date currentTime = pageArguments.getStartDate();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = cal.getTime();
        cal.add(Calendar.HOUR, 24 * 35);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date nextMonth = cal.getTime();
        cal.add(Calendar.MINUTE, -1);
        Date endOfMonth = cal.getTime();
        cal.add(Calendar.MINUTE, 1);
        cal.add(Calendar.HOUR, -24 * 35);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date previousMonth = cal.getTime();
        String graphTitle = String.format("%s, %2$tB %2$tY", item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE), currentTime);
        printParameter(p, "graph_title", graphTitle);
        printParameter(p, "jsonurl", String.format(LOG_REST_URL,
                item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                logDateFormat.format(startOfMonth),
                logDateFormat.format(endOfMonth)));
        printParameter(p, "tick_format", "%a %#d");

        printGraphNavigationPanel(p, item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                startOfMonth, "month",
                "day", "Day", "week", "Week",
                previousMonth, nextMonth, currentTime);
        return graphTitle;
    }

    private String printDayGraph(PrintWriter p, GraphPageArguments pageArguments, HomeItemProxy item) {
        // Calculate time window
        Calendar cal = Calendar.getInstance();
        cal.setTime(pageArguments.getStartDate());
        Date currentTime = pageArguments.getStartDate();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startOfDay = cal.getTime();
        Date previousDay = new Date(startOfDay.getTime() - HOURS_24_IN_MS);
        Date nextDay = new Date(startOfDay.getTime() + HOURS_24_IN_MS);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endOfDay = cal.getTime();
        String graphTitle = String.format("%s %2$tA %2$tF", item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE), currentTime);
        printParameter(p, "graph_title", graphTitle);
        printParameter(p, "jsonurl", String.format(LOG_REST_URL,
                item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                logDateFormat.format(startOfDay),
                logDateFormat.format(endOfDay)));
        printParameter(p, "tick_format", "%R");

        printGraphNavigationPanel(p, item.getAttributeValue(HomeItemProxy.ID_ATTRIBUTE),
                startOfDay, "day",
                "week", "Week", "month", "Month",
                previousDay, nextDay, currentTime);
        return graphTitle;
    }

    private void printParameter(PrintWriter p, String parameterName, String parameterValue) {
        p.println(String.format("<script>var %s=\"%s\";</script>", parameterName, parameterValue));
    }
}
