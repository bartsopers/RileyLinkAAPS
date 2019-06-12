package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.defs.InsulinSchedule;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalTableEntry;

import static junit.framework.Assert.assertEquals;

public class BasalTableEntryUnitTests {
    @Test
    public void testChecksum() {
        BasalTableEntry basalTableEntry = new BasalTableEntry(2, 300, false);
        assertEquals(0x5a, basalTableEntry.getChecksum());
    }

    @Test
    public void testChecksumWithAlternatePulses() {
        BasalTableEntry basalTableEntry = new BasalTableEntry(2, 260, true);
        assertEquals(0x0b, basalTableEntry.getChecksum());
    }
}
