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

package nu.nethome.home.items.nexa;

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.items.util.TstHomeItemProxy;
import nu.nethome.home.items.util.TstHomeService;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.apache.xerces.parsers.SAXParser;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * User: Stefan
 * Date: 2012-05-29
 * Time: 20:15
 */
public class NexaRemapButtonTest {

	// Local Members
	protected HomeService server;
	public NexaRemapButton m_testItem;

	@Before
	public void setUp() throws Exception {
		server = new TstHomeService();
		m_testItem = new NexaRemapButton();
        m_testItem.setHoldOffTime("0");
	}

	@After
	public void tearDown() throws Exception {
		m_testItem = null;
	}

	@Test
	public void canGetModel() throws SAXException, IOException {
		SAXParser parser = new SAXParser();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(m_testItem.getModel().getBytes());
		InputSource source = new InputSource(byteStream);
		// Just verify that the XML is valid
		parser.parse(source);
	}

	@Test
	public void canSetName() {
		m_testItem.setName("FooBar");
		assertTrue(m_testItem.getName().equals("FooBar"));
	}


	@Test
	public void canGetSetOnCommand() {
		assertTrue(m_testItem.getOnCommand().equals(""));
		m_testItem.setOnCommand("FooBar");
		assertTrue(m_testItem.getOnCommand().equals("FooBar"));
	}

	@Test
	public void canGetOffCommand() {
		assertTrue(m_testItem.getOffCommand().equals(""));
		m_testItem.setOffCommand("FieBar");
		assertTrue(m_testItem.getOffCommand().equals("FieBar"));
	}

	@Test
	public void canGetSetHouseCode() {
		assertEquals("A", m_testItem.getHouseCode());
		m_testItem.setHouseCode("C");
		assertEquals("C", m_testItem.getHouseCode());
		m_testItem.setHouseCode("p");
		assertEquals("P", m_testItem.getHouseCode());
		m_testItem.setHouseCode("Q");
		assertEquals("P", m_testItem.getHouseCode());
		m_testItem.setHouseCode("olle");
		assertEquals("P", m_testItem.getHouseCode());
	}

	@Test
	public void canGetSetDeviceCode() {
		assertEquals("1", m_testItem.getButton());
		m_testItem.setButton("3");
		assertEquals("3", m_testItem.getButton());
		m_testItem.setButton("9");
		assertEquals("3", m_testItem.getButton());
		m_testItem.setButton("7");
		assertEquals("7", m_testItem.getButton());
		m_testItem.setButton("olle");
		assertEquals("7", m_testItem.getButton());
	}

	@Test
	public void canReceiveOnCommand() throws InterruptedException {
		// Set up the remap button
		m_testItem.setOnCommand("call,Foo,OnMethod");
		m_testItem.setButton("1");
		m_testItem.setHouseCode("B");
		m_testItem.activate(server);

		// Create and "send" the on-event
		Event onEvent = server.createEvent("Nexa_Message", "");
		onEvent.setAttribute("Direction", "In");
		onEvent.setAttribute("Nexa.HouseCode", "1");
		onEvent.setAttribute("Nexa.Button", "1");
		onEvent.setAttribute("Nexa.Command", "1");
		m_testItem.receiveEvent(onEvent);

		// Get the faked target instance and verify it has been called
		TstHomeItemProxy foo = (TstHomeItemProxy)server.openInstance("Foo");
		assertEquals(1, foo.getNumberOfCalledActions());
		assertEquals(1, foo.getNumberTimesCalled("OnMethod"));

		m_testItem.stop();
	}

	@Test
	public void canReceiveOffCommand() throws InterruptedException {
		// Set up the remap button
		m_testItem.setOffCommand("call,Foo,OffMethod");
		m_testItem.setButton("7");
		m_testItem.setHouseCode("G");
		m_testItem.activate(server);

		// Create and "send" the on-event
		Event onEvent = server.createEvent("Nexa_Message", "");
		onEvent.setAttribute("Direction", "In");
		onEvent.setAttribute("Nexa.HouseCode", "6");
		onEvent.setAttribute("Nexa.Button", "7");
		onEvent.setAttribute("Nexa.Command", "0");
		m_testItem.receiveEvent(onEvent);

		// Get the faked target instance and verify it has been called
		TstHomeItemProxy foo = (TstHomeItemProxy)server.openInstance("Foo");
		assertEquals(1, foo.getNumberOfCalledActions());
		assertEquals(1, foo.getNumberTimesCalled("OffMethod"));

		m_testItem.stop();
	}

    @Test
    public void canConfigureFromInitEvent() throws InterruptedException {
        Event initEvent = new InternalEvent("Init");
        initEvent.setAttribute("InitId", (int)m_testItem.getItemId());
        initEvent.setAttribute("Direction", "In");
        initEvent.setAttribute("Nexa.Command", "1");
        initEvent.setAttribute("Nexa.Button", "5");
        initEvent.setAttribute("Nexa.HouseCode", "2");
        m_testItem.receiveEvent(initEvent);
        MatcherAssert.assertThat(m_testItem.getButton(), Is.is("5"));
        MatcherAssert.assertThat(m_testItem.getHouseCode(), Is.is("C"));
    }
}