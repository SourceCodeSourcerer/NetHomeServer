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

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import nu.nethome.home.item.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * This class can process the HomeItem model description XML-file and extract the
 * information into Java objects. It is used both internally by the server for model
 * processing and by HomeItems via the HomeItemModel interface
 */
public class StaticHomeItemModel implements HomeItemModel {

    private final int modelHash;
    private String className = "";
    private String category = "";
    private int startOrder = 5;
    private ActionModel defaultAction;
    private ReflectionAttributeModel defaultAttribute;
    private Map<String, ActionModel> actions = new HashMap<String, ActionModel>();
    private Map<String, ReflectionAttributeModel> attributes = new HashMap<String, ReflectionAttributeModel>();
    private List<AttributeModel> attributesInOrder = new ArrayList<AttributeModel>();
    private List<Action> actionsInOrder = new ArrayList<Action>();
    private static Map<Class<? extends HomeItem>, StaticHomeItemModel> modelCache = new HashMap<Class<? extends HomeItem>, StaticHomeItemModel>();
    private static ReflectionAttributeModel nameAttribute = new ReflectionAttributeModel(HomeItemProxy.NAME_ATTRIBUTE, "String", null, HomeItem.class, "getName", null, null);
    private static ReflectionAttributeModel modelAttribute = new ReflectionAttributeModel(HomeItemProxy.MODEL_ATTRIBUTE, "String", null, HomeItem.class, "getModel", null, null);
    private boolean isMorphing = false;

    public static StaticHomeItemModel getModel(HomeItem item) throws ModelException {
        synchronized (modelCache) {
            StaticHomeItemModel foundModel = modelCache.get(item.getClass());
            if ((foundModel == null) || (foundModel != null && foundModel.isMorphing() && foundModel.hasMorphed(item))) {
                foundModel = new StaticHomeItemModel(item);
                modelCache.put(item.getClass(), foundModel);
            }
            return foundModel;
        }
    }

    public static void clearCache() {
        modelCache.clear();
    }

    public StaticHomeItemModel(HomeItem item) throws ModelException {
        String modelXML = item.getModel();
        modelHash = modelXML.hashCode();
        // Start parsing the Items model description
        DOMParser parser = new DOMParser();
        ByteArrayInputStream byteStream;
        try {
            byteStream = new ByteArrayInputStream(modelXML.getBytes("UTF-8"));
            InputSource source = new InputSource(byteStream);
            parser.parse(source);
        } catch (UnsupportedEncodingException e1) {
            throw new ModelException("Failed parsing model for " + item.getName(), e1);
        } catch (Exception e) {
            throw new ModelException("Failed parsing model for " + item.getName(), e);
        }
        Document document = parser.getDocument();
        parseHomeItemDocument(document, item.getClass());
        addDefaultAttributes();
    }

    private void addDefaultAttributes() {
        attributes.put(nameAttribute.getName(), nameAttribute);
        attributes.put(modelAttribute.getName(), modelAttribute);
    }

