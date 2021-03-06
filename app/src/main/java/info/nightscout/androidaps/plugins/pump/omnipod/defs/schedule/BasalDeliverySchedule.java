package info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class BasalDeliverySchedule extends DeliverySchedule implements IRawRepresentable {

    private final byte currentSegment;
    private final int secondsRemaining;
    private final int pulsesRemaining;
    private final BasalDeliveryTable basalTable;

    public BasalDeliverySchedule(byte currentSegment, int secondsRemaining, int pulsesRemaining,
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
        for (BasalTableEntry entry : basalTable.getEntries()) {
            rawData = ByteUtil.concat(rawData, entry.getRawData());
        }
        return rawData;
    }

    @Override
    public InsulinScheduleType getType() {
        return InsulinScheduleType.BASAL_SCHEDULE;
    }

    @Override
    public int getChecksum() {
        int checksum = 0;
        byte[] rawData = getRawData();
        for (int i = 0; i < rawData.length && i < 5; i++) {
            checksum += ByteUtil.convertUnsignedByteToInt(rawData[i]);
        }
        for (BasalTableEntry entry : basalTable.getEntries()) {
            checksum += entry.getChecksum();
        }

        return checksum;
    }
}
