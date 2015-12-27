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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: Stefan
 * Date: 2013-07-30
 * Time: 17:07
 */
public class TellstickEventTest {

    @Test
    public void canDecodeUPM() {
        String data = "+Wclass:sensor;protocol:mandolyn;model:temperaturehumidity;data:0x275146C6;";

        TellstickEvent event = new TellstickEvent(data);

        assertThat(event.getData(), is(0x275146C6L));
        assertThat(event.getModel(), is("temperaturehumidity"));
        assertThat(event.getProtocol(), is("mandolyn"));
        assertThat(event.getSignalClass(), is("sensor"));
        assertThat(event.getEventProtocolModel(), is("protocol:mandolyn;model:temperaturehumidity"));
    }
}
