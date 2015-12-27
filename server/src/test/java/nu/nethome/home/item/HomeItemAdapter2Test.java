/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.nethome.home.item;

import nu.nethome.home.item.annotation.HomeItemAction;
import nu.nethome.home.item.annotation.HomeItemAttribute;
import nu.nethome.home.impl.HomeItemClassInfo;
import nu.nethome.home.impl.HomeItemClassInfoTest;
import nu.nethome.util.plugin.Plugin;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class HomeItemAdapter2Test {

    public HomeItemAdapter2Test() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getModel method, of class HomeItemAdapter2.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        HomeItemAdapter2 instance = new SampleHomeItemWithAttribute();
        String result = instance.getModel();
        assertTrue(result.startsWith(instance.getXmlTag()));
        System.out.println(result);
    }

    // With @HomeItemAttribute annotations
    public class SampleHomeItemWithNoCategory extends HomeItemAdapter2 {
    }

    @Test
    public void testGetModelWithNoCategory() {
        System.out.println("getModelWithNoCategory");
        HomeItemAdapter2 instance = new SampleHomeItemWithNoCategory();
        String result = instance.getModel();
        assertTrue(result.contains("Category=\"Unknown\""));
        System.out.println(result);
    }

    // With @HomeItemAttribute annotations
    @HomeItemType(value = "Lights")
    public class SampleHomeItemWithLightsCategory extends HomeItemAdapter2 {
    }

    @Test
    public void testGetModelWithACategory() {
        System.out.println("getModelWithNoCategory");
        HomeItemAdapter2 instance = new SampleHomeItemWithLightsCategory();
        instance.category = "Lights";
        String result = instance.getModel();
        assertTrue(result.contains("Category=\"Lights\""));
        System.out.println(result);
    }

    // With @HomeItemAttribute annotations
    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttribute extends HomeItemAdapter2 {

        @HomeItemAttribute
        private String sampleAttribute = "Sample Value";
    }

    @Test
    public void testHomeItemAttributes() {
        HomeItem instance = new SampleHomeItemWithAttribute();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"SampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeSpecificName extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute")
        private String sampleAttribute = "Sample Value";
    }

    @Test
    public void testHomeItemAttributeSpecific() {
        HomeItem instance = new SampleHomeItemWithAttributeSpecificName();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertFalse(result.contains("Get=\"getPublicAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeSpecificGetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute", get = "getSpecificSampleAttribute")
        private String sampleAttribute = "Sample Value";

        public String getSpecificSampleAttribute() {
            return sampleAttribute;
        }
    }

    @Test
    public void testHomeItemAttributeSpecificGetter() {
        HomeItem instance = new SampleHomeItemWithAttributeSpecificGetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertTrue(result.contains("Get=\"getSpecificSampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeMissingSpecificGetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute", get = "getNonExistingSpecificSampleAttribute")
        private String sampleAttribute = "Sample Value";

        public String getSpecificSampleAttribute() {
            return sampleAttribute;
        }
    }

    @Test
    public void testSampleHomeItemWithAttributeMissingSpecificGetter() {
        HomeItem instance = new SampleHomeItemWithAttributeMissingSpecificGetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertFalse(result.contains("Get=\"getNonExistingSpecificSampleAttribute\""));
        assertFalse(result.contains("Get=\"getSpecificSampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeMissingSpecificGetterAndDefaultsToDefaultGetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute", get = "getNonExistingSpecificSampleAttribute")
        private String sampleAttribute = "Sample Value";

        public String getSampleAttribute() {
            return sampleAttribute;
        }
    }

    @Test
    public void testSampleHomeItemWithAttributeMissingSpecificGetterAndDefaultToDefaultGetter() {
        HomeItem instance = new SampleHomeItemWithAttributeMissingSpecificGetterAndDefaultsToDefaultGetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertFalse(result.contains("Get=\"getNonExistingSpecificSampleAttribute\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeDefaultGetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute")
        private String sampleAttribute = "Sample Value";

        public String getSampleAttribute() {
            return sampleAttribute;
        }
    }

    @Test
    public void testSampleHomeItemWithAttributeDefaultGetter() {
        HomeItem instance = new SampleHomeItemWithAttributeDefaultGetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        assertFalse(result.contains("Set=\"setSampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeDefaultSetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute")
        private String sampleAttribute = "Sample Value";

        public String getSampleAttribute() {
            return sampleAttribute;
        }

        public void setSampleAttribute(String value) {
            sampleAttribute = value;
        }
    }

    @Test
    public void testSampleHomeItemWithAttributeDefaultSetter() {
        HomeItem instance = new SampleHomeItemWithAttributeDefaultSetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        assertTrue(result.contains("Set=\"setSampleAttribute\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAttributeSpecificSetter extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "PublicAttribute", set = "mySetter")
        private String sampleAttribute = "Sample Value";

        public String getSampleAttribute() {
            return sampleAttribute;
        }

        public void mySetter(String value) {
            sampleAttribute = value;
        }
    }

    @Test
    public void testSampleHomeItemWithAttributeSpecificSetter() {
        HomeItem instance = new SampleHomeItemWithAttributeSpecificSetter();
        String result = instance.getModel();
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        assertFalse(result.contains("Set=\"setSampleAttribute\""));
        assertTrue(result.contains("Set=\"mySetter\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithNoRealExistingAttribute extends HomeItemAdapter2 {

        @HomeItemAttribute("PublicAttribute")
        public String getSampleAttribute() {
            return "Sample Value";
        }

        // Can't be associated with read-only pseudo attribute
        public void mySetter(String value) {
        }
    }

    @Test
    public void testSampleHomeItemWithNoRealExistingAttribute() {
        HomeItem instance = new SampleHomeItemWithNoRealExistingAttribute();
        System.out.println(instance.getClass().getSimpleName());
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        assertFalse(result.contains("Set=\"setSampleAttribute\""));
        assertFalse(result.contains("Set=\"mySetter\""));
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithPseudoAttribute extends HomeItemAdapter2 {

        // Define attribute by member variable
        @HomeItemAttribute(value = "PublicAttribute", set = "mySetter")
        private String sampleAttribute = "Sample Value";

        // Define pseudo attribute by getter
        @HomeItemAttribute("PublicAttribute with spaces")
        public String getSampleAttribute() {
            return "Sample Value";
        }

        public void setSampleAttribute(String value) {
            // something;
        }

        // Can't be associated with read-only pseudo attribute
        public void mySetter(String value) {
        }
    }

    @Test
    public void testSampleHomeItemWithPseudoAttribute() {
        HomeItem instance = new SampleHomeItemWithPseudoAttribute();
        String result = instance.getModel();
        System.out.println("SampleHomeItemWithPseudoAttribute");
        System.out.println(result);
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\" Type=\"String\" Get=\"getSampleAttribute\" Set=\"mySetter\""));
        assertTrue(result.contains("Attribute Name=\"PublicAttribute with spaces\" Type=\"String\" Get=\"getSampleAttribute\" Set=\"setSampleAttribute\""));
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithPseudoAttribute2 extends HomeItemAdapter2 {

        // Define attribute by member variable
        @HomeItemAttribute(value = "PublicAttribute", set = "mySetter")
        private String sampleAttribute = "Sample Value";

        // Define pseudo attribute by getter
        @HomeItemAttribute("PublicAttribute with spaces")
        public String getSampleAttribute() {
            return "Sample Value";
        }

        // Can't be associated with read-only pseudo attribute
        public void mySetter(String value) {
        }
    }

    @Test
    public void testSampleHomeItemWithPseudoAttribute2() {
        HomeItem instance = new SampleHomeItemWithPseudoAttribute2();
        String result = instance.getModel();
        System.out.println("SampleHomeItemWithPseudoAttribute2");
        System.out.println(result);
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\" Type=\"String\" Get=\"getSampleAttribute\" Set=\"mySetter\""));
        assertTrue(result.contains("Attribute Name=\"PublicAttribute with spaces\" Type=\"String\" Get=\"getSampleAttribute\"/>"));
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithPseudoAttribute3 extends HomeItemAdapter2 {

        // Define attribute by member variable
        @HomeItemAttribute(value = "PublicAttribute", set = "mySetter")
        private String sampleAttribute = "Sample Value";

        // Define pseudo attribute by getter
        @HomeItemAttribute("PublicAttribute with spaces")
        public void setSampleAttribute() {
            // Something;
        }

        // Can't be associated with read-only pseudo attribute
        public void mySetter(String value) {
        }
    }

    @Test
    public void testSampleHomeItemWithPseudoAttribute3() {
        HomeItem instance = new SampleHomeItemWithPseudoAttribute3();
        String result = instance.getModel();
        System.out.println("SampleHomeItemWithPseudoAttribute3");
        System.out.println(result);
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\" Type=\"String\" Set=\"mySetter\""));
        assertTrue(result.contains("Attribute Name=\"PublicAttribute with spaces\" Set=\"setSampleAttribute\"/>"));
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithPseudoAttribute4 extends HomeItemAdapter2 {

        // Define attribute by member variable
        @HomeItemAttribute(value = "PublicAttribute", set = "mySetter")
        private String sampleAttribute = "Sample Value";

        // Define pseudo attribute by getter
        @HomeItemAttribute("PublicAttribute with spaces")
        public void sampleAttribute() {
            // Something;
        }

        // Can't be associated with read-only pseudo attribute
        public void mySetter(String value) {
        }
    }

    @Test
    public void testSampleHomeItemWithPseudoAttribute4() {
        HomeItem instance = new SampleHomeItemWithPseudoAttribute4();
        String result = instance.getModel();
        System.out.println("SampleHomeItemWithPseudoAttribute4");
        System.out.println(result);
        assertTrue(result.contains("Attribute Name=\"PublicAttribute\" Type=\"String\" Set=\"mySetter\""));
        assertFalse(result.contains("Attribute Name=\"PublicAttribute with spaces\" Set=\"setSampleAttribute\"/>"));
    }

    // With @HomeItemAction annotations
    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithAction extends HomeItemAdapter2 {

        @HomeItemAction
        public void on() {
        }

        @HomeItemAction
        public void off() {
        }

        @HomeItemAction(name = "Enable Item")
        public void enableItem() {
        }
    }

    @Test
    public void testSampleHomeItemWithAction() {
        HomeItem instance = new SampleHomeItemWithAction();
        String result = instance.getModel();
        assertTrue(result.contains("Action Name=\"On\""));
        assertTrue(result.contains("Method=\"on\""));
        assertTrue(result.contains("Action Name=\"Off\""));
        assertTrue(result.contains("Method=\"off\""));
        assertTrue(result.contains("Action Name=\"Enable Item\""));
        assertTrue(result.contains("Method=\"enableItem\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithActionSpecificName extends HomeItemAdapter2 {

        @HomeItemAction(name = "CoolThingToDo")
        public void on() {
        }
    }

    @Test
    public void testSampleHomeItemWithActionSpecificName() {
        HomeItem instance = new SampleHomeItemWithActionSpecificName();
        String result = instance.getModel();
        assertTrue(result.contains("Action Name=\"CoolThingToDo\""));
        assertTrue(result.contains("Method=\"on\""));
        System.out.println(result);
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemSpecificNameWithSpecificDynamicAttributes extends HomeItemAdapter2 {

        private final String[] dynaValues = {"One", "two", "Three"};

        @HomeItemAttribute(value = "PublicAttribute", items = "getAttributeItems")
        protected String sampleAttribute = "Sample Value";

        public String[] getAttributeItems() {
            return dynaValues;
        }

        public String getSampleAttribute() {
            return sampleAttribute;
        }
    }

    @Test
    public void testSampleHomeItemSpecificNameWithSpecificDynamicAttributes() {
        System.out.println("testSampleHomeItemSpecificNameWithSpecificDynamicAttributes");
        HomeItem instance = new SampleHomeItemSpecificNameWithSpecificDynamicAttributes();
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("<Attribute Name=\"PublicAttribute\" Type=\"StringList\""));
        assertTrue(result.contains("Get=\"getSampleAttribute\""));
        assertTrue(result.contains("<Item>One</Item>"));
        assertTrue(result.contains("<Item>two</Item>"));
        assertTrue(result.contains("<Item>Three</Item>"));
    }

    @HomeItemType(value = "Controls")
    public class SampleHomeItemWithStandardDynamicAttributes extends HomeItemAdapter2 {

        private final String[] dynaValues = {"Four", "Five", "six"};

        @HomeItemAttribute()
        private String sampleAttribute = "Sample Value";

        public String[] getSampleAttributes() {
            return dynaValues;
        }
    }

    @Test
    public void testSampleHomeItemWithStandardDynamicAttributes() {
        HomeItem instance = new SampleHomeItemWithStandardDynamicAttributes();
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("<Attribute Name=\"SampleAttribute\" Type=\"String\""));
        assertFalse(result.contains("<Attribute Name=\"SampleAttribute\" Type=\"StringList\""));
        assertFalse(result.contains("<Item>Four</Item>"));
        assertFalse(result.contains("<Item>Five</Item>"));
        assertFalse(result.contains("<Item>six</Item>"));
    }

    @HomeItemType(value = "Controls")
    public class HomeItemBaseClass extends HomeItemAdapter2 {

        @HomeItemAttribute(value = "OnCommand", type = "Command", get = "getOnCommand", set = "setOnCommand")
        protected String m_OnCommand = "";
        @HomeItemAttribute(value = "OffCommand", type = "Command", get = "getOffCommand", set = "setOffCommand")
        protected String m_OffCommand = "";
    }

    public class HomeItemExtendedClass extends HomeItemBaseClass {

        @HomeItemAttribute()
        private String sampleAttribute = "Sample Value";
    }

    @Test
    public void testHomeItemExtendedClass() {
        HomeItem instance = new HomeItemExtendedClass();
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("<Attribute Name=\"SampleAttribute\" Type=\"String\""));
        assertTrue(result.contains("<Attribute Name=\"OnCommand\" Type=\"Command\"/>"));
        assertTrue(result.contains("<Attribute Name=\"OffCommand\" Type=\"Command\"/>"));
    }

    @Plugin
    @HomeItemType(value = "Timers", noinherit = false)
    public class HomeItemExtendedClass2 extends HomeItemBaseClass {

        @HomeItemAttribute()
        private String sampleAttribute = "Sample Value";
    }

    @Test
    public void testHomeItemExtendedClass2() {
        HomeItem instance = new HomeItemExtendedClass2();
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("<Attribute Name=\"SampleAttribute\" Type=\"String\""));
        assertTrue(result.contains("<Attribute Name=\"OnCommand\" Type=\"Command\"/>"));
        assertTrue(result.contains("<Attribute Name=\"OffCommand\" Type=\"Command\"/>"));
    }
    
    @Plugin
    @HomeItemType("Timers")
    public class HomeItemExtendedClass3 extends HomeItemBaseClass {

        @HomeItemAttribute()
        private String sampleAttribute = "Sample Value";
    }

    @Test
    public void testHomeItemExtendedClass3() {
        HomeItem instance = new HomeItemExtendedClass3();
        String result = instance.getModel();
        System.out.println(result);
        assertTrue(result.contains("<Attribute Name=\"SampleAttribute\" Type=\"String\""));
        assertFalse(result.contains("<Attribute Name=\"OnCommand\" Type=\"Command\"/>"));
        assertFalse(result.contains("<Attribute Name=\"OffCommand\" Type=\"Command\"/>"));
    }
}
