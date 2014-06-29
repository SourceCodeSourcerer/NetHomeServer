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

package nu.nethome.home.item;

import java.util.List;

/**
 * Describes a HomeItem attribute and also contains a snapshot of the attribute
 * value at the time of query. This class maps part of the information that can be
 * specified in the XML-model specification of an attribute in a HomeItem model.
 */
public interface Attribute {

    /**
     * @return name of the attribute
     */
    String getName();

    /**
     * @return the current value of the attribute
     */
    String getValue();

    /**
     * @return a list of all available attribute values if the attribute
     * has a specified set of values
     */
    List<String> getValueList();

    /**
     * @return the type of the attribute
     */
    String getType();

    /**
     * @return the unit of the attribute value
     */
    String getUnit();

    /**
     * @return true if the attribute is read only
     */
    boolean isReadOnly();

    /**
     * @return true if the attribute can be set at creation/initiation of Item
     */
    boolean isCanInit();

    /**
     * @return true if the attribute is write only
     */
    boolean isWriteOnly();
}