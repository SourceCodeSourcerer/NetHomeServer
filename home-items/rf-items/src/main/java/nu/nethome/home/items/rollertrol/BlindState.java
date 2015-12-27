package nu.nethome.home.items.rollertrol;

public class BlindState {
    public static final String UP_STRING = "Up";
    public static final String DOWN_STRING = "Down";
    private long fullTravelTime = 0;
    private long travelStartTime = 0;
    private long travelStartPosition = 0;
    private int travelDirection = 0;

    public String getStateString() {
        String result = getCurrentPosition() == 0 && travelDirection != 1 ? UP_STRING : DOWN_STRING;
        if (!isInEndPosition()) {
            result += String.format(" %d%%", currentPositionInRoundedPercent());
        }
        return result;
    }

    private long currentPositionInRoundedPercent() {
        long percent = getCurrentPosition() * 100 / fullTravelTime;
        percent = ((percent) / 5) * 5;
        return percent;
    }

    private boolean isInEndPosition() {
        return (getCurrentPosition() == 0 && travelDirection != 1) ||
                getCurrentPosition() >= fullTravelTime + 1 && travelDirection != -1;
    }

    public void down() {
        move(1);
    }

    public void up() {
        move(-1);
    }

    public void stop() {
        move(0);
    }

    private void move(int direction) {
        travelStartPosition = getCurrentPosition();
        travelStartTime = System.currentTimeMillis();
        travelDirection = direction;
    }

    public long getCurrentPosition() {
        long rawPosition;
        if (fullTravelTime != 0) {
            rawPosition = travelStartPosition + travelDirection * timePassedSince(travelStartTime);
        } else {
            rawPosition = travelStartPosition + travelDirection;
        }
        rawPosition = Math.min(rawPosition, fullTravelTime + 1);
        rawPosition = Math.max(rawPosition, 0);
        return rawPosition;
    }

    /**
     * Time to travel the full length in ms
     * @param travelTime time in ms
     */
    public void setTravelTime(long travelTime) {
        this.fullTravelTime = travelTime;
    }

    public long getTravelTime() {
        return fullTravelTime;
    }

    public long timePassedSince(long time) {
        return System.currentTimeMillis() - time;
    }

    public Boolean isTravelling() {
        return !isInEndPosition() && (travelDirection != 0);
    }
}
