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

import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.AttributeModel;

import java.util.List;

public class ModelAttribute implements Attribute {

    private final String value;
    private final AttributeModel model;

    public ModelAttribute(String value, AttributeModel model) {
        this.value = value;
        this.model = model;
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<String> getValueList() {
        return model.getValueList();
    }

    @Override
    public String getType() {
        return model.getType();
    }

    @Override
    public String getUnit() {
        return model.getUnit();
    }

    @Override
    public boolean isReadOnly() {
        return model.isReadOnly();
    }

    @Override
    public boolean isCanInit() {
        return model.isCanInit();
    }

    @Override
    public boolean isWriteOnly() {
        return model.isWriteOnly();
    }
}
