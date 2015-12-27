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

package nu.nethome.home.items.rollertrol;

import nu.nethome.coders.RollerTrolG;
import nu.nethome.home.item.AutoCreationInfo;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

import static nu.nethome.coders.RollerTrolG.*;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType(value = "Actuators", creationInfo = RollerTrolBlindGSeries.RollerTrolCreationInfo.class)
public class RollerTrolBlindGSeries extends RollerTrolBlind implements HomeItem {

    public static final String ADDRESS_ATTRIBUTE = ROLLER_TROL_G_PROTOCOL_NAME + "." + ADDRESS_NAME;
    public static final String CHANNEL_ATTRIBUTE = ROLLER_TROL_G_PROTOCOL_NAME + "." + CHANNEL_NAME;
    public static final String COMMAND_ATTRIBUTE = ROLLER_TROL_G_PROTOCOL_NAME + "." + RollerTrolG.COMMAND_NAME;
    public static final int MAX_G_ID = ((1 << RollerTrolG.ADDRESS.length) - 1);

    public static class RollerTrolCreationInfo implements AutoCreationInfo {
        static final String[] CREATION_EVENTS = {"RollerTrolG_Message"};
        @Override
        public String[] getCreationEvents() {
            return CREATION_EVENTS;
        }

        @Override
        public boolean canBeCreatedBy(Event e) {
            return true;
        }

        @Override
        public String getCreationIdentification(Event e) {
            return String.format("Rollertrol Blind Motor Series G, Ch: %s, Remote: %s",
                    e.getAttribute(CHANNEL_ATTRIBUTE), e.getAttribute(ADDRESS_ATTRIBUTE));
        }
    }

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"RollerTrolBlindGSeries\" Category=\"Actuators\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"RemoteId\" Type=\"String\" Get=\"getRemoteId\" 	Set=\"setRemoteId\" />"
            + "  <Attribute Name=\"Channel\" Type=\"StringList\" Get=\"getChannel\" Set=\"setChannel\" >"
            + "     <item>1</item> <item>2</item> <item>3</item> <item>4</item> <item>5</item> <item>6</item> <item>7</item> <item>8</item>"
            + "     <item>9</item> <item>10</item> <item>11</item> <item>12</item> <item>13</item> <item>14</item> <item>15</item> </Attribute>"
            + "  <Attribute Name=\"TravelTime\" Type=\"String\" Get=\"getTravelTime\" 	Set=\"setTravelTime\" />"
            + "  <Attribute Name=\"Position1\" Type=\"String\" Get=\"getPosition1\" 	Set=\"setPosition1\" />"
            + "  <Attribute Name=\"Position2\" Type=\"String\" Get=\"getPosition2\" 	Set=\"setPosition2\" />"
            + "  <Attribute Name=\"TransmissionRepeats\" Type=\"String\" Get=\"getRepeats\" 	Set=\"setRepeats\" />"
            + "  <Action Name=\"up\" 	Method=\"blindUp\" />"
            + "  <Action Name=\"stop\" 	Method=\"blindStop\" />"
            + "  <Action Name=\"down\" 	Method=\"blindDown\" />"
            + "  <Action Name=\"toggle\" 	Method=\"blindToggle\" Default=\"true\" />"
            + "  <Action Name=\"Position1\" 	Method=\"position1\" />"
            + "  <Action Name=\"Position2\" 	Method=\"position2\" />"
            + "  <Action Name=\"bindToMotorCW\" 	Method=\"blindLearn\" />"
            + "  <Action Name=\"travelLimitMode\" 	Method=\"blindTravelLimit\" />"
            + "  <Action Name=\"p2\" 	Method=\"blindProgram\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(RollerTrolBlindGSeries.class.getName());

    public RollerTrolBlindGSeries() {
        super((int)(Math.random() * MAX_G_ID));
    }

    protected String getCommandAttributeName() {
        return COMMAND_ATTRIBUTE;
    }

    protected String getChannelAttributeName() {
        return CHANNEL_ATTRIBUTE;
    }

    protected String getAddressAttributeName() {
        return ADDRESS_ATTRIBUTE;
    }

    protected String getProtocolName() {
        return "RollerTrolG_Message";
    }

    protected int getStopCopmmandCode() {
        return RollerTrolG.COMMAND_STOP;
    }

    protected int getDownCommandCode() {
        return RollerTrolG.COMMAND_DOWN;
    }

    protected int getUpCommandCode() {
        return RollerTrolG.COMMAND_UP;
    }

    @Override
    public String getModel() {
        return MODEL;
    }

    public String blindProgram() {
        sendCommand(RollerTrolG.COMMAND_LEARN);
        return "";
    }

    public String blindLearn() {
        try {
            sendCommand(RollerTrolG.COMMAND_LEARN);
            Thread.sleep(3000);
            sendCommand(RollerTrolG.COMMAND_LEARN);
            Thread.sleep(3000);
            sendCommand(RollerTrolG.COMMAND_UP);
        } catch (InterruptedException e) {
            // Ignore
        }
        return "";
    }

    public String blindTravelLimit() {
        try {
            sendCommand(RollerTrolG.COMMAND_LEARN);
            Thread.sleep(3000);
            sendCommand(RollerTrolG.COMMAND_UP);
            Thread.sleep(3000);
            sendCommand(RollerTrolG.COMMAND_LEARN);
        } catch (InterruptedException e) {
            // Ignore
        }
        return "";
    }
}
