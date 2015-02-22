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

import nu.nethome.home.item.Attribute;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AttributeDto {
    private String name;
    private String value;
    private String unit;
    private String type;
    private List<String> valueList;
    private Boolean readOnly;
    private Boolean canInit;
    private Boolean isDefault;

    public AttributeDto(Attribute attribute, boolean isDefaultAttribute) {
        unit = attribute.getUnit().isEmpty() ? null : attribute.getUnit();
        type = attribute.getType().equals("String") ? null : attribute.getType();
        name = attribute.getName();
        value = attribute.getValue();
        valueList = attribute.getValueList().isEmpty() ? null : attribute.getValueList();
        readOnly = attribute.isReadOnly() ? true : null;
        canInit = attribute.isCanInit() && attribute.isReadOnly() ? true : null;
        isDefault = isDefaultAttribute ? true : null;
    }

    public AttributeDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getCanInit() {
        return canInit;
    }

    public void setCanInit(Boolean canInit) {
        this.canInit = canInit;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}
