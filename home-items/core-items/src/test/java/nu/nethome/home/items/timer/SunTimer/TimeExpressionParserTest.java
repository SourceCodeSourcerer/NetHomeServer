package nu.nethome.home.items.timer.SunTimer;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class TimeExpressionParserTest {

    private static final int HOUR = 60 * 60;
    private static final int MINUTE = 60;
    private TimeExpressionParser timeExpressionParser;

    @Before
    public void setUp() throws Exception {
        timeExpressionParser = new TimeExpressionParser();
    }

    @Test
    public void parseEmptyString() throws Exception {
        assertThat(timeExpressionParser.parseExpression("").size(), is(0));
    }

    @Test
    public void parseSimpleOnTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(MINUTE * 2 + HOUR));
        assertThat(switchTimes.get(0).valueAsTimeString(), is("01:02"));
    }

    @Test
    public void parseSimpleOffTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("->2:03");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(false));
        assertThat(switchTimes.get(0).value(), is(MINUTE * 3 + HOUR * 2));
    }

    @Test
    public void parseSimpleTimeSpan() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04");
        assertThat(switchTimes.size(), is(2));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
    }

    @Test
    public void parseMultipleTimeSpans() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:02->03:04,05:06->,->07:08");
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).value(), is(HOUR * 5 + MINUTE * 6));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).value(), is(HOUR * 7 + MINUTE * 8));
    }

    @Test
    public void useVariables() throws Exception {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("A", "01:02");
        variables.put("B", "03:04");
        variables.put("C", "05:02");
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("A->B,04:00->C", variables);
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR + MINUTE * 2));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 3 + MINUTE * 4));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).value(), is(HOUR * 4));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).value(), is(HOUR * 5+ MINUTE * 2));
    }

    @Test
    public void plusTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("->+2:07");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(false));
        assertThat(switchTimes.get(0).value(), is(HOUR * 2 + MINUTE * 7));
    }

    @Test
    public void minusTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("-2:07->");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * -2 + MINUTE * -7));
    }

    @Test
    public void minusMinutes() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("-00:07->");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(MINUTE * -7));
    }

    @Test
    public void addTimes() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("05:01+2:07+01:1");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 8 + MINUTE * 9));
    }

    @Test
    public void subtractTimes() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("->05:01-2:07+01:1");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(false));
        assertThat(switchTimes.get(0).value(), is(HOUR * 4 - MINUTE * 5));
    }

    @Test
    public void negativeVariables() throws Exception {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("A", "-01:02");
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("10:00+A->10:00-A", variables);
        assertThat(switchTimes.size(), is(2));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 8 + MINUTE * 58));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 11 + MINUTE * 2));
    }

    @Test
    public void selectFirstTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("05:01/04:02/04:30");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 4 + MINUTE * 2));
    }

    @Test
    public void selectLastTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("01:01\\05:01\\04:02\\04:30");
        assertThat(switchTimes.size(), is(1));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 5 + MINUTE));
    }

    @Test
    public void mixCombinations() throws Exception {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("A", "10:00");
        variables.put("B", "15:00");
        variables.put("C", "00:30");
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("A-C/A/A+C->A+B+C-10:30+60\\15:59", variables);
        assertThat(switchTimes.size(), is(2));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).value(), is(HOUR * 9 + MINUTE * 30));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).value(), is(HOUR * 16));
    }

    @Test
    public void endBeforeStartDisappears() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("15:00->14:59");
        assertThat(switchTimes.size(), is(0));
    }

    @Test
    public void endSameAsStartDisappears() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("15:00->15:00");
        assertThat(switchTimes.size(), is(0));
    }

    @Test
    public void reportsBadTime() {
        try {
            timeExpressionParser.parseExpression("15:00->14:59,19;00->20:00");
            fail();
        } catch (TimeExpressionParser.TimeExpressionException e) {
            assertThat(e.badExpression, is("19;00"));
        }
    }

    @Test
    public void sortsResultOnTime() throws Exception {
        List<TimeExpressionParser.SwitchTime> switchTimes = timeExpressionParser.parseExpression("04:00->05:00,03:00->03:30");
        assertThat(switchTimes.size(), is(4));
        assertThat(switchTimes.get(0).isOn(), is(true));
        assertThat(switchTimes.get(0).valueAsTimeString(), is("03:00"));
        assertThat(switchTimes.get(1).isOn(), is(false));
        assertThat(switchTimes.get(1).valueAsTimeString(), is("03:30"));
        assertThat(switchTimes.get(2).isOn(), is(true));
        assertThat(switchTimes.get(2).valueAsTimeString(), is("04:00"));
        assertThat(switchTimes.get(3).isOn(), is(false));
        assertThat(switchTimes.get(3).valueAsTimeString(), is("05:00"));
    }

}
