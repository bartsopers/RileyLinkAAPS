package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public class RateEntry implements IRawRepresentable {

    private final double ttotalPUlses;
    private final Duration delayBetweenPulses;

    public RateEntry(double ttotalPUlses, Duration delayBetweenPulses) {
        this.ttotalPUlses = ttotalPUlses;
        this.delayBetweenPulses = delayBetweenPulses;
    }

    public static List<RateEntry> fromBasalScheduleEntry(BasalScheduleEntry entry) {
        ArrayList<RateEntry> list = new ArrayList<RateEntry>();
        return list;
    }

    @Override
    public byte[] getRawData() {
        throw new NotImplementedException("RateEntry.getRawData");
        //return new byte[0];
    }
}
