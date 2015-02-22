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

package nu.nethome.home.system;

import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.util.plugin.PluginProvider;

import java.util.List;

/**
 * Interface to the services offered by the HomeServer.
 */
public interface HomeService {

    String MINUTE_EVENT_TYPE = "MinuteEvent";

    /**
     * Creates a new instance of an Event
     *
     * @param type  name of the event type
     * @param value value of the event
     * @return a new event instance
     */
    Event createEvent(String type, String value);

    /**
     * Send an event for distribution to all HomeItems
     *
     * @param event The event to send
     */
    void send(Event event);

    /**
     * Finds and opens the HomeItem instance with the specified name.
     *
     * @param name of instance to open
     * @return Instance or null
     */
    HomeItemProxy openInstance(String name);

    /**
     * Create a new instance of a HomeItem with the specified class and name. The created instance is not activated.
     * To activate it the "activate"-method should be called.
     *
     * @param publicClassName class of instance to create
     * @param instanceName instance name
     * @return created instance
     */
    HomeItemProxy createInstance(String publicClassName, String instanceName);

    /**
     * Rename the specified HomeItem instance
     *
     * @param fromInstanceName name or identity of instance to rename
     * @param toInstanceName new instance name
     * @return true if the HomeItem was renamed, false if the renaming failed
     */
    boolean renameInstance(String fromInstanceName, String toInstanceName);

    /**
     * Removes the specified instance. The server will stop the instance before removing it.
     *
     * @param instanceName name or identity of instance to remove
     * @return true if the instance was removed
     */
    boolean removeInstance(String instanceName);

    /**
     * List the names of all HomeItem instances in the server
     *
     * @param pattern a search pattern for finding instances. This is currently ignored
     * @return list of instance names
     */
    List<DirectoryEntry> listInstances(String pattern);

    /**
     * List the names of all HomeItem classes available in the server
     *
     * @return List of names of HomeItem classes
     */
    List<HomeItemInfo> listClasses();

    /**
     * Get the plugin provider for access to available plugin classes
     */
    PluginProvider getPluginProvider();

    /**
     * Get the current state of the service
     */
    ServiceState getState();

    /**
     * Get the global configuration of the server
     */
    ServiceConfiguration getConfiguration();

    /**
     * Stop and exit the server
     */
    void stopServer();

    /**
     * Register a listener for events after they have been offered to all HomeItems
     * @param listener
     */
    void registerFinalEventListener(FinalEventListener listener);

    /**
     * Un-register a listener for events after they have been offered to all HomeItems
     * @param listener
     */
    void unregisterFinalEventListener(FinalEventListener listener);
}

