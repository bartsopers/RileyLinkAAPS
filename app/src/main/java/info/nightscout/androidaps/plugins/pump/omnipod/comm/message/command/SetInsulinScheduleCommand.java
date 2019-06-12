package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalDeliveryTable;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.DeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.TempBasalDeliverySchedule;

public class SetInsulinScheduleCommand extends MessageBlock {

    private int nonce;
    private DeliverySchedule schedule;

    // Bolus
    public SetInsulinScheduleCommand(int nonce, BolusDeliverySchedule schedule) {
        this.nonce = nonce;
        this.schedule = schedule;
        encode();
    }

    // Basal schedule
    public SetInsulinScheduleCommand(int nonce, BasalSchedule schedule, Duration scheduleOffset) {
        int scheduleOffsetInSeconds = (int)scheduleOffset.getStandardSeconds();

        BasalDeliveryTable table = new BasalDeliveryTable(schedule);
        double rate = schedule.rateAt(scheduleOffset);
        byte segment = (byte)(scheduleOffsetInSeconds / BasalDeliveryTable.SEGMENT_DURATION);
        int segmentOffset = scheduleOffsetInSeconds % BasalDeliveryTable.SEGMENT_DURATION;

        int timeRemainingInSegment = BasalDeliveryTable.SEGMENT_DURATION - segmentOffset;

        double timeBetweenPulses = 3600 / (rate / Constants.POD_PULSE_SIZE);

        double offsetToNextTenth = timeRemainingInSegment % (timeBetweenPulses / 10.0);

        int pulsesRemainingInSegment  = (int)((timeRemainingInSegment + timeBetweenPulses / 10.0 - offsetToNextTenth) / timeBetweenPulses);

        this.nonce = nonce;
        this.schedule = new BasalDeliverySchedule(segment, timeRemainingInSegment, pulsesRemainingInSegment, table);
        encode();
    }

    // Temp basal
    public SetInsulinScheduleCommand(int nonce, double tempBasalRate, Duration duration) {
        int pulsesPerHour = (int)Math.round(tempBasalRate / Constants.POD_PULSE_SIZE);
        int pulsesPerSegment = pulsesPerHour / 2;
        this.nonce = nonce;
        this.schedule = new TempBasalDeliverySchedule(BasalDeliveryTable.SEGMENT_DURATION, pulsesPerSegment, new BasalDeliveryTable(tempBasalRate, duration));
        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        encodedData = ByteUtil.concat(encodedData, schedule.getType().getValue());
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16(schedule.getChecksum()));
        encodedData = ByteUtil.concat(encodedData, schedule.getRawData());
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.SET_INSULIN_SCHEDULE;
    }
}
