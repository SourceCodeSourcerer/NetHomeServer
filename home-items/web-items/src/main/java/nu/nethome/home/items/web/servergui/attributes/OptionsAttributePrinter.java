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
import nu.nethome.home.system.DirectoryEntry;
import nu.nethome.home.system.HomeService;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.util.List;

public class OptionsAttributePrinter implements AttributeTypePrinterInterface {

	protected HomeService m_Server;

	public OptionsAttributePrinter(HomeService serverConnection) {
		m_Server = serverConnection;
	}

	public String getTypeName() {
		return "Options";
	}

    @Override
    public String attributeToPrintValue(String value) {
        return value;
    }

    public boolean printAttributeValue(PrintWriter p, Attribute attribute,
			int attributeNumber) {
		String[] homeItems = attribute.getValue().split(",");

		p.println("  <td><table>");
		List<DirectoryEntry> directoryEntries = m_Server.listInstances("");

		// Loop through all instances to find all home items
		String identity;
		int col = 0;
		int subNumber = 1;
		for (DirectoryEntry directoryEntry : directoryEntries) {
			identity = getIdFromNumber(attributeNumber, subNumber++);
			if (col == 0)
				p.println("  <tr>");
			// Open the instance so we know class and category
			HomeItemProxy item = m_Server.openInstance(directoryEntry.getInstanceName());
			p.print("    <td>");
			p.format("<input type=\"checkbox\" name=\"%s\" value=\"%s\"",
					identity, item.getAttributeValue("ID"));
			for (int i = 0; i < homeItems.length; i++) {
				if (0 == homeItems[i].compareToIgnoreCase(directoryEntry.getInstanceName())) {
					p.print(" checked='checked'");
					break;
				}
			}
			p.format(">%s</input>", item.getAttributeValue("Name"));
			p.println("</td>");
			col++;
			if (col > 1) {
				col = 0;
				p.println("  </tr>");
			}
		}
		if (col != 0)
			p.println("  </tr>");
		p.format("</table><input type=\"hidden\" name=\"%s\" value=\"%s\"/></td>\n",
				getIdFromNumber(attributeNumber, 0), subNumber);
		return true;
	}

	public void updateAttributeValue(HomeItemProxy item,
			Attribute attribute, HttpServletRequest req, boolean isInitiation,
			int attributeNumber) throws IllegalValueException {
		String homeItemList = "";
		String identity, value;
		int subNumber = 1;
		// Get the identity to look for
		identity = req.getParameter(getIdFromNumber(attributeNumber, 0));
		if (null == identity)
			// Didn't find the info needed to continue
			return;
		int itemCount = Integer.parseInt(identity);
		for (int i = 0; i < itemCount; i++) {
			identity = getIdFromNumber(attributeNumber, subNumber++);
			value = req.getParameter(identity);
			if (null != value) {
				HomeItemProxy item2 = m_Server.openInstance(value);
				if (item2 != null) {
					if (homeItemList.length() > 0)
						homeItemList += ",";
					homeItemList += item2.getAttributeValue("Name");
				}
			}
		}
		
		item.setAttributeValue(attribute.getName(), homeItemList);
	}

	/**
	 * Create a unique parameter name for this attribute
	 * 
	 * @param number
	 *            an attribute number that is unique
	 * @return an identity string
	 */
	protected String getIdFromNumber(int number, int subNumber) {
		return "opt" + Integer.toString(number) + "_" + Integer.toString(subNumber);
	}
}
