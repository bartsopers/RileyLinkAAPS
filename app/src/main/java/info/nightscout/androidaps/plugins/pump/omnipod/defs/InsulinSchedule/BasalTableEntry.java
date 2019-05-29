package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class BasalTableEntry implements IRawRepresentable {

    private final int segments;
    private final int pulses;
    private final boolean alternateSegmentPulse;

    public BasalTableEntry(int segments, int pulses, boolean alternateSegmentPulse) {
        this.segments = segments;
        this.pulses = pulses;
        this.alternateSegmentPulse = alternateSegmentPulse;
    }

    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[2];
        byte pulsesHighByte = (byte) ((pulses >> 8) & 0b11);
        byte pulsesLowByte = (byte) (pulses & 0xFF);
        rawData[0] = (byte) ((byte)((segments - 1) << 4) + (byte)((alternateSegmentPulse ? 1 : 0) << 3) + pulsesHighByte);
        rawData[1] = (byte)pulsesLowByte;
        return rawData;
    }

    public int checksum() {
        int checksumPerSegment = (pulses & 0xff) + (pulses >> 8);
        return (checksumPerSegment * segments + (alternateSegmentPulse ? segments / 2 : 0));
    }

}
