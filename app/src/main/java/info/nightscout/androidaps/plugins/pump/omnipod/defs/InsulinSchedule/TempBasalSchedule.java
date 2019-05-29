package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class TempBasalSchedule extends DeliverySchedule implements IRawRepresentable {

    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[0];
        throw new NotImplementedException("TempBasalSchedule.getRawData");
    }

    @Override
    public InsulinScheduleType getType() {
        return InsulinScheduleType.TempBasalSchedule;
    }

    @Override
    public int checksum() {
        throw new NotImplementedException("InsulinScheduleType.checksum");
        //return 0;
    }
}
