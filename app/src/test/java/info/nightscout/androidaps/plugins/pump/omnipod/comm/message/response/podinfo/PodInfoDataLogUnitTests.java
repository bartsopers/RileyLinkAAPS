package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.joda.time.Duration;
import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FaultEventCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PodInfoDataLogUnitTests {
    @Test
    public void testMessageCorrect() {
        PodInfoDataLog podInfoDataLog = new PodInfoDataLog(ByteUtil.fromHexString("030100010001043c")); // From https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/PodInfoTests.swift

        assertEquals(FaultEventCode.FAILED_FLASH_ERASE, podInfoDataLog.getFaultEventCode());
        assertTrue(Duration.standardMinutes(1).isEqual(podInfoDataLog.getTimeFaultEvent()));
        assertTrue(Duration.standardMinutes(1).isEqual(podInfoDataLog.getTimeActivation()));
        assertEquals(4, podInfoDataLog.getDataChunkSize());
        assertEquals(60, podInfoDataLog.getDataChunkWords());
    }
}
