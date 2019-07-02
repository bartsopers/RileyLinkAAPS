package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.RateEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

public class TempBasalExtraCommand extends MessageBlock {
    private final boolean acknowledgementBeep;
    private final boolean completionBeep;
    private final Duration programReminderInterval;
    private final double remainingPulses;
    private final double delayUntilNextPulse;
    private final List<RateEntry> rateEntries;

    public TempBasalExtraCommand(double rate, Duration duration, boolean acknowledgementBeep, boolean completionBeep,
                                 Duration programReminderInterval) {

        this.acknowledgementBeep = acknowledgementBeep;
        this.completionBeep = completionBeep;
        this.programReminderInterval = programReminderInterval;

        rateEntries = RateEntry.createEntries(rate, duration);

        RateEntry currentRateEntry = rateEntries.get(0);
        remainingPulses = currentRateEntry.getTotalPulses();
        delayUntilNextPulse = currentRateEntry.getDelayBetweenPulsesInSeconds();

        encode();
    }

    private void encode() {
        byte beepOptions = (byte)((programReminderInterval.getStandardMinutes() & 0x3f) + (completionBeep ? 1 << 6 : 0) + (acknowledgementBeep ? 1 << 7 : 0));

        encodedData = new byte[] {
                beepOptions,
                (byte)0x00
        };

        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16((int)Math.round(remainingPulses * 10)));
        if(remainingPulses == 0) {
            encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt((int)(delayUntilNextPulse * 1000 * 100) * 10));
        } else {
            encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt((int)(delayUntilNextPulse * 1000 * 100)));
        }

        for(RateEntry entry : rateEntries) {
            encodedData = ByteUtil.concat(encodedData, entry.getRawData());
        }
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.TEMP_BASAL_EXTRA;
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
    double getRemainingPulses() {
        return remainingPulses;
    }

    // For testing
    double getDelayUntilNextPulse() {
        return delayUntilNextPulse;
    }

    // For testing
    List<RateEntry> getRateEntries() {
        return rateEntries;
    }
}
