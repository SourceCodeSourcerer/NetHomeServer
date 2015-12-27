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
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemModel;
import nu.nethome.home.item.HomeItemProxy;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeItemFileLoader implements HomeItemLoader {

    private static Logger logger = Logger.getLogger(HomeItemFileLoader.class.getName());

    public final void saveItems(List<HomeItem> items, String fileName) {
        try {
            Iterator<HomeItem> i = items.iterator();
            // Open the output file and make sure we use UTF-8 as encoding
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"));

            // Print XML Header
            out.write("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
            out.newLine();
            out.write("<HomeItems>");
            out.newLine();
            while (i.hasNext()) {
                HomeItem rawItem = i.next();
                HomeItemProxy item = new LocalHomeItemProxy(rawItem);
                // Begin Item
                out.write("\t<HomeItem Class=\"" + rawItem.getClass().getSimpleName()
                        + "\" >");
                out.newLine();
                out.write("\t\t<Attribute Name=\"ID\">" + rawItem.getItemId()
                        + "</Attribute>");
                out.newLine();
                out.write("\t\t<Attribute Name=\"Name\">" + rawItem.getName()
                        + "</Attribute>");
                out.newLine();

                // Print attributes
                for (Attribute attribute : item.getAttributeValues()) {
                    if (!attribute.isWriteOnly()) {
                        try {
                            String name = escape(attribute.getName());
                            String value = escape(attribute.getValue());
                            out.write("\t\t<Attribute Name=\"" + name + "\">" + value + "</Attribute>");
                            out.newLine();
                        } catch (Exception e) {
                            logger.info("Warning! the attribute name or value throws exception: " + e.toString());
                        }

                    }
                }

                // Print end of Item
                out.write("\t</HomeItem>");
                out.newLine();
            }
            out.write("</HomeItems>");
            out.newLine();
            out.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save HomeItems", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not save HomeItems", e);
        }
    }

    public final List<HomeItem> loadItems(String fileName, HomeItemFactory factory, HomeServer homeServer) {
        List<HomeItem> loadedItems = new ArrayList<HomeItem>();
        try {
            logger.info("Loading Items from " + fileName);
            DOMParser parser = new DOMParser();
            parser.parse(fileName);
            Document document = parser.getDocument();
            Node firstNode = document.getDocumentElement();
            NodeList elements = firstNode.getChildNodes();
            int numberOfChilds = elements.getLength();


            // Loop through all instances in the file and create the HomeItems
            for (int loopIndex = 0; loopIndex < numberOfChilds; loopIndex++) {
                if (elements.item(loopIndex).getNodeName().equals("HomeItem")) {

                    // Create the HomeItem from the attribute values
                    try {
                        loadedItems.add(createItemFromNode(elements.item(loopIndex), factory, homeServer));
                    } catch (Exception e) {
                        logger.warning("Failed to load Item nr" + Integer.toString(loopIndex) + "from file. " + e.getMessage());
                    }
                }
            }
        } catch (SAXException e) {
            logger.warning(fileName + " is not well-formed.");
        } catch (IOException e) {
            logger.warning("Due to an IOException, the parser could not load " + fileName);
        }
        return loadedItems;
    }

    private HomeItem createItemFromNode(Node itemNode, HomeItemFactory factory, HomeServer homeServer) throws Exception {
        HomeItem result;
        String className = extractItemClassName(itemNode);
        if (className == null) {
            throw new Exception("No class name specified");
        }

        // Check if it is us
        if (className.equals(homeServer.getClass().getName()) ||
                className.equals(homeServer.getClass().getSimpleName()) ||
                className.equals("BridgeBroker")) {
            // Yes, this is us (and we are already created)
            result = homeServer;
        } else {
            // Try to instantiate a HomeItem with this class name
            result = factory.createInstance(className);
        }
        if (result == null) {
            throw new Exception("Could not create class: " + className);
        }
        extractAndApplyAttributeValues(itemNode, result);

        return result;
    }

    private String extractItemClassName(Node itemNode) {
        String className = null;
        int numberAttributes = 0;

        // Get the attributes of the Item Node
        if (itemNode.getAttributes() != null) {
            numberAttributes = itemNode.getAttributes().getLength();
        }
        // Loop through them (but currently there should only be one)
        for (int loopIndex = 0; loopIndex < numberAttributes; loopIndex++) {
            Attr attribute = (Attr) itemNode.getAttributes().item(loopIndex);
            // Check for Class Element
            if (attribute.getNodeName().equals("Class")) {
                className = attribute.getNodeValue();
            }
        }
        return className;
    }

    private void extractAndApplyAttributeValues(Node itemNode, HomeItem item) throws ModelException {
        StaticHomeItemModel model = StaticHomeItemModel.getModel(item);
        NodeList attributeElements = itemNode.getChildNodes();

        for (int attributeIndex = 0; attributeIndex < attributeElements.getLength(); attributeIndex++) {
            Node attributeNode = attributeElements.item(attributeIndex);
            if (isItemAttributeNode(attributeNode)) {
                String attributeName = extractItemAttributeName(attributeNode);
                if (attributeName != null) {
                    initiateAttributeValue(item, model, attributeName, extractItemAttributeValue(attributeNode));
                }
            }
        }
    }

    private boolean isItemAttributeNode(Node attributeNode) {
        return attributeNode.getNodeName().equals("Attribute");
    }

    private String extractItemAttributeValue(Node attributeNode) {
        // Get the actual value of the attribute
        Node attributeValue = attributeNode.getFirstChild();
        String value;
        if (attributeValue != null) {
            value = attributeValue.getNodeValue();
        } else {
            value = "";
        }
        return value;
    }

    private String extractItemAttributeName(Node attributeNode) {
        int numberAttributes;// Get the attributes of the Attribute Node
        numberAttributes = 0;
        if (attributeNode.getAttributes() != null) {
            numberAttributes = attributeNode.getAttributes().getLength();
        }
        String attributeName = null;
        // Loop through them (but currently there should only be one)
        for (int loopIndex = 0; loopIndex < numberAttributes; loopIndex++) {
            Attr attribute = (Attr) attributeNode.getAttributes().item(loopIndex);
            // Check for Name Element
            if (attribute.getNodeName().equals("Name")) {
                attributeName = attribute.getNodeValue();
            }
        }
        return attributeName;
    }

    private void initiateAttributeValue(HomeItem item, StaticHomeItemModel model, String attributeName, String value) {
        try {
            if (attributeName.equals(HomeItemProxy.ID_ATTRIBUTE)) {
                item.setItemId(Long.parseLong(value));
            } else if (attributeName.equals(HomeItemProxy.NAME_ATTRIBUTE)) {
                item.setName(value);
            } else {
                ReflectionAttributeModel attributeModel = model.getAttribute(attributeName);
                if (attributeModel.isCanInit()) {
                    attributeModel.initValue(item, value);
                }
            }
        } catch (InvocationTargetException e) {
            logger.log(Level.INFO, "Failed initializing attribute " + attributeName + " due to exception in setter", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.INFO, "Failed initializing attribute " + attributeName + " due to bad setter method", e);
        } catch (ModelException e) {
            logger.log(Level.INFO, "Failed initializing attribute " + attributeName + ": " + e.getMessage(), e);
        }
    }

    // assumes UTF-8 or UTF-16 as encoding,
    public String escape(String content) {
        if (content == null) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '<') {
                buffer.append("&lt;");
            } else if (c == '>') {
                buffer.append("&gt;");
            } else if (c == '&') {
                buffer.append("&amp;");
            } else if (c == '"') {
                buffer.append("&quot;");
            } else if (c == '\'') {
                buffer.append("&apos;");
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }


}