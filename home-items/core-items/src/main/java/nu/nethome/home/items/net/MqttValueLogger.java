/*
  Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>

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
@HomeItemType("Gauges")
public class MqttValueLogger extends HomeItemAdapter implements ValueItem, HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"ValueLogger\" Category=\"Gauges\"  >"
            + "  <Attribute Name=\"Value\" Type=\"String\" Get=\"getLastValue\" Default=\"true\" />"
            + "  <Attribute Name=\"ValueAction\" Type=\"Value\" Get=\"getValueAction\" 	Set=\"setValueAction\" />"
            + "  <Attribute Name=\"LogInterval\" Type=\"String\" Get=\"getLogInterval\" 	Set=\"setLogInterval\" />"
            + "  <Attribute Name=\"Topic\" Type=\"String\" Get=\"getTopic\" 	Set=\"setTopic\" />"
            + "  <Attribute Name=\"ValuePrefix\" Type=\"String\" Get=\"getValuePrefix\" 	Set=\"setValuePrefix\" />"
            + "  <Attribute Name=\"QOS\" Type=\"StringList\" Get=\"getQos\" 	Set=\"setQos\" >"
            + "     <item>0</item>  <item>1</item> <item>2</item></Attribute>"
            + "  <Attribute Name=\"Retain\" Type=\"Boolean\" Get=\"getRetain\" 	Set=\"setRetain\" />"
            + "  <Attribute Name=\"ReactOnEvent\" Type=\"Boolean\" Get=\"getReactOnEvent\" 	Set=\"setReactOnEvent\" />"
            + "  <Attribute Name=\"MqttClient\" Type=\"Item\" Get=\"getMqttClient\" 	Set=\"setMqttClient\" />"
            + "</HomeItem> ");

	private static Logger logger = Logger.getLogger(MqttValueLogger.class.getName());
    private CommandLineExecutor executor;
    protected Timer timer;
    private String valueAction = "";
    private int logInterval = 60;
    private String topic = "";
    private int qos = 0;
    private boolean retain;
    private String lastValue = "";
    private String valuePrefix = "";
    private boolean reactOnEvent;
    private FinalEventListener listener;
    private String mqttClient = "";

    public String getModel() {
		return MODEL;
	}

    @Override
    public void activate(HomeService server) {
        super.activate(server);
        executor = new CommandLineExecutor(server, true);
        startTimer();
        listener = new FinalEventListener() {
            @Override
            public void receiveFinalEvent(Event event, boolean isHandled) {
                if (reactOnEvent) {
                    reactOnHandledEvent(event);
                }
            }
        };
        server.registerFinalEventListener(listener);
    }

    private void startTimer() {
        timer = new Timer("MQTTValueLogger", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logValue();
            }
        }, 0, logInterval * 1000);
    }

    private void reactOnHandledEvent(Event event) {
        if (!event.isType(MqttClient.MQTT_MESSAGE_TYPE)) {
            logValue();
        }
    }

    private void logValue() {
        String value = getValue();
        if (!value.isEmpty() && !value.equals(lastValue)) {
            Event mqtt_message = server.createEvent(MqttClient.MQTT_MESSAGE_TYPE, "");
            mqtt_message.setAttribute("Direction", "Out");
            mqtt_message.setAttribute(MqttClient.MQTT_TOPIC, topic);
            mqtt_message.setAttribute(MqttClient.MQTT_MESSAGE, valuePrefix + value);
            mqtt_message.setAttribute(MqttClient.MQTT_QOS, qos);
            mqtt_message.setAttribute(MqttClient.MQTT_RETAIN, retain ? "Yes" : "No");
            if (!mqttClient.isEmpty()) {
                mqtt_message.setAttribute("Mqtt.Client", mqttClient);
            }
            server.send(mqtt_message);
            lastValue = value;
        }
    }

    @Override
    public void stop() {
        stopTimer();
        if (listener != null) {
            server.unregisterFinalEventListener(listener);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public String getValueAction() {
        return valueAction;
    }

    public void setValueAction(String valueAction) {
        this.valueAction = valueAction;
    }

    @Override
    public String getValue() {
        String result = executor.executeCommandLine(getValueAction());
        String results[] = result.split(",");
        if (results.length != 3 || !results[0].equalsIgnoreCase("ok") || results[2].length() == 0) {
            return "";
        }
        return results[2].replace("%2C", ".");
    }

    public String getLogInterval() {
        return getIntAttribute(logInterval);
    }

    public void setLogInterval(String logInterval) throws IllegalValueException {
        final int interval = setIntAttribute(logInterval, 5, 15 * 60);
        if (isActivated() && interval != this.logInterval) {
            stopTimer();
            this.logInterval = interval;
            startTimer();
        }
        this.logInterval = interval;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public String getReactOnEvent() {
        return reactOnEvent ? "Yes" : "No";
    }

    public void setReactOnEvent(String reactOnEvent) {
        this.reactOnEvent = reactOnEvent.equalsIgnoreCase("Yes") || reactOnEvent.equalsIgnoreCase("True");
    }

    public String getLastValue() {
        return lastValue;
    }

    public String getValuePrefix() {
        return valuePrefix;
    }

    public void setValuePrefix(String valuePrefix) {
        this.valuePrefix = valuePrefix;
    }

    public String getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(String mqttClient) {
        this.mqttClient = mqttClient;
    }
}

