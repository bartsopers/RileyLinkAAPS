package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import java.util.Arrays;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoRecentFlashLog extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 166;

    private final int lastEntryIndex;
    private final byte[] hexWordLog;

    public PodInfoRecentFlashLog(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        lastEntryIndex = ByteUtil.convertUnsignedByteToInt(encodedData[2]);
        hexWordLog = ByteUtil.substring(encodedData, 3, lastEntryIndex - 3);
    }

    @Override
    public PodInfoType getType() {
        return null;
    }

    public int getLastEntryIndex() {
        return lastEntryIndex;
    }

    public byte[] getHexWordLog() {
        return hexWordLog;
    }

    @Override
    public String toString() {
        return "PodInfoRecentFlashLog{" +
                "lastEntryIndex=" + lastEntryIndex +
                ", hexWordLog=" + Arrays.toString(hexWordLog) +
                '}';
    }
}
