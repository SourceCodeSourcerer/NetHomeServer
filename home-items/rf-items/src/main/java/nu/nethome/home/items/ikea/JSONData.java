/*
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

package nu.nethome.home.items.ikea;

import nu.nethome.home.items.hue.HueProcessingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class JSONData {
    private final JSONObject object;
    private final JSONArray array;

    public JSONData(String data)  {
        if (data.trim().startsWith("[")) {
            array = new JSONArray(data);
            object = null;
        } else if (data.trim().startsWith("{")) {
            object = new JSONObject(data);
            array = null;
        } else {
            throw new JSONException("Data not object or array");
        }
    }

    public boolean isObject() {
        return object != null;
    }

    public JSONObject getObject()  {
        if (object == null) {
            throw new JSONException("Not a JSON Object");
        }
        return object;
    }

    public JSONArray getArray() {
        if (array == null) {
            throw new JSONException("Not a JSON Array");
        }
        return array;
    }

    @Override
    public String toString() {
        if (isObject()) {
            return object.toString(3);
        } else {
            return array.toString(3);
        }
    }
}
