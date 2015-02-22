/*
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

import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds static HomeITem class info
 */
public class HomeItemClassInfo implements HomeItemInfo {

    private Class<? extends HomeItem> itemClass;
    private HomeItemType type;
    AutoCreationInfo creationInfo;

    public HomeItemClassInfo(Class<? extends HomeItem> itemClass) {
        this.itemClass = itemClass;
        type = itemClass.getAnnotation(HomeItemType.class);
        if (!hasCreationInfo(type)) {
            creationInfo = new NoCreationInfo();
        } else if (hasCreationEventsSpecified(type)){
            creationInfo = new GenericAutoCreationInfo(type.creationEvents());
        } else {
            try {
                creationInfo = type.creationInfo().newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                creationInfo = new NoCreationInfo();
            }
        }
    }

    private boolean hasCreationEventsSpecified(HomeItemType type) {
        return !type.creationEvents().isEmpty();
    }

    private boolean hasCreationInfo(HomeItemType type) {
        return type != null && (!type.creationEvents().isEmpty() || type.creationInfo() != AutoCreationInfo.class);
    }

    @Override
    public String getClassName() {
        return itemClass.getSimpleName();
    }

    @Override
    public String getCategory() {
        return (type != null) ? type.value() : "Unknown";
    }

    @Override
    public String[] getCreationEventTypes() {
        return creationInfo.getCreationEvents();
    }

    @Override
    public Boolean canBeCreatedBy(Event event) {
        return creationInfo.canBeCreatedBy(event);
    }

    @Override
    public String getCreationIdentification(Event event) {
        return creationInfo.getCreationIdentification(event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HomeItemClassInfo that = (HomeItemClassInfo) o;

        if (!itemClass.getSimpleName().equals(that.itemClass.getSimpleName())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return itemClass.getSimpleName().hashCode();
    }

    private static class NoCreationInfo implements AutoCreationInfo {

        @Override
        public String[] getCreationEvents() {
            return new String[0];
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return false;
        }

        @Override
        public String getCreationIdentification(Event e) {
            return "";
        }
    }

    private static class GenericAutoCreationInfo implements AutoCreationInfo {
        private static String ignoredAttributeNames[] = {"Type", "UPM.SequenceNumber", "Direction", "Value", "UPM.Primary",
                "UPM.Secondary", "UPM.LowBattery", "Hue.Brightness", "Hue.Command", "Oregon.Temp", "Oregon.Moisture"};
        private static Set<String> ignoredAttributes = new HashSet<String>(Arrays.asList(ignoredAttributeNames));
        private String[] eventList;

        public GenericAutoCreationInfo(String events) {
            eventList = events.split(",");
        }

        @Override
        public String[] getCreationEvents() {
            return eventList;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            String eventName = e.getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
            for (String s : eventList) {
                if (s.equals(eventName)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getCreationIdentification(Event e) {
            return extractContent(e);
        }

        private String extractContent(Event event) {
            String divider="";
            StringBuilder result = new StringBuilder();
            result.append(stripProtocolSuffix(event.getAttribute("Type")));
            result.append(":");
            for (String attributeName : event.getAttributeNames()) {
                String value = event.getAttribute(attributeName);
                if (!isAttributeIgnored(attributeName, value)) {
                    result.append(divider);
                    result.append(stripNamePrefix(attributeName));
                    result.append("=");
                    result.append(value);
                    divider = ",";
                }
            }
            return result.toString();
        }

        private String stripProtocolSuffix(String type) {
            int index = type.indexOf("_");
            if (index > 0 && index < type.length() - 1) {
                return type.substring(0, index);
            }
            return type;
        }

        private String stripNamePrefix(String attributeName) {
            int index = attributeName.indexOf(".");
            if (index > 0 && index < attributeName.length() - 1) {
                return attributeName.substring(index + 1, attributeName.length());
            }
            return attributeName;
        }

        private boolean isAttributeIgnored(String attributeName, String value) {
            return ignoredAttributes.contains(attributeName) || value.length() == 0;
        }
    }
}
