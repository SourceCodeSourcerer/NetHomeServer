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
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class BooleanAttributePrinter extends StringAttributePrinter {

    public String getTypeName() {
        return "Boolean";
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        String identity = getIdFromNumber(attributeNumber);
        String selected = isTrue(attribute) ? "checked" : "";

        p.println("  <td>");
        p.println("  <input class=\"iteminput checkbox\" type=\"checkbox\" name=\""
                + identity + "\" value=\"True\" " + selected + ">");
        return true;
    }

    private boolean isTrue(Attribute attribute) {
        return attribute.getValue().equalsIgnoreCase("True") || attribute.getValue().equalsIgnoreCase("Yes");
    }

    public void updateAttributeValue(HomeItemProxy item,
                                     Attribute attribute, HttpServletRequest req, boolean isInitiation, int attributeNumber) throws IllegalValueException {
        // Get the identity to look for
        String identity = getIdFromNumber(attributeNumber);

        // Get the corresponding parameter value from the request
        String value = req.getParameter(identity);

        // Check and update the HomeItem
        if ((!attribute.isReadOnly() || (attribute.isCanInit() && isInitiation))) {
            item.setAttributeValue(attribute.getName(), value == null ? "false" : "true");
        }
    }

}