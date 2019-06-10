package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationManager;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ExpirationAdvisory;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;

public class AlertConfiguration{
    private AlertType alertType;
    private boolean audible;
    private boolean autoOffModifier;
    private int duration;
    private ExpirationAdvisory expirationType;
    private BeepRepeat beepRepeat;
    private BeepType beepType;

    public AlertConfiguration(AlertType alertType, boolean audible, boolean autoOffModifier,
                              int duration, ExpirationAdvisory expirationType,
                              BeepRepeat beepRepeat, BeepType beepType) {
        this.alertType = alertType;
        this.audible = audible;
        this.autoOffModifier = autoOffModifier;
        this.duration = duration;
        this.expirationType = expirationType;
        this.beepRepeat = beepRepeat;
        this.beepType = beepType;
    }

    public byte[] getRawData() {
        byte[] encodedData = new byte[6];

        int firstByte = (alertType.getValue() << 4);
        firstByte |= expirationType.expirationType.getValue();
        firstByte += audible ? (1 << 3) : 0;

        byte[] valueBuffer = ByteUtil.getBytesFromInt(duration);

        byte durationHigh = (byte) (valueBuffer[2] & (byte)1);
        firstByte |= durationHigh;

        encodedData[0] = (byte) (firstByte & 0xFF);
        encodedData[1] = valueBuffer[3];

        switch (expirationType.expirationType) {
            case RESERVOIR:
                int ticks = (int)(expirationType.reservoirLevel / Constants.PodPulseSize / 2);
                valueBuffer = ByteUtil.getBytesFromInt(ticks);
                break;
            case TIMER:
                int duration = (int)expirationType.timeToExpire.getStandardMinutes();
                valueBuffer = ByteUtil.getBytesFromInt(duration);
                break;
        }

        encodedData[2] = valueBuffer[2];
        encodedData[3] = valueBuffer[3];
        encodedData[4] = beepRepeat.getValue();
        encodedData[5] = beepType.getValue();
        return encodedData;
    }
}
