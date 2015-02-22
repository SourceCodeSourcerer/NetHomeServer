package nu.nethome.home.items.net.wemo;

/**
 *
 */
public class InsightState {
    private int state;          // 0 off, 1 on, 8 on without load
    private long lastChange;
    private long onForSeconds;
    private long onTodaySeconds;
    private long onTotalSeconds;
    private long timePeriod;    // Period over which averages are calculated
    private long currentMW;
    private long todayMW;
    private double total_mW;
    private long powerThresholdMW;

    public InsightState(String rawStateRepresentation) throws WemoException {
        String states[] = rawStateRepresentation.split("\\|");
        if (states.length < 11) {
            throw new WemoException("Wrong number of parameters in WemoInsight state");
        }
        state = Integer.parseInt(states[0]);
        lastChange = Long.parseLong(states[1]);
        onForSeconds = Long.parseLong(states[2]);
        onTodaySeconds = Long.parseLong(states[3]);
        onTotalSeconds = Long.parseLong(states[4]);
        timePeriod = Long.parseLong(states[5]);
        String unknown = states[6];
        currentMW = Long.parseLong(states[7]);
        todayMW = Long.parseLong(states[8]);
        total_mW = Double.parseDouble(states[9]);
        powerThresholdMW = Long.parseLong(states[10]);
    }

    public State getState() {
        switch (state) {
            case 1:
            return State.On;
            case 8:
            return State.Idle;
            default:
                return State.Off;
        }
    }

    public long getLastChange() {
        return lastChange;
    }

    public long getOnForSeconds() {
        return onForSeconds;
    }

    public long getOnTodaySeconds() {
        return onTodaySeconds;
    }

    /**
     * @return Total on time in seconds
     */
    public long getTotalOnTime() {
        return onTotalSeconds;
    }

    public long getTimePeriod() {
        return timePeriod;
    }

    /**
     * @return Current power consumption in Watt
     */
    public double getCurrentConsumption() {
        return currentMW / 1000.0D;
    }

    public long getTodayMW() {
        return todayMW;
    }

    /**
     * @return Total power consumption in Kilo Watt Hours
     */
    public double getTotalConsumption() {
        return total_mW / 60_000_000.0D;
    }

    public long getPowerThresholdMW() {
        return powerThresholdMW;
    }

    public enum State {
        Off, Idle, On
    }
}
