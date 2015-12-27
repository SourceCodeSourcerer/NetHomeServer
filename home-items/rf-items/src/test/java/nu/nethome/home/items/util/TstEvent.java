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

package nu.nethome.home.items.util;

import nu.nethome.home.system.Event;

import java.util.Map;
import java.util.TreeMap;

/**
 * User: Stefan
 * Date: 2012-05-29
 * Time: 20:49
 */
public class TstEvent implements Event {
    private Map<String, String> attributes = new TreeMap<String, String>();

    /**
     * Create a new event with given type and value
     *
     * @param type  Type of event (The name of the Event)
     * @param value The value of the Event
     */
    public TstEvent(String type, String value) {
        attributes.put(EVENT_TYPE_ATTRIBUTE, type);
        attributes.put(EVENT_VALUE_ATTRIBUTE, value);
    }

    /**
     * Create a new event with given type
     *
     * @param type Type of event (The name of the Event)
     */
    public TstEvent(String type) {
        attributes.put(EVENT_TYPE_ATTRIBUTE, type);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("event,");
        result.append(getAttribute(EVENT_TYPE_ATTRIBUTE));
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            if (!e.getKey().equals(EVENT_TYPE_ATTRIBUTE)) {
                result.append(",");
                result.append(e.getKey());
                result.append(",");
                result.append(e.getValue());
            }
        }
        return result.toString();
    }

    public String getAttribute(String name) {
        Object temp = attributes.get(name);
        return temp == null ? "" : temp.toString();
    }

    public int getAttributeInt(String name) {
        try {
            return Integer.parseInt(getAttribute(name));
        } catch (NumberFormatException n) {
            return 0;
        }
    }

    public float getAttributeFloat(String name) {
        try {
            return Float.parseFloat(getAttribute(name));
        } catch (NumberFormatException n) {
            return 0;
        }
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public void setAttribute(String name, int value) {
        attributes.put(name, Integer.toString(value));
    }

    public void setAttribute(String name, float value) {
        attributes.put(name, Float.toString(value));
    }

    public int[] getAttributeArr(String name) {
        String arrString = getAttribute(name);
        int arr[] = new int[arrString.length() / 2];
        for (int i = 0; i < arrString.length(); i += 2) {
            arr[i / 2] = Integer.parseInt(arrString.substring(i, i + 2), 16);
        }
        return arr;
    }

    @Override
    public boolean isType(String typeName) {
        return getAttribute(EVENT_TYPE_ATTRIBUTE).equals(typeName);
    }

    public void setAttribute(String name, int[] value) {
        StringBuilder buildAttribute = new StringBuilder();
        for (int valueItem : value) {
            buildAttribute.append(Integer.toHexString((valueItem >> 4) & 0xF).toUpperCase());
            buildAttribute.append(Integer.toHexString(valueItem & 0xF).toUpperCase());
        }
        attributes.put(name, buildAttribute.toString());
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

}
