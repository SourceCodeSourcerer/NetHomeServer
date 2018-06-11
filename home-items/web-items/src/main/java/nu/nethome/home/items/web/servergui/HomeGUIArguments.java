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

import javax.servlet.http.HttpServletRequest;

/**
 * Represents request parameters for a page in the HomeGUI
 */
public class HomeGUIArguments {
    public static final String RETURN_TO_ITEM = "RETURN2ITEM";
    private static final String DEFAULT_PAGE = "rooms";
    private static final String CLOUD_ACCOUNT = "Cloud-Account";
    protected final String action;
    protected final boolean editMode;
    protected String rawAction;
    protected String page;
    protected String subpage;
    protected boolean isIE;
    protected final String name;
    protected final String returnPage;
    protected final String returnSubpage;
    protected final String cloudAccount;

    public HomeGUIArguments(HttpServletRequest req) {
        rawAction = req.getParameter("a");
        name = PortletPage.fromURL(req.getParameter("name"));
        action = rawAction == null ? "" : rawAction;
        String mode = req.getParameter("mode");
        editMode = (mode == null) ? false : mode.equals("edit");
        page = req.getParameter("page");
        subpage = req.getParameter("subpage");
        isIE = isInternetExplorer(req);
        returnSubpage = req.getParameter("returnsp");
        returnPage = req.getParameter("return");
        cloudAccount = req.getHeader(CLOUD_ACCOUNT);
    }

    public boolean isAction(String actionName) {
        return action.equals(actionName);
    }

    public boolean hasAction() {
        return rawAction != null;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public String getPage() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public boolean hasSubpage() {
        return subpage != null;
    }

    public String getSubpage() {
        return subpage;
    }

    public boolean isIE() {
        return isIE;
    }

    public boolean isInternetExplorer(HttpServletRequest req) {
        String s = req.getHeader("user-agent");
        if (s == null) {
            return false;
        }
        if (s.indexOf("MSIE") > -1) {
            return true;
        }
        return false;
    }

    public boolean hasName() {
        return name != null || hasSubpage();
    }

    public String getName() {
        return name != null ? name : subpage;
    }

    public boolean hasReturnPage() {
        return returnPage != null;
    }

    public String getReturnPage() {
        return returnPage;
    }

    public boolean hasReturnSubpage() {
        return returnSubpage != null;
    }

    public String getReturnSubpage() {
        return returnSubpage;
    }

    public String getEffectiveReturnSubpage() {
        return (RETURN_TO_ITEM.equals(returnSubpage) && name != null) ? name : returnSubpage;
    }

    public boolean isCloudAccess() {
        return cloudAccount != null;
    }

    public String getCloudAccount() {
        return cloudAccount;
    }
}
