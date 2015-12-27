package nu.nethome.home.items.jeelink;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

import java.io.IOException;

/**
 *
 */
public class JeeLinkPort {

    public boolean isJeeLinkConnected(String portName) {
        return false;
    }

    public void readArduinoVersion(String portName) throws IOException, PortException, SerialPortException, SerialPortTimeoutException {
        SerialPort serialPort = openSerialPort(portName);

        System.out.println("syncing");
        synchronizeConnection(serialPort);
        System.out.println("insync");

        System.out.println("Verifying sync");
        executeCommand(serialPort, 1, 0x30);
        System.out.println("insync");

        System.out.println("entering programming mode");
        executeCommand(serialPort, 1, 0x50);
        System.out.println("insync");

        System.out.println("getting device signature");
        byte[] signature = executeCommand(serialPort, 3, 0x75);
        System.out.println("insync");
        System.out.println("signature: " + signature[0] + "." + signature[1] + "." + signature[2]);

        System.out.println("leaving programming mode");
        executeCommand(serialPort, 1, 0x51);
        System.out.println("insync");

        System.out.println("reading major version");
        int major = executeCommand(serialPort, 1, 0x41, 0x81)[0];
        System.out.println("insync");

        System.out.println("reading minor version");
        int minor = executeCommand(serialPort, 1, 0x41, 0x82)[0];
        System.out.println("insync");
        System.out.println("version: " + major + "." + minor);

        serialPort.closePort();
    }

    private void synchronizeConnection(SerialPort serialPort) throws SerialPortException, SerialPortTimeoutException {
        for (int i = 0; i < 5; i++) {
            sendCommand(serialPort, 0x30);
        }
        System.out.println("waiting for response");
        receiveCommandResponse(serialPort, 0);
    }

    private SerialPort openSerialPort(String portName) throws PortException {
        SerialPort serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();
            if (!serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)) {
                throw new PortException("Could not set serial port parameters");
            }
        } catch (SerialPortException e) {
            throw new PortException("Could not open port " + portName, e);
        }
        return serialPort;
    }

    private byte[] executeCommand(SerialPort serialPort, int responseLength, int... commandBytes) throws SerialPortException, SerialPortTimeoutException {
        sendCommand(serialPort, commandBytes);
        return receiveCommandResponse(serialPort, responseLength);
    }

    private void sendCommand(SerialPort serialPort, int... commandBytes) throws SerialPortException {
        for (int commandByte : commandBytes) {
            serialPort.writeByte((byte) commandByte);
        }
        serialPort.writeByte((byte) 0x20);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] receiveCommandResponse(SerialPort serialPort, int responseLength) throws SerialPortException, SerialPortTimeoutException {
        int insync = readByte(serialPort, 10000);
        byte[] resultBytes = new byte[responseLength];
        for (int i = 0; i < responseLength; i++) {
            resultBytes[i] = readByte(serialPort, 10000);
        }
        int ok = readByte(serialPort, 10000);
        if (insync != 0x14 || ok != 0x10) {
            throw new IllegalArgumentException(String.format("Command respomse failed, insync = %X, ok = %X", insync, ok));
        }
        return resultBytes;
    }

    private byte readByte(SerialPort port, int timeoutMs) throws SerialPortException, SerialPortTimeoutException {
        byte b = port.readBytes(1, timeoutMs)[0];
        return b;
    }


}
