package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

import static java.lang.Math.floor;

public class BasalDeliveryTable implements IRawRepresentable {

    private BasalTableEntry[] entries = new BasalTableEntry[0];

    public BasalDeliveryTable(BasalSchedule schedule) {
        ArrayList<BasalTableEntry> entries = new ArrayList<>();
        for(BasalScheduleEntry e: schedule.entries) {
            double pulsesPerSegment = e.rate * 0.5 / Constants.PodPulseSize; //0.5 hours per segment
            boolean alternateSegmentPulse = pulsesPerSegment - floor(pulsesPerSegment) > 0;
            int remainingSegments = (int) (e.duration.getStandardMinutes() / 30); //30 minutes for each segment
            while(remainingSegments > 0) {
                int segments = Math.min(remainingSegments, 16);
                BasalTableEntry tableEntry = new BasalTableEntry(segments, (int) floor(pulsesPerSegment), alternateSegmentPulse);
                entries.add(tableEntry);
                remainingSegments -= segments;
            }
        }
        this.entries = entries.toArray(this.entries);
    }
    public BasalTableEntry[] getEntries() {
        return entries;
    }

    @Override
    public byte[] getRawData() {
        throw new NotImplementedException("BasalDeliveryTable.getRawData");
    }
}

