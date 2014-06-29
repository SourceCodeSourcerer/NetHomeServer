package nu.nethome.home.items.fineoffset;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 *
 */
public class PeriodCounterTest {

    public static final String STATE = "1,5,10";
    public static final int PULSES_AT_START_OF_FIRST_PERIOD = 10;
    public static final long PULSES_DURING_FIRST_PERIOD = 10L;
    public static final long PULSES_DURING_SECOND_PERIOD = 20L;
    long period;
    PeriodCounter counter;


    @Before
    public void setUp() throws Exception {
        counter = new PeriodCounter() {
            @Override
            public long getNewPeriod() {
                return period;
            }
        };
        period = 1;
    }

    @Test
    public void uninitializedStateIsEmpty() throws Exception {
        assertThat(counter.getState(), is(""));
    }

    @Test
    public void preservesState() throws Exception {
        counter.setState(STATE);
        assertThat(counter.getState(), is(STATE));
    }

    @Test
    public void updatesWithinPeriod() throws Exception {
        counter.updateCounter(PULSES_AT_START_OF_FIRST_PERIOD);
        assertThat(counter.getPulsesDuringPeriod(PULSES_AT_START_OF_FIRST_PERIOD + PULSES_DURING_FIRST_PERIOD), is(PULSES_DURING_FIRST_PERIOD));
    }

    @Test
    public void updatesWithNewPeriod() throws Exception {
        counter.updateCounter(PULSES_AT_START_OF_FIRST_PERIOD);
        period++;
        counter.updateCounter(PULSES_AT_START_OF_FIRST_PERIOD + PULSES_DURING_FIRST_PERIOD);
        assertThat(counter.getPulsesDuringPeriod(PULSES_AT_START_OF_FIRST_PERIOD + PULSES_DURING_FIRST_PERIOD + PULSES_DURING_SECOND_PERIOD), is(PULSES_DURING_SECOND_PERIOD));
    }
}
