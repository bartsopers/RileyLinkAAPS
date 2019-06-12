package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

public class BasalScheduleEntry {
    public final double rate;
    public final Duration startTime;

    public BasalScheduleEntry(double rate, Duration startTime) {
        this.rate = rate;
        this.startTime = startTime;
    }
}
