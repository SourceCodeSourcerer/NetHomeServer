package nu.nethome.home.items.net.wemo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class InsightStateTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void canDecodeState() throws Exception, WemoException {
        InsightState insightState = new InsightState("8|1423995823|0|0|0|1185615|0|0|0|0.000000|8000");
        assertThat(insightState.getState(), is(InsightState.State.Idle));

        insightState = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        assertThat(insightState.getState(), is(InsightState.State.On));

        insightState = new InsightState("0|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        assertThat(insightState.getState(), is(InsightState.State.Off));
    }

    @Test
    public void canDecodeCurrentPower() throws Exception, WemoException {
        InsightState insightState = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        assertThat(insightState.getCurrentConsumption(), is(105.745));
    }

    @Test
    public void canDecodeTotalPower() throws Exception, WemoException {
        InsightState insightState = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        assertThat(insightState.getTotalConsumption(), closeTo(0.022037, 0.000001));
    }

    @Test
    public void canDecodeTotalOnTime() throws Exception, WemoException {
        InsightState insightState = new InsightState("1|1424028730|713|784|762|1209600|105|105745|1322278|1322278.000000|8000");
        assertThat(insightState.getTotalOnTime(), is(762L));
    }
}
