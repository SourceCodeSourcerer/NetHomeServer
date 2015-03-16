/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNetHome is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Changes:
 * 2015-03-16 - Peter Lagerhem:
 * - Added annotation based model retrieval
 */
package nu.nethome.home.item;

import nu.nethome.home.item.annotation.HomeItemAction;
import nu.nethome.home.item.annotation.HomeItemAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.nethome.home.impl.HomeItemClassInfo;

/**
 * This Adapter is simply a helper to avoid some of the boiler plate code in the
 * HomeItem Using this you only really have to implement the getModel()-method.
 */
public abstract class HomeItemAdapter2 extends HomeItemAdapter implements HomeItem {

    private static final Logger logger = Logger.getLogger(HomeItemAdapter2.class.getName());

    // model tag names
    protected String xmlTag = "<?xml version = \"1.0\"?>\n";
    protected String homeItemTag = "HomeItem";
    private final String newLine = "\n";

    protected String getXmlTag() {
        return xmlTag;
    }

    protected String getHomeItemTag() {
        return homeItemTag;
    }

    // Extended attributes
    protected String category = "";

    protected String getCategory() {
        return category;
    }

    /**
     * Get the definition of the HomeItem model. The model is returned as an
     * XML-String
     *
     * @return XML formatted model description
     */
    @Override
    public String getModel() {

        StringBuilder model = new StringBuilder();
        HomeItemInfo instanceInfo = new HomeItemClassInfo(this.getClass());

        model.append(this.getXmlTag());
        model.append("<HomeItem Class=\"");
        model.append(instanceInfo.getClassName());
        model.append("\"");

        model.append(" Category=\"");
        model.append(instanceInfo.getCategory());
        model.append("\"");

        model.append(">");
        model.append(newLine);

        Class<?> clz = getClass();
        do {
            addFields(clz.getDeclaredFields(), model);
            HomeItemType type = clz.getAnnotation(HomeItemType.class);
            if (null != type && type.noinherit()) {
                clz = null;
            } else {
                clz = clz.getSuperclass();
            }
        } while (null != clz);

        Annotation getAnnotation, setAnnotation;
        for (Method method : getClass().getDeclaredMethods()) {

            String name = "";

            Annotation annotation = method.getAnnotation(HomeItemAction.class);
            if (annotation instanceof HomeItemAction) {
                // Overrides
                HomeItemAction myAnnotation = (HomeItemAction) annotation;
                name = myAnnotation.name().length() > 0 ? myAnnotation.name() : method.getName();

                model.append("<Action Name=\"");
                model.append(composeName(name, "", ""));
                model.append("\"");

                model.append(" Method=\"");
                model.append(method.getName());
                model.append("\"");

                model.append("/>");
                model.append(newLine);
            }

            annotation = method.getAnnotation(HomeItemAttribute.class);
            if (annotation instanceof HomeItemAttribute) {
                // Overrides
                HomeItemAttribute myAnnotation = (HomeItemAttribute) annotation;
                name = myAnnotation.value().length() > 0 ? myAnnotation.value() : method.getName();

                Method aGetMethod = null;
                Method aSetMethod = null;

                boolean isGetMethod = method.getName().startsWith("get");
                if (isGetMethod) {
                    aSetMethod = getMethodNames(composeName(method.getName().substring(3), "set", ""), composeName(name, "set", ""));
                }
                boolean isSetMethod = method.getName().startsWith("set");
                if (isSetMethod) {
                    aGetMethod = getMethodNames(composeName(method.getName().substring(3), "get", ""), composeName(name, "get", ""));
                }

                if (!isGetMethod && !isSetMethod) {
                    // Missing set/get method
                    continue;
                }
                model.append("<Attribute Name=\"");
                model.append(composeName(name, "", ""));
                model.append("\"");

                if (isGetMethod) {
                    model.append(" Type=\"");
                    model.append(method.getReturnType().getSimpleName());
                    model.append("\"");
                }

                if (null != aGetMethod || isGetMethod) {
                    model.append(" Get=\"");
                    model.append(null != aGetMethod ? aGetMethod.getName() : method.getName());
                    model.append("\"");
                }

                if (null != aSetMethod || isSetMethod) {
                    model.append(" Set=\"");
                    model.append(null != aSetMethod ? aSetMethod.getName() : method.getName());
                    model.append("\"");
                }

                model.append("/>");
                model.append(newLine);
            }
        }

        model.append("</HomeItem>");
        return model.toString();
    }

