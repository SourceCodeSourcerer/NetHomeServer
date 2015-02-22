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

package nu.nethome.home.items.audio;

import nu.nethome.coders.decoders.*;
import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.home.system.Event;
import nu.nethome.util.plugin.Plugin;
import nu.nethome.util.ps.FieldValue;
import nu.nethome.util.ps.ProtocolDecoder;
import nu.nethome.util.ps.ProtocolDecoderSink;
import nu.nethome.util.ps.ProtocolMessage;
import nu.nethome.util.ps.impl.AudioProtocolPort;
import nu.nethome.util.ps.impl.FIRFilter6000;
import nu.nethome.util.ps.impl.ProtocolDecoderGroup;
import nu.nethome.util.ps.impl.SimpleFlankDetector;

import javax.sound.sampled.Mixer.Info;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * AudioProtocolParser
 *
 * @author Stefan
 */
@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Hardware")
public class AudioProtocolParser extends HomeItemAdapter implements HomeItem, ProtocolDecoderSink {

    private final static String MODEL_1 = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"AudioProtocolParser\"  Category=\"Hardware\" Morphing=\"true\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getConnected\" Default=\"true\" />"
            + "  <Attribute Name=\"Protocols\" Type=\"Strings\" Get=\"getDecoderNames\" 	Set=\"setDecoderNames\" >");

    private final static String MODEL_2 = ("   </Attribute>"
            + "  <Attribute Name=\"SourceNumber\" Type=\"String\" Init=\"setSourceName\" />"
            + "  <Attribute Name=\"SourceName\" Type=\"StringList\" Get=\"getSourceName\" 	Set=\"setSourceName\" >");

    private final static String MODEL_3 = ("   </Attribute>"
            + "  <Attribute Name=\"Channel\" Type=\"StringList\" Get=\"getChannel\" 	Set=\"setChannel\" >"
            + "    <item>MONO</item><item>RIGHT</item><item>LEFT</item></Attribute>"
            + "  <Attribute Name=\"FlankSwing\" Type=\"String\" Get=\"getFlankSwing\" 	Set=\"setFlankSwing\" />"
            + "  <Attribute Name=\"FlankLength\" Type=\"StringList\" Get=\"getFlankLength\" 	Set=\"setFlankLength\" >"
            + "    <item>1</item><item>2</item><item>3</item><item>4</item><item>5</item></Attribute>"
            + "  <Attribute Name=\"FlankHoldoff\" Type=\"String\" Get=\"getFlankHoldoff\" 	Set=\"setFlankHoldoff\" />"
            + "  <Attribute Name=\"UseFilter\" Type=\"Boolean\" Get=\"getUseFilter\" 	Set=\"setUseFilter\" />"
            + "  <Attribute Name=\"PulseWidthModification\" Type=\"String\" Get=\"getPulseWidthModification\" 	Set=\"setPulseWidthModification\" />"
            + "  <Attribute Name=\"MaxRepeats\" Type=\"String\" Get=\"getMaxRepeats\" 	Set=\"setMaxRepeats\" />"
            + "  <Attribute Name=\"Received\" Type=\"String\" Get=\"getReceived\"  />"
            + "  <Attribute Name=\"Sent\" Type=\"String\" Get=\"getSent\"  />"
            + "  <Attribute Name=\"ActiveSource\" Type=\"String\" Get=\"getActiveSource\" />"
            + "</HomeItem> ");

    private static final long RESTART_INTERVAL = 1000 * 60 * 60 * 1; // 1 hour
    private static final long REACTIVATE_INTERVAL = 1000 * 9; // 9 seconds

    private static Logger logger = Logger.getLogger(AudioProtocolParser.class.getName());
    private Timer restartTimer = new Timer("AudioProtocolParserRestarter");
    private Timer reactivateTimer = new Timer("AudioProtocolParserReactivator");
    private AudioProtocolPort audioSampler;
    private SimpleFlankDetector flankDetector;
    private ProtocolDecoderGroup decoders = new ProtocolDecoderGroup();
    private FIRFilter6000 filter;
    private boolean isActive = false;


    // Public attributes
    private int level = 0;
    private int received = 0;
    private String decoderNames = "UPMDecoder";
    private String sourceName = "";
    private int sent;
    private int maxRepeats = 0;
    private boolean lostConnection = false;

    public String getModel() {
        StringBuilder result = new StringBuilder(MODEL_1);
        for (ProtocolDecoder decoder : decoders.getAllDecoders()) {
            result.append("<item>");
            result.append(decoder.getInfo().getName());
            result.append("</item>");
        }
        result.append(MODEL_2);
        result.append("<item>");
        result.append(getSourceName());
        result.append("</item>");
        for (Info source : audioSampler.getSourceList()) {
            result.append("<item>");
            result.append(source.getName());
            result.append("</item>");
        }
        result.append(MODEL_3);
        return result.toString();
    }

    /**
     * Activate the instance
     */
    public void activate() {

        // Start the Sampler.
        audioSampler.open();

        // Start the restart timer. On the Windows platform it seems that after a while
        // some kind of delay of the input signal is inserted. The only way I have found
        // to handle this is to simply restart the sampler on even intervals as this seems
        // to remove the problem.
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            restartTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    logger.fine("Cycling audio sampling");
                    // Restart sampling
                    cycleAudioSampler(true);
                }
            }, getRestartInterval(), getRestartInterval());
        }

        // Verify sampler connection on regular intervals and restart if needed.
        reactivateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cycleAudioSampler(false);
            }
        }, getReactivateInterval(), getReactivateInterval());

        isActive = true;
    }

    /**
     * Restart the audio sampler if needed. On windows platform the sampling may deteriorate after a while
     * and if the sound card is USB-based, it may be disconnected and then reconnected.
     *
     * @param force If true, the sampler is ALLWAYS restarted
     */
    protected synchronized void cycleAudioSampler(boolean force) {
        if (force) {
            // Restart sampling and by setting source name again. The order of
            // sources may have shifted, if for example a USB sound card was temporarily removed
            setSourceName(getSourceName());
            // No force - check if the channel is open and active - if not try to reactivate
        } else if (!audioSampler.isOpen() || !audioSampler.isActive()) {
            // If it was previously open - log that connection was lost
            if (!lostConnection) {
                logger.warning("Restarting audio sampler - lost connection");
                lostConnection = true;
            }
            // Restart sampling and by setting source name again.
            setSourceName(getSourceName());
            // If we succeeded in opening it, log this
            if (audioSampler.isOpen() && audioSampler.isActive()) {
                logger.info("Audio Sampler resumed connection");
            }
        } else {
            lostConnection = false;
        }
    }

    private long getRestartInterval() {
        long result = RESTART_INTERVAL;
        String intervalString = System.getProperty("nu.nethome.home.items.audio.AudioProtocolParser.RestartInterval");
        if (intervalString != null) {
            result = Long.parseLong(intervalString) * 1000 * 60; // Scale to minutes
        }
        return result;
    }

    private long getReactivateInterval() {
        long result = REACTIVATE_INTERVAL;
        String intervalString = System.getProperty("nu.nethome.home.items.audio.AudioProtocolParser.ReactivateInterval");
        if (intervalString != null) {
            result = Long.parseLong(intervalString) * 1000; // Scale to seconds
        }
        return result;
    }

    /**
     * HomeItem method which stops all object activity for program termination
     */
    public void stop() {
        isActive = false;
        // Stop the timer
        restartTimer.cancel();
        reactivateTimer.cancel();

        // Stop the sampler
        if (audioSampler != null) {
            audioSampler.close();
        }
        super.stop();
    }

    /**
     * Analyze the protocols attribute and activate/deactivate the
     * corresponding decoders
     */
    public void updateDecoderActivation() {

        // Find the selected protocol decoders and add the names to a map
        HashMap<String, Boolean> parsers = new HashMap<>();
        StringTokenizer parser = new StringTokenizer(decoderNames, ",", false);
        String token;
        while (parser.hasMoreTokens()) {
            token = parser.nextToken(",");
            parsers.put(token, true);
        }

        // Attach the decoders
        for (ProtocolDecoder decoder : decoders.getAllDecoders()) {
            // Activate/Deactivate the selected ones
            decoders.setActive(decoder, parsers.containsKey(decoder.getInfo().getName()));
        }

    }

    public void parsedMessage(ProtocolMessage message) {
        received++;

        // Check if this is a repeated message, and if it is more repeats than specified
        if (message.getRepeat() > maxRepeats) {
            return;
        }
        sent++;
        // Build an internal event from the protocol message
        Event event = server.createEvent(message.getProtocol() + "_Message", "");
        // Specify message direction
        event.setAttribute("Direction", "In");
        // Add all protocol specific parameters as event attributes. Use protocol
        // name as prefix to avoid mix with standard attribute names
        for (FieldValue f : message.getFields()) {
            event.setAttribute(message.getProtocol() + "." + f.getName(), f.getStringValue());
        }
        // Send the event internally
        server.send(event);
    }

    public void partiallyParsedMessage(String protocol, int bits) {
    }

    public void reportLevel(int level) {
        this.level = level;
    }

    public AudioProtocolParser() {

        // Create all the Protocol-Decoders
        decoders.add(new SIRCDecoder());
        decoders.add(new RC6Decoder());
        decoders.add(new RC5Decoder());
        decoders.add(new JVCDecoder());
        decoders.add(new ViasatDecoder());
        decoders.add(new PioneerDecoder());
        decoders.add(new HKDecoder());
        decoders.add(new UPMDecoder());
        decoders.add(new NexaDecoder());
        decoders.add(new NexaLDecoder());
        decoders.add(new DeltronicDecoder());
        decoders.add(new X10Decoder());
        decoders.add(new ProntoDecoder());
        decoders.add(new WavemanDecoder());
        decoders.add(new RisingSunDecoder());
        decoders.add(new NexaFireDecoder());
        decoders.add(new ZhejiangDecoder());
        decoders.add(new OregonDecoder());
        decoders.add(new FineOffsetDecoder());

        StringBuilder allDecoderNames = new StringBuilder();
        String separator = "";
        for (ProtocolDecoder decoder : decoders.getAllDecoders()) {
            decoder.setTarget(this);
            if (!decoder.getInfo().getName().equals("Pronto")) {
                allDecoderNames.append(separator);
                allDecoderNames.append(decoder.getInfo().getName());
                separator = ",";
            } else {
                decoders.setActive(decoder, false);
            }
        }
        decoderNames = allDecoderNames.toString();

        // Activate the selected ones
        updateDecoderActivation();

        // Create The Flank Detector and attach the decoders
        flankDetector = new SimpleFlankDetector();
        flankDetector.setProtocolDecoder(decoders);

        // Create the FIR-Filter and attach to the samplers
        filter = new FIRFilter6000(flankDetector);

        // Create our sampler port and attach the Filter
        audioSampler = new AudioProtocolPort(filter);

    }

    /**
     * @return Returns the level.
     */
    public String getLevel() {
        return String.valueOf(level);
    }

    /**
     * @param Level The level to set.
     */
    public void setLevel(String Level) {
        level = Integer.parseInt(Level);
    }

    /**
     * @return Returns the m_SourceNumber.
     */
    public String getSourceNumber() {
        return String.valueOf(audioSampler.getSource());
    }

    /**
     * @param Attribute3 The m_SourceNumber to set.
     */
    public void setSourceNumber(String Attribute3) {
        audioSampler.setSource(Integer.parseInt(Attribute3));
        if (audioSampler.isOpen()) {
            audioSampler.close();
            audioSampler.open();
        }
    }

    /**
     * @return Returns the received.
     */
    public String getReceived() {
        return String.valueOf(received);
    }

    /**
     * @param Received The received to set.
     */
    public void setReceived(String Received) {
        received = Integer.parseInt(Received);
    }

    /**
     * @return Returns the m_Channel.
     */
    public String getChannel() {
        switch (audioSampler.getChannel()) {
            case LEFT:
                return "LEFT";
            case RIGHT:
                return "RIGHT";
            default:
                return "MONO";
        }
    }

    /**
     * @param channel The channel to set.
     */
    public void setChannel(String channel) {
        AudioProtocolPort.Channel c = AudioProtocolPort.Channel.MONO;
        if (channel.compareToIgnoreCase("RIGHT") == 0) c = AudioProtocolPort.Channel.RIGHT;
        if (channel.compareToIgnoreCase("LEFT") == 0) c = AudioProtocolPort.Channel.LEFT;
        audioSampler.setChannel(c);
    }

    /**
     * @return Returns the m_FlankSwing.
     */
    public String getFlankSwing() {
        return String.valueOf(flankDetector.getFlankSwing());
    }

    /**
     * @param Attribute5 The m_FlankSwing to set.
     */
    public void setFlankSwing(String Attribute5) {
        flankDetector.setFlankSwing(Integer.parseInt(Attribute5));
    }

    /**
     * @return the m_FlankLength
     */
    public String getFlankLength() {
        return String.valueOf(flankDetector.getFlankLength());
    }

    /**
     * @param flankLength the m_FlankLength to set
     */
    public void setFlankLength(String flankLength) {
        flankDetector.setFlankLength(Integer.parseInt(flankLength));
    }

    /**
     * @return the m_FlankHoldoff
     */
    public String getFlankHoldoff() {
        return String.valueOf(flankDetector.getFlankHoldoff());
    }

    /**
     * @param flankHoldoff the m_FlankHoldoff to set
     */
    public void setFlankHoldoff(String flankHoldoff) {
        flankDetector.setFlankHoldoff(Integer.parseInt(flankHoldoff));
    }

    /**
     * @return the m_UseFilter
     */
    public String getUseFilter() {
        return String.valueOf(filter.isActive());
    }

    /**
     * @param useFilter the m_UseFilter to set
     */
    public void setUseFilter(String useFilter) {
        filter.setActive(Boolean.parseBoolean(useFilter));
    }

    /**
     * @return the decoderNames
     */
    public String getDecoderNames() {
        return decoderNames;
    }

    /**
     * Specify audio source. This can be specified in two ways, either the
     * source number or by the name of the source. Even if the number is
     * specified, the source name is stored and used.
     *
     * @param name source name or source number
     */
    public synchronized void setSourceName(String name) {
        int newSourceNumber = 0;
        boolean foundSource = false;

        // Close the line so we get fresh info about available mixers
        if (isActive) {
            audioSampler.close();
        }

        // Get a fresh list of available sources
        audioSampler.findMixers();
        Info sources[] = audioSampler.getSourceList();

        // Check if the source name was specified as a source number
        if ((name.length() == 1) &&
                (name.charAt(0) >= '0') &&
                (name.charAt(0) <= '9') &&
                (Integer.parseInt(name) < sources.length)) {
            newSourceNumber = Integer.parseInt(name);
            foundSource = true;
            // Update the source name with the real name of the source
            sourceName = sources[newSourceNumber].getName();
        } else {
            // Set the name as specified
            sourceName = name;
            // Loop through the mixers to find one with the specified name
            for (int i = 0; i < sources.length; i++) {
                if (sources[i].getName().equalsIgnoreCase(name)) {
                    newSourceNumber = i;
                    foundSource = true;
                }
            }
        }
        if (!foundSource) {
            logger.warning("Could not find specified audio source:" + name);
        }
        // Set the source number. Note that if we failed to find the specified source, the
        // source number is set to 0, but the source name is still kept.
        audioSampler.setSource(newSourceNumber);

        // Open the sampler again
        if (isActive && foundSource) {
            audioSampler.open();
        }
    }

    public String getSourceName() {
        return sourceName;
    }

    /**
     * Specify which protocols to decode. The argument is a comma separated string
     * listing the names of the protocols to decode (note, the name of the protocol,
     * not the decoder).
     *
     * @param decoderNames the DecoderNames to set
     */
    public void setDecoderNames(String decoderNames) {
        this.decoderNames = decoderNames;
        updateDecoderActivation();
    }

    public String getSource(int nr) {
        if (nr >= audioSampler.getSourceList().length) {
            return "";
        }
        String selected = "";
        if ((nr == audioSampler.getSource()) && audioSampler.isOpen()) {
            selected = "->";
        }
        return selected + audioSampler.getSourceList()[nr].getName();
    }

    public String getActiveSource() {
        if (audioSampler.isOpen()) {
            int sourceNumber = audioSampler.getSource();
            return "[" + sourceNumber + "] " + audioSampler.getSourceList()[sourceNumber].getName();
        }
        return "[No active source]";
    }

    public String getSent() {
        return Integer.toString(sent);
    }

    public String getPulseWidthModification() {
        return Integer.toString(flankDetector.getPulseWidthCompensation());
    }

    public void setPulseWidthModification(String pulseWidthModification) {
        flankDetector.setPulseWidthCompensation(Integer.parseInt(pulseWidthModification));
    }

    public String getMaxRepeats() {
        return Integer.toString(maxRepeats);
    }

    public void setMaxRepeats(String maxRepeats) {
        this.maxRepeats = Integer.parseInt(maxRepeats);
    }

    public String getConnected() {
        return audioSampler.isOpen() ? "Connected" : "Not Connected";
    }
}

