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

 */
public interface HomeItemModel {

    static final String HOME_ITEM_CATEGORIES[] = {
        "Lamps",
        "Timers",
        "Ports",
        "GUI",
        "Hardware",
        "Controls",
        "Gauges",
        "Thermometers",
        "Infrastructure",
        "Actuators"
    };

    static final String STRING_LIST_TYPE = "StringList";

    String getCategory();

    String getClassName();

    int getStartOrder();

    String getDefaultAction();

    AttributeModel getDefaultAttribute();

    boolean hasAttribute(String attributeName);

    boolean hasAction(String actionName);

    /**
     * Returns a list of all supported actions of the current HomeItem instance.
     *
     * @return Supported Actions
     */
    List<Action> getActions();
}