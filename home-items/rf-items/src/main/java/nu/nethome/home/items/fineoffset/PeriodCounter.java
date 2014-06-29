package nu.nethome.home.items.fineoffset;

public abstract class PeriodCounter {
    private long currentPeriod;
    private long totalPulsesAtStartOfPeriod;
    private long pulsesDuringLastPeriod;
    private boolean isInitialized;

    public PeriodCounter() {
    }

    public void updateCounter(long currentTotalPulses) {
        long newPeriod = getNewPeriod();
        if (!isInitialized) {
            currentPeriod = getNewPeriod();
            totalPulsesAtStartOfPeriod = currentTotalPulses;
            isInitialized = true;
        } else if (currentPeriod != newPeriod) {
            pulsesDuringLastPeriod = currentTotalPulses - totalPulsesAtStartOfPeriod;
            totalPulsesAtStartOfPeriod = currentTotalPulses;
            currentPeriod = newPeriod;
        }
    }

    public long getPulsesDuringPeriod(long currentTotalPulses) {
        updateCounter(currentTotalPulses);
        return currentTotalPulses - totalPulsesAtStartOfPeriod;
    }

    public String getState() {
        return isInitialized ? String.format("%d,%d,%d", currentPeriod, totalPulsesAtStartOfPeriod, pulsesDuringLastPeriod) : "";
    }

    public void setState(String state) {
        String parts[] = state.split(",");
        if (parts.length == 3) {
            currentPeriod = Long.parseLong(parts[0]);
            totalPulsesAtStartOfPeriod = Long.parseLong(parts[1]);
            pulsesDuringLastPeriod = Long.parseLong(parts[2]);
            isInitialized = true;
        }
    }

    public abstract long getNewPeriod();

    public long getPulsesDuringLastPeriod() {
        return pulsesDuringLastPeriod;
    }
}