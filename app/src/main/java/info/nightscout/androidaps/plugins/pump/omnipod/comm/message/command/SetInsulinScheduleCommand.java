package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalDeliveryTable;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalScheduleCommandPart;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.DeliverySchedule;

public class SetInsulinScheduleCommand extends MessageBlock {

    private int nonce;
    private DeliverySchedule schedule;

    public SetInsulinScheduleCommand(int nonce, DeliverySchedule schedule) {
        this.nonce = nonce;
        this.schedule = schedule;
        encode();
    }

    public SetInsulinScheduleCommand(int nonce, BasalSchedule schedule, Duration scheduleOffset) {
        BasalDeliveryTable table = new BasalDeliveryTable(schedule);
        int offsetMinutes = (int) scheduleOffset.getStandardMinutes();
        int segment = offsetMinutes / 30;
        int timeRemainingInSegment = offsetMinutes % 30;
        double rate = schedule.rateAt(scheduleOffset);
        // pulses per segment is rate (u/hour) * pulses per unit / 2 (a segment = 30m)
        int pulsesPerSegment = (int) (rate  / 0.05 / 2); //there was a constant with 0.05 somewhere
        int pulsesRemainingInSegment = pulsesPerSegment / 30 * timeRemainingInSegment;

        //FIXME: !!! We should check this very well as in the command there are seconds and we operate with minutes
        this.schedule = new BasalScheduleCommandPart((byte) segment, timeRemainingInSegment, pulsesRemainingInSegment, table);
        this.nonce = nonce;

        encode();

    }



    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        encodedData = ByteUtil.concat(encodedData, schedule.getType().getValue());
        encodedData = ByteUtil.concat(encodedData,
                ByteUtil.substring(
                        ByteUtil.getBytesFromInt(schedule.checksum()), 2,2));
        encodedData = ByteUtil.concat(encodedData, schedule.getRawData());

    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.SetInsulinSchedule;
    }
}
