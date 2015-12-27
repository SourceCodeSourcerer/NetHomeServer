package nu.nethome.home.items.timer.SunTimer;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import nu.nethome.home.impl.InternalEvent;
import nu.nethome.home.impl.LocalHomeItemProxy;
import nu.nethome.home.system.HomeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 *
 */
public class SunTimerTest {

    private SunTimer sunTimer;
    private LocalHomeItemProxy proxy;
    private HomeService server;
    private Timer timer;
    private Calendar calendar;
    private DateFormat dateFormat;
    private TimeZone timeZone;
    private static final InternalEvent MINUTE_EVENT = new InternalEvent(HomeService.MINUTE_EVENT_TYPE);

    @Before
    public void setUp() throws Exception {
        sunTimer = spy(new SunTimer());
        proxy = new LocalHomeItemProxy(sunTimer);
        server = mock(HomeService.class);
        timer = mock(Timer.class);
        doReturn(timer).when(sunTimer).createTimer();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.YEAR, 2015);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DATE, 24);
        doReturn(calendar).when(sunTimer).getTime();
        dateFormat = new SimpleDateFormat("HH:mm");
        // doReturn(Calendar.TUESDAY).when(sunTimer).getDayToday();
        timeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
    }

    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(timeZone);
    }

    @Test
    public void testGetModel() throws SAXException, IOException {
        SAXParser parser = new SAXParser();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(sunTimer.getModel().getBytes());
        InputSource source = new InputSource(byteStream);
        // Just verify that the XML is valid
        parser.parse(source);
    }

    static final String[] weekdays = {"Mondays", "Tuesdays", "Wednesdays", "Thursdays", "Fridays", "Saturdays", "Sundays"};

    @Test
    public void setAndGetWeekDays() throws Exception {
        for (String weekday : weekdays) {
            proxy.setAttributeValue(weekday, weekday + "x");
        }
        for (String weekday : weekdays) {
            assertThat(proxy.getAttributeValue(weekday), is(weekday + "x"));
        }
    }

    @Test
    public void activationSetsCurrentDayString() throws Exception {
        proxy.setAttributeValue("Thursdays", "10:00-01:00->11:00,->13:00,15:00->");
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("09:00->11:00,->13:00,15:00->"));
    }

    @Test
    public void updateDayTimeExpressionDoesNotSetCurrentDayStringIfNotActivated() throws Exception {
        proxy.setAttributeValue("Thursdays", "10:00->11:00");
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
    }

    @Test
    public void updateDayTimeExpressionSetsCurrentDayStringIfActivated() throws Exception {
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
        proxy.setAttributeValue("Thursdays", "11:00->12:00");
        assertThat(proxy.getAttributeValue("Timer Today"), is("11:00->12:00"));
    }

    @Test
    public void updateDayTimeExpressionWithUnchangedValueDoesNotUpdateCurrentDayString() throws Exception {
        proxy.setAttributeValue("Thursdays", "11:00->12:00");
        sunTimer.activate(server);
        proxy.setAttributeValue("Thursdays", "11:00->12:00");
        proxy.setAttributeValue("Thursdays", "11:00->12:00");
        proxy.setAttributeValue("Thursdays", "11:00->12:00");
        verify(sunTimer, times(1)).applySwitchTimesForToday();
    }

    @Test
    public void activationCreatesTimerTasks() throws Exception {
        proxy.setAttributeValue("Thursdays", "13:00->14:00");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer, times(2)).schedule(taskCaptor.capture(), dateCaptor.capture());
        assertThat(taskCaptor.getAllValues().size(), is(2));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("13:00"));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(1)), is("14:00"));
        assertThat(taskCaptor.getAllValues().get(0), instanceOf(SunTimer.SunTimerTask.class));
        assertThat(taskCaptor.getAllValues().get(1), instanceOf(SunTimer.SunTimerTask.class));
    }

    @Test
    public void activationDoesNotCreatesTimerTasksInThePast() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Thursdays", "10:00->11:00,11:30->12:30");

        sunTimer.activate(server);

        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer).schedule(any(TimerTask.class), dateCaptor.capture());
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("12:30"));
    }

    @Test
    public void onTimerTaskExecutesOnMethod() throws Exception {
        proxy.setAttributeValue("Thursdays", "13:00->");
        proxy.setAttributeValue("OnCommand", "call,foo,fie");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer).schedule(taskCaptor.capture(), any(Date.class));
        taskCaptor.getValue().run();
        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void offTimerTaskExecutesOffMethod() throws Exception {
        proxy.setAttributeValue("Thursdays", "->13:00");
        proxy.setAttributeValue("OffCommand", "call,foo,fie");

        sunTimer.activate(server);

        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer).schedule(taskCaptor.capture(), any(Date.class));
        taskCaptor.getValue().run();
        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void executesMostRecentOnCommandInThePastWhenActivated() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Thursdays", "10:00->11:00,11:30->12:30");
        proxy.setAttributeValue("OnCommand", "call,foo,fie");

        sunTimer.activate(server);

        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void executesMostRecentOffCommandInThePastWhenActivated() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Thursdays", "10:00->11:00,11:30->11:40");
        proxy.setAttributeValue("OffCommand", "call,foo,fie");

        sunTimer.activate(server);

        verify(server,times(1)).openInstance("foo"); // Verify that the executor tries to open foo
    }

    @Test
    public void stopsTimerWhenItemIsStopped() throws Exception {
        sunTimer.activate(server);

        sunTimer.stop();

        verify(timer).cancel();
    }

    @Test
    public void updateDayTimeExpressionCreatesTimerTasksAndCancelsExistingTimer() throws Exception {
        sunTimer.activate(server);

        proxy.setAttributeValue("Thursdays", "13:00->14:00");

        verify(timer).cancel();
        ArgumentCaptor<TimerTask> taskCaptor = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(timer, times(2)).schedule(taskCaptor.capture(), dateCaptor.capture());
        assertThat(taskCaptor.getAllValues().size(), is(2));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(0)), is("13:00"));
        assertThat(dateFormat.format(dateCaptor.getAllValues().get(1)), is("14:00"));
        assertThat(taskCaptor.getAllValues().get(0), instanceOf(SunTimer.SunTimerTask.class));
        assertThat(taskCaptor.getAllValues().get(1), instanceOf(SunTimer.SunTimerTask.class));
    }

    @Test
    public void variableValuesAreInsertedInTimeExpressions() throws Exception {
        proxy.setAttributeValue("Thursdays", "A->B,C->13:00");
        proxy.setAttributeValue("Variable A", "10:00");
        proxy.setAttributeValue("Variable B", "11:00");
        proxy.setAttributeValue("Variable C", "12:00");

        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("Variable A"), is("10:00"));
        assertThat(proxy.getAttributeValue("Variable B"), is("11:00"));
        assertThat(proxy.getAttributeValue("Variable C"), is("12:00"));
        assertThat(proxy.getAttributeValue("Timer Today"), is("10:00->11:00,12:00->13:00"));
    }

    @Test
    public void calculatesSunRiseTimeForKnownData() throws Exception {
        proxy.setAttributeValue("Location: Lat,Long", "59.225527,18.000718");
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Sunrise(R)"), is("08:44"));
    }

    @Test
    public void calculatesSunSetTimeForKnownData() throws Exception {
        proxy.setAttributeValue("Location: Lat,Long", "59.225527,18.000718");
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Sunset(S)"), is("14:51"));
    }

    @Test
    public void SunriseVariableIsInsertedInTimeExpressions() throws Exception {
        proxy.setAttributeValue("Location: Lat,Long", "59.225527,18.000718");
        proxy.setAttributeValue("Thursdays", "00:00->R");

        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("Timer Today"), is("00:00->08:44"));
    }

    @Test
    public void SunsetVariableIsInsertedInTimeExpressions() throws Exception {
        proxy.setAttributeValue("Location: Lat,Long", "59.225527,18.000718");
        proxy.setAttributeValue("Thursdays", "S->23:59");

        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("Timer Today"), is("14:51->23:59"));
    }

    @Test
    public void switchTimesRecalculatedOnNewDay() throws Exception {
        proxy.setAttributeValue("Thursdays", "10:00->11:00");
        proxy.setAttributeValue("Fridays", "11:00->12:00");
        sunTimer.activate(server);
        sunTimer.receiveEvent(MINUTE_EVENT);
        assertThat(proxy.getAttributeValue("Timer Today"), is("10:00->11:00"));
        calendar.set(Calendar.DATE, 25);
        sunTimer.receiveEvent(MINUTE_EVENT);
        assertThat(proxy.getAttributeValue("Timer Today"), is("11:00->12:00"));
    }

    @Test
    public void repeatPreviousCharacterTakesValueFromPrevious() throws Exception {
        proxy.setAttributeValue("Tuesdays", "10:00->11:00");
        proxy.setAttributeValue("Wednesdays", SunTimer.REPEAT_STRING);
        proxy.setAttributeValue("Thursdays", SunTimer.REPEAT_STRING);
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("10:00->11:00"));
    }

    @Test
    public void repeatPreviousCharacterTakesValueFromPreviousAndWrapsAroundWeek() throws Exception {
        for (String weekday : weekdays) {
            proxy.setAttributeValue(weekday, SunTimer.REPEAT_STRING);
        }
        proxy.setAttributeValue("Fridays", "10:00->11:00");
        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("10:00->11:00"));
    }

    @Test
    public void stateIsEnabledFromStart() throws Exception {
        assertThat(proxy.getAttributeValue("State"), is("Enabled"));
    }

    @Test
    public void stateIsOnIfSwitchedOn() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Thursdays", "11:30->12:30");

        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("State"), is("On"));
    }

    @Test
    public void stateIsEnabledIfSwitchedOff() throws Exception {
        // Note, time now is 12:00
        proxy.setAttributeValue("Thursdays", "11:30->11:40");

        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("State"), is("Enabled"));
    }

    @Test
    public void stateIsDisabledIfDisabled() throws Exception {
        sunTimer.activate(server);
        proxy.callAction("Disable timer");

        assertThat(proxy.getAttributeValue("State"), is("Disabled"));
    }

    @Test
    public void noActiveTimesIfDisabled() throws Exception {
        proxy.setAttributeValue("Thursdays", "11:30->11:40");

        sunTimer.activate(server);
        assertThat(proxy.getAttributeValue("Timer Today"), is("11:30->11:40"));
        proxy.callAction("Disable timer");

        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
    }

    @Test
    public void noSwitchTimesRecalculatedOnNewDayIfDisabled() throws Exception {
        proxy.setAttributeValue("Thursdays", "10:00->11:00");
        proxy.setAttributeValue("Fridays", "11:00->12:00");
        sunTimer.activate(server);
        proxy.callAction("Disable timer");
        sunTimer.receiveEvent(MINUTE_EVENT);
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
        calendar.set(Calendar.DATE, 25);
        sunTimer.receiveEvent(MINUTE_EVENT);
        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
    }

    @Test
    public void noSwitchTimesRecalculatedOnActivationIfDisabled() throws Exception {
        proxy.setAttributeValue("Thursdays", "10:00->11:00");

        proxy.setAttributeValue("State", "Disabled", false);
        sunTimer.activate(server);

        assertThat(proxy.getAttributeValue("Timer Today"), is(""));
    }
}
