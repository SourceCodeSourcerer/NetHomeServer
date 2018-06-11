/*
  Copyright (C) 2005-2018, Stefan Strömberg <stefangs@nethome.nu>

  This file is part of OpenNetHome  (http://www.nethome.nu)

  OpenNetHome is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  OpenNetHome is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.net;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Lamps")
public class MqttLamp extends MqttCommander implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MqttCommander\" Category=\"Lamps\"  >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" 	Set=\"setState\" Default=\"True\" />"
            + "  <Attribute Name=\"Topic\" Type=\"String\" Get=\"getTopic\" 	Set=\"setTopic\" />"
            + "  <Attribute Name=\"OnMessage\" Type=\"String\" Get=\"getCommand1\" 	Set=\"setCommand1\" />"
            + "  <Attribute Name=\"OffMessage\" Type=\"String\" Get=\"getCommand2\" 	Set=\"setCommand2\" />"
            + "  <Attribute Name=\"QOS\" Type=\"StringList\" Get=\"getQos\" 	Set=\"setQos\" >"
            + "     <item>0</item>  <item>1</item> <item>2</item></Attribute>"
            + "  <Attribute Name=\"Retain\" Type=\"Boolean\" Get=\"getRetain\" 	Set=\"setRetain\" />"
            + "  <Attribute Name=\"MqttClient\" Type=\"Item\" Get=\"getMqttClient\" 	Set=\"setMqttClient\" />"
            + "  <Action Name=\"On\" Method=\"on\" />"
            + "  <Action Name=\"Off\" Method=\"off\" />"
            + "  <Action Name=\"Toggle\" Method=\"toggle\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MqttLamp.class.getName());
    private String state = "Off";
    public String getModel() {
        return MODEL;
    }

    @Override
    public boolean receiveEvent(Event event) {
        // Check the event and see if they affect our current state.
        if (event.isType(MqttClient.MQTT_MESSAGE_TYPE)
                && event.getAttribute("Direction").equals("In")
                && (event.getAttribute(MqttClient.MQTT_TOPIC).equals(getTopic()))) {
            //Ok, this event affects us, act on it
            processEvent(event.getAttribute(MqttClient.MQTT_MESSAGE));
            return true;
        }
        return false;
    }

    private void processEvent(String event) {
        if (event.equalsIgnoreCase(getCommand1())) {
            state = "On";
        } else if (event.equalsIgnoreCase(getCommand2())) {
            state = "Off";
        }
    }

    public String on() {
        sendCommand1();
        state = "On";
        return "";
    }

    public String off() {
        sendCommand2();
        state = "Off";
        return "";
    }

    public String toggle() {
        if (state.equals("Off")) {
            on();
        } else {
            off();
        }
        return "";
    }

    public String getState() {
        return state;
    }

    public void setState(String  state) {
        this.state =  state;
    }
}

