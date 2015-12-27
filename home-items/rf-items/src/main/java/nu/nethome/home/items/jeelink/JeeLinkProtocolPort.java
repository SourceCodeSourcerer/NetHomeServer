/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
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

package nu.nethome.home.items.jeelink;


import jssc.*;
import nu.nethome.util.ps.ProtocolDecoder;

import java.io.IOException;
import java.util.*;

/**
 * The JeeLinkProtocolPort interfaces with an USB radio transceiver and sends and receives data
 * in form of pulse trains to/from the transceiver. The JeeLink transceiver can be found at:
 * {@see http://jeelink.com}. Special firmware from NetHome is needed on the JeeLink.
 *
 * @author Stefan
 */
public class JeeLinkProtocolPort  {
    String portName = "COM4";
    protected SerialPort serialPort;
    protected boolean isOpen = false;
    private ProtocolDecoder decoder;
    private double pulseLengthCompensation = 0;
    private String reportedVersion = "";

    private int modulationOnPeriod = 0;
    private int modulationOffPeriod = 0;
    private List<String> portList;

    public JeeLinkProtocolPort(String portName, ProtocolDecoder decoder) throws PortException {
        this.decoder = decoder;
        this.portName = portName;
        serialPort = new SerialPort(this.portName);
        open();
    }

    private void open() throws PortException {
        portList = Arrays.asList(SerialPortList.getPortNames());
        if (!portList.contains(portName)) {
            throw new PortException("Port " + portName + " not Found");
        }
        try {
            serialPort.openPort();
            if (!serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)) {
                throw new PortException("Could not set serial port parameters");
            }
            sendQueryVersionCommand();
            sendQueryVersionCommand();
        } catch (SerialPortException e) {
            throw new PortException("Could not open port " + portName, e);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveLoop();
            }
        }, "Port receive thread").start();
        isOpen = true;
    }

    public void close() {
        isOpen = false;
        if (serialPort != null) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                // Ignore
            }
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    private void receiveLoop() {
        String result = "";
        SerialPort localPort = serialPort;
        while(localPort.isOpened() && isOpen) {
            try {
                String nextChar = localPort.readString(1, 10000);
                result += nextChar;
                if (nextChar.charAt(0) == 0x0A) {
                    analyzeReceivedCommand(result);
                    result = "";
                }

            } catch (SerialPortException e) {
                // Probably port is closed, ignore and will exit the while
            } catch (SerialPortTimeoutException e) {
                // Ignore
            } catch (Exception e) {
                // Problem in the decoders.
            }
        }
    }

    /**
     * Analyze a command/event received from the device
     *
     * @param commandString Command string to analyze
     */
    private void analyzeReceivedCommand(String commandString) {
        if (commandString.length() < 1) return; // Make sure string is not empty
        char command = commandString.charAt(0);

        // Check if it is a version report
        if (command == 'V' && commandString.length() > 2) {
            reportedVersion = commandString.substring(1);
            return;
        }

        // Check if it is acknowledgment of command
        if (command == 'o') {
            acknowledgeCommand(commandString);
            return;
        }

        // Unknown command - ignore
    }

    /**
     * Received ack on a command. Currently does nothing
     *
     * @param commandString
     */
    private void acknowledgeCommand(String commandString) {

    }

    /**
     * Send a processed pulse to the decoders
     *
     * @param pulseLength
     * @param isMark
     */
    private void parsePulse(double pulseLength, boolean isMark) {

        pulseLength += isMark ? pulseLengthCompensation : -pulseLengthCompensation;

        // Give the pulse to the decoder
        decoder.parse(pulseLength, isMark);
    }

    public static List<String> listAvailablePortNames() {
         return Arrays.asList(SerialPortList.getPortNames());
    }

    public String getSerialPort() {
        return portName;
    }

    /**
     * Transmit the list of pulses (in microseconds) via the CUL device
     *
     * @param message      List of pulse lengths in microseconds, beginning with a mark pulse
     * @param repeat       Number of times to repeat message
     * @param repeatOffset Number pulses into the message the repeat sequence should begin
     * @return True if successful
     */
    public boolean playMessage(int message[], int repeat, int repeatOffset) throws PortException {

        sendResetTransmitBufferCommand();
        // Loop through the flanks in the message
        for (int i = 0; i < message.length; i++) {
            int mark = message[i++];
            // Fill with 0 if uneven number of flanks
            int space = i < message.length ? message[i] : 0;
            sendAddFlankCommand(mark, space);
        }
        // Transmit the message, check if it should be modulated
        if ((modulationOnPeriod > 0) || (repeatOffset > 0)) {
            // Yes, also write modulation parameters
            sendTransmitWithModulationCommand(repeat, repeatOffset, modulationOnPeriod, modulationOffPeriod);
        } else {
            sendTransmitCommand(repeat);

        }
        // NYI - Wait for confirmation
        return true;
    }

    private void sendTransmitWithModulationCommand(int repeat, int repeatOffset, int modulationOnPeriod, int modulationOffPeriod) throws PortException {
        writeLine(String.format("S%02X%02X%02X%02X", repeat, modulationOnPeriod, modulationOffPeriod, repeatOffset));
    }

    private void sendTransmitCommand(int repeat) throws PortException {
        writeLine(String.format("S%02X", repeat));
    }

    private void sendAddFlankCommand(int mark, int space) throws PortException {
        String command = String.format("A%04X%04X", mark, space);
        writeLine(command);
    }

    private void sendResetTransmitBufferCommand() throws PortException {
        writeLine("E");
    }

    private void sendQueryVersionCommand() throws PortException {
        writeLine("V");
    }


    /**
     * Write a text line to the serial port. EOL-characters are added to the string
     *
     * @param line Line to be written
     */
    protected void writeLine(String line) throws PortException {
        String totalLine = line + "\r\n";
        try {
            if (!serialPort.writeBytes(totalLine.getBytes())) {
                throw new PortException("Could not write to port");
            }
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    /**
     * See setModulationOnPeriod
     *
     * @return ModulationOnPeriod
     */
    public int getModulationOnPeriod() {
        return modulationOnPeriod;
    }

    /**
     * The mark pulses may be modulated. This parameter specifies the on period
     * of this modulation. The time is specified in increments of 375nS. If on
     * and off periods are set to the same value the resulting modulation frequency
     * will be 10E9/(OnPeriod * 375 * 2).
     * Setting the period to 0 turns off the mark modulation.
     *
     * @param modulationOnPeriod 0 - 255.
     */
    public void setModulationOnPeriod(int modulationOnPeriod) {
        this.modulationOnPeriod = modulationOnPeriod;
    }

    /**
     * See setModulationOffPeriod
     *
     * @return ModulationOffPeriod
     */
    public int getModulationOffPeriod() {
        return modulationOffPeriod;
    }

    /**
     * Set the off period of mark pulse modulation. The time is specified in
     * increments of 375nS. See setModulationOnPeriod for details.
     *
     * @param modulationOffPeriod
     */
    public void setModulationOffPeriod(int modulationOffPeriod) {
        this.modulationOffPeriod = modulationOffPeriod;
    }

    public String getReportedVersion() {
        return reportedVersion;
    }
}
