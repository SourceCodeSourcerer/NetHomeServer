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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GateKeeperTest {

    private GateKeeper gateKeeper;
    @Mock private HomeService server;
    @Mock private Event sentEvent;
    @Mock private Event receivedEvent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        gateKeeper = new GateKeeper();
        when(server.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        gateKeeper.setOpenCommand("event,Open");
        gateKeeper.setCloseCommand("event,Close");
        gateKeeper.setOpenString("OpenMe");
        gateKeeper.setCloseString("CloseMe");
    }

    @After
    public void tearDown() {
        gateKeeper.stop();
    }

    @Test
   	public void modelIsParsableXML() throws SAXException, IOException, ParserConfigurationException {
    	SAXParserFactory factory = SAXParserFactory.newInstance();
   		SAXParser parser = factory.newSAXParser();
   		ByteArrayInputStream byteStream = new ByteArrayInputStream(gateKeeper.getModel().getBytes());
   		InputSource source = new InputSource(byteStream);
   		// Just verify that the XML is valid
   		parser.parse(source, new DefaultHandler());
   	}

    @Test
    public void isClosedWhenActivated() {

        gateKeeper.activate(server);

        assertThat(gateKeeper.getState(), is("Closed"));
        gateKeeper.stop();
    }

    @Test
    public void isSetToOpenWhenOpened() {
        gateKeeper.activate(server);

        gateKeeper.openGate();

        assertThat(gateKeeper.getState(), is("Open"));
    }

    @Test
    public void callsOpenCommandWhenOpened() {
        gateKeeper.activate(server);

        gateKeeper.openGate();

        verify(server, times(1)).createEvent("Open", "");
        verify(server, times(1)).send(sentEvent);
    }

    @Test
    public void isSetToClosedWhenClosed() {
        gateKeeper.activate(server);
        gateKeeper.openGate();

        gateKeeper.closeGate();

        assertThat(gateKeeper.getState(), is("Closed"));
    }

    @Test
    public void callsCloseCommandWhenClosed() {
        gateKeeper.activate(server);
        gateKeeper.openGate();

        gateKeeper.closeGate();

        verify(server, times(1)).createEvent("Close", "");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void opensWhenOpenEventReceived() {
        gateKeeper.activate(server);
        when(receivedEvent.getAttribute("Type")).thenReturn("TCPMessage");
        when(receivedEvent.getAttribute("Value")).thenReturn("OpenMe");

        gateKeeper.receiveEvent(receivedEvent);

        assertThat(gateKeeper.getState(), is("Open"));
        verify(server, times(1)).createEvent("Open", "");
    }

    @Test
    public void closeWhenCloseEventReceived() {
        gateKeeper.activate(server);
        gateKeeper.openGate();
        when(receivedEvent.getAttribute("Type")).thenReturn("TCPMessage");
        when(receivedEvent.getAttribute("Value")).thenReturn("CloseMe");

        gateKeeper.receiveEvent(receivedEvent);

        assertThat(gateKeeper.getState(), is("Closed"));
        verify(server, times(1)).createEvent("Close", "");
    }

    @Test
    public void closeAfterTimeout() throws InterruptedException {
        gateKeeper.activate(server);
        gateKeeper.setOpenTime(50);

        gateKeeper.openGate();

        Thread.sleep(200);
        assertThat(gateKeeper.getState(), is("Closed"));
        verify(server, times(1)).createEvent("Close", "");
    }
}
