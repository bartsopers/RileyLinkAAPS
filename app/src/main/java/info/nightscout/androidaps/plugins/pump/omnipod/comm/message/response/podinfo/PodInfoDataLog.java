package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FaultEventCode;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoDataLog extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 8;

    private final FaultEventCode faultEventCode;
    private final Duration timeFaultEvent;
    private final Duration timeActivation;
    private final byte dataChunkSize;
    private final byte dataChunkWords;

    public PodInfoDataLog(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        faultEventCode = FaultEventCode.fromByte(encodedData[1]);
        timeFaultEvent = Duration.standardMinutes(((encodedData[2] & 0b1) << 8) + ByteUtil.convertUnsignedByteToInt(encodedData[3]));
        timeActivation = Duration.standardMinutes(((encodedData[4] & 0b1) << 8) + ByteUtil.convertUnsignedByteToInt(encodedData[5]));
        dataChunkSize = encodedData[6];
        dataChunkWords = encodedData[7];
    }

    @Override
    public PodInfoType getType() {
        return PodInfoType.DATA_LOG;
    }

    public FaultEventCode getFaultEventCode() {
        return faultEventCode;
    }

    public Duration getTimeFaultEvent() {
        return timeFaultEvent;
    }

    public Duration getTimeActivation() {
        return timeActivation;
    }

    public byte getDataChunkSize() {
        return dataChunkSize;
    }

    public byte getDataChunkWords() {
        return dataChunkWords;
    }

    @Override
    public String toString() {
        return "PodInfoDataLog{" +
                "faultEventCode=" + faultEventCode +
                ", timeFaultEvent=" + timeFaultEvent +
                ", timeActivation=" + timeActivation +
                ", dataChunkSize=" + dataChunkSize +
                ", dataChunkWords=" + dataChunkWords +
                '}';
    }
}
