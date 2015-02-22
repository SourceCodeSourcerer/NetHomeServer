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
import nu.nethome.home.items.web.servergui.HTMLEncode;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.items.web.servergui.attributes.ItemAttributePrinter;
import nu.nethome.home.system.HomeService;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class StringsAttributePrinter extends ItemAttributePrinter {

    public StringsAttributePrinter(HomeService serverConnection) {
        super(serverConnection);
    }

    @Override
    public String getTypeName() {
        return "Strings";
    }

    @Override
    public String attributeToPrintValue(String value) {
        return HTMLEncode.encode(value);
    }

    @Override
    protected String inputToAttributeValue(String value) {
        return PortletPage.fromURL(value);
    }

    @Override
    protected String getListItemClass() {
        return "refsel";
    }

    @Override
    protected void printItemSelectionList(PrintWriter p, String identity, Attribute attribute) {
        Set<String> refs = new HashSet<String>(Arrays.asList(attribute.getValue().split(",")));
        p.println("    <div class=\"iteminputrows\">");
        p.println("     <ul>");
        for (String instance : attribute.getValueList()) {
            p.println("      <li> <input type=\"checkbox\" value=\"" + identity +
                    "\" " + (refs.contains(instance) ? "checked=\"checked\" " : "") + " class=\"" + getListItemClass() + "\">" + instance + "</li>");
        }
        p.println("     </ul>");
        p.println("    </div>");
    }
}
