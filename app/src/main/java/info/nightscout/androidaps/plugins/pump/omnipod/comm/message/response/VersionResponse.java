package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FirmwareVersion;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;

// https://github.com/openaps/openomni/wiki/Command-01-Version-response
public class VersionResponse extends MessageBlock {
    public PodProgressStatus podProgressStatus;
    public FirmwareVersion pmVersion;
    public FirmwareVersion piVersion;
    public int lot;
    public int tid;
    public int address;
    public Integer gain;
    public Integer rssi;

    public VersionResponse(byte[] encodedData) {
        int length = encodedData[1] + 2;
        switch (length) {
            case 0x17:
                initializeMembers(2, encodedData, true);
                break;
            case 0x1D:
                initializeMembers(9, encodedData, false);
                break;
            default:
                return;
        }
        this.encodedData = ByteUtil.substring(encodedData, 1, length - 1);
    }

    private void initializeMembers(int startOffset, byte[] data, boolean extraByte) {
        this.podProgressStatus = PodProgressStatus.fromByte(data[startOffset + 7]);
        this.pmVersion = new FirmwareVersion(data[startOffset], data[startOffset + 1], data[startOffset + 2]);
        this.piVersion = new FirmwareVersion(data[startOffset + 3], data[startOffset + 4], data[startOffset + 5]);
        this.lot = ByteUtil.toInt((int) data[startOffset + 8], (int) data[startOffset + 9],
                (int) data[startOffset + 10], (int) data[startOffset + 11], ByteUtil.BitConversion.BIG_ENDIAN);
        this.tid = ByteUtil.toInt((int) data[startOffset + 12], (int) data[startOffset + 13],
                (int) data[startOffset + 14], (int) data[startOffset + 15], ByteUtil.BitConversion.BIG_ENDIAN);

        if (extraByte) {
            this.gain = (data[startOffset + 16] & 0b11000000) >> 6;
            this.rssi = (data[startOffset + 16] & 0b00111111);
            startOffset++;
        }

        this.address = ByteUtil.toInt((int) data[startOffset + 16], (int) data[startOffset + 17],
                (int) data[startOffset + 18], (int) data[startOffset + 19], ByteUtil.BitConversion.BIG_ENDIAN);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.VERSION_RESPONSE;
    }

}
