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

import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.items.web.servergui.HTMLEncode;
import nu.nethome.home.items.web.servergui.HomeGUI;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import java.io.PrintWriter;
import java.util.*;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class ItemAttributePrinter extends StringAttributePrinter {

    private class CategorizedItemList {
        private String category;
        private List<HomeItemProxy> items;

        private CategorizedItemList(String category) {
            this.category = category;
            items = new ArrayList<HomeItemProxy>();
        }

        public void addItem(HomeItemProxy item) {
            items.add(item);
        }

        public String getCategory() {
            return category;
        }

        public List<HomeItemProxy> getItems() {
            return Collections.unmodifiableList(items);
        }
    }

    protected HomeService server;

    public ItemAttributePrinter(HomeService serverConnection) {
        server = serverConnection;
    }

    @Override
    public String getTypeName() {
        return "Item";
    }

    @Override
    public String attributeToPrintValue(String value) {
        return HTMLEncode.encode(itemRefToString(value));
    }

    @Override
    protected String inputToAttributeValue(String value) {
        return stringToItemRef(PortletPage.fromURL(value));
    }

    @Override
    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        // Create an identity for the parameter
        String identity = getIdFromNumber(attributeNumber);
        String itemsIdentity = identity + "Items";

        // Print the HTML
        p.println("  <td>");
        p.println("  <input id=\"" + identity + "\" class=\"iteminputrefs\" type=\"" + getInputType() + "\" name=\""
                + identity + "\" value=\"" + attributeToPrintValue(attribute.getValue()) + "\">");
        p.println("  <input class =\"iteminputrefsbutton\" type=\"button\" value=\"...\" onmousedown='$(\"#" + itemsIdentity + "\").toggle();'>");
        p.println("  </td></tr>");
        p.println("  <tr id=\"" + itemsIdentity + "\" style=\"display: none;\">");
        p.println("   <td class=\"attributename\"></td>");
        p.println("   <td>");
        printItemSelectionList(p, identity, attribute);
        p.println("  </td></tr>");
        return true;
    }

    protected void printItemSelectionList(PrintWriter p, String identity, Attribute attribute) {
        Set<String> refs = new HashSet<String>(Arrays.asList(attribute.getValue().split(",")));
        p.println("    <div class=\"iteminputrows\">");
        p.println("     <ul>");
        Map<String, CategorizedItemList> categories = getCategorizedItems();
        for (String category : HomeItemModel.HOME_ITEM_CATEGORIES) {
            if (categories.containsKey(category)) {
                CategorizedItemList itemsInCategory = categories.get(category);
                p.println("      <li><img src=\"web/home/"
                        + HomeGUI.itemIcon(category, true) + "\" /><h2>" + category + "</h2></li> ");
                for (HomeItemProxy instance : itemsInCategory.getItems()) {
                    String instanceId = instance.getAttributeValue("ID");
                    p.println("      <li> <input type=\"checkbox\" value=\"" + identity +
                            "\" " + (refs.contains(instanceId) ? "checked=\"checked\" " : "") + " class=\"" + getListItemClass() + "\">" + instance.getAttributeValue("Name") + "</li>");
                }
            }
        }
        p.println("     </ul>");
        p.println("    </div>");
    }

    private Map<String, CategorizedItemList> getCategorizedItems() {
        Map<String, CategorizedItemList> itemCategories = new HashMap<String, CategorizedItemList>();
        for (DirectoryEntry directoryEntry : this.server.listInstances("")) {
            HomeItemProxy item = server.openInstance(directoryEntry.getInstanceName());
            HomeItemModel model = item.getModel();
            CategorizedItemList category = itemCategories.get(model.getCategory());
            if (category == null) {
                category = new CategorizedItemList(model.getCategory());
                itemCategories.put(model.getCategory(), category);
            }
            category.addItem(item);
        }
        return itemCategories;
    }

    protected String getListItemClass() {
        return "refselsingle";
    }

    protected String itemRefToString(String reference) {
        if (reference.length() == 0) {
            return reference;
        }
        HomeItemProxy item = server.openInstance(reference);
        if (item != null) {
            return item.getAttributeValue("Name");
        }
        return "\"" + reference + "\"";
    }

    protected String stringToItemRef(String value) {
        if (value.length() == 0) {
            return value;
        }
        String reference = trimQuotations(value.trim());
        HomeItemProxy item = server.openInstance(reference);
        if (item != null) {
            return item.getAttributeValue("ID");
        }
        return reference;
    }

    private String trimQuotations(String reference) {
        if (reference.length() > 1 && reference.charAt(0) == '"' && reference.charAt(reference.length() - 1) == '"') {
            return reference.substring(1, reference.length() - 1);
        }
        return reference;
    }
}
