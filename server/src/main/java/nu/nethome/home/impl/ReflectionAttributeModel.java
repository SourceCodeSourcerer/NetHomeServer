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

import nu.nethome.home.item.AttributeModel;
import nu.nethome.home.item.HomeItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class ReflectionAttributeModel implements AttributeModel {
    private final Method setMethod;
    private final Method getMethod;
    private final Method initMethod;
    private final String name;
    private final List<String> valueList;
    private final String type;
    private final String unit;


    public ReflectionAttributeModel(String name, String type, String unit, Class<? extends HomeItem> clazz, String getMethodName, String setMethodName,
                                    String initMethodName) {
        this(name, type, unit, clazz, getMethodName, setMethodName,
                initMethodName, Collections.<String>emptyList());
    }

    public ReflectionAttributeModel(String name, String type, String unit, Class<? extends HomeItem> clazz, String getMethodName, String setMethodName,
                                    String initMethodName, List<String> valueList) {
        this.name = name;
        this.type = type;
        this.setMethod = getGetMethod(clazz, setMethodName, new Class[]{String.class});
        this.getMethod = getGetMethod(clazz, getMethodName, (Class[]) null);
        Method init = getGetMethod(clazz, initMethodName, new Class[]{String.class});
        initMethod = init != null ? init : setMethod;
        this.valueList = Collections.unmodifiableList(valueList);
        this.unit = unit == null ? "" : unit;
    }

    private Method getGetMethod(Class<? extends HomeItem> clazz, String methodName, Class<?>... parameterType) {
        try {
            return methodName != null ? clazz.getMethod(methodName, parameterType) : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getValueList() {
        return valueList;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public boolean isReadOnly() {
        return setMethod == null && getMethod != null;
    }

    @Override
    public boolean isCanInit() {
        return initMethod != null;
    }

    @Override
    public boolean isWriteOnly() {
        return getMethod == null;
    }

    public String getValue(HomeItem item) throws InvocationTargetException, IllegalAccessException, ModelException {
        if (getMethod != null) {
            return (String) getMethod.invoke(item, (Object[]) null);
        } else {
            throw new ModelException("No Get Method");
        }
    }

    public void setValue(HomeItem item, String value) throws InvocationTargetException, IllegalAccessException, ModelException {
        if (setMethod != null) {
            setMethod.invoke(item, value);
        } else {
            throw new ModelException("No Set Method");
        }
    }

    public void initValue(HomeItem item, String value) throws InvocationTargetException, IllegalAccessException, ModelException {
        if (initMethod != null) {
            initMethod.invoke(item, value);
        } else {
            throw new ModelException("No Init Method");
        }
    }
}
