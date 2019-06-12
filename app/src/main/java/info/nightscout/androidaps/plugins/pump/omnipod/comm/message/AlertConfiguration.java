package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ExpirationAdvisory;

public class AlertConfiguration{
    private AlertType alertType;
    private boolean audible;
    private boolean autoOffModifier;
    private Duration duration;
    private ExpirationAdvisory expirationType;
    private BeepRepeat beepRepeat;
    private BeepType beepType;

    public AlertConfiguration(AlertType alertType, boolean audible, boolean autoOffModifier,
                              Duration duration, ExpirationAdvisory expirationType,
                              BeepType beepType, BeepRepeat beepRepeat) {
        this.alertType = alertType;
        this.audible = audible;
        this.autoOffModifier = autoOffModifier;
        this.duration = duration;
        this.expirationType = expirationType;
        this.beepRepeat = beepRepeat;
        this.beepType = beepType;
    }

    public byte[] getRawData() {
        int firstByte = (alertType.getValue() << 4);
        firstByte += audible ? (1 << 3) : 0;

        if(expirationType.expirationType == ExpirationAdvisory.ExpirationType.RESERVOIR) {
            firstByte += 1 << 2;
        }
        if(autoOffModifier) {
            firstByte += 1 << 1;
        }

        firstByte += ((int)duration.getStandardMinutes() >> 8) & 0x1;

        byte[] encodedData = new byte[] {
                (byte)firstByte,
                (byte)(duration.getStandardMinutes() & 0xff)
        };

        switch (expirationType.expirationType) {
            case RESERVOIR:
                int ticks = (int)(expirationType.reservoirLevel / Constants.POD_PULSE_SIZE / 2);
                encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16(ticks));
                break;
            case TIMER:
                int duration = (int)expirationType.timeToExpire.getStandardMinutes();
                encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16(duration));
                break;
        }

        encodedData = ByteUtil.concat(encodedData, beepType.getValue());
        encodedData = ByteUtil.concat(encodedData, beepRepeat.getValue());

        return encodedData;
    }
}
