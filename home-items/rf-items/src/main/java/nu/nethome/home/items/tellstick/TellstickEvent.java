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

package nu.nethome.home.items.tellstick;

public class TellstickEvent {
    private long data;
    private String dataString;
    private String model = "";
    private String protocol = "";
    private String signalClass = "";

    public TellstickEvent(String message) {
        String eventData = message.substring(2);
        String attributes[] = eventData.split(";");
        for (String attribute : attributes) {
            String fields[] = attribute.split(":");
            if (fields[0].equals("protocol")) {
                protocol = fields[1];
            } else if (fields[0].equals("model")) {
                model = fields[1];
            } else if (fields[0].equals("class")) {
                signalClass = fields[1];
            } else if (fields[0].equals("data") && fields[1].startsWith("0x") && fields[1].length() > 2) {
                data = Long.parseLong(fields[1].substring(2), 16);
            } else if (fields[0].equals("data") && fields[1].length() > 0) {
                dataString = fields[1];
            }
        }
    }

    public long getData() {
        return data;
    }

    public String getDataString() {
        return dataString;
    }

    public String getModel() {
        return model;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getSignalClass() {
        return signalClass;
    }

    public String getEventProtocolModel() {
        return String.format("protocol:%s;model:%s", protocol, model);
    }

    public String getEventProtocol() {
        return String.format("protocol:%s", protocol, model);
    }
}
