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
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

/**
 * This is an interface for attribute handlers in the EditItemPage. An implementation of the 
 * AttributeTypePrinterInterface can print an attribute value of its type as HTML, and parse 
 * the corresponding values from the resulting servlet request.
 * 
 * If an attribute type needs more information, for example a HomeService, this should be
 * supplied in the constructor.
 *  
 * @author Stefan
 */
public interface AttributeTypePrinterInterface {
	
	/**
	 * @return the name of the attribute type this instance handles  
	 */
	String getTypeName();

    /**
     * Returns a string ready for printing of the value as html
     * @param value
     * @return value
     */
    String attributeToPrintValue(String value);

	/**
	 * Prints the value part of an editable attribute on the supplied PrintWriter. The value should be contained 
	 * in a &LTtd&GT ... &LT/td&GT" section.
	 * 
	 * @param p PrintWriter used to print HTML
	 * @param attribute the attribute which value should be printed
	 * @param attributeNumber the number of the attribute, this is a helper to name the attribute uniquely
	 * @return true if the attribute was rendered, false if rendering failed 
	 */
	boolean printAttributeValue(PrintWriter p, Attribute attribute, int attributeNumber);
	
	/**
	 * Updates the attribute value in the HomeItem based on the value extracted from the supplied HTTP-request
	 *  
	 * @param item Proxy to the HomeItem to update
	 * @param attribute attribute in question
	 * @param req the HTTP-request where the new attribute value is available
	 * @param isInitiation true if this is part of the HomeItem initiation/creation
	 * @param attributeNumber the number of the attribute, this is a helper to name the attribute uniquely
	 */
	void updateAttributeValue(HomeItemProxy item, Attribute attribute,
                                     HttpServletRequest req, boolean isInitiation, int attributeNumber)  throws IllegalValueException;
	
}
