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


package nu.nethome.home.items.infra;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;

import java.util.logging.Logger;

/**
 * 
 * Room
 * 
 * @author Stefan
 */
@Plugin
@HomeItemType("Infrastructure")
public class Box extends Room implements HomeItem {

	private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
			+ "<HomeItem Class=\"Box\" Category=\"%s\" Morphing=\"True\" >"
			+ "  <Attribute Name=\"Items\" Type=\"Items\" Get=\"getItems\" 	Set=\"setItems\" />"
            + "  <Attribute Name=\"Position\" Type=\"StringList\" Get=\"getPosition\" 	Set=\"setPosition\">"
            + "    <item>Left</item> <item>Right</item>"
            + "  </Attribute>"
            + "  <Attribute Name=\"Category\" Type=\"StringList\" Get=\"getCategory\" 	Set=\"setCategory\">"
            + "    <item>Lamps</item><item>Timers</item><item>Ports</item><item>GUI</item><item>Hardware</item><item>Controls</item>"
            + "    <item>Gauges</item><item>Thermometers</item><item>Infrastructure</item><item>Actuators</item>"
			+ "  </Attribute>"
			+ "</HomeItem> ");

	private static Logger logger = Logger.getLogger(Box.class.getName());
    private String category = "Infrastructure";
    private String action1 = "";

	public String getModel() {
		return String.format(MODEL, category);
	}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
