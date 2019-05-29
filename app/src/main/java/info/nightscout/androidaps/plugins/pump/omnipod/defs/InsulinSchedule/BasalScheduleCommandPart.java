package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class BasalScheduleCommandPart extends DeliverySchedule implements IRawRepresentable {

    private final byte currentSegment;
    private final int secondsRemaining;
    private final int pulsesRemaining;
    private final BasalDeliveryTable basalTable;

    public BasalScheduleCommandPart(byte currentSegment, int secondsRemaining, int pulsesRemaining,
                                    BasalDeliveryTable basalTable) {
        this.currentSegment = currentSegment;
        this.secondsRemaining = secondsRemaining;
        this.pulsesRemaining = pulsesRemaining;
        this.basalTable = basalTable;
    }

    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[0];
        rawData = ByteUtil.concat(rawData, currentSegment);
        rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt16(secondsRemaining << 3));
        rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt16(pulsesRemaining));
        for (BasalTableEntry entry: basalTable.getEntries()) {
            rawData = ByteUtil.concat(rawData, entry.getRawData());
        }
        return rawData;
    }

    @Override
    public InsulinScheduleType getType() {
        return InsulinScheduleType.BasalSchedule;
    }
}
