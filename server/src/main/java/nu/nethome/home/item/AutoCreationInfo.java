package nu.nethome.home.item;

import nu.nethome.home.system.Event;

/**
 * Gives information regarding auto creation of a HomeItem class
 */
public interface AutoCreationInfo {
    public String[] getCreationEvents();

    public boolean canBeCreatedBy(Event e);

    public String getCreationIdentification(Event e);
}
