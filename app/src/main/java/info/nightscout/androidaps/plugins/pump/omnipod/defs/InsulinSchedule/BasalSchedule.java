package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

public class BasalSchedule {
    public final BasalScheduleEntry[] entries;

    public BasalSchedule(BasalScheduleEntry[] entries) {
        this.entries = entries;
    }

    public double rateAt(Duration timeOffset) {
        double rate = 0;
        int offset = (int) timeOffset.getStandardMinutes();
        int cumulatedMinutes = 0;
        for (BasalScheduleEntry entry : entries) {
            rate = entry.rate;
            cumulatedMinutes += entry.duration.getStandardMinutes();
            if (offset < cumulatedMinutes) {
                return rate;
            }
        }
        throw new IllegalArgumentException("timeOffset");
    }
}
