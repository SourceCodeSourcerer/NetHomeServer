/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stefangs@nethome.nu>
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

import gnu.io.*;
import nu.nethome.coders.RollerTrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface class to access the Telldus Tellstick RF interface unit.
 */
public class TellstickPort {

    public static final int TELLSTICK_BUFFER_LENGTH = 79;
    public static final int TELLSTICK_MAX_PULSE_LENGTH = 2550;

    private static final int HEADER_LENGTH = 5;
    private static final int TRAILER_LENGTH = 1;
    private static final int EXTENDED_HEADER_LENGTH = 10;
    private static final int US_PER_MS = 1000;
    private static final int EXTENDED_HEADER_MESSAGE_LENGTH_POSITION = 9;
    private static final int COMPRESSED_MESSAGE_START_BIT_POSITION = 6;
    private static final int TELLSTICK_US_PER_UNIT = 10;
    private static final int TELLSTICK_MAX_PULSE_VALUE = 255;
    public static final String ERROR_MESSAGE = "ERROR";
    private static Logger logger = Logger.getLogger(TellstickPort.class.getName());

    private volatile String firmwareVersion = "";
    private boolean isClosed = false;

    /**
     * Client callback interface used to receive messages from Tellstick
     */
    public interface Client {
        void received(String message);
    }

    private class SerialDevice implements SerialPortEventListener {
        protected final int MAX_WAIT_TIME_MS = 2000;
        protected Enumeration<CommPortIdentifier> portList;
        protected InputStream inputStream;
        protected OutputStream outputStream;
        protected SerialPort serialPort;
        protected CommPortIdentifier portId = null;
        protected boolean m_CallbackActive = false;

        public void activate() throws IOException {
            m_CallbackActive = true;
        }

        public SerialDevice(String m_ComPort) throws IOException {
            // In order for RxTx to recognize CUL as a serial port on Linux, we have
            // to add this system property. We make it possible to override by checking if the
            // property has already been set.
            if ((System.getProperty("os.name").toUpperCase().indexOf("LINUX") != -1) &&
                    (System.getProperty("gnu.io.rxtx.SerialPorts") == null)) {
                System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:" + "" +
                        "/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyUSB2:/dev/ttyUSB3:/dev/ttyUSB4:" +
                        "/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2");
            }

            portList = CommPortIdentifier.getPortIdentifiers();

            boolean foundPort = false;

   	    	/* Find the configured serial port */
            while (portList.hasMoreElements()) {
                portId = portList.nextElement();
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (portId.getName().equals(m_ComPort)) {
                        // Ok, found
                        foundPort = true;
                        break;
                    }
                }
            }
            if (!foundPort) {
                logger.warning("Failed to find COM Port: " + m_ComPort);
                throw new IOException("Failed to find COM Port: " + m_ComPort);
            }

   	    	/* Try to open the serial port */
            try {
                serialPort = (SerialPort) portId.open("TellstickPort", 2000);
            } catch (PortInUseException e) {
                logger.warning("COM Port " + m_ComPort + " is already in use");
                throw new IOException("COM Port " + m_ComPort + " is already in use");
            }
            try {
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();
            } catch (IOException e) {
                logger.warning("COM Port " + m_ComPort + " could not be read " + e);
                throw new IOException("COM Port " + m_ComPort + " could not be read " + e);
            }
            try {
                serialPort.addEventListener(this);
            } catch (TooManyListenersException e) {
                logger.warning("COM Port " + m_ComPort + " has too many listeners" + e);
                throw new IOException("COM Port " + m_ComPort + " has too many listeners" + e);
            }
            serialPort.notifyOnDataAvailable(true);

