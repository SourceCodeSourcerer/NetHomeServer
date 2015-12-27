package nu.nethome.home.items.hue;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 *
 */
public class LightStateTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void canCheckForColorTemperature() throws Exception {
        LightState lightStateWithCT = new LightState(10, 5);
        LightState lightStateWithoutCT = new LightState(10, 10, 20);

        assertThat(lightStateWithCT.hasColorTemperature(), is(true));
        assertThat(lightStateWithoutCT.hasColorTemperature(), is(false));
    }

    @Test
    public void canCheckForHueSat() throws Exception {
        LightState lightStateWithoutHueSat = new LightState(10, 5);
        LightState lightStateWithHueSat = new LightState(10, 10, 20);

        assertThat(lightStateWithHueSat.hasHueSat(), is(true));
        assertThat(lightStateWithoutHueSat.hasHueSat(), is(false));
    }
}
