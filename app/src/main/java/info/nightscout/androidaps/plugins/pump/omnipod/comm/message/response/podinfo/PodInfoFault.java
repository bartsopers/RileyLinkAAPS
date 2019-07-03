package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.FaultEventCode;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoFault extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 17;
    private final FaultEventCode faultEventCode;
    private final Duration activationTime;
    private final DateTime dateTime;

    public PodInfoFault(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        faultEventCode = FaultEventCode.fromByte(encodedData[1]);
        activationTime = Duration.standardMinutes(((encodedData[2] & 0b1) << 8) + encodedData[3]);
        dateTime = new DateTime(2000 + encodedData[14], encodedData[12], encodedData[13], encodedData[15], encodedData[16]);
    }

    @Override
    public PodInfoType getType() {
        return PodInfoType.FAULT;
    }

    public FaultEventCode getFaultEventCode() {
        return faultEventCode;
    }

    public Duration getActivationTime() {
        return activationTime;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
}
