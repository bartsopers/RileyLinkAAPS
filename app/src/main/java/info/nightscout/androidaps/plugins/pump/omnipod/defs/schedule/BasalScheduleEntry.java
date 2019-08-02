package info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule;

import org.joda.time.Duration;

public class BasalScheduleEntry {
    private final double rate;
    private final Duration startTime;

    public BasalScheduleEntry(double rate, Duration startTime) {
        this.rate = rate;
        this.startTime = startTime;
    }

    public double getRate() {
        return rate;
    }

    public Duration getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "BasalScheduleEntry{" +
                "rate=" + rate +
                ", startTime=" + startTime +
                '}';
    }
}
