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

package nu.nethome.home.items.net;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.Arrays;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class LampMessageInteractor extends Message implements HomeItem {

    private class MessageContent {
        public final String command;
        public final String itemName;

        private MessageContent(String command, String itemName) {
            this.command = command;
            this.itemName = itemName;
        }
    }

    private final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"LampMessageInteractor\" Category=\"Ports\"  >"
            + "  <Attribute Name=\"Subject\" Type=\"String\" Get=\"getSubject\" 	Set=\"setSubject\" />"
            + "  <Attribute Name=\"Reply\" Type=\"Text\" Get=\"getMessage\" 	Set=\"setMessage\" />"
            + "  <Attribute Name=\"OnString\" Type=\"String\" Get=\"getOnString\" 	Set=\"setOnString\" />"
            + "  <Attribute Name=\"OffString\" Type=\"String\" Get=\"getOffString\" 	Set=\"setOffString\" />"
            + "  <Attribute Name=\"Lamps\" Type=\"Items\" Get=\"getLamps\" 	Set=\"setLamps\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(LampMessageInteractor.class.getName());
    private String command = "";
    private String onString = "turn on";
    private String offString = "turn off";
    private String lamps = "";

    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (isInboundMessage(event)) {
            return processMessage(event.getAttribute(FROM), event.getAttribute(BODY));
        }
        return false;
    }

    private boolean isInboundMessage(Event event) {
        return event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE).equals(MESSAGE_TYPE) &&
                event.getAttribute(DIRECTION).equals(IN_BOUND);
    }

    private boolean processMessage(String from, String body) {
        MessageContent content = parseContent(body.toLowerCase());
        if (content != null) {
            HomeItemProxy item = openItemFromPartialName(content.itemName);
            if (item != null) {
                try {
                    if (content.command.equals(onString)) {
                        item.callAction("on");
                    } else if (content.command.equals(offString)) {
                        item.callAction("off");
                    }
                    if (!getMessage().isEmpty()) {
                        String reply = this.message.replace("#LAMP", item.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE));
                        sentTo(from, reply);
                    }
                    return true;
                } catch (ExecutionFailure executionFailure) {
                    logger.warning("Error changing lamp state: " + executionFailure.getMessage());
                }
            }
        }
        return false;
    }

    private HomeItemProxy openItemFromPartialName(String itemName) {
        String itemIds[] = lamps.split(",");
        for (String itemId : itemIds) {
            HomeItemProxy proxy = server.openInstance(itemId);
            if (proxy != null && proxy.getAttributeValue(HomeItemProxy.NAME_ATTRIBUTE).toLowerCase().startsWith(itemName)) {
                return proxy;
            }
        }
        return null;
    }

    private MessageContent parseContent(String body) {
        for (String commandString : Arrays.asList(onString, offString)) {
            String result;
            if (body.contains(commandString)) {
                return new MessageContent(commandString, body.replace(commandString, "").trim());
            }
        }
        return null;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOnString() {
        return onString;
    }

    public void setOnString(String onString) {
        this.onString = onString;
    }

    public String getOffString() {
        return offString;
    }

    public void setOffString(String offString) {
        this.offString = offString;
    }

    public String getLamps() {
        return lamps;
    }

    public void setLamps(String lamps) {
        this.lamps = lamps;
    }
}
