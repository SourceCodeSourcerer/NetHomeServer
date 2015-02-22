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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MediaFileAttributePrinter extends StringListAttributePrinter {

    public static final String MEDIA_FILE_TYPE = "MediaFile";
    private String mediaFileDirectory;

    public MediaFileAttributePrinter(String mediaFileDirectory) {
        this.mediaFileDirectory = mediaFileDirectory;
    }

    public String getTypeName() {
        return MEDIA_FILE_TYPE;
    }

    protected List<String> getValueList(Attribute attribute) {
        List<String> result = new ArrayList<String>();
        File f = new File(mediaFileDirectory);
        if (f.exists() && f.isDirectory()) {
            ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
            result.add(attribute.getValue());
            for (String name : names) {
                result.add("media/" + name);
            }
        }
        return result;
    }
}