    protected void addFields(Field[] fields, StringBuilder model) {

        for (Field field : fields) {

            String name = "";
            Class<?> classType = field.getType();
            String type = "";
            String def = "";
            Method aGetMethod = null;
            Method aSetMethod = null;
            Method aInitMethod = null;
            Method anItemsMethod = null;

            Annotation annotation = field.getAnnotation(HomeItemAttribute.class);

            if (annotation instanceof HomeItemAttribute) {
                // Overrides
                HomeItemAttribute myAnnotation = (HomeItemAttribute) annotation;
                name = myAnnotation.value().length() > 0 ? myAnnotation.value() : field.getName();
                type = myAnnotation.type().length() > 0 ? myAnnotation.type() : classType.getSimpleName();
                def = myAnnotation.def();

                aGetMethod = getMethodNames(myAnnotation.get(), composeName(field.getName(), "get", ""));
                aSetMethod = getMethodNames(myAnnotation.set(), composeName(field.getName(), "set", ""));
                aInitMethod = getMethodNames(myAnnotation.init(), composeName(field.getName(), "init", ""));

                // Check if this is a StringList type
                anItemsMethod = getMethodNames(myAnnotation.items(), composeName(field.getName(), "get", "Items"));

                model.append("<Attribute Name=\"");
                model.append(composeName(name, "", ""));
                model.append("\" Type=\"");
                boolean isOfTypeArray = (null != anItemsMethod && anItemsMethod.getReturnType().getSimpleName().compareToIgnoreCase("String[]") == 0);
                if (isOfTypeArray) {
                    model.append("StringList");
                } else {
                    if (myAnnotation.type().length() > 0) {
                        model.append(myAnnotation.type());
                    } else {
                        model.append(aGetMethod != null ? aGetMethod.getReturnType().getSimpleName() : type);
                    }
                }
                model.append("\"");

                if (null != aGetMethod) {
                    model.append(" Get=\"");
                    model.append(aGetMethod.getName());
                    model.append("\"");
                }

                if (null != aSetMethod) {
                    model.append(" Set=\"");
                    model.append(aSetMethod.getName());
                    model.append("\"");
                }

                if (null != aInitMethod) {
                    model.append(" Init=\"");
                    model.append(aInitMethod.getName());
                    model.append("\"");
                }

                if (def.length() > 0) {
                    model.append(" Default=\"");
                    model.append(def);
                    model.append("\"");
                }

                if (isOfTypeArray) {
                    model.append(">");
                    model.append(newLine);
                    try {
                        String[] items = (String[]) anItemsMethod.invoke(this, (Object[]) null);
                        for (String item : items) {
                            model.append("<Item>");
                            model.append(item);
                            model.append("</Item>");
                            model.append(newLine);
                        }
                        model.append("</Attribute>");
                        model.append(newLine);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(HomeItemAdapter2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(HomeItemAdapter2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(HomeItemAdapter2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    model.append("/>");
                    model.append(newLine);
                }
            }
        }

    }

    protected Method getMethodNames(String... names) {
        for (String name : names) {
            if (null == name || name.length() == 0) {
                continue;
            }
            for (Method m : getClass().getMethods()) {
                if (m.getName().compareTo(name) == 0) {
                    return m;
                }
            }
        }

        return null;
    }

    protected String composeName(String name, String prefix, String postfix) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1) + postfix;
    }
}
