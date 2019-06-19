package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoResetStatus extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 8;

    private final byte zero;
    private final byte numberOfBytes;
    private final byte[] dataFromFlashMemory;
    private final int podAddress;

    public PodInfoResetStatus(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        zero = encodedData[1];
        numberOfBytes = encodedData[2];
        podAddress = ByteUtil.toInt((int)encodedData[3], (int)encodedData[4], (int)encodedData[5], (int)encodedData[6], ByteUtil.BitConversion.BIG_ENDIAN);
        dataFromFlashMemory = ByteUtil.substring(encodedData, 3, encodedData[2]);
    }

    @Override
    public PodInfoType getType() {
        return PodInfoType.RESET_STATUS;
    }

    public byte getZero() {
        return zero;
    }

    public byte getNumberOfBytes() {
        return numberOfBytes;
    }

    public byte[] getDataFromFlashMemory() {
        return dataFromFlashMemory;
    }

    public int getPodAddress() {
        return podAddress;
    }
}
