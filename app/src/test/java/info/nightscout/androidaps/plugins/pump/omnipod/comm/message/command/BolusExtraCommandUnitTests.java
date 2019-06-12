package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.Duration;
import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;

import static org.junit.Assert.assertArrayEquals;

public class BolusExtraCommandUnitTests {
    @Test
    public void testMessageCorrect() {
        BolusExtraCommand bolusExtraCommand = new BolusExtraCommand(1.25, 0.0,
                Duration.ZERO, false, true, Duration.standardHours(1),
                Duration.standardSeconds(2));

        assertArrayEquals( //
                ByteUtil.createByteArrayFromHexString("170d7c00fa00030d40000000000000"), // From https://github.com/openaps/openomni/wiki/Bolus
                bolusExtraCommand.getRawData());
    }
}
