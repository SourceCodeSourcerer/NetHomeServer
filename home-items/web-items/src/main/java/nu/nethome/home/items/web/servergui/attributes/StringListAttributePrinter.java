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
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.web.servergui.AttributeTypePrinterInterface;
import nu.nethome.home.items.web.servergui.PortletPage;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.List;

public class StringListAttributePrinter implements
        AttributeTypePrinterInterface {

    public String getTypeName() {
        return HomeItemModel.STRING_LIST_TYPE;
    }

    @Override
    public String attributeToPrintValue(String value) {
        return value;
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        // Create an identity for the parameter
        String identity = getIdFromNumber(attributeNumber);

        // Print the HTML
        p.println("  <td>");
        p.println("  <select name=\"" + identity + "\">");
        for (String value : getValueList(attribute)) {
            p.print("    <option");
            if (value.equalsIgnoreCase(attribute.getValue())) {
                p.print(" selected=\"selected\"");
            }
            p.println(">" + value + "</option>");
        }
        p.println("</select>");
        return true;
    }

    protected List<String> getValueList(Attribute attribute) {
        return attribute.getValueList();
    }

    public void updateAttributeValue(HomeItemProxy item,
                                     Attribute attribute, HttpServletRequest req, boolean isInitiation,
                                     int attributeNumber) throws IllegalValueException {
        // Get the identity to look for
        String identity = getIdFromNumber(attributeNumber);

        // Get the corresponding parameter value from the request
        String value = req.getParameter(identity);

        // Check and update the HomeItem
        if ((value != null)
                && (!attribute.isReadOnly() || (attribute.isCanInit() && isInitiation))) {
            value = PortletPage.fromURL(value);
            item.setAttributeValue(attribute.getName(), value);
        }
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
