package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

public class BasalScheduleEntry {
    public final double rate;
    public final Duration duration;

    public BasalScheduleEntry(double rate, Duration duration) {
        this.rate = rate;
        this.duration = duration;
    }
}
