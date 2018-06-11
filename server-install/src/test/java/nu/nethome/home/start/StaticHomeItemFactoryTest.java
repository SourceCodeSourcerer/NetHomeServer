/*
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

package nu.nethome.home.start;

import nu.nethome.home.impl.ModelException;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.Action;
import nu.nethome.home.item.Attribute;
import nu.nethome.home.item.HomeItemInfo;
import nu.nethome.home.item.HomeItemProxy;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Stefan 2014-01-05
 */
public class StaticHomeItemFactoryTest {

    StaticHomeItemFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new StaticHomeItemFactory();
    }

    @Ignore
    @Test
    public void generateDoc() {
        for (HomeItemInfo classInfo : factory.listItemTypes()) {
            HomeItemProxy pr = null;
            try {
                pr = new LocalHomeItemProxy(factory.createInstance(classInfo.getClassName()), null);
            } catch (Throwable e) {
                continue;
            }
            Iterator<Attribute> atts = pr.getAttributeValues().iterator();
            Iterator<Action> actions = pr.getModel().getActions().iterator();
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter("c:\\doc\\" + classInfo.getClassName() + ".txt", true));
                out.write("{{::server:" + itemIcon(classInfo.getCategory(), true) + "?nolink|}}");
                out.newLine();
                out.write("====== " + classInfo.getClassName() + " ======");
                out.newLine();
                out.write("This module is a part of the [[NetHomeServer]]. The " + classInfo.getClassName() + " is not yet documented.");
                out.newLine();
                out.write("===== Attributes =====");
                out.newLine();
                while (atts.hasNext()) {
                    Attribute att = atts.next();
                    out.write("  * **" + att.getName() + "** //[get]");
                    if (!att.isReadOnly()) {
                        out.write(" [set]");
                    }
                    if (att.isReadOnly() && att.isCanInit()) {
                        out.write(" [init]");
                    }
                    out.write("// ");
                    out.newLine();
                }
                out.write("===== Actions =====");
                out.newLine();
                while (actions.hasNext()) {
                    Action action = actions.next();
                    out.write("  * **" + action.getName() + "**  ");
                    out.newLine();
                }
                out.write("==== See also ====");
                out.newLine();
                out.write("[[NetHomeServer]]");
                out.newLine();
                out.flush();
                out.close();
            } catch (IOException e) {
                System.out.println("Failed generating doc");
            }
        }
    }

    public String itemIcon(String itemType, boolean big) {
        if (itemType.equals("Lamps")) {
            return big ? "lamp64_off.png" : "lamp_off.png";
        }
        if (itemType.equals("Timers")) {
            return big ? "clock64.png" : "timer.png";
        }
        if (itemType.equals("Ports")) {
            return big ? "port64.png" : "port.png";
        }
        if (itemType.equals("GUI")) {
            return big ? "gui64.png" : "gui.png";
        }
        if (itemType.equals("Hardware")) {
            return big ? "hw64.png" : "hw.png";
        }
        if (itemType.equals("Controls")) {
            return big ? "control64.png" : "control.png";
        }
        if (itemType.equals("Gauges")) {
            return big ? "gauge64.png" : "gauge.png";
        }
        if (itemType.equals("Thermometers")) {
            return big ? "thermometer64.png" : "thermometer.png";
        }
        if (itemType.equals("Infrastructure")) {
            return big ? "infra64.png" : "infra.png";
        }
        return big ? "item64.png" : "item.png";
    }
}
