package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;

import static org.junit.Assert.assertEquals;


public class PodInfoTesterUnitTests {
    @Test
    public void testDecoding() {
        PodInfoTester podInfoTester = new PodInfoTester(ByteUtil.fromHexString("0601003FA8"));

        assertEquals((byte)0x01, podInfoTester.getByte1());
        assertEquals((byte)0x00, podInfoTester.getByte2());
        assertEquals((byte)0x3f, podInfoTester.getByte3());
        assertEquals((byte)0xa8, podInfoTester.getByte4());
    }
}
