/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
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
 */
package nu.nethome.home.items.net;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Reacts to messages received from a MQTT-Topic and calls configured actions
 *
 * @author Jocke G and Patrik Gustavsson
 */

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType(value = "Controls", creationEvents = "Mqtt_Message")
public class MqttCommandPort extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MqttCommandPort\" Category=\"Controls\" >"
            + "  <Attribute Name=\"State\" 	Type=\"String\" Get=\"getState\" Init=\"setState\" Default=\"true\" />"
            + "  <Attribute Name=\"Topic\" 	Type=\"String\" Get=\"getTopic\" Set=\"setTopic\" />"
            + "  <Attribute Name=\"AttributeSeparator\" 	Type=\"String\" Get=\"getAttributeSeparator\" Set=\"setAttributeSeparator\" />"
            + "  <Action Name=\"enable\" 	Method=\"enable\" />"
            + "  <Action Name=\"disable\" 	Method=\"disable\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MqttCommandPort.class.getName());

    // Public attributes
    private CommandLineExecutor commandExecutor;
    private boolean isEnabled = true;
    private String topic = "MyHome/Indoor/Floor1/Livingroom/Lamp";
    private String attributeSeparator = "_";

    public MqttCommandPort() {
    }

    @Override
    public boolean receiveEvent(Event event) {
        if (event.isType("Mqtt_Message")
                && event.getAttribute("Direction").equals("In")
                && (event.getAttribute("Mqtt.Topic").startsWith(topic))) {
            processEvent(event);
            return true;
        } else {
            return handleInit(event);
        }
    }

    protected void processEvent(Event event) {
        if (!isEnabled) {
            return;
        }
        actOnEvent(event);
    }

    public void activate(HomeService server) {
        super.activate(server);
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    protected boolean initAttributes(Event event) {
        topic = event.getAttribute("Mqtt.Topic");
        return true;
    }

    protected void actOnEvent(Event event) {
        String message = event.getAttribute("Mqtt.Message");
        String topic = event.getAttribute("Mqtt.Topic");
        String[] split = topic.split("/");
        String itemPart = split[split.length - 1];
        String[] itemParts = itemPart.split(this.quote(attributeSeparator));
        String itemName = itemParts[0];
        String attributeName = null;
        if (itemParts.length == 2) {
            attributeName = itemParts[1];
        }
        HomeItemProxy homeItemProxy = server.openInstance(itemName);
        if (homeItemProxy != null) {
            try {
                if (itemParts.length == 1) {
                    homeItemProxy.callAction(message);
                } else {
                    homeItemProxy.setAttributeValue(attributeName, message);
                }
            } catch (ExecutionFailure executionFailure) {
                logger.info("MqttCommandPort failed to execute action " + message + " in item " + itemPart);
            } catch (IllegalValueException e) {
                logger.info("MqttCommandPort failed to set attribute " + attributeName + " in item " + itemPart + " to value " + message);
            }
        }
    }

    private String quote(String attributeSeparator) {
        if (Character.isLetterOrDigit(attributeSeparator.charAt(0))) {
            return attributeSeparator;
        } else {
            return "\\" + attributeSeparator;
        }
    }

    public String getModel() {
        return MODEL;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String value) {
        topic = value;
    }

    public String getState() {
        return isEnabled ? "Enabled" : "Disabled";
    }

    public void setState(String state) {
        isEnabled = state.equalsIgnoreCase("Enabled");
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    public String getAttributeSeparator() {
        return attributeSeparator;
    }

    public void setAttributeSeparator(String attributeSeparator) throws IllegalValueException {
        if (attributeSeparator.length() == 1 && !attributeSeparator.equals("/")) {
            this.attributeSeparator = attributeSeparator;
        } else {
            throw new IllegalValueException("Bad separator", attributeSeparator);
        }
    }
}
