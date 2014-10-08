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

package nu.nethome.home.items.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nu.nethome.home.items.MockHomeItemProxy;
import nu.nethome.home.items.MockServiceConnection;
import nu.nethome.home.items.MockTCPClient;
import nu.nethome.home.system.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TCPCommandPortTest {
	// Local Members
	protected MockServiceConnection m_ServiceConnection;
	public TCPCommandPort m_TestItem;

	@Before
	public void setUp() throws Exception {
		m_ServiceConnection = new MockServiceConnection();
		m_TestItem = new TCPCommandPort();
	}

	@After
	public void tearDown() throws Exception {
		m_TestItem = null;
	}

	@Test
	public void testGetModel() throws SAXException, IOException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(m_TestItem.getModel().getBytes());
		InputSource source = new InputSource(byteStream);
		// Just verify that the XML is valid
		parser.parse(source, new DefaultHandler());
	}

	@Test
	public void testSetName() {
		m_TestItem.setName("FooBar");
		assertTrue(m_TestItem.getName().equals("FooBar"));
	}
	
	@Test
	public void testSetListenPort() {
		m_TestItem.setListenPort("9005");
		assertTrue(m_TestItem.getListenPort().equals("9005"));
	}
	
	@Test
	public void testGetSessionCount() {
		assertEquals("0", m_TestItem.getSessionCount());
	}

	@Test
	public void testConnectOne() throws SocketTimeoutException, UnknownHostException, IOException, InterruptedException {
		m_TestItem.setName("Test1");
		m_TestItem.setListenPort("9010");
		m_TestItem.activate(m_ServiceConnection);
		Thread.sleep(20); // Wait so the listen thread in the port is started
		assertEquals("0", m_TestItem.getSessionCount());
		MockTCPClient client = new MockTCPClient("127.0.0.1", 9010);
		Thread.sleep(100);
		assertEquals("1", m_TestItem.getSessionCount());
		
		client.writeLine("call,Foo,fie\n");
		assertEquals("ok,", client.readLine());
		MockHomeItemProxy foo = (MockHomeItemProxy)m_ServiceConnection.openInstance("Foo");
		assertEquals(1, foo.getNumberOfCalledActions());
		assertEquals(1, foo.getNumberTimesCalled("fie"));

		client.disconnect();
		Thread.sleep(100);
		assertEquals("0", m_TestItem.getSessionCount());
		m_TestItem.stop();
	}
	
	@Test
	public void testSubscribe() throws SocketTimeoutException, UnknownHostException, IOException, InterruptedException {
		m_TestItem.setName("Test3");
		m_TestItem.setListenPort("9012");
		m_TestItem.activate(m_ServiceConnection);
		Thread.sleep(20); // Wait so the listen thread in the port is started
		assertEquals("0", m_TestItem.getSessionCount());
		MockTCPClient client1 = new MockTCPClient("127.0.0.1", 9012);
		MockTCPClient client2 = new MockTCPClient("127.0.0.1", 9012);
		Thread.sleep(100);
		assertEquals("2", m_TestItem.getSessionCount());

		// Let one client subscribe
		client1.writeLine("subscribe\n");
		assertEquals("ok", client1.readLine());
		
		Event event1 = m_ServiceConnection.createEvent("event1", "");
		m_TestItem.receiveEvent(event1);
		assertEquals("event,event1", client1.readLine());
		assertTrue(client2.readEmptyLine());

		// Let both clients subscribe
		client2.writeLine("subscribe\n");
		assertEquals("ok", client2.readLine());
		
		Event event2 = m_ServiceConnection.createEvent("event2", "");
		m_TestItem.receiveEvent(event2);
		assertEquals("event,event2", client1.readLine());
		assertEquals("event,event2", client2.readLine());

		client1.writeLine("unsubscribe\n");
		assertEquals("ok", client1.readLine());
		
		// Let one client unsubscribe
		Event event3 = m_ServiceConnection.createEvent("event3", "");
		m_TestItem.receiveEvent(event3);
		assertTrue(client1.readEmptyLine());
		assertEquals("event,event3", client2.readLine());

		client1.disconnect();
		client2.disconnect();
		Thread.sleep(100);
		assertEquals("0", m_TestItem.getSessionCount());
		m_TestItem.stop();
	}
}
