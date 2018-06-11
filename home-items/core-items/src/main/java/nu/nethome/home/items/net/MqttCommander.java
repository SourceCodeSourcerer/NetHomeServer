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

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.*;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.FinalEventListener;
import nu.nethome.home.system.HomeService;
import nu.nethome.util.plugin.Plugin;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Controls")
public class MqttCommander extends HomeItemAdapter implements HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"MqttCommander\" Category=\"Controls\"  >"
            + "  <Attribute Name=\"Topic\" Type=\"String\" Get=\"getTopic\" 	Set=\"setTopic\" />"
            + "  <Attribute Name=\"Command1\" Type=\"String\" Get=\"getCommand1\" 	Set=\"setCommand1\" />"
            + "  <Attribute Name=\"Command2\" Type=\"String\" Get=\"getCommand2\" 	Set=\"setCommand2\" />"
            + "  <Attribute Name=\"Command3\" Type=\"String\" Get=\"getCommand3\" 	Set=\"setCommand3\" />"
            + "  <Attribute Name=\"Command4\" Type=\"String\" Get=\"getCommand4\" 	Set=\"setCommand4\" />"
            + "  <Attribute Name=\"QOS\" Type=\"StringList\" Get=\"getQos\" 	Set=\"setQos\" >"
            + "     <item>0</item>  <item>1</item> <item>2</item></Attribute>"
            + "  <Attribute Name=\"Retain\" Type=\"Boolean\" Get=\"getRetain\" 	Set=\"setRetain\" />"
            + "  <Attribute Name=\"MqttClient\" Type=\"Item\" Get=\"getMqttClient\" 	Set=\"setMqttClient\" />"
            + "  <Action Name=\"Command1\" Method=\"sendCommand1\" />"
            + "  <Action Name=\"Command2\" Method=\"sendCommand2\" />"
            + "  <Action Name=\"Command3\" Method=\"sendCommand3\" />"
            + "  <Action Name=\"Command4\" Method=\"sendCommand4\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(MqttCommander.class.getName());
    private String topic = "";
    private String command1 = "";
    private String command2 = "";
    private String command3 = "";
    private String command4 = "";
    private int qos = 0;
    private boolean retain;
    private String mqttClient = "";

    public String getModel() {
        return MODEL;
    }

    @Override
    public void activate(HomeService server) {
        super.activate(server);
    }

    public String sendCommand1() {
        sendMQTTMessage(command1);
        return "";
    }

    public String sendCommand2() {
        sendMQTTMessage(command2);
        return "";
    }

    public String sendCommand3() {
        sendMQTTMessage(command3);
        return "";
    }

    public String sendCommand4() {
        sendMQTTMessage(command4);
        return "";
    }

    private void sendMQTTMessage(String message) {
        Event mqtt_message = server.createEvent(MqttClient.MQTT_MESSAGE_TYPE, "");
        mqtt_message.setAttribute("Direction", "Out");
        mqtt_message.setAttribute(MqttClient.MQTT_TOPIC, topic);
        mqtt_message.setAttribute(MqttClient.MQTT_MESSAGE, message);
        mqtt_message.setAttribute(MqttClient.MQTT_QOS, qos);
        mqtt_message.setAttribute(MqttClient.MQTT_RETAIN, retain ? "Yes" : "No");
        if (!mqttClient.isEmpty()) {
            mqtt_message.setAttribute("Mqtt.Client", mqttClient);
        }
        server.send(mqtt_message);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCommand1() {
        return command1;
    }

    public void setCommand1(String  command) {
        this.command1 =  command;
    }

    public String getCommand2() {
        return command2;
    }

    public void setCommand2(String  command) {
        this.command2 =  command;
    }

    public String getCommand3() {
        return command3;
    }

    public void setCommand3(String  command) {
        this.command3 =  command;
    }

    public String getCommand4() {
        return command4;
    }

    public void setCommand4(String  command) {
        this.command4 =  command;
    }

    public String getQos() {
        return getIntAttribute(qos);
    }

    public void setQos(String qos) throws IllegalValueException {
        this.qos = setIntAttribute(qos, 0, 3);
    }

    public String getRetain() {
        return retain ? "Yes" : "No";
    }

    public void setRetain(String retain) {
        this.retain = retain.equalsIgnoreCase("Yes") || retain.equalsIgnoreCase("True");
    }

    public String getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(String mqttClient) {
        this.mqttClient = mqttClient;
    }
}

