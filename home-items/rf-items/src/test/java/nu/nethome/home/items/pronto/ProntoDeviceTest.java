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

package nu.nethome.home.items.pronto;

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

import nu.nethome.home.impl.HomeServer;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.impl.ModelException;
import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.HomeItemProxy;
import nu.nethome.home.item.IllegalValueException;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProntoDeviceTest {

    private ProntoDevice pronto;

    @Mock private HomeService server;
    @Mock private HomeServer realServer;
    @Mock private Event sentEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(server.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        when(realServer.createEvent(any(String.class), any(String.class))).thenReturn(sentEvent);
        pronto = new ProntoDevice();
        pronto.activate(server);
    }

    @Test
    public void modelIsParsableXML() throws SAXException, IOException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        DefaultHandler handler = new DefaultHandler();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(pronto.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        saxParser.parse(source, handler);
    }

    @Test
    public void onSendsOnCommand() {
        pronto.setOnCode("On");
        pronto.on();
        assertThat(pronto.getState(), is("On"));
        verify(sentEvent).setAttribute("Pronto.Message", "On");
        verify(server, times(1)).send(sentEvent);
        pronto.on();
        verify(sentEvent, times(2)).setAttribute("Pronto.Message", "On");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void onSendsToggleWhenNeededIfNoOnCommand() {
        pronto.setToggleCode("Toggle");
        pronto.on();
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "Toggle");
        verify(server, times(1)).send(sentEvent);
        pronto.on();
        verify(server, times(1)).send(sentEvent);
    }

    @Test
    public void offSendsOffCommand() {
        pronto.setOffCode("Off");
        pronto.off();
        assertThat(pronto.getState(), is("Off"));
        verify(sentEvent).setAttribute("Pronto.Message", "Off");
        verify(server, times(1)).send(sentEvent);
        pronto.off();
        verify(sentEvent, times(2)).setAttribute("Pronto.Message", "Off");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void offSendsToggleWhenNeededIfNoOffCommand() {
        pronto.on();
        pronto.setToggleCode("Toggle");
        pronto.off();
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "Toggle");
        verify(server, times(1)).send(sentEvent);
        pronto.off();
        verify(server, times(1)).send(sentEvent);
    }

    @Test
    public void toggleSendsToggleCommand() {
        pronto.setToggleCode("Toggle");
        pronto.toggle();
        assertThat(pronto.getState(), is("On"));
        verify(sentEvent).setAttribute("Pronto.Message", "Toggle");
        verify(server, times(1)).send(sentEvent);
        pronto.toggle();
        verify(sentEvent, times(2)).setAttribute("Pronto.Message", "Toggle");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void toggleSendsOnOffWhenNeededIfNoToggleCommand() {
        pronto.setOnCode("On");
        pronto.setOffCode("Off");
        pronto.toggle();
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "On");
        verify(server, times(1)).send(sentEvent);
        pronto.toggle();
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "Off");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void delaysCommandsAtStart() throws InterruptedException {
        pronto.setStartupTime(50);
        pronto.setOnCode("On");
        pronto.setCommand0Code("Command");
        pronto.on();
        pronto.command0();
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "On");
        verify(server, times(1)).send(sentEvent);
        Thread.sleep(200);
        verify(sentEvent, times(1)).setAttribute("Pronto.Message", "Command");
        verify(server, times(2)).send(sentEvent);
    }

    @Test
    public void callMethodCallsMethod() throws IllegalValueException, ExecutionFailure, ModelException {
        ProntoDevice item = new ProntoDevice();
        item.activate(realServer);
        HomeItemProxy proxy = new LocalHomeItemProxy(item, realServer);

        for (int i = 0; i < ProntoDevice.COMMAND_COUNT; i++) {
            proxy.setAttributeValue("Command" + Integer.toString(i), "CommandName" + Integer.toString(i));
            proxy.setAttributeValue("CommandCode" + Integer.toString(i), "CommandPronto" + Integer.toString(i));
            proxy = new LocalHomeItemProxy(item, realServer);
            proxy.callAction(proxy.getAttributeValue("Command" + Integer.toString(i)));
            verify(sentEvent, times(1)).setAttribute("Pronto.Message", proxy.getAttributeValue("CommandCode" + Integer.toString(i)));
            verify(realServer, times(i + 1)).send(sentEvent);
        }
    }
}
