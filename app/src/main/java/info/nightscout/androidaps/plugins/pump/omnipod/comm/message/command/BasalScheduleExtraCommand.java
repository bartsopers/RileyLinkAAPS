package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;

public class BasalScheduleExtraCommand extends MessageBlock {
    private final boolean confidenceReminder;
    private final Duration programReminderInterval;
    private final BasalSchedule schedule;

    public BasalScheduleExtraCommand(BasalSchedule schedule, Duration scheduleOffset,
                                     boolean confidenceReminder, Duration programReminderInterval) {
        this.confidenceReminder = confidenceReminder;
        this.programReminderInterval = programReminderInterval;
        this.schedule = schedule;
        encode();
    }

    private void encode() {
//        byte[] rawData = new byte[0];
//        ArrayList<RateEntry> rates = new ArrayList<RateEntry>();
//        for (BasalScheduleEntry entry:schedule.entries) {
//            rates.addAll(RateEntry.fromBasalScheduleEntry(entry));
//        }
//        byte reminders = (byte) ((programReminderInterval.getStandardMinutes()  & 0x3F) * (confidenceReminder ? (1 <<6) : 0));
//        rawData = ByteUtil.concat(rawData, reminders);
//        rawData = ByteUtil.concat(rawData, currentEntryIndex);
//        rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt16(remainingPulses * 10));
//        rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt((int) (delayUntilNextPulse.getMillis() / 100))); // conversion to int is safe as the maxium value is 10 (hundreds of mills) * 60 (seconds) * 60 (minutes) * 1 (hours) // as the minimum value for basal is 0.05 u/h
//        for ( RateEntry rate : rates) {
//            rawData = ByteUtil.concat(rawData, rate.getRawData());
//        }
//        encodedData = rawData;
    }

    @Override
    public MessageBlockType getType() {
        return null;
    }
}
