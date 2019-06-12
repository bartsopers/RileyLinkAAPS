package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;
import org.junit.Test;

import java.util.Collections;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BolusDeliverySchedule;

import static org.junit.Assert.assertArrayEquals;

public class SetInsulineScheduleCommandUnitTests {
    @Test
    public void testBasalScheduleMessageCorrect() {
        BasalSchedule basalSchedule = new BasalSchedule(Collections.singletonList(new BasalScheduleEntry(0.05, Duration.ZERO)));

        SetInsulinScheduleCommand setInsulinScheduleCommand = new SetInsulinScheduleCommand( //
                0x01020304, //
                basalSchedule, //
                Duration.standardHours(8).plus(Duration.standardMinutes(15)));

        assertArrayEquals( //
                ByteUtil.fromHexString("1a1201020304000064101c200000f800f800f800"), // from https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/BasalScheduleTests.swift
                setInsulinScheduleCommand.getRawData());
    }

    @Test
    public void testTemporaryBasalScheduleMessageCorrect() {
        SetInsulinScheduleCommand setInsulinScheduleCommand = new SetInsulinScheduleCommand(0xea2d0a3b, 0.2, Duration.standardMinutes(30));

        assertArrayEquals( //
                ByteUtil.fromHexString("1a0eea2d0a3b01007d01384000020002"), // from https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/TempBasalTests.swift
                setInsulinScheduleCommand.getRawData());
    }

    @Test
    public void testBolusMessageCorrect() {
        BolusDeliverySchedule bolusDeliverySchedule = new BolusDeliverySchedule(2.6, Duration.standardSeconds(1));
        SetInsulinScheduleCommand setInsulineScheduleCommand = new SetInsulinScheduleCommand(0xbed2e16b, bolusDeliverySchedule);

        assertArrayEquals( //
                ByteUtil.createByteArrayFromHexString("1a0ebed2e16b02010a0101a000340034"), // from https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/BolusTests.swift
                setInsulineScheduleCommand.getRawData());
    }
}