    private void parseHomeItemDocument(Document document, Class<? extends HomeItem> aClass) throws ModelException {
        Node homeItem = document.getDocumentElement();
        if (!homeItem.getNodeName().equals("HomeItem")) {
            throw new ModelException("Parsing failed, Not a HomeItem");
        }
        parseItemAttributes(homeItem);

        NodeList elements = homeItem.getChildNodes();
        if (elements != null) {
            int numberOfChilds = elements.getLength();
            for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
                Node currentNode = elements.item(loopIndex);
                if (isActionNode(currentNode)) {
                    parseActionModel(currentNode, aClass);
                }
                if (isAttributeNode(currentNode)) {
                    parseAttributeModel(currentNode, aClass);
                }
            }
        }
    }

    private void parseAttributeModel(Node attributeNode, Class<? extends HomeItem> aClass) {
        NamedNodeMap nodeAttributes = attributeNode.getAttributes();
        String name = getNodeAttributeValue(nodeAttributes, HomeItemProxy.NAME_ATTRIBUTE);
        String getMethod = getNodeAttributeValue(nodeAttributes, "Get");
        String setMethod = getNodeAttributeValue(nodeAttributes, "Set");
        String initMethod = getNodeAttributeValue(nodeAttributes, "Init");
        String type = getNodeAttributeValue(nodeAttributes, "Type");
        String unit = getNodeAttributeValue(nodeAttributes, "Unit");
        List<String> values = parseAttributeStringList(attributeNode);
        if (name != null && type != null) {
            ReflectionAttributeModel model = new ReflectionAttributeModel(name, type, unit, aClass, getMethod, setMethod, initMethod, values);
            addAttribute(name, model);
            if (getNodeAttributeValue(nodeAttributes, "Default") != null) {
                defaultAttribute = model;
            }
        }
    }

    private void addAttribute(String name, ReflectionAttributeModel model) {
        attributes.put(name, model);
        attributesInOrder.add(model);
    }

    private List<String> parseAttributeStringList(Node item) {
        List<String> result = new ArrayList<String>();
        if (item.hasChildNodes()) {
            // Grab all the values of the model and save
            NodeList listOfPossibleAttributeValues = item.getChildNodes();
            for (int i = 0; i < listOfPossibleAttributeValues.getLength(); i++) {
                if (listOfPossibleAttributeValues.item(i).getNodeName()
                        .compareToIgnoreCase("item") == 0) {
                    result.add(listOfPossibleAttributeValues.item(
                            i).getTextContent());
                }
            }
        }
        return result;
    }

    private String getNodeAttributeValue(NamedNodeMap attributes, String nameAttribute) {
        if ((attributes == null) || (attributes.getNamedItem(nameAttribute) == null)) {
            return null;
        }
        return attributes.getNamedItem(nameAttribute).getNodeValue();
    }

    private void parseActionModel(Node actionNode, Class<? extends HomeItem> aClass) {
        NamedNodeMap attributes = actionNode.getAttributes();
        String name = getNodeAttributeValue(attributes, HomeItemProxy.NAME_ATTRIBUTE);
        String method = getNodeAttributeValue(attributes, "Method");
        if (name != null && method != null) {
            try {
                ActionModel model = new ActionModel(name, method, aClass);
                addAction(name, model);
                if (getNodeAttributeValue(attributes, "Default") != null) {
                    defaultAction = model;
                }
            } catch (NoSuchMethodException e) {
                // Not adding the action
            }
        }
    }

    private void addAction(String name, ActionModel model) {
        actions.put(name, model);
        actionsInOrder.add(model);
    }

    private boolean isAttributeNode(Node currentNode) {
        return currentNode.getNodeName().equals("Attribute");
    }

    private boolean isActionNode(Node currentNode) {
        return currentNode.getNodeName().equals("Action");
    }

    private void parseItemAttributes(Node homeItem) {
        int numberAttributes = 0;
        if (homeItem.getAttributes() != null) {
            numberAttributes = homeItem.getAttributes().getLength();
        }
        // Loop through all the attribute element's attributes and process them
        for (int elemAttIndex = 0; elemAttIndex < numberAttributes; elemAttIndex++) {
            Attr attribute = (Attr) homeItem.getAttributes().item(elemAttIndex);
            String name = attribute.getNodeName();
            String value = attribute.getNodeValue();
            // Check for Name Element
            if (name.equals("Class")) {
                className = value;
            }
            if (name.equals("Category")) {
                category = value;
            }
            if (name.equals("StartOrder")) {
                startOrder = Integer.parseInt(value);
            }
            if (name.equals("Morphing") && value.equalsIgnoreCase("true")) {
                this.isMorphing = true;
            }
        }
    }

    /**
     * @return the Category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return the ClassName
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the startOrder
     */
    public int getStartOrder() {
        return startOrder;
    }

    /**
     * @return the default action of the class. Empty string if none.
     */
    public String getDefaultAction() {
        return defaultAction != null ? defaultAction.getName() : "";
    }

    /**
     * @return the default attribute of the class. Empty string if none.
     */
    public AttributeModel getDefaultAttribute() {
        return defaultAttribute;
    }

    @Override
    public boolean hasAttribute(String attributeName) {
        return attributes.get(attributeName) != null;
    }

    @Override
    public boolean hasAction(String actionName) {
        return actions.get(actionName) != null;
    }

    public ActionModel getAction(String name) throws ModelException {
        ActionModel action = actions.get(name);
        if (action == null) {
            throw new ModelException("No such action: " + name + " in " + className);
        }
        return action;
    }

    public ReflectionAttributeModel getAttribute(String attributeName) throws ModelException {
        ReflectionAttributeModel attribute = attributes.get(attributeName);
        if (attribute == null) {
            throw new ModelException("No such attribute: " + attributeName + " in " + className);
        }
        return attribute;
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actionsInOrder);
    }

    public List<AttributeModel> getAttributes() {
        return Collections.unmodifiableList(attributesInOrder);
    }

    public boolean isMorphing() {
        return isMorphing;
    }

    public boolean hasMorphed(HomeItem item) {
        return item.getModel().hashCode() != modelHash;
    }
}
