package nu.nethome.home.items.timer.SunTimer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TimeExpressionParser {

    public static final String TIME_EXPRESSION_SEPARATOR = ",";
    public static final String TIME_PERIOD_SEPARATOR = "->";
    public static final String TIME_SEPARATOR = ":";
    private static final String SUM_SEPARATOR_INCLUDING_SEPARATOR = "(?=\\+|\\-)";
    public static final String FIRST_TIME_ALTERNATIVE_SEPARATOR = "/";
    public static final String LAST_TIME_ALTERNATIVE_SEPARATOR = "\\\\";

    public static List<SwitchTime> parseExpression(String expression) throws TimeExpressionException {
        return parseExpression(expression, Collections.<String, String>emptyMap());
    }

    public static List<SwitchTime> parseExpression(String expression, Map<String, String> variables) throws TimeExpressionException {
        ArrayList<SwitchTime> switchTimes = new ArrayList<>();
        String[] switchTimeExpressions = replaceVariables(expression, variables).split(TIME_EXPRESSION_SEPARATOR);
        for (String switchTimeExpression : switchTimeExpressions) {
            switchTimes.addAll(SwitchTime.parseSwitchTimes(switchTimeExpression));
        }
        Collections.sort(switchTimes);
        return switchTimes;
    }

    private static String replaceVariables(String expression, Map<String, String> variables) {
        String result = expression;
        for (String variable : variables.keySet()) {
            result = result.replace(variable, variables.get(variable));
        }
        result = result.replace("--", "+");
        result = result.replace("+-", "-");
        result = result.replace("-+", "-");
        return result;
    }

    public static class TimeExpressionException extends Exception {
        public final String badExpression;

        public TimeExpressionException(String badExpression) {
            super("Cannot parse time expression: " + badExpression);
            this.badExpression = badExpression;
        }
    }

    public static class SwitchTime implements Comparable<SwitchTime> {
        private boolean on;
        private int value = 0;

        public static List<SwitchTime> parseSwitchTimes(String expression) throws TimeExpressionException {
            ArrayList<SwitchTime> switchTimes = new ArrayList<>();
            String[] switchTimeExpressions = expression.split(TIME_PERIOD_SEPARATOR);
            if (switchTimeExpressions.length > 0 && !switchTimeExpressions[0].isEmpty()) {
                switchTimes.add(new SwitchTime(switchTimeExpressions[0], true));
            }
            if (switchTimeExpressions.length > 1 && !switchTimeExpressions[1].isEmpty()) {
                switchTimes.add(new SwitchTime(switchTimeExpressions[1], false));
            }
            if (switchTimes.size() == 2 && switchTimes.get(0).value() >= switchTimes.get(1).value()) {
                return Collections.emptyList();
            }
            return switchTimes;
        }

        public SwitchTime(String switchTimeExpression, boolean isOn) throws TimeExpressionException {
            this.on = isOn;
            this.value = parseLastTimeAlternative(switchTimeExpression);
        }

        private int parseLastTimeAlternative(String switchTimeExpression) throws TimeExpressionException {
            int result = 0;
            boolean isFirst = true;
            String[] alternatives = switchTimeExpression.split(LAST_TIME_ALTERNATIVE_SEPARATOR);
            for (String alternative : alternatives) {
                result = isFirst ? parseFirstTimeAlternative(alternative) : Math.max(result, parseFirstTimeAlternative(alternative));
                isFirst = false;
            }
            return result;
        }

        private int parseFirstTimeAlternative(String switchTimeExpression) throws TimeExpressionException {
            int result = 0;
            boolean isFirst = true;
            String[] alternatives = switchTimeExpression.split(FIRST_TIME_ALTERNATIVE_SEPARATOR);
            for (String alternative : alternatives) {
                result = isFirst ? parseTimeSum(alternative) : Math.min(result, parseTimeSum(alternative));
                isFirst = false;
            }
            return result;
        }

        private int parseTimeSum(String switchTimeExpression) throws TimeExpressionException {
            int value = 0;
            for (String singleTime : switchTimeExpression.split(SUM_SEPARATOR_INCLUDING_SEPARATOR)) {
                value += parseTime(singleTime);
            }
            return value;
        }

        public int parseTime(String switchTimeExpression) throws TimeExpressionException {
            try {
                int value = 0;
                String[] strings = switchTimeExpression.split(TIME_SEPARATOR);
                int sign = switchTimeExpression.charAt(0) == '-' ? -1 : 1;
                if (strings.length > 1) {
                    value = Integer.parseInt(strings[1]) * 60 * sign + Integer.parseInt(strings[0]) * 60 * 60;
                } else if (strings.length == 1) {
                    value = Integer.parseInt(strings[0]) * 60;
                }
                return value;
            } catch (NumberFormatException e) {
                throw new TimeExpressionException(switchTimeExpression);
            }
        }

        public int value() {
            return value;
        }

        public boolean isOn() {
            return on;
        }

        public String valueAsTimeString() {
            return String.format("%02d:%02d", value / (60 * 60), (value % (60 * 60)) / 60);
        }

        @Override
        public int compareTo(SwitchTime time) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            if (this == time) return EQUAL;
            if (this.value < time.value) return BEFORE;
            if (this.value > time.value) return AFTER;
            if (!this.on && time.on) return BEFORE;
            if (this.on && !time.on) return AFTER;
            return EQUAL;
        }
    }
}
