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

import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.HomeService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Stefan
 * Date: 2012-04-08
 * Time: 15:35
 */
public class EditSettingsPage extends EditItemPage {
    public EditSettingsPage(String mLocalURL, HomeService server, EditPermission editPermission) {
        super(mLocalURL, server, "", null, editPermission);
        pageName = "settings";
    }

    public String getPageName() {
        return "Settings";
    }

    @Override
    public String getIconUrl() {
        return "web/home/preferences32.png";
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
        EditItemArguments pageArguments = new EditItemArguments(req);

        String action = req.getParameter("a");

        HomeItemProxy item = this.server.openInstance(bridgeBrokerId);
        List<HomeItemError> homeItemErrors = new ArrayList<HomeItemError>();

        // Analyze actions which require an instance
        if (action != null) {
            if (action.equals("update_attributes")) {
                String saveType = req.getParameter("save_type");
                if ((saveType != null) && !saveType.equals(CANCEL_BUTTON_TEXT)) {
                    updateAttributes(item, req, pageArguments, homeItemErrors);
                    saveItems(homeItemErrors);
                }
            }
        }

        String name = item.getAttributeValue("Name");
        HomeItemModel model = item.getModel();


        // Print static start of page
        printItemEditColumnStart(p);

        // Print page heading
        printItemHeading(p, name, model);

        List<Attribute> attributes = item.getAttributeValues();

        if (countReadonlyAttributes(attributes) > 0) {
            printReadOnlyAttributes(p, attributes.iterator());
        }

        // Print any general errors
        for (HomeItemError homeItemError : homeItemErrors) {
            boolean bSet = false;
            if (homeItemError.type == HomeItemError.ErrorType.general) {
                if (!bSet) {
                    p.println("<div class=\"homeitem-errors\">");
                    bSet = true;
                }
                p.println(homeItemError.getErrorMessage() + "<br/>");
            }
            if (bSet)
                p.println("</div>");
        }

        p.println("<br>");

        // Print Actions
        printActions(p, item, pageArguments);

        // Print Writable Attributes
        printWritableAttributes(p, item, attributes.iterator(), false, homeItemErrors, false, null, null);

        // End of the Item info section
        p.println("</div>");

        // Print page end
        printColumnEnd(p);
    }

    protected void printItemHeading(PrintWriter p, String name, HomeItemModel model) {
        p.println("<div class=\"item_details\">");
        p.println("<div class=\"iheader\">");
        p.println(" <span class=\"homeiteminfo\">");
        p.println("  <ul>");
        p.println("   <li>Server Settings</li>");
        p.println("  </ul>");
        p.println(" </span>");
        p.println("</div>");
        p.println("<div class=\"deviderline\"></div>");
        p.println("<br>");
    }
}
