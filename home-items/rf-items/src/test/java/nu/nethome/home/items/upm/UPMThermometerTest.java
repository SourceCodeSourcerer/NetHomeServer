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

package nu.nethome.home.items.upm;

import nu.nethome.home.impl.ModelException;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.items.util.ItemAttributeTester;
import nu.nethome.home.items.util.TstEvent;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: Stefan
 * Date: 2012-06-03
 * Time: 21:25
 */
public class UPMThermometerTest {

    private UPMThermometer temp;
    private HomeService server;
    private HomeItemProxy proxy;

    @Before
    public void setUp() throws Exception {
        temp = new UPMThermometer();
        server = new TstHomeService();
        proxy = new LocalHomeItemProxy(temp, null);
        temp.setHouseCode("4");
        temp.setDeviceCode("3");
        temp.activate(server);
    }

    @Test
    public void attributes() throws IllegalValueException, ModelException {
        ItemAttributeTester.testAttributes(temp, "HouseCode=1", "DeviceCode=1", "LogFile=foo", "K=1.0", "M=1.0");
    }

    @Test
    public void initialValues() throws IllegalValueException {
        assertThat(proxy.getAttributeValue("Temperature"), is(""));
        assertThat(proxy.getAttributeValue("LastUpdate"), is(""));
        assertThat(proxy.getAttributeValue("TimeSinceUpdate"), is("0"));
    }

    @Test
    public void otherHouseCode() throws IllegalValueException {
        temp.setHouseCode("1");
        Event testEvent = createEvent(20, false);
        temp.receiveEvent(testEvent);
        assertThat(proxy.getAttributeValue("Temperature"), is(""));
        assertThat(proxy.getAttributeValue("LastUpdate"), is(""));
        assertThat(proxy.getAttributeValue("TimeSinceUpdate"), is("0"));
    }

    @Test
    public void otherDeviceCode() throws IllegalValueException {
        temp.setDeviceCode("1");
        Event testEvent = createEvent(20, false);
        temp.receiveEvent(testEvent);
        assertThat(proxy.getAttributeValue("Temperature"), is(""));
        assertThat(proxy.getAttributeValue("LastUpdate"), is(""));
        assertThat(proxy.getAttributeValue("TimeSinceUpdate"), is("0"));
    }

    @Test
    public void updatedValues() throws IllegalValueException {
        Event testEvent = createEvent(20, false);
        temp.receiveEvent(testEvent);
        assertThat(proxy.getAttributeValue("Temperature"), is("20.0"));
        assertThat(proxy.getAttributeValue("LastUpdate").length() > 0, is(true));
        assertThat(proxy.getAttributeValue("TimeSinceUpdate"), is("0"));
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("100"));
    }

    @Test
    public void lowBattery() throws IllegalValueException {
        Event testEvent = createEvent(20, true);
        temp.receiveEvent(testEvent);
        assertThat(proxy.getAttributeValue("BatteryLevel"), is("10"));
    }

    private Event createEvent(int temperature, boolean lowBattery) {
        Event testEvent;
        testEvent = new TstEvent("UPM_Message");
        testEvent.setAttribute("Direction", "In");
        testEvent.setAttribute("UPM.HouseCode", "4");
        testEvent.setAttribute("UPM.DeviceCode", "3");
        testEvent.setAttribute("UPM.Primary", (temperature + 50) * 16);
        testEvent.setAttribute("UPM.Secondary", "0");
        testEvent.setAttribute("UPM.LowBattery", lowBattery ? 1 : 0);
        return testEvent;
    }

}