   	    	/* Configure serial port parameters */
            try {
                serialPort.setSerialPortParams(9600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.enableReceiveTimeout(2000);
            } catch (UnsupportedCommOperationException e) {
                logger.warning("Could not set parameters on " + m_ComPort + " " + e);
                throw new IOException("Could not set parameters on " + m_ComPort + " " + e);
            }
        }

        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                case SerialPortEvent.BI:
                case SerialPortEvent.OE:
                case SerialPortEvent.FE:
                case SerialPortEvent.PE:
                case SerialPortEvent.CD:
                case SerialPortEvent.CTS:
                case SerialPortEvent.DSR:
                case SerialPortEvent.RI:
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;
                case SerialPortEvent.DATA_AVAILABLE:
                    if (m_CallbackActive) {
                        deviceEvent();
                    }
                    break;
            }
        }

        public int write(byte data[]) throws IOException {
            outputStream.write(data);
            return data.length;
        }

        public String readLine() throws IOException {
            StringBuilder result = new StringBuilder();
            byte tempResult[] = new byte[1];
            int read;
            int i = 0;
            int waitCount = 0;
            long start = System.currentTimeMillis();    // starting time

            // The Linux drivers for the FTDI serial chip does not appear to support waiting for more
            // than one character at the time. Therefore we have to read the characters one at the time
            // and then assemble the result.
            tempResult[0] = 0;
            while (tempResult[0] != 10) {
                read = inputStream.read(tempResult);
                if (read == 1) {
                    result.append((char) tempResult[0]);
                    i++;
                } else {
                    // If we did not get any character, we wait approximately one character "time"
                    // and try again, but max for MAX_WAIT_TIME_MS milliseconds.
                    try {
                        Thread.sleep(1);
                        waitCount++;
                        long current = System.currentTimeMillis();
                        if (current - start > MAX_WAIT_TIME_MS)
                            break;
                    } catch (InterruptedException e) {
                        // Do Dinada
                    }
                }
            }
            read = i;
            logger.finer("Read " + Integer.toString(read) + " bytes with " + Integer.toString(waitCount) + " waits");
            return result.toString();
        }

        public void deactivate() throws IOException {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.close();
            }
        }
    }

    private SerialDevice portDevice;
    private Client callbackInterface;
    private static byte[] versionRequest = {'V', '+'};

    static public List<String> listSerialPorts() {
        Enumeration<CommPortIdentifier> portList;
        portList = CommPortIdentifier.getPortIdentifiers();
        List<String> result = new ArrayList<String>();

    	/* Find the configured serial port */
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                result.add(portId.getName());
            }
        }
        return result;
    }

    /**
     * For test purposes only
     */
    TellstickPort() {
    }

    public TellstickPort(String serialPortName, Client callbackClient) throws IOException {
        callbackInterface = callbackClient;
        portDevice = new SerialDevice(serialPortName);
        portDevice.activate();
        portDevice.write(versionRequest);
    }

    public void sendCommand(int[] pulseSequence, int repeats) throws IOException {
        if (isClosed) {
            throw new IOException("Port is closed");
        }
        byte[] byteMessage;
        if (pulseSequence.length - 1 <= TELLSTICK_BUFFER_LENGTH) {
            byteMessage = buildSendCommand((byte) repeats, pulseSequence);
        } else {
            byteMessage = buildExtendedSendCommand((byte) repeats, pulseSequence);
        }
        portDevice.write(byteMessage);
    }

    private byte[] buildSendCommand(byte repeat, int[] rawMessage) {
        byte[] byteMessage = new byte[rawMessage.length - 1 + HEADER_LENGTH + TRAILER_LENGTH];
        byte repeatSpace = calculateRepeatValue(rawMessage);
        byteMessage[0] = 'R';
        byteMessage[1] = (byte) repeat;
        byteMessage[2] = 'P';
        byteMessage[3] = repeatSpace;
        byteMessage[4] = 'S';
        for (int i = 0; i < rawMessage.length - 1; i++) {
            byteMessage[i + HEADER_LENGTH] = microsecondPulseToTellstickSByte(rawMessage[i]);
        }
        byteMessage[byteMessage.length - 1] = '+';
        return byteMessage;
    }

    private byte microsecondPulseToTellstickSByte(int pulseLength) {
        if (pulseLength > TELLSTICK_MAX_PULSE_LENGTH) {
            return (byte) TELLSTICK_MAX_PULSE_VALUE;
        } else {
            byte pulse = (byte) (pulseLength / TELLSTICK_US_PER_UNIT);
            if (pulse == '+') {
                pulse = '+' + 1;
            }
            return pulse;
        }
    }

    private byte[] buildExtendedSendCommand(byte repeat, int[] rawMessage) {
        int adjustedMessage[] = protocolSpecificAdjustments(rawMessage);
        byte repeatSpacePulse = calculateRepeatValue(adjustedMessage);
        int messageToProcess[] = Arrays.copyOfRange(adjustedMessage, 0, adjustedMessage.length - 1);
        reduceTo4PulseLengthValues(messageToProcess);
        Integer pulseLengthValues[] = distinctPulseLengthValues(messageToProcess);
        int messageLength = calculateCompressedMessageLength(messageToProcess);

        byte[] byteMessage = new byte[messageLength + EXTENDED_HEADER_LENGTH + TRAILER_LENGTH];
        int resultMessageIndex = 0;
        byteMessage[resultMessageIndex++] = 'R';
        byteMessage[resultMessageIndex++] = (byte) repeat;
        byteMessage[resultMessageIndex++] = 'P';
        byteMessage[resultMessageIndex++] = repeatSpacePulse;
        byteMessage[resultMessageIndex++] = 'T';
        for (int value : pulseLengthValues) {
            byteMessage[resultMessageIndex++] = microsecondPulseToTellstickTByte(value);
        }
        resultMessageIndex = EXTENDED_HEADER_MESSAGE_LENGTH_POSITION;
        byteMessage[resultMessageIndex++] = (byte) messageToProcess.length;
        writeCompressedPulseLengths(messageToProcess, pulseLengthValues, byteMessage, resultMessageIndex);
        byteMessage[byteMessage.length - 1] = '+';
        return byteMessage;
    }

    private byte calculateRepeatValue(int[] adjustedMessage) {
        byte repeat = (byte) (adjustedMessage[adjustedMessage.length - 1] / US_PER_MS);
        return repeat > 0 ? repeat : 1;
    }

    private int[] protocolSpecificAdjustments(int[] messageToProcess) {
        if (messageToProcess[0] == RollerTrol.LONG_PREAMBLE_MARK.length()) {
            return rollerTrolAdjustment(messageToProcess);
        } else {
            return messageToProcess;
        }
    }

    private int[] rollerTrolAdjustment(int[] messageToProcess) {
        int [] newMessage = new int[messageToProcess.length + 8];
        newMessage[0] = 2450;
        newMessage[1] = 10;
        newMessage[2] = 2450;
        newMessage[3] = 2450;
        newMessage[4] = 600;
        newMessage[5] = 10;
        newMessage[6] = 600;
        newMessage[7] = 10;
        newMessage[8] = 300;
        newMessage[9] = 300;
        for (int i = 0; i < messageToProcess.length - 4; i++) {
            newMessage[i + 10] = messageToProcess[i + 4];
        }
        newMessage[messageToProcess.length + 6] = 600;
        newMessage[messageToProcess.length + 7] = 300;
        return newMessage;
    }

    private void writeCompressedPulseLengths(int[] messageToProcess, Integer[] pulseLengthValues, byte[] byteMessage, int resultMessageIndex) {
        int resultMessageBitPos = COMPRESSED_MESSAGE_START_BIT_POSITION;
        for (int i = 0; i < messageToProcess.length; i++) {
            int valueIndex = 0;
            for (int j = 1; j < pulseLengthValues.length; j++) {
                if (pulseLengthValues[j] == messageToProcess[i]) {
                    valueIndex = j;
                }
            }
            byteMessage[resultMessageIndex] |= valueIndex << resultMessageBitPos;
            resultMessageBitPos -= 2;
            if (resultMessageBitPos < 0) {
                resultMessageBitPos = COMPRESSED_MESSAGE_START_BIT_POSITION;
                resultMessageIndex++;
            }
        }
    }

    private int calculateCompressedMessageLength(int[] messageToProcess) {
        int messageLength = messageToProcess.length / 4;
        if ((messageToProcess.length % 4) != 0) {
            messageLength++;
        }
        return messageLength;
    }

    private void reduceTo4PulseLengthValues(int[] messageToProcess) {
        int processLoops = distinctPulseLengthValues(messageToProcess).length - 4;
        for (int i = 0; i < processLoops; i++) {
            mergeTheTwoClosestValues(messageToProcess);
        }
    }

    private byte microsecondPulseToTellstickTByte(int i) {
        if (i > TELLSTICK_MAX_PULSE_LENGTH) {
            return (byte) 255;
        } else {
            return (byte) (i / TELLSTICK_US_PER_UNIT);
        }
    }

    void mergeTheTwoClosestValues(int[] messageToProcess) {
        SortedSet<Integer> values = new TreeSet<Integer>();
        for (int value : messageToProcess) {
            values.add(value);
        }
        int lastValue = Integer.MAX_VALUE;
        int foundValue1 = 0;
        int foundValue2 = 0;
        int minDifference = Integer.MAX_VALUE;
        for (int value : values) {
            int differance = Math.abs(value - lastValue);
            if (differance < minDifference) {
                minDifference = differance;
                foundValue1 = lastValue;
                foundValue2 = value;
            }
            lastValue = value;
        }
        int replacementValue = (foundValue1 + foundValue2) / 2;
        for (int i = 0; i < messageToProcess.length; i++) {
            if (messageToProcess[i] == foundValue1 || messageToProcess[i] == foundValue2) {
                messageToProcess[i] = replacementValue;
            }
        }
    }

    Integer[] distinctPulseLengthValues(int[] messageToProcess) {
        Set<Integer> values = new TreeSet<Integer>();
        for (int value : messageToProcess) {
            values.add(value);
        }
        return values.toArray(new Integer[values.size()]);
    }

    public void stop() throws IOException {
        if (portDevice != null) {
            isClosed = true;
            portDevice.deactivate();
        }
    }

    public void deviceEvent() {
        try {
            if (isClosed) {
                return;
            }
            String event = portDevice.readLine().trim();
            if (event.startsWith("+V") && event.length() > 2) {
                firmwareVersion = event.substring(2).trim();
            }
            if (callbackInterface != null) {
                callbackInterface.received(event);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "failed to read from Tellstick serial port: ", e);
            isClosed = true;
            if (callbackInterface != null) {
                callbackInterface.received(ERROR_MESSAGE);
            }
        }
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }
}
