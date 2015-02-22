/**
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.web.rest;

import nu.nethome.home.system.DirectoryEntry;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemDirectoryEntryDto {

    private String name;
    private String id;
    private String category;

    public ItemDirectoryEntryDto() {
    }

    public ItemDirectoryEntryDto(DirectoryEntry entry) {
        name = entry.getInstanceName();
        id = Long.toString(entry.getInstanceId());
        category = entry.getCategory();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public static List<ItemDirectoryEntryDto> toDtos(Collection<DirectoryEntry> entries) {
        List<ItemDirectoryEntryDto> items = new ArrayList<ItemDirectoryEntryDto>(entries.size());
        for (DirectoryEntry entry : entries) {
            items.add(new ItemDirectoryEntryDto(entry));
        }
        return items;
    }
}
