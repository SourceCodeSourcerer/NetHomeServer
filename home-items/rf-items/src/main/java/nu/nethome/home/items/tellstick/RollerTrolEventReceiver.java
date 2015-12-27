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

import nu.nethome.coders.RollerTrol;
import nu.nethome.coders.decoders.NexaDecoder;
import nu.nethome.coders.decoders.RollerTrolDecoder;
import nu.nethome.util.ps.BitString;
import nu.nethome.util.ps.ProtocolDecoderSink;

public class RollerTrolEventReceiver extends TellstickEventReceiverAdaptor {

    RollerTrolDecoder decoder = new RollerTrolDecoder();

    public RollerTrolEventReceiver(ProtocolDecoderSink sink) {
        decoder.setTarget(sink);
    }

    @Override
    public void processActiveEvent(TellstickEvent event) {
        long tellstickData = event.getData();
        BitString data = new BitString(40);
        data.insert(RollerTrol.HOUSE_CODE, (int)(((tellstickData >> 24) & 0xFF) + ((tellstickData >> 8) & 0xFF00)));
        data.insert(RollerTrol.DEVICE_CODE, (int)(tellstickData >> 8) & 0xF);
        data.insert(RollerTrol.COMMAND, (int)(tellstickData >> 12) & 0xF);
        data.insert(RollerTrol.CHECK_SUM, RollerTrol.calculateChecksum(data));
        decoder.decodeMessage(data);
    }

    @Override
    public String getEventType() {
        return "protocol:hasta;model:selflearningv2";
    }

    @Override
    public String getProtocolName() {
        return "RollerTrol";
    }
}
