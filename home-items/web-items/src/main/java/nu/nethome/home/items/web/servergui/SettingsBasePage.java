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

import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SettingsBasePage extends PortletPage {

    private final HomeService server;
    private List<PortletPage> subpages = new ArrayList<PortletPage>();

    public SettingsBasePage(String mLocalURL, HomeService server, EditPermission editPermission) {
        super(mLocalURL);
        this.server = server;
        subpages.add(new EditSettingsPage(mLocalURL, server, editPermission));
        subpages.add(new LogPage(mLocalURL, server));
        subpages.add(new CodersPage(mLocalURL, server));
    }

    @Override
    public String getPageNameURL() {
        return "settings";
    }

    public String getPageName() {
        return "Settings";
    }

    /**
     * This is the main entrance point of the class. This is called when a http
     * request is routed to this servlet.
     */
    public void printPage(HttpServletRequest req, HttpServletResponse res,
                          HomeService server) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        PrintWriter p = res.getWriter();
        HomeGUIArguments pageArguments = new HomeGUIArguments(req);

        p.println("<div class=\"itemcolumn menu\">");
        PortletPage page = chooseContentPage(pageArguments);
        printMenuPanel(p, page);
        p.println("</div>");
        page.printPage(req, res, server);
    }

    private void printMenuPanel(PrintWriter p, PortletPage selectedPage) {
        printMenuPanelStart(p, "");
        p.println("<ul>");
        for (PortletPage subpage : subpages) {
            printMenuItem(p, subpage, selectedPage);
        }
        p.println("</ul>");
        printMenuPanelEnd(p);
    }

    private void printMenuItem(PrintWriter p, PortletPage page, PortletPage selectedPage) {
        String usedIconPath = page.getIconUrl() != null ? page.getIconUrl() : "/web/home/gauge.png";
        String link = this.localURL + "?page=" + getPageNameURL() + "&subpage=" + page.getPageNameURL();
        String pageName = page.getPageName() != null ? page.getPageName() : "&nbsp;";
        if (page.getPageNameURL().equals(selectedPage.getPageNameURL())) {
            p.println("<li class=\"selected\">");
        } else {
            p.println("<li>");
        }
        p.println(" <ul>");
        p.println("  <li><a href=\"" + link + "\"><img src=\"" + usedIconPath + "\"></a></li>");
        p.println("<li>" + pageName + "</li>");
        p.println(" </ul>");
        p.println("</li>");
    }

    private PortletPage chooseContentPage(HomeGUIArguments pageArguments) throws ServletException, IOException {
        PortletPage page = subpages.get(0);
        for (PortletPage subpage : subpages) {
            if (pageArguments.hasSubpage() && subpage.getPageNameURL().equals(pageArguments.getSubpage())) {
                page = subpage;
            }
        }
        return page;
    }

    protected void printMenuPanelStart(PrintWriter p, String header) {
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader thin\">");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>" + header + "</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");

    }

    private void printMenuPanelEnd(PrintWriter p) {
        p.println("        <div class=\"footer thin\"></div>");
        p.println("    </div>");
    }
}
