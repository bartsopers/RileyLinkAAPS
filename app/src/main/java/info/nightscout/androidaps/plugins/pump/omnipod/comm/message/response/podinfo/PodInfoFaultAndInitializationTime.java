package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.FaultEventCode;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoFaultAndInitializationTime extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 17;
    private final FaultEventCode faultEventCode;
    private final Duration timeFaultEvent;
    private final DateTime dateTime;

    public PodInfoFaultAndInitializationTime(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        faultEventCode = FaultEventCode.fromByte(encodedData[1]);
        timeFaultEvent = Duration.standardMinutes(((encodedData[2] & 0b1) << 8) + encodedData[3]);
        // FIXME take care of time zone
        dateTime = new DateTime(2000 + encodedData[14], encodedData[12], encodedData[13], encodedData[15], encodedData[16]);
    }

    @Override
    public PodInfoType getType() {
        return PodInfoType.FAULT_AND_INITIALIZATION_TIME;
    }

    public FaultEventCode getFaultEventCode() {
        return faultEventCode;
    }

    public Duration getTimeFaultEvent() {
        return timeFaultEvent;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "PodInfoFaultAndInitializationTime{" +
                "faultEventCode=" + faultEventCode +
                ", timeFaultEvent=" + timeFaultEvent +
                ", dateTime=" + dateTime +
                '}';
    }
}
