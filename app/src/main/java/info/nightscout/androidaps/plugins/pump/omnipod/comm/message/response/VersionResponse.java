package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FirmwareVersion;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;

// https://github.com/openaps/openomni/wiki/Command-01-Version-response
public class VersionResponse extends MessageBlock {
    public final PodProgressStatus podProgressStatus;
    public final FirmwareVersion pmVersion;
    public final FirmwareVersion piVersion;
    public final int lot;
    public final int tid;
    public final int address;

    public VersionResponse(byte[] encodedData) {
        int length = ByteUtil.convertUnsignedByteToInt(encodedData[1]) + 2;

        boolean extraByte;
        byte[] truncatedData;

        switch (length) {
            case 0x17:
                truncatedData = ByteUtil.substring(encodedData, 2);
                extraByte = true;
                break;
            case 0x1D:
                truncatedData = ByteUtil.substring(encodedData, 9);
                extraByte = false;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized VersionResponse message length: "+ length);
        }

        this.podProgressStatus = PodProgressStatus.fromByte(truncatedData[7]);
        this.pmVersion = new FirmwareVersion(truncatedData[0], truncatedData[1], truncatedData[2]);
        this.piVersion = new FirmwareVersion(truncatedData[3], truncatedData[4], truncatedData[5]);
        this.lot = ByteUtil.toInt((int) truncatedData[8], (int) truncatedData[9],
                (int) truncatedData[10], (int) truncatedData[11], ByteUtil.BitConversion.BIG_ENDIAN);
        this.tid = ByteUtil.toInt((int) truncatedData[12], (int) truncatedData[13],
                (int) truncatedData[14], (int) truncatedData[15], ByteUtil.BitConversion.BIG_ENDIAN);

        int indexIncrementor = extraByte ? 1 : 0;

        this.address = ByteUtil.toInt((int) truncatedData[16 + indexIncrementor], (int) truncatedData[17 + indexIncrementor],
                (int) truncatedData[18 + indexIncrementor], (int) truncatedData[19 + indexIncrementor], ByteUtil.BitConversion.BIG_ENDIAN);

        this.encodedData = encodedData;
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.VERSION_RESPONSE;
    }

    public PodProgressStatus getPodProgressStatus() {
        return podProgressStatus;
    }

    public FirmwareVersion getPmVersion() {
        return pmVersion;
    }

    public FirmwareVersion getPiVersion() {
        return piVersion;
    }

    public int getLot() {
        return lot;
    }

    public int getTid() {
        return tid;
    }

    public int getAddress() {
        return address;
    }
}
