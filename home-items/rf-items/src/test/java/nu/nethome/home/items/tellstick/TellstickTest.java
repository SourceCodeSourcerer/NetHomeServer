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

import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TellstickTest {

    Tellstick tellstick;
    int testMessage[] = {20, 40, 60, 80, 64, 100, 20, 40, 60, 80, 64, 100};
    int copy[];
    private HomeService homeService;
    private Event event;

    @Before
    public void setUp() throws Exception {
        tellstick = new Tellstick() {
            @Override
            void createTellstickPort() {}
        };
        copy = Arrays.copyOf(testMessage, testMessage.length);
        homeService = mock(HomeService.class);
        event = new InternalEvent("Foo");
        doReturn(event).when(homeService).createEvent(any(String.class), any(String.class));
    }

    @Test
    public void notConnectedAfterCreation() {
        assertThat(tellstick.getState(), is("Not connected"));
    }

    @Test
    public void canReceiveOregonWind() throws Exception {
        tellstick.activate(homeService);
        tellstick.receivedTellstickEvent("+Wclass:sensor;protocol:oregon;model:0x1984;data:0174D0C92093025B;");
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(homeService).send(captor.capture());
        assertThat(captor.getValue().getAttributeInt("Oregon.LowBattery"), is(1));
        assertThat(captor.getValue().getAttributeInt("Oregon.Wind"), is(29));
    }

    @Test
    public void canReceiveOregonTemp() throws Exception {
        tellstick.activate(homeService);
        tellstick.receivedTellstickEvent("+Wclass:sensor;protocol:oregon;model:0x1A2D;data:10F45215088243A7;");
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(homeService).send(captor.capture());
        assertThat(captor.getValue().getAttributeInt("Oregon.Temp"), is(-155));
    }
}
