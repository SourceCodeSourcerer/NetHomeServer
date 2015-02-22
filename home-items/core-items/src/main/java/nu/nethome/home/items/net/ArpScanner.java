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

import nu.nethome.home.item.*;
import nu.nethome.util.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Stefan
 */
@Plugin
@HomeItemType("Ports")
public class ArpScanner extends HomeItemAdapter implements HomeItem {

    public static final String ARP_SCAN_MESSAGE = "ArpScan_Message";
    public static final int MAX_ERROR_DISPLAY_SIZE = 30;
    private final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"ArpScanner\"  Category=\"Ports\" >"
            + "  <Attribute Name=\"MacCount\" 	Type=\"String\" Get=\"getMacCount\" Default=\"true\" />"
            + "  <Attribute Name=\"ArpScan\"	Type=\"String\" Get=\"getExecName\" 	Set=\"setExecName\" />"
            + "  <Attribute Name=\"ScanInterval\"	Type=\"String\" Get=\"getScanInterval\"  Set=\"setScanInterval\" />"
            + "  <Action Name=\"Scan\" 	Method=\"reportScanResult\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(ArpScanner.class.getName());
    private volatile Timer scanTimer;

    private String execName = "/usr/bin/arp-scan -r 3 -b 2 -q --interface=eth0 --localnet";
    private String macCount = "";
    long scanInterval = 30000;

    public ArpScanner() {
    }

    public String getModel() {
        return m_Model;
    }

    @Override
    public void activate() {
        startTimer();
    }

    private void startTimer() {
        scanTimer = new Timer("ArpScanner", true);
        scanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                reportScanResult();
            }
        }, scanInterval, scanInterval);
    }

    @Override
    public void stop() {
        stopTimer();
        super.stop();
    }

    private void stopTimer() {
        if (scanTimer != null) {
            scanTimer.cancel();
            scanTimer = null;
        }
    }

    public String reportScanResult() {
        try {
            List<String> macs = null;
            macs = scan();
            String macString = "";
            String separator = "";
            for (String mac : macs) {
                macString += separator + mac;
                separator = ",";
            }
            server.send(server.createEvent(ARP_SCAN_MESSAGE, macString));
            macCount = "" + macs.size();
        } catch (ExecutionFailure executionFailure) {
            macCount = executionFailure.getMessage().substring(0, Math.min(MAX_ERROR_DISPLAY_SIZE, executionFailure.getMessage().length()));
            macCount += executionFailure.getMessage().length() > MAX_ERROR_DISPLAY_SIZE ? "..." : "";
        }
        return null;
    }

    public List<String> scan() throws ExecutionFailure {
        try {
            Runtime r = Runtime.getRuntime();
            Process proc = r.exec(execName);
            ResponseParser response = new ResponseParser(proc.getInputStream());
            response.start();
            int result = proc.waitFor();
            response.join(1000);
            return response.responseLines;

        } catch (IOException | InterruptedException e) {
            logger.warning("Failed to execute arp-scan: " + e.getMessage());
            throw new ExecutionFailure(e.getMessage());
        }
    }

    public String getMacCount() {
        return macCount;
    }

    public void setExecName(String execName) {
        this.execName = execName;
    }

    public String getExecName() {
        return execName;
    }

    public String getScanInterval() {
        return "" + (scanInterval / 1000);
    }

    public void setScanInterval(String scanInterval) {
        stopTimer();
        this.scanInterval = Long.parseLong(scanInterval) * 1000;
        if (isActivated()) {
            startTimer();
        }
    }

    static class ResponseParser extends Thread {
        private InputStream is;
        public List<String> responseLines = new ArrayList<>();

        ResponseParser(InputStream is) {
            super("arp-scan parser");
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    Scanner lineScanner = new Scanner(line);
                    String value = lineScanner.findInLine("[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]:[0-9,a-f][0-9,a-f]");
                    if ((value != null) && (value.length() != 0)) {
                        responseLines.add(value);
                    }
                }
            } catch (IOException e) {
                logger.warning("Failed to execute arp-scan command: " + e.getMessage());
            }
        }
    }
}
