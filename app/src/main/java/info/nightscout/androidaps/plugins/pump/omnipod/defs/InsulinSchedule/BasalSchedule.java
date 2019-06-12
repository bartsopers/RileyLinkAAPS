package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasalSchedule {
    public final List<BasalScheduleEntry> entries;

    public BasalSchedule(List<BasalScheduleEntry> entries) {
        this.entries = entries;
    }

    public double rateAt(Duration offset) {
        return lookup(offset).getBasalScheduleEntry().rate;
    }

    public BasalScheduleLookupResult lookup(Duration offset) {
        if(offset.isLongerThan(Duration.standardHours(24)) || offset.isShorterThan(Duration.ZERO)) {
            throw new IllegalArgumentException("Invalid duration");
        }

        List<BasalScheduleEntry> reversedBasalScheduleEntries = reversedBasalScheduleEntries();

        Duration last = Duration.standardHours(24);
        int index = 0;
        for(BasalScheduleEntry entry : reversedBasalScheduleEntries) {
            if(entry.startTime.isShorterThan(offset) || entry.startTime.equals(offset)) {
                return new BasalScheduleLookupResult( //
                        reversedBasalScheduleEntries.size() - (index + 1), //
                        entry, //
                        entry.startTime, //
                        last.minus(entry.startTime));
            }
            last = entry.startTime;
            index++;
        }

        throw new IllegalArgumentException("Basal schedule incomplete");
    }

    private List<BasalScheduleEntry> reversedBasalScheduleEntries() {
        List<BasalScheduleEntry> reversedEntries = new ArrayList<>();
        reversedEntries.addAll(entries);
        Collections.reverse(reversedEntries);
        return reversedEntries;
    }

    public List<BasalScheduleEntry> adjacentEqualRatesMergedEntries() {
        List<BasalScheduleEntry> mergedEntries = new ArrayList<>();
        Double lastRate = null;
        for(BasalScheduleEntry entry : entries) {
            if(lastRate == null || entry.rate != lastRate) {
                mergedEntries.add(entry);
            }
            lastRate = entry.rate;
        }
        return mergedEntries;
    }

    public List<BasalScheduleDurationEntry> getDurations() {
        List<BasalScheduleDurationEntry> durations = new ArrayList<>();
        Duration last = Duration.standardHours(24);
        List<BasalScheduleEntry> basalScheduleEntries = reversedBasalScheduleEntries();
        for(BasalScheduleEntry entry : basalScheduleEntries) {
            durations.add(new BasalScheduleDurationEntry( //
                    entry.rate, //
                    entry.startTime, //
                    last.minus(entry.startTime)));
            last = entry.startTime;
        }

        Collections.reverse(durations);
        return durations;
    }

    public static class BasalScheduleDurationEntry {
        private final double rate;
        private final Duration duration;
        private final Duration startTime;

        public BasalScheduleDurationEntry(double rate, Duration startTime, Duration duration) {
            this.rate = rate;
            this.duration = duration;
            this.startTime = startTime;
        }

        public double getRate() {
            return rate;
        }

        public Duration getDuration() {
            return duration;
        }

        public Duration getStartTime() {
            return startTime;
        }
    }

    public static class BasalScheduleLookupResult {
        private final int index;
        private final BasalScheduleEntry basalScheduleEntry;
        private final Duration startTime;
        private final Duration duration;

        public BasalScheduleLookupResult(int index, BasalScheduleEntry basalScheduleEntry, Duration startTime, Duration duration) {
            this.index = index;
            this.basalScheduleEntry = basalScheduleEntry;
            this.startTime = startTime;
            this.duration = duration;
        }

        public int getIndex() {
            return index;
        }

        public BasalScheduleEntry getBasalScheduleEntry() {
            return basalScheduleEntry;
        }

        public Duration getStartTime() {
            return startTime;
        }

        public Duration getDuration() {
            return duration;
        }
    }
}
