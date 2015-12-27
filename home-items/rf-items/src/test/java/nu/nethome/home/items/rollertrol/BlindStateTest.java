package nu.nethome.home.items.rollertrol;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static nu.nethome.home.items.rollertrol.BlindState.DOWN_STRING;
import static nu.nethome.home.items.rollertrol.BlindState.UP_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class BlindStateTest {

    public static final int TRAVEL_TIME = 10000;
    private BlindState blindState;
    private BlindState timeState;

    @Before
    public void setUp() throws Exception {
        blindState = new BlindState();
        timeState = spy(new BlindState());
        timeState.setTravelTime(TRAVEL_TIME);
    }

    @Test
    public void initiatesToUp() throws Exception {
        assertThat(blindState.getStateString(), is(UP_STRING));
        assertThat(timeState.getStateString(), is(UP_STRING));
        assertThat(blindState.isTravelling(), is(false));
        assertThat(timeState.isTravelling(), is(false));
    }

    @Test
    public void stateIsDownAfterDown() throws Exception {
        blindState.down();
        assertThat(blindState.getStateString(), is(DOWN_STRING));
        assertThat(blindState.isTravelling(), is(false));
    }

    @Test
    public void stateIsStillUpAfterUp() throws Exception {
        blindState.up();
        timeState.up();
        assertThat(blindState.getStateString(), is(UP_STRING));
        assertThat(timeState.getStateString(), is(UP_STRING));
        assertThat(blindState.isTravelling(), is(false));
        assertThat(timeState.isTravelling(), is(false));
    }

    @Test
    public void stateIsStillDownAfterDown() throws Exception {
        blindState.down();
        assertThat(blindState.getStateString(), is(DOWN_STRING));
        blindState.down();
        assertThat(blindState.getStateString(), is(DOWN_STRING));
    }

    @Test
    public void stateIsUpAfterUp() throws Exception {
        blindState.down();
        blindState.up();
        assertThat(blindState.getStateString(), is(UP_STRING));
    }

    @Test
    public void timeStateIsDownLongTimeAfterDown() throws Exception {
        timeState.down();
        assertStateAfterTime(TRAVEL_TIME * 1000, DOWN_STRING);
        assertThat(timeState.isTravelling(), is(false));
    }

    private void assertStateAfterTime(long timePassed, String expectedState) {
        doReturn(timePassed).when(timeState).timePassedSince(anyLong());
        assertThat(timeState.getStateString(), is(expectedState));
    }

    @Test
    public void timeStateIsUpLongTimeAfterUp() throws Exception {
        timeState.down();
        doReturn(TRAVEL_TIME * 1000L).when(timeState).timePassedSince(anyLong());
        timeState.up();
        assertStateAfterTime(TRAVEL_TIME * 1000, UP_STRING);
        assertThat(timeState.isTravelling(), is(false));
    }

    @Test
    public void timeStateIsDown0DirectlyAfterDown() throws Exception {
        timeState.down();
        assertStateAfterTime(0, DOWN_STRING + " 0%");
    }

    @Test
    public void timeStateIsDown50HalfTravelTimeAfterDown() throws Exception {
        timeState.down();
        assertStateAfterTime(TRAVEL_TIME / 2, DOWN_STRING + " 50%");
        assertThat(timeState.isTravelling(), is(true));
    }

    @Test
    public void timeStateIsDown95DirectlyAfterUp() throws Exception {
        timeState.down();
        doReturn(TRAVEL_TIME * 1000L).when(timeState).timePassedSince(anyLong());
        timeState.up();
        assertStateAfterTime(0, DOWN_STRING + " 100%");
    }

    @Test
    public void timeStateIsDown50HalfTravelTimeAfterUp() throws Exception {
        timeState.down();
        doReturn(TRAVEL_TIME * 1000L).when(timeState).timePassedSince(anyLong());
        timeState.up();
        assertStateAfterTime(TRAVEL_TIME / 2, DOWN_STRING + " 50%");
    }

    @Test
    public void stopDoesNotAffectUp() throws Exception {
        blindState.stop();
        timeState.stop();
        assertThat(blindState.getStateString(), is(UP_STRING));
        assertThat(timeState.getStateString(), is(UP_STRING));
    }

    @Test
    public void stopDoesNotAffectDown() throws Exception {
        blindState.down();
        timeState.down();
        doReturn(TRAVEL_TIME * 1000L).when(timeState).timePassedSince(anyLong());
        blindState.stop();
        timeState.stop();
        assertThat(blindState.getStateString(), is(DOWN_STRING));
        assertStateAfterTime(TRAVEL_TIME * 1000, DOWN_STRING);
        assertThat(blindState.isTravelling(), is(false));
        assertThat(timeState.isTravelling(), is(false));
    }

    @Test
    public void timeStateIsDown50WhenStopAfterHalfTravelTimeAfterDown() throws Exception {
        timeState.down();
        doReturn(TRAVEL_TIME / 2L).when(timeState).timePassedSince(anyLong());
        timeState.stop();
        assertStateAfterTime(TRAVEL_TIME * 1000, DOWN_STRING + " 50%");
        assertThat(timeState.isTravelling(), is(false));
    }


}
