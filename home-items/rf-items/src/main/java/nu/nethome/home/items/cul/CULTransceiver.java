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

package nu.nethome.home.items.cul;

import nu.nethome.coders.decoders.*;
import nu.nethome.coders.encoders.Encoders;
import nu.nethome.coders.encoders.ShortBeepEncoder;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.home.system.HomeService;
import nu.nethome.home.util.EncoderFactory;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.*;
import nu.nethome.util.ps.impl.CULProtocolPort;
import nu.nethome.util.ps.impl.ProtocolDecoderGroup;
import nu.nethome.util.ps.impl.RawDecoder;

import java.util.logging.Logger;

/**
 * CULTransceiver ...
 *
 * @author Stefan
 */
@Plugin
@HomeItemType("Hardware")
public class CULTransceiver extends HomeItemAdapter implements HomeItem, ProtocolDecoderSink {

    private final String m_Model = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"CULTransceiver\" Category=\"Hardware\" >"
            + "  <Attribute Name=\"SerialPort\" Type=\"String\" Get=\"getSerialPort\" 	Set=\"setSerialPort\" />"
            + "  <Attribute Name=\"ModulationFrequency\" Type=\"String\" Get=\"getModulationFrequency\" 	Set=\"setModulationFrequency\" />"
            + "  <Attribute Name=\"TransmissionPower\" Type=\"StringList\" Get=\"getTransmissionPower\" 	Set=\"setTransmissionPower\">"
            + "    <item>0</item><item>1</item><item>2</item><item>3</item><item>4</item><item>5</item><item>6</item><item>7</item></Attribute>"
            + "  <Attribute Name=\"SendCount\" Type=\"String\" Get=\"getSendCount\"  />"
            + "  <Attribute Name=\"Connected\" Type=\"String\" Get=\"getConnected\" Default=\"true\" />"
            + "  <Action Name=\"Reconnect\"		Method=\"reconnect\" Default=\"true\" />"
            + "  <Action Name=\"PlayTestBeep\"		Method=\"playTestBeep\" />"
            + "  <Attribute Name=\"TestBeepFrequency\" Type=\"String\" Get=\"getTestBeepFrequency\" 	Set=\"setTestBeepFrequency\" />"
            + "</HomeItem> ");

    private static Logger logger = Logger.getLogger(CULTransceiver.class.getName());
    private ProtocolDecoderGroup m_ProtocolDecoders = new ProtocolDecoderGroup();

    // Public attributes
    private int testBeepFrequency = 2000;
    CULProtocolPort culPort;
    private long sendCount = 0;
    private float modulationFrequency = 0;
    private EncoderFactory factory;


    public CULTransceiver() {

        // Create the Protocol-Decoders and add them to the decoder group
        m_ProtocolDecoders.add(new SIRCDecoder());
        m_ProtocolDecoders.add(new RC6Decoder());
        m_ProtocolDecoders.add(new RC5Decoder());
        m_ProtocolDecoders.add(new JVCDecoder());
        m_ProtocolDecoders.add(new ViasatDecoder());
        m_ProtocolDecoders.add(new PioneerDecoder());
        m_ProtocolDecoders.add(new HKDecoder());
        m_ProtocolDecoders.add(new UPMDecoder());
        m_ProtocolDecoders.add(new NexaDecoder());
        m_ProtocolDecoders.add(new NexaLDecoder());
        m_ProtocolDecoders.add(new DeltronicDecoder());
        m_ProtocolDecoders.add(new X10Decoder());
        m_ProtocolDecoders.add(new ProntoDecoder());
        m_ProtocolDecoders.add(new WavemanDecoder());
        m_ProtocolDecoders.add(new RisingSunDecoder());
        m_ProtocolDecoders.add(new NexaFireDecoder());
        m_ProtocolDecoders.add(new RollerTrolDecoder());
        m_ProtocolDecoders.add(new RawDecoder());

        for (ProtocolDecoder decoder : m_ProtocolDecoders.getAllDecoders()) {
            decoder.setTarget(this);
        }

        // Create our CUL-Port and attach the decoders directly to it.
        culPort = new CULProtocolPort(m_ProtocolDecoders);
        culPort.setMode(1);

        // TODO This is a temporary fix...
        culPort.setSerialPort("COM12");
        factory = new EncoderFactory(Encoders.getAllTypes());
    }

    public boolean receiveEvent(Event event) {
        if (!event.getAttribute("Direction").equals("Out") || !culPort.isOpen()) {
            return false;
        }
        ProtocolEncoder foundEncoder = factory.getEncoder(event);
        if (foundEncoder != null) {
            try {
                Message parameters = factory.extractMessage(event);
                int repeat = calculateRepeat(event, foundEncoder);
                int modulationFrequency = calculateModulationFrequency(event, foundEncoder, parameters);
                setModulationFrequencyOnPort(modulationFrequency);
                culPort.playMessage(foundEncoder.encode(parameters, ProtocolEncoder.Phase.REPEATED), repeat, 0);
                setModulationFrequency(getModulationFrequency());
                sendCount++;
                return true;
            } catch (BadMessageException e) {
                logger.warning("Bad protocol message received: " + event.getAttribute(Event.EVENT_TYPE_ATTRIBUTE));
            }
        }
        return false;
    }

    private int calculateModulationFrequency(Event event, ProtocolEncoder encoder, Message message) {
        if (event.getAttribute("Modulation").equals("On")) {
            return encoder.modulationFrequency(message);
        }
        return 0;
    }

    private int calculateRepeat(Event event, ProtocolEncoder foundEncoder) {
        int result = event.getAttributeInt("Repeat");
        if (result < 1) {
            result = foundEncoder.getInfo().getDefaultRepeatCount();
        }
        return result;
    }

    /* (non-Javadoc)
      * @see ssg.home.HomeItem#getModel()
      */
    public String getModel() {
        return m_Model;
    }

    public void activate(HomeService server) {
        super.activate(server);
        factory.addEncoderTypes(server.getPluginProvider().getPluginsForInterface(ProtocolEncoder.class));
        culPort.open();
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        if (culPort.isOpen()) {
            culPort.close();
        }
    }

    /**
     * Reconnect the port
     */
    public void reconnect() {
        if (culPort.isOpen()) {
            culPort.close();
        }
        culPort.open();
    }

    /**
     * Play a short test beep. This is for test purposes
     */
    public void playTestBeep() {
        ShortBeepEncoder beep = new ShortBeepEncoder();
        beep.setFrequency(testBeepFrequency);
        beep.setDuration(0.05F);
        culPort.playMessage(beep.encode(), 10, 0);
        sendCount++;
    }

    /**
     * @return Returns the SerialPort.
     */
    public String getSerialPort() {
        return culPort.getSerialPort();
    }

    /**
     * @param SerialPort The SerialPort to set.
     */
    public void setSerialPort(String SerialPort) {
        culPort.setSerialPort(SerialPort);
        if (culPort.isOpen()) {
            culPort.close();
            culPort.open();
        }
    }

    /**
     * @return Returns the m_TestBeepFrequency.
     */
    public String getTestBeepFrequency() {
        return Integer.toString(testBeepFrequency);
    }

    /**
     * @param TestBeepFrequency The m_TestBeepFrequency to set.
     */
    public void setTestBeepFrequency(String TestBeepFrequency) {
        testBeepFrequency = Integer.parseInt(TestBeepFrequency);
    }

    public String getConnected() {
        return culPort.isOpen() ? "Connected" : "Not Connected";
    }

    public void parsedMessage(ProtocolMessage message) {
        // TODO Auto-generated method stub

    }

    public void partiallyParsedMessage(String protocol, int bits) {
        // TODO Auto-generated method stub

    }

    public void reportLevel(int level) {
        // TODO Auto-generated method stub

    }

    public String getModulationFrequency() {
        return Float.toString(modulationFrequency);
    }

    public void setModulationFrequency(String newModulationFrequency) {
        float frequency = Float.parseFloat(newModulationFrequency);
        if ((frequency >= 10000) && (frequency <= 100000)) {
            modulationFrequency = frequency;
        } else {
            modulationFrequency = 0;
        }
        setModulationFrequencyOnPort(modulationFrequency);
    }

    private void setModulationFrequencyOnPort(float frequency) {
        int modulationPeriod;
        if ((frequency > 0)) {
            modulationPeriod = (int) (1 / (frequency * 375E-9 * 2.0));
        } else {
            modulationPeriod = 0;
        }
        culPort.setModulationOnPeriod(modulationPeriod);
        culPort.setModulationOffPeriod(modulationPeriod);
    }

    public String getTransmissionPower() {
        return Integer.toString(culPort.getOutputPowerOrdinal());
    }

    public void setTransmissionPower(String transmissionPower) {
        int power = Integer.parseInt(transmissionPower);
        if ((power >= 0) && (power <= 7)) {
            culPort.setOutputPowerOrdinal(power);
        }
    }

    public String getSendCount() {
        return Long.toString(sendCount);
    }
}
