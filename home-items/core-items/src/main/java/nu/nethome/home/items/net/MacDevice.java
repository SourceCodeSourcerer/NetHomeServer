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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class MacDevice extends HomeItemAdapter implements HomeItem {

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MacDevice\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"MacAddress\" Type=\"String\" Get=\"getMacAddress\" 	Set=\"setMacAddress\" />"
            + "  <Attribute Name=\"ActionOnAbsent\" Type=\"Command\" Get=\"getActionOnAbsent\" 	Set=\"setActionOnAbsent\" />"
            + "  <Attribute Name=\"ActionOnPresent\" Type=\"Command\" Get=\"getActionOnPresent\" 	Set=\"setActionOnPresent\" />"
            + "  <Attribute Name=\"ActionWhileAbsent\" Type=\"Command\" Get=\"getActionWhileAbsent\" 	Set=\"setActionWhileAbsent\" />"
            + "  <Attribute Name=\"ActionWhilePresent\" Type=\"Command\" Get=\"getActionWhilePresent\" 	Set=\"setActionWhilePresent\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MacDevice.class.getName());
    private String macAddress = "";
    private String actionOnAbsent = "";
    private String actionOnPresent = "";
    private String actionWhileAbsent = "";
    private String actionWhilePresent = "";
    private CommandLineExecutor commandLineExecutor;
    private Boolean isPresent = null;

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
        boolean ourMacIsPresent = isOurMacPresent(event);
        if (ourMacIsPresent) {
            commandLineExecutor.executeCommandLine(actionWhilePresent);
            if (isPresent != null && !isPresent) {
                commandLineExecutor.executeCommandLine(actionOnPresent);
            }
        } else {
            commandLineExecutor.executeCommandLine(actionWhileAbsent);
            if (isPresent != null && isPresent) {
                commandLineExecutor.executeCommandLine(actionOnAbsent);
            }
        }
        isPresent = ourMacIsPresent;
        return false;
    }

    private boolean isOurMacPresent(Event event) {
        boolean ourMacIsPresent = false;
        for (String mac : event.getAttribute(Event.EVENT_VALUE_ATTRIBUTE).split(",")) {
            if (!mac.isEmpty() && mac.equals(this.macAddress)) {
                ourMacIsPresent = true;
                break;
            }
        }
        return ourMacIsPresent;
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

    public String getActionWhileAbsent() {
        return actionWhileAbsent;
    }

    public void setActionWhileAbsent(String actionWhileAbsent) {
        this.actionWhileAbsent = actionWhileAbsent;
    }

    public String getActionWhilePresent() {
        return actionWhilePresent;
    }

    public void setActionWhilePresent(String actionWhilePresent) {
        this.actionWhilePresent = actionWhilePresent;
    }

    public String getState() {
        if (isPresent == null) {
            return "";
        }
        return isPresent ? "Present" : "Absent";
    }

    public String getActionOnAbsent() {
        return actionOnAbsent;
    }

    public void setActionOnAbsent(String actionOnAbsent) {
        this.actionOnAbsent = actionOnAbsent;
    }

    public String getActionOnPresent() {
        return actionOnPresent;
    }

    public void setActionOnPresent(String actionOnPresent) {
        this.actionOnPresent = actionOnPresent;
    }
}
