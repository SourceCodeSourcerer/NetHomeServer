/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
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

package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MacDeviceGroup extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MacDeviceGroup\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"MacAddresses\" Type=\"String\" Get=\"getMacAddresses\" 	Set=\"setMacAddresses\" />"
            + "  <Attribute Name=\"ActionOnAllAbsent\" Type=\"Command\" Get=\"getActionOnAllAbsent\" 	Set=\"setActionOnAllAbsent\" />"
            + "  <Attribute Name=\"ActionOnPresent\" Type=\"Command\" Get=\"getActionOnSomePresent\" 	Set=\"setActionOnSomePresent\" />"
            + "  <Attribute Name=\"ActionWhileAllAbsent\" Type=\"Command\" Get=\"getActionWhileAllAbsent\" 	Set=\"setActionWhileAllAbsent\" />"
            + "  <Attribute Name=\"ActionWhilePresent\" Type=\"Command\" Get=\"getActionWhileSomePresent\" 	Set=\"setActionWhileSomePresent\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MacDeviceGroup.class.getName());
    private String macAddress = "";
    private String actionOnAllAbsent = "";
    private String actionOnSomePresent = "";
    private String actionWhileAllAbsent = "";
    private String actionWhileSomePresent = "";
    private List<String> macAddresses = new ArrayList<>();
    private CommandLineExecutor commandLineExecutor;
    private Boolean isSomePresent = null;

    public String getModel() {
        return MODEL;
    }

    public void activate(HomeService service) {
        super.activate(service);
        commandLineExecutor = new CommandLineExecutor(service, true);
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isMacMessage(event) && isActivated()) {
            return processEvent(event);
        }
        return false;
    }

    private boolean processEvent(Event event) {
        boolean someOfOurMacsArePresent = areSomeOfOurMacsPresent(event);
        if (someOfOurMacsArePresent) {
            commandLineExecutor.executeCommandLine(actionWhileSomePresent);
            if (isSomePresent != null && !isSomePresent) {
                commandLineExecutor.executeCommandLine(actionOnSomePresent);
            }
        } else {
            commandLineExecutor.executeCommandLine(actionWhileAllAbsent);
            if (isSomePresent != null && isSomePresent) {
                commandLineExecutor.executeCommandLine(actionOnAllAbsent);
            }
        }
        isSomePresent = someOfOurMacsArePresent;
        return false;
    }

    private boolean areSomeOfOurMacsPresent(Event event) {
        boolean someMacIsPresent = false;
        Set<String> macsInEvent = new HashSet<>(Arrays.asList(event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).split(",")));
        for (String mac : macAddresses) {
            if (!mac.isEmpty() && macsInEvent.contains(mac)) {
                someMacIsPresent = true;
                break;
            }
        }
        return someMacIsPresent;
    }

    private boolean isMacMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(ArpScanner.ARP_SCAN_MESSAGE);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getActionWhileAllAbsent() {
        return actionWhileAllAbsent;
    }

    public void setActionWhileAllAbsent(String actionWhileAbsent) {
        this.actionWhileAllAbsent = actionWhileAbsent;
    }

    public String getActionWhileSomePresent() {
        return actionWhileSomePresent;
    }

    public void setActionWhileSomePresent(String actionWhilePresent) {
        this.actionWhileSomePresent = actionWhilePresent;
    }

    public String getState() {
        if (isSomePresent == null) {
            return "";
        }
        return isSomePresent ? "Present" : "Absent";
    }

    public String getActionOnAllAbsent() {
        return actionOnAllAbsent;
    }

    public void setActionOnAllAbsent(String actionOnAbsent) {
        this.actionOnAllAbsent = actionOnAbsent;
    }

    public String getActionOnSomePresent() {
        return actionOnSomePresent;
    }

    public void setActionOnSomePresent(String actionOnPresent) {
        this.actionOnSomePresent = actionOnPresent;
    }

    public String getMacAddresses() {
         String result = "";
         String separator = "";
         for (String s : macAddresses) {
             result += separator + s;
             separator = ",";
         }
         return result;
     }

     public void setMacAddresses(String texts) {
         List<String> senders = new ArrayList<>();
         if (!texts.isEmpty()) {
             senders.addAll(Arrays.asList(texts.toLowerCase().split(",")));
         }
         this.macAddresses = senders;
     }
}
