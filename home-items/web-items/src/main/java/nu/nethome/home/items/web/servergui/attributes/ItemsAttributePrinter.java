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

import nu.nethome.home.items.web.servergui.HTMLEncode;
import nu.nethome.home.items.web.servergui.PortletPage;
import nu.nethome.home.items.web.servergui.attributes.ItemAttributePrinter;
import nu.nethome.home.system.HomeService;

/**
 * An attribute printer for Item attribute type
 *
 * @author Stefan
 */
public class ItemsAttributePrinter extends ItemAttributePrinter {

    public ItemsAttributePrinter(HomeService serverConnection) {
        super(serverConnection);
    }

    @Override
    public String getTypeName() {
        return "Items";
    }

    @Override
    public String attributeToPrintValue(String value) {
        String[] references = value.split(",");
        StringBuilder result = new StringBuilder();
        boolean addComma = false;
        for (String reference : references) {
            result.append(addComma ? "," : "");
            result.append(itemRefToString(reference));
            addComma = true;
        }
        return HTMLEncode.encode(result.toString());
    }

    @Override
    protected String inputToAttributeValue(String value) {
        String[] references = PortletPage.fromURL(value).split(",");
        StringBuilder result = new StringBuilder();
        boolean addComma = false;
        for (String reference : references) {
            result.append(addComma ? "," : "");
            result.append(stringToItemRef(reference));
            addComma = true;
        }
        return result.toString();
    }

    @Override
    protected String getListItemClass() {
        return "refsel";
    }
}
