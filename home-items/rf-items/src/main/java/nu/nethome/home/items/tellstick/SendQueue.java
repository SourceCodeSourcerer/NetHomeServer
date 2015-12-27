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

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;


public class SendQueue {

    public static final int MAX_MESSAGE_QUEUE_TIME = 5000;
    private int timeoutMilliseconds = 5000;
    Queue<RawMessage> messages = new ArrayDeque<RawMessage>(20);
    Date lastSendTime = null;

    public SendQueue(int timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    private boolean isBusy() {
        return lastSendTime != null && (lastSendTime.getTime() > (System.currentTimeMillis() - timeoutMilliseconds));
    }

    synchronized RawMessage newMessage(RawMessage message) {
        messages.offer(message);
        if (!isBusy()) {
            lastSendTime = new Date();
            return getNextMessage();
        }
        return null;
    }

    synchronized RawMessage messageAcknowledge() {
        lastSendTime = messages.isEmpty() ? null : new Date();
        return getNextMessage();
    }

    private RawMessage getNextMessage() {
        RawMessage result = messages.poll();
        while (result != null  && isTooOld(result) ) {
            result = messages.poll();
        }
        return result;
    }

    private boolean isTooOld(RawMessage result) {
        return result.getCreationTime().getTime() < (System.currentTimeMillis() - MAX_MESSAGE_QUEUE_TIME);
    }

    synchronized void flush() {
        messages.clear();
    }
}
