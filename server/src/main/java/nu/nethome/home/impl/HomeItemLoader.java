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

package nu.nethome.home.impl;

import nu.nethome.home.item.HomeItem;

import java.util.List;

/**
 * This is the interface for saving and loading HomeItems
 */
public interface HomeItemLoader {

    /**
     * Save all HomeItems in the list to the specified resource. The state of the HomeItems
     * is extracted by calling the get-methods for all attributes.
     * @param items List of items to save
     * @param name Name of the resource to save to, file name for example
     */
    void saveItems(List<HomeItem> items, String name);

    /**
     * Load HomeItems from the specified source. The HomeItems are created and their
     * state is loaded by applying the set-methods for all attributes
     */
    List<HomeItem> loadItems(String fileName, HomeItemFactory factory, HomeServer homeServer);
}
