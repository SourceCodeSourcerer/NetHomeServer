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
import nu.nethome.home.items.web.servergui.AttributeTypePrinterInterface;
import nu.nethome.home.items.web.servergui.HTMLEncode;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.system.HomeService;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class TextAttributePrinter extends StringAttributePrinter {

    public String getTypeName() {
        return "Text";
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
                                       int attributeNumber) {
        // Create an identity for the parameter
        String identity = getIdFromNumber(attributeNumber);

        // Print the HTML
        p.println("  <td>");
        p.println("  <textarea class=\"iteminput\"  name=\""
                + identity + "\">" + attributeToPrintValue(attribute.getValue()) + "</textarea>");
        return true;
    }
}