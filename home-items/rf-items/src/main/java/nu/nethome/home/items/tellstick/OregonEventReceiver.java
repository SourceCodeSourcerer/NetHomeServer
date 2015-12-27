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

import nu.nethome.coders.decoders.NexaDecoder;
import nu.nethome.coders.decoders.OregonDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;

public class OregonEventReceiver extends TellstickEventReceiverAdaptor {

    OregonDecoder decoder = new OregonDecoder();

    public OregonEventReceiver(ProtocolDecoderSink sink) {
        decoder.setTarget(sink);
    }

    @Override
    public void processActiveEvent(TellstickEvent event) {
        byte nybbles[] = new byte[25];

        nybbles[0] = Byte.parseByte(event.getModel().substring(3, 4), 16);
        nybbles[1] = Byte.parseByte(event.getModel().substring(2, 3), 16);
        nybbles[2] = Byte.parseByte(event.getModel().substring(5, 6), 16);
        nybbles[3] = Byte.parseByte(event.getModel().substring(4, 5), 16);

        int offset = 1;
        for (int i = 0; i < event.getDataString().length(); i++) {
            nybbles[i + 4 + offset] = Byte.parseByte(event.getDataString().substring(i, i + 1), 16);
            offset = -offset;
        }
        decoder.decodeMessage(nybbles);

        nybbles[1] = Byte.parseByte(event.getModel().substring(2, 3), 16);
        nybbles[2] = Byte.parseByte(event.getModel().substring(3, 4), 16);
        nybbles[3] = Byte.parseByte(event.getModel().substring(4, 5), 16);
        nybbles[4] = Byte.parseByte(event.getModel().substring(5, 6), 16);
        for (int i = 0; i < event.getDataString().length(); i++) {
            nybbles[i + 4 + offset] = Byte.parseByte(event.getDataString().substring(i, i + 1), 16);
        }
        decoder.decodeMessage(nybbles);
    }

    @Override
    public String getEventType() {
        return "protocol:oregon";
    }

    @Override
    public String getProtocolName() {
        return "Oregon";
    }
}
