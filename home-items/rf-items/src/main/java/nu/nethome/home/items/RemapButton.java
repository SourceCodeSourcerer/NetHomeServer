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

package nu.nethome.home.items;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.items.nexa.NexaRemapButton;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A base class which handles the hold off functions used by all remap buttons to avoid sending an RF-command
 * while the causing RF signal is still active, by waiting until it has not seen a command in a configurable
 * number of ms.
 * It also holds the CommandLineExecutor needed by all remap buttons.
 */
public abstract class RemapButton extends HomeItemAdapter {
    public static final int SAMPLE_INTERVAL_MS = 60;
    private static Logger logger = Logger.getLogger(NexaRemapButton.class.getName());
    private CommandLineExecutor commandExecutor;
    private volatile AtomicBoolean inHoldOff = new AtomicBoolean(false);
    private long holdOffStart;
    private Event latestHoldOffEvent;
    private long latestReceivedEventTime;
    private Timer holdOffTimer = new Timer("RemapHoldOffTimer", true);
    private String onCommand = "";
    private String offCommand = "";
    private int holdOffTime = 200;
    private boolean isEnabled = true;

    protected void processEvent(Event event) {
        if (!isEnabled) {
            return;
        }
        if (holdOffTime > 0) {
            latestHoldOffEvent = event;
            latestReceivedEventTime = System.currentTimeMillis();
            if (inHoldOff.compareAndSet(false, true)) {
                startHoldoffTimer();
                holdOffStart = System.currentTimeMillis();
            }
        } else {
            actOnEvent(event);
        }
    }

    private void startHoldoffTimer() {
        holdOffTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkHoldOff();
            }
        }, SAMPLE_INTERVAL_MS);
    }

    private void checkHoldOff() {
        if (System.currentTimeMillis() - latestReceivedEventTime > holdOffTime) {
            try {
                actOnEvent(latestHoldOffEvent);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error when processing event in RemapButton " + this.name, e);
            } finally {
                inHoldOff.set(false);
            }
        } else {
            startHoldoffTimer();
        }
    }

    protected abstract void actOnEvent(Event event);

    public void activate(HomeService server) {
        super.activate(server);
        commandExecutor = new CommandLineExecutor(server, true);
    }

    @Override
    public void stop() {
        holdOffTimer.cancel();
        super.stop();
    }

    public void on() {
        performCommand(onCommand);
    }

    public void off() {
        performCommand(offCommand);
    }

    protected void performCommand(String commandString) {
        if (commandString.equals("")) {
            return;
        }
        String result = commandExecutor.executeCommandLine(commandString);
        if (!result.startsWith("ok")) {
            logger.warning(result);
        }
    }

    /**
     * @return the m_OffCommand
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getOffCommand() {
        return offCommand;
    }

    /**
     * @param offCommand the m_OffCommand to set
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setOffCommand(String offCommand) {
        this.offCommand = offCommand;
    }

    /**
     * @return the m_OnCommand
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getOnCommand() {
        return onCommand;
    }

    /**
     * @param onCommand the m_OnCommand to set
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setOnCommand(String onCommand) {
        this.onCommand = onCommand;
    }

    public String getHoldOffTime() {
        return Integer.toString(holdOffTime);
    }

    public void setHoldOffTime(String holdOffTime) {
        try {
            this.holdOffTime = Integer.parseInt(holdOffTime);
        } catch (NumberFormatException e) {
            this.holdOffTime = 0;
        }
    }

    public String getState() {
        return isEnabled ? "Enabled" : "Disabled";
    }

    public void setState(String state) {
        isEnabled = state.equalsIgnoreCase("Enabled");
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    public boolean isInHoldOff() {
        return inHoldOff.get();
    }

    public long getHoldOffStart() {
        return holdOffStart;
    }
}
