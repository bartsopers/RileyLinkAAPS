package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.RateEntry;

public class BasalScheduleExtraCommand extends MessageBlock {
    private final boolean acknowledgementBeep;
    private final boolean completionBeep;
    private final Duration programReminderInterval;
    private final byte currentEntryIndex;
    private final double remainingPulses;
    private final Duration delayUntilNextTenthOfPulse;
    private final List<RateEntry> rateEntries;

    public BasalScheduleExtraCommand(boolean acknowledgementBeep, boolean completionBeep,
                                     Duration programReminderInterval, byte currentEntryIndex,
                                     double remainingPulses, Duration delayUntilNextTenthOfPulse, List<RateEntry> rateEntries) {

        this.acknowledgementBeep = acknowledgementBeep;
        this.completionBeep = completionBeep;
        this.programReminderInterval = programReminderInterval;
        this.currentEntryIndex = currentEntryIndex;
        this.remainingPulses = remainingPulses;
        this.delayUntilNextTenthOfPulse = delayUntilNextTenthOfPulse;
        this.rateEntries = rateEntries;
        encode();
    }

    public BasalScheduleExtraCommand(BasalSchedule schedule, Duration scheduleOffset,
                                     boolean acknowledgementBeep, boolean completionBeep, Duration programReminderInterval) {
        rateEntries = new ArrayList<>();
        this.acknowledgementBeep = acknowledgementBeep;
        this.completionBeep = completionBeep;
        this.programReminderInterval = programReminderInterval;

        BasalSchedule mergedSchedule = new BasalSchedule(schedule.adjacentEqualRatesMergedEntries());
        List<BasalSchedule.BasalScheduleDurationEntry> durations = mergedSchedule.getDurations();

        for(BasalSchedule.BasalScheduleDurationEntry entry : durations) {
            rateEntries.addAll(RateEntry.createEntries(entry.getRate(), entry.getDuration()));
        }

        BasalSchedule.BasalScheduleLookupResult entryLookupResult = mergedSchedule.lookup(scheduleOffset);
        currentEntryIndex = (byte)entryLookupResult.getIndex();
        Duration timeRemainingInEntry = entryLookupResult.getStartTime().minus(scheduleOffset.minus(entryLookupResult.getDuration()));
        double rate = mergedSchedule.rateAt(scheduleOffset);
        int pulsesPerHour = (int)Math.round(rate / Constants.POD_PULSE_SIZE);
        double timeBetweenPulses = 3600.0 / pulsesPerHour;
        delayUntilNextTenthOfPulse = Duration.millis(timeRemainingInEntry.getMillis() % ((long)(timeBetweenPulses * 1000) / 10));
        remainingPulses = pulsesPerHour * timeRemainingInEntry.minus(delayUntilNextTenthOfPulse).getMillis() / 3600.0 / 1000 + 0.1;

        encode();
    }

    private void encode() {
        byte beepOptions = (byte)((programReminderInterval.getStandardMinutes() & 0x3f) + (completionBeep ? 1 << 6 : 0) + (acknowledgementBeep ? 1 << 7 : 0));

        encodedData = new byte[] {
                beepOptions,
                currentEntryIndex
        };

        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16((int)Math.round(remainingPulses * 10)));
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt(Math.round(delayUntilNextTenthOfPulse.getMillis() * 1000)));

        for(RateEntry entry : rateEntries) {
            encodedData = ByteUtil.concat(encodedData, entry.getRawData());
        }
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.BASAL_SCHEDULE_EXTRA;
    }

    // For testing
    boolean isAcknowledgementBeep() {
        return acknowledgementBeep;
    }

    // For testing
    boolean isCompletionBeep() {
        return completionBeep;
    }

    // For testing
    Duration getProgramReminderInterval() {
        return programReminderInterval;
    }

    // For testing
    byte getCurrentEntryIndex() {
        return currentEntryIndex;
    }

    // For testing
    double getRemainingPulses() {
        return remainingPulses;
    }

    // For testing
    Duration getDelayUntilNextTenthOfPulse() {
        return delayUntilNextTenthOfPulse;
    }

    // For testing
    List<RateEntry> getRateEntries() {
        return rateEntries;
    }
}
