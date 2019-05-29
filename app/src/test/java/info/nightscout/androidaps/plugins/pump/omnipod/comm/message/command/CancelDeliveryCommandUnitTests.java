package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class CancelDeliveryCommandUnitTests {

    @Test
    public void Encode_ValidParams_CorrectBytes()
    {
        CancelDeliveryCommand command = new CancelDeliveryCommand(0x10203040, BeepType.TwoBips, true, false, true);

        byte[] expected = ByteUtil.fromHexString("1F051020304035");
        assertArrayEquals(expected, command.getRawData());
    }
}
