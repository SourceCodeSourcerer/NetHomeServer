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

package nu.nethome.home.items.web.servergui.attributes;

import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.web.servergui.AttributeTypePrinterInterface;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.List;

public class CommandAttributePrinter implements AttributeTypePrinterInterface {

    protected HomeService server;

    public CommandAttributePrinter(HomeService serverConnection) {
        server = serverConnection;
    }

    public String getTypeName() {
        return "Command";
    }

    @Override
    public String attributeToPrintValue(String value) {
        return value;
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        // Create an identity for the parameter
        String identity = getIdFromNumber(attributeNumber);

        String[] callCmdAction = attribute.getValue().split(",");
        String targetItemIdentity = "";
        String targetItemAction = "";
        String command = "";
        HomeItemProxy targetItem;

        if (callCmdAction.length > 0) {
            command = callCmdAction[0];
        }
        if (attribute.getValue().length() > 0 && (callCmdAction.length != 3 || !command.equalsIgnoreCase("call"))) {
            return printCustomCommand(p, attribute, identity);
        }
        if (callCmdAction.length > 1) {
            targetItemIdentity = callCmdAction[1];
        }
        targetItem = server.openInstance(targetItemIdentity);
        String targetItemName = (targetItem == null) ? "" : targetItem.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE);
        if (callCmdAction.length > 1) {
            targetItemAction = callCmdAction[2];
        }

        printItemSelection(p, identity, targetItemName);

        printItemActionSelection(p, identity, targetItemAction, targetItem);

        return true;
    }

    private void printItemSelection(PrintWriter p, String identity, String targetItemName) {
        List<DirectoryEntry> directoryEntries = server.listInstances("");
        p.println("  <td>");
        p.println("  <select class=\"attributecmd-item\" name=\""
                + identity + "\">");
        p.println("  <optgroup label=\"Select a home item\">");
        p.println("  <option class=\"attributecmd-itemdim\" value=\"\">[No Action Selected]</option>");
        for (DirectoryEntry directoryEntry : directoryEntries) {
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            List<Action> actions = item.getModel().getActions();
            if (actions.size() == 0)
                continue;
            p.println("  <option value=\""
                    + item.getAttributeValue("ID")
                    + "\""
                    + (directoryEntry.getInstanceName().equals(targetItemName) ? " selected='selected'" : "")
                    + ">" + item.getAttributeValue("Name")
                    + "</option>");
        }
        p.println("  </optgroup>");
        p.println("  <optgroup label=\"Custom actions\">");
        p.println("  <option value=\"set,[item name],[item attribute],[attribute value]\">Set</option>");
        p.println("  <option value=\"exec,[shell command line]\">Exec</option>");
        p.println("  <option value=\"event,[event name],[attribute name],[attribute value]\">Event</option>");
        p.println("  <option value=\"python,[function name]\">Python</option>");
        p.println("  </optgroup>");
        p.println("  </select>");
    }

    private void printItemActionSelection(PrintWriter p, String identity, String targetItemAction, HomeItemProxy targetItem) {
        p.println("  <select class=\"attributecmd-action\" name=\"" + identity + "_a\">");
        p.println("  <optgroup label=\"Select an Action\">");
        if (targetItem != null) {
            List<Action> actions = targetItem.getModel().getActions();
            for (Action anAction : actions) {
                p.println("  <option"
                        + (targetItemAction.compareToIgnoreCase(anAction.getName()) == 0 ? " selected='selected'"
                        : "") + ">" + anAction.getName() + "</option>");
            }
        }
        p.println("  </optgroup></select>");
    }

    private boolean printCustomCommand(PrintWriter p, Attribute attribute, String identity) {
        p.println("  <td>");
        p.println("  <input class=\"iteminput\" type=\"string\" name=\""
                + identity + "\" value=\"" + attribute.getValue() + "\">");
        return true;
    }

    public void updateAttributeValue(HomeItemProxy item,
                                     Attribute attribute, HttpServletRequest req, boolean isInitiation,
                                     int attributeNumber) throws IllegalValueException {
        // Get the identity to look for
        String identity = getIdFromNumber(attributeNumber);

        // Get the corresponding parameter value from the request
        String itemIdentity = req.getParameter(identity);
        String itemAction = req.getParameter(identity + "_a");
        String newAttributeValue;

        if (itemIdentity == null || !isAttributeWritable(attribute, isInitiation)) {
            return;
        }

        // Check and update the HomeItem
        itemIdentity = PortletPage.fromURL(itemIdentity);
        HomeItemProxy actionTargetItem = server.openInstance(itemIdentity);
        if (itemAction != null && itemAction.length() > 0 && actionTargetItem != null) {
            itemAction = PortletPage.fromURL(itemAction);
            newAttributeValue = "call," + actionTargetItem.getAttributeValue("ID");
            newAttributeValue = newAttributeValue + "," + itemAction;
        } else {
            newAttributeValue = itemIdentity;
        }
        item.setAttributeValue(attribute.getName(), newAttributeValue);
    }

    private boolean isAttributeWritable(Attribute attribute, boolean isInitiation) {
        return (!attribute.isReadOnly() || (attribute.isCanInit() && isInitiation));
    }

    /**
     * Create a unique parameter name for this attribute
     *
     * @param number an attribute number that is unique
     * @return an identity string
     */
    protected String getIdFromNumber(int number) {
        return "a" + Integer.toString(number);
    }
}
