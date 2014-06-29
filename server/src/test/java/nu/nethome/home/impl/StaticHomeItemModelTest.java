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

import nu.nethome.home.item.Action;
import nu.nethome.home.item.AttributeModel;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.hamcrest.core.Is.is;

public class StaticHomeItemModelTest {

    StaticHomeItemModel model;
    MockSafeHomeItem target;
    MockHomeItem altTarget;

    @Before
    public void setUp() throws Exception {
        target = new MockSafeHomeItem();
        altTarget = new MockHomeItem();
        model = new StaticHomeItemModel(target);
    }

    @Test
    public void canParseActionsInCorrectOrder() {
        List<Action> result = model.getActions();
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getName(), is("ReturnFoo"));
        assertThat(result.get(1).getName(), is("IsDefault"));
        assertThat(result.get(2).getName(), is("Extra"));
    }

    @Test
    public void canParseDefaultAction() {
        assertThat(model.getDefaultAction(), is("IsDefault"));
    }

    @Test
    public void canGetAction() throws ModelException {
        ActionModel result = model.getAction("ReturnFoo");
        assertThat(result.getName(), is("ReturnFoo"));
    }

    @Test
    public void canParseActionMethod() throws ModelException, InvocationTargetException, IllegalAccessException {
        ActionModel result = model.getAction("ReturnFoo");
        assertThat(result.call(target), is("Foo"));
    }

    @Test(expected = ModelException.class)
    public void canNotGetNonExistingAction() throws ModelException {
        ActionModel result = model.getAction("X");
    }

    @Test
    public void canGetAttribute() throws ModelException {
        AttributeModel result = model.getAttribute("AttValueFoo");
        assertThat(result.getName(), is("AttValueFoo"));
    }

    @Test
    public void canParseAttributeGetMethod() throws ModelException, InvocationTargetException, IllegalAccessException {
        ReflectionAttributeModel result = model.getAttribute("AttValueFoo");
        assertThat(result.getValue(target), is("Foo"));
    }

    @Test
    public void canParseAttributeSetMethod() throws ModelException, InvocationTargetException, IllegalAccessException {
        ReflectionAttributeModel result = model.getAttribute("AttValueFoo");
        result.setValue(target, "X");
        assertThat(result.getValue(target), is("X"));
    }

    @Test
    public void canParseAttributeInitMethod() throws ModelException, InvocationTargetException, IllegalAccessException {
        ReflectionAttributeModel result = model.getAttribute("AttInit");
        result.initValue(target, "Y");
        assertThat(result.getValue(target), is("Y"));
    }

    @Test
    public void canParseAttributeType() throws ModelException, InvocationTargetException, IllegalAccessException {
        AttributeModel result = model.getAttribute("AttValueFoo");
        assertThat(result.getType(), is("String"));
    }

    @Test
    public void canParseAttributeValueList() throws ModelException, InvocationTargetException, IllegalAccessException {
        AttributeModel result = model.getAttribute("AttList");
        assertThat(result.getValueList().size(), is(3));
        assertThat(result.getValueList(), hasItems("Foo", "Fie", "Fum"));
    }

    @Test(expected = ModelException.class)
    public void canNotGetNonExistingAttribute() throws ModelException {
        AttributeModel result = model.getAttribute("X");
    }

    @Test
    public void canGetAllAttributesInCorrectOrder() {
        List<AttributeModel> atts = model.getAttributes();
        assertThat(atts.size(), is(5));
        assertThat(atts.get(0).getName(), is("AttValueFoo"));
        assertThat(atts.get(1).getName(), is("AttSet"));
        assertThat(atts.get(2).getName(), is("AttInit"));
        assertThat(atts.get(3).getName(), is("AttReadOnly"));
        assertThat(atts.get(4).getName(), is("AttList"));
    }

    @Test
    public void canParseMorphingAttribute() throws ModelException {
        StaticHomeItemModel altModel = new StaticHomeItemModel(altTarget);
        assertThat(altModel.isMorphing(), is(false));
        assertThat(model.isMorphing(), is(true));
    }

    @Test
    public void canParseCategoryAttribute() throws ModelException {
        assertThat(model.getCategory(), is("Lamps"));
    }

    @Test
    public void canParseClassAttribute() throws ModelException {
        assertThat(model.getClassName(), is("MockSafeHomeItem"));
    }

    @Test
    public void factoryMethodCaches() throws ModelException {
        StaticHomeItemModel model1 = StaticHomeItemModel.getModel(target);
        StaticHomeItemModel model2 = StaticHomeItemModel.getModel(target);
        assertThat(model1, is(model2));
        assertThat(model1, not(is(model)));
    }

    @Test
    public void factoryMethodRefreshesCacheForMorphedItem() throws ModelException {
        StaticHomeItemModel model1 = StaticHomeItemModel.getModel(target);
        target.modelAddition = " ";
        StaticHomeItemModel model2 = StaticHomeItemModel.getModel(target);
        StaticHomeItemModel model3 = StaticHomeItemModel.getModel(target);
        assertThat(model1, not(is(model2)));
        assertThat(model2, is(model3));
        assertThat(model1, not(is(model)));
    }

    @Test
    public void canClearCache() throws ModelException {
        StaticHomeItemModel model1 = StaticHomeItemModel.getModel(target);
        StaticHomeItemModel.clearCache();
        StaticHomeItemModel model2 = StaticHomeItemModel.getModel(target);
        assertThat(model1, not(is(model2)));
        assertThat(model1, not(is(model)));
    }

    List<String> getActionNames(List<Action> actions) {
        List<String> result = new ArrayList<String>();
        for(Action action : actions) {
            result.add(action.getName());
        }
        return result;
    }
}
