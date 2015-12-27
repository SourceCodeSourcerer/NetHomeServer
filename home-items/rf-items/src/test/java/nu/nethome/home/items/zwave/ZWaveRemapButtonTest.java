package nu.nethome.home.items.zwave;

import nu.nethome.home.impl.CommandLineExecutor;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.Event;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class ZWaveRemapButtonTest {

    public static final String CALL_1_ON = "call,1,on";
    public static final String CALL_1_OFF = "call,1,off";
    private ZWaveRemapButton zWaveRemapButton;
    private CommandLineExecutor commandLineExecutor;
    private LocalHomeItemProxy homeItemProxy;

    @Before
    public void setUp() throws Exception {
        zWaveRemapButton = new ZWaveRemapButton();
        commandLineExecutor = mock(CommandLineExecutor.class);
        zWaveRemapButton.commandExecutor = commandLineExecutor;
        homeItemProxy = new LocalHomeItemProxy(zWaveRemapButton);
        homeItemProxy.setAttributeValue("InstanceId", "5");
        homeItemProxy.setAttributeValue("OnCommand", CALL_1_ON);
        homeItemProxy.setAttributeValue("OffCommand", CALL_1_OFF);
    }

    @Test
    public void switchesOnForOnCommand() throws Exception {
        Event event = createZWaveEvent("0004000607600D00052001FF41", 4);

        zWaveRemapButton.receiveEvent(event);

        verify(commandLineExecutor).executeCommandLine(CALL_1_ON);
    }

    @Test
    public void switchesOffForOffCommand() throws Exception {
        Event event = createZWaveEvent("0004000607600D000520010041", 4);

        zWaveRemapButton.receiveEvent(event);

        verify(commandLineExecutor).executeCommandLine(CALL_1_OFF);
    }

    @Test
    public void doesNotSwitchOffForOffCommandForOtherInstance() throws Exception {
        Event event = createZWaveEvent("0004000607600D000520010041", 4);
        homeItemProxy.setAttributeValue("InstanceId", "17");

        zWaveRemapButton.receiveEvent(event);

        verifyNoMoreInteractions(commandLineExecutor);
    }

    @Test
    public void doesNotSwitchOffIfDisabled() throws Exception {
        Event event = createZWaveEvent("0004000607600D000520010041", 4);
        homeItemProxy.callAction("disable");

        zWaveRemapButton.receiveEvent(event);

        verifyNoMoreInteractions(commandLineExecutor);
    }

    private Event createZWaveEvent(String s, int messageType) {
        Event event = new InternalEvent("ZWave_Message");
        event.setAttribute("Value", s);
        event.setAttribute(ZWaveController.ZWAVE_TYPE, "Request");
        event.setAttribute(ZWaveController.ZWAVE_MESSAGE_TYPE, messageType);
        event.setAttribute("Direction", "In");
        return event;
    }
}
