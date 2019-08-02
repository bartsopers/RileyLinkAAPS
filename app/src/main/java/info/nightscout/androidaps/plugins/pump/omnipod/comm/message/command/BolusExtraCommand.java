package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

public class BolusExtraCommand extends MessageBlock {
    private final boolean acknowledgementBeep;
    private final boolean completionBeep;
    private final Duration programReminderInterval;
    private final double units;
    private final Duration timeBetweenPulses;
    private final double squareWaveUnits;
    private final Duration squareWaveDuration;

    public BolusExtraCommand(double units) {
        this(units, Duration.standardSeconds(2));
    }

    public BolusExtraCommand(double units, Duration timeBetweenPulses) {
        this(units, 0.0, Duration.ZERO, false, false, Duration.ZERO, timeBetweenPulses);
    }

    public BolusExtraCommand(double units, double squareWaveUnits, Duration squareWaveDuration,
                             boolean acknowledgementBeep, boolean completionBeep,
                             Duration programReminderInterval, Duration timeBetweenPulses) {
        if(units <= 0D) {
            throw new IllegalArgumentException("Units should be > 0");
        } else if(units > Constants.MAX_BOLUS) {
            throw new IllegalArgumentException("Units exceeds max bolus");
        }
        this.units = units;
        this.squareWaveUnits = squareWaveUnits;
        this.squareWaveDuration = squareWaveDuration;
        this.acknowledgementBeep = acknowledgementBeep;
        this.completionBeep = completionBeep;
        this.programReminderInterval = programReminderInterval;
        this.timeBetweenPulses = timeBetweenPulses;
        encode();
    }

    private void encode() {
        byte beepOptions = (byte)((programReminderInterval.getStandardMinutes() & 0x3f) + (completionBeep ? 1 << 6 : 0) + (acknowledgementBeep ? 1 << 7 : 0));

        int squareWavePulseCountCountX10 = (int) Math.round(squareWaveUnits * 200);
        int timeBetweenExtendedPulses = squareWavePulseCountCountX10 > 0 ? (int)squareWaveDuration.getMillis() * 100 / squareWavePulseCountCountX10 : 0;

        encodedData = ByteUtil.concat(encodedData, beepOptions);
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16((int)Math.round(units * 200)));
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt((int)timeBetweenPulses.getMillis() * 100));
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt16(squareWavePulseCountCountX10));
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt(timeBetweenExtendedPulses));
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.BOLUS_EXTRA;
    }
}
