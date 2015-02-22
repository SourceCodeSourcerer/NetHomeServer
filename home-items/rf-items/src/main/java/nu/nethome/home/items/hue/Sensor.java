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

package nu.nethome.home.items.hue;

import org.json.JSONObject;

/**
 *
 */
public class Sensor {
    private final String id;
    private final String name;
    private final String type;
    private final String modelid;
    private final String manufacturername;

    public Sensor(String id, String type, String name, String modelid, String manufacturername) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.modelid = modelid;
        this.manufacturername = manufacturername;
    }

    public Sensor(String id, JSONObject json) {
        this.id = id;
        name = json.getString("name");
        type = json.getString("type");
        modelid = json.getString("modelid");
        manufacturername = json.getString("manufacturername");
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getModelid() {
        return modelid;
    }

    public String getManufacturername() {
        return manufacturername;
    }
}
