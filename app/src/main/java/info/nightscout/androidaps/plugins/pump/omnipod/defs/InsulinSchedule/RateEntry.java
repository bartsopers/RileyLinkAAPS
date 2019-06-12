package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class RateEntry implements IRawRepresentable {

    private final double totalPulses;
    private final Duration delayBetweenPulses;

    public RateEntry(double totalPulses, Duration delayBetweenPulses) {
        this.totalPulses = totalPulses;
        this.delayBetweenPulses = delayBetweenPulses;
    }

    public static List<RateEntry> createEntries(double rate, Duration duration) {
        List<RateEntry> entries = new ArrayList<>();
        int remainingSegments = (int)Math.round(duration.getStandardSeconds() / 1800.0);
        double pulsesPerSegment = (int)Math.round(rate / Constants.POD_PULSE_SIZE) / 2.0;
        int maxSegmentsPerEntry = pulsesPerSegment > 0 ? (int)(BasalDeliveryTable.MAX_PULSES_PER_RATE_ENTRY / pulsesPerSegment) : 1;

        double durationInHours = duration.getStandardSeconds() / 3600.0;

        double remainingPulses = rate * durationInHours / Constants.POD_PULSE_SIZE;
        Duration delayBetweenPulses = Duration.millis((long)(3600 / rate * Constants.POD_PULSE_SIZE * 1000));

        while(remainingSegments > 0) {
            if(rate == 0.0) {
                entries.add(new RateEntry(0, Duration.standardMinutes(30)));
                remainingSegments -= 1;
            } else {
                int numSegments = Math.min(maxSegmentsPerEntry, (int)Math.round(remainingPulses / pulsesPerSegment));
                double totalPulses = pulsesPerSegment * numSegments;
                entries.add(new RateEntry(totalPulses, delayBetweenPulses));
                remainingSegments -= numSegments;
                remainingPulses -= totalPulses;
            }
        }

        return entries;
    }

    public double getTotalPulses() {
        return totalPulses;
    }

    public Duration getDelayBetweenPulses() {
        return delayBetweenPulses;
    }

    @Override
    public byte[] getRawData() {
        byte[] rawData = new byte[0];
        rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt16((int)Math.round(totalPulses * 10)));
        if(totalPulses == 0) {
            rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt((int) (delayBetweenPulses.getMillis() * 10)));
        } else {
            rawData = ByteUtil.concat(rawData, ByteUtil.getBytesFromInt((int)(delayBetweenPulses.getMillis() * 100)));
        }
        return rawData;
    }
}
