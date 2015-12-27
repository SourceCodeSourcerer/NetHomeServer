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

package nu.nethome.home.system;

/**
 * An internal event in the system. HomeItems may sent events when something has occurred that
 * may be interesting for other items to know.
 * The Event has a very "soft" interface. It is basically a set of name/value pairs called attributes.
 * Every event has a "Type"-attribute which specifies the type (or name of the type) of the event.
 * All events also has a "Value"-attribute, but it is up to the event to decide what this value means.
 *
 * @author Stefan
 */
public interface Event {
    String EVENT_TYPE_ATTRIBUTE = "Type";
    String EVENT_VALUE_ATTRIBUTE = "Value";
    String EVENT_SENDER_ATTRIBUTE = "Sender";

    /**
     * Returns the value of the specified attribute. If the attribute does not exist, an empty string is returned
     *
     * @param name Name of the attribute
     * @return The value of the attribute as a string
     */
    String getAttribute(String name);

    /**
     * @param name Name of the attribute
     * @return The value of the attribute as an integer
     */
    int getAttributeInt(String name);

    /**
     * @param name Name of the attribute
     * @return The value of the attribute as a float
     */
    float getAttributeFloat(String name);

    /**
     * Get the value of an attribute as list of integers
     *
     * @param name name of the attribute
     * @return the value of the attribute
     */
    int[] getAttributeArr(String name);

    /**
     * Is the event of the specified type
     * @param typeName
     * @return true id the type is the same
     */
    boolean isType(String typeName);

    /**
     * Set an attribute value in the Event
     *
     * @param name  name of the attribute
     * @param value value of the attribute
     */
    void setAttribute(String name, String value);

    /**
     * Set an integer attribute value in the Event
     *
     * @param name  name of the attribute
     * @param value value of the attribute
     */
    void setAttribute(String name, int value);

    /**
     * Set a float attribute value in the Event
     *
     * @param name  name of the attribute
     * @param value value of the attribute
     */
    void setAttribute(String name, float value);

    /**
     * Set the value of an attribute as a list of integers
     *
     * @param name
     * @param value
     */
    void setAttribute(String name, int[] value);

    /**
     * Check if the specified attribute exists in the event.
     *
     * @param name
     * @return true if the attribute exists
     */
    boolean hasAttribute(String name);

    /**
     * Get the names of all attributes in the Event
     * @return array of attribute names
     */
    String[] getAttributeNames();

    /**
     * @return string representation of the Event
     */
    String toString();
}
