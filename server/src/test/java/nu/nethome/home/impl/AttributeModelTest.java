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
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;

public class AttributeModelTest {

    ReflectionAttributeModel attribute;
    MockSafeHomeItem target;
    AttributeModel attribute1;
    AttributeModel attribute2;
    AttributeModel attribute3;
    AttributeModel attribute4;

    @Before
    public void setUp() throws Exception {
        target = new MockSafeHomeItem();
        attribute1 = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo",
                "setAttValueFoo", null);
        attribute2 = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo",
                null, null);
        attribute3 = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo",
                null, "setAttValueFoo");
        attribute4 = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), null,
                null, "setAttValueFoo");
    }

    @Test
    public void canDetermineReadOnly() {
        assertThat(attribute1.isReadOnly(), is(false));
        assertThat(attribute2.isReadOnly(), is(true));
        assertThat(attribute3.isReadOnly(), is(true));
        assertThat(attribute4.isReadOnly(), is(false));
    }

    @Test
    public void canDetermineIfCanInit() {
        assertThat(attribute1.isCanInit(), is(true));
        assertThat(attribute2.isCanInit(), is(false));
        assertThat(attribute3.isCanInit(), is(true));
        assertThat(attribute4.isCanInit(), is(true));
    }

    @Test
    public void canDetermineIfIsWriteOnly() {
        assertThat(attribute1.isWriteOnly(), is(false));
        assertThat(attribute2.isWriteOnly(), is(false));
        assertThat(attribute3.isWriteOnly(), is(false));
        assertThat(attribute4.isWriteOnly(), is(true));
    }

    @Test
    public void canGetAttributeValue() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo", "setAttValueFoo",
                null);
        assertThat(attribute.getValue(target), is("Foo"));
    }

    @Test(expected = ModelException.class)
    public void exceptionForCallingUnknownAttributeGetter() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "X", "setAttValueFoo",
                null);
        attribute.getValue(target);
    }

    @Test(expected = ModelException.class)
    public void exceptionForCallingUnknownAttributeSetter() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "X", "Y",
                null);
        attribute.setValue(target, "Foo");
    }

    @Test(expected = ModelException.class)
    public void exceptionForCallingUnknownAttributeInit() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "X", "Y",
                null);
        attribute.initValue(target, "Foo");
    }

    @Test
    public void canSetAttributeValue() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo", "setAttValueFoo",
                null);
        attribute.setValue(target, "Y");
        assertThat(attribute.getValue(target), is("Y"));
    }

    @Test
    public void canInitAttributeValue() throws InvocationTargetException, IllegalAccessException, ModelException {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo", "setAttValueFoo",
                null);
        attribute.initValue(target, "Y");
        assertThat(attribute.getValue(target), is("Y"));
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo", null, "setAttValueFoo");
        attribute.initValue(target, "Z");
        assertThat(attribute.getValue(target), is("Z"));
    }

    @Test
    public void canGetValueList() {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", null, target.getClass(), "getAttValueFoo", "setAttValueFoo",
                null, Arrays.asList("A", "B", "C"));
        assertThat(attribute.getValueList(), hasItems("A", "B", "C"));
    }

    @Test
    public void canGetUnit() {
        attribute = new ReflectionAttributeModel("AttValueFoo", "String", "MHz", target.getClass(), "getAttValueFoo", "setAttValueFoo",
                null, Arrays.asList("A", "B", "C"));
        assertThat(attribute.getUnit(), is("MHz"));
    }
}
