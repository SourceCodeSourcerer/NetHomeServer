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

package nu.nethome.home.items;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.system.*;
import nu.nethome.util.plugin.PluginProvider;

import java.util.*;

/**
 * This is an implementation of the HomeService interface intended for
 * unit test cases. It contains a list of HomeItems, which are really
 * MockHomeItemProxy:s and they are created as soon as they are asked for.
 *
 * @author Stefan
 */
public class MockServiceConnection implements HomeService {

    protected Map<String, MockHomeItemProxy> m_Instances = new HashMap<String, MockHomeItemProxy>();
    public List<HomeItem> m_Items = new LinkedList<HomeItem>();
    
    @Override
    public boolean executePython(String pythonCode) {
        return true;
    }
    
    public static class MockEvent implements Event {

        private Map<String, String> attributes = new TreeMap<String, String>();

        public String toString() {
            return "event," + getAttribute(Event.EVENT_TYPE_ATTRIBUTE);
        }

        public MockEvent(String type) {
            setAttribute(Event.EVENT_TYPE_ATTRIBUTE, type);
        }

        public String getAttribute(String name) {
            Object temp = attributes.get(name);
            return temp == null ? "" : temp.toString();
        }

        public int getAttributeInt(String name) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public float getAttributeFloat(String name) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int[] getAttributeArr(String name) {
            return new int[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isType(String typeName) {
            return getAttribute(EVENT_TYPE_ATTRIBUTE).equals(typeName);
        }

        public void setAttribute(String name, String value) {
            attributes.put(name, value);
        }

        public void setAttribute(String name, int value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setAttribute(String name, float value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setAttribute(String name, int[] value) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasAttribute(String name) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String[] getAttributeNames() {
            return attributes.keySet().toArray(new String[attributes.size()]);
        }
    };


    public HomeItemProxy createInstance(String publicClassName,
                                        String instanceName) {
        return null;
    }

    public List<DirectoryEntry> listInstances(String pattern) {
        return null;
    }

    public Event createEvent(String type, String value) {
        return new MockEvent(type);
    }

    public int registerInstance(HomeItem item) {
        m_Items.add(item);
        return 0;
    }

    public void send(Event event) {
        for (HomeItem i : m_Items) {
            i.receiveEvent(event);
        }
    }

    public HomeItemProxy openInstance(String name) {
        if (!m_Instances.containsKey(name)) {
            m_Instances.put(name, new MockHomeItemProxy(name));
        }
        return m_Instances.get(name);
    }

    public boolean removeInstance(String instanceName) {
        return false;
    }

    public boolean renameInstance(String fromInstanceName,
                                  String toInstanceName) {
        return false;
    }

    public List<HomeItemInfo> listClasses() {
        return null;
    }

    public PluginProvider getPluginProvider() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServiceState getState() {
        return null;
    }

    @Override
    public ServiceConfiguration getConfiguration() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopServer() {
    }

    @Override
    public void registerFinalEventListener(FinalEventListener listener) {

    }

    @Override
    public void unregisterFinalEventListener(FinalEventListener listener) {

    }
}
