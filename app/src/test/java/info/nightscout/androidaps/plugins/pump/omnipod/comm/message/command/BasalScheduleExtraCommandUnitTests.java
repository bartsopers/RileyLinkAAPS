package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.RateEntry;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BasalScheduleExtraCommandUnitTests {
    @Test
    public void testBasalEncodingFromBasalSchedule() {
        List<RateEntry> rateEntries = RateEntry.createEntries(3.0, Duration.standardHours(24));
        BasalScheduleExtraCommand basalScheduleExtraCommand = new BasalScheduleExtraCommand( //
                false, //
                true, //
                Duration.ZERO, //
                (byte) 0, //
                689, //
                Duration.standardSeconds(20), //
                rateEntries);

        assertArrayEquals( //
                ByteUtil.createByteArrayFromHexString("130e40001aea01312d003840005b8d80"), // From https://github.com/openaps/openomni/wiki/Bolus
                basalScheduleExtraCommand.getRawData());
    }

    @Test
    public void testParametersCorrectFromBasalSchedule() {
        BasalSchedule basalSchedule = new BasalSchedule(Collections.singletonList(new BasalScheduleEntry(0.05, Duration.ZERO)));
        BasalScheduleExtraCommand basalScheduleExtraCommand = new BasalScheduleExtraCommand( //
                basalSchedule, //
                Duration.standardHours(8).plus(Duration.standardMinutes(15)), //
                false, //
                true, //
                Duration.standardMinutes(1));

        assertFalse(basalScheduleExtraCommand.isAcknowledgementBeep());
        assertTrue(basalScheduleExtraCommand.isCompletionBeep());
        assertEquals(0, basalScheduleExtraCommand.getCurrentEntryIndex());
        assertEquals(3, basalScheduleExtraCommand.getDelayUntilNextTenthOfPulse().getStandardMinutes());
        assertEquals(60, basalScheduleExtraCommand.getProgramReminderInterval().getStandardSeconds());
        assertEquals(15.8, basalScheduleExtraCommand.getRemainingPulses(), 0.01);

        List<RateEntry> rateEntries = basalScheduleExtraCommand.getRateEntries();

        assertEquals(1, rateEntries.size());

        RateEntry rateEntry = rateEntries.get(0);

        assertEquals(3600.0, rateEntry.getDelayBetweenPulsesInSeconds(), 0.00000001);
        assertEquals(24, rateEntry.getTotalPulses(), 0.001);
    }

    @Test
    public void testEncodingFromBasalScheduleWithMultipleEntries() {
        BasalSchedule schedule = new BasalSchedule(Arrays.asList( //
                new BasalScheduleEntry(1.05, Duration.ZERO), //
                new BasalScheduleEntry(0.9, Duration.standardHours(10).plus(Duration.standardMinutes(30))), //
                new BasalScheduleEntry(1.0, Duration.standardHours(18).plus(Duration.standardMinutes(30)))));

        BasalScheduleExtraCommand basalScheduleExtraCommand = new BasalScheduleExtraCommand(schedule, Duration.standardMinutes((0x2e + 1) * 30).minus(Duration.standardSeconds(0x1be8 / 8)),
                false, true, Duration.ZERO);

        assertArrayEquals(ByteUtil.fromHexString("131a4002009600a7d8c0089d0105944905a001312d00044c0112a880"),
                basalScheduleExtraCommand.getRawData());
    }
    // TODO add tests

}
