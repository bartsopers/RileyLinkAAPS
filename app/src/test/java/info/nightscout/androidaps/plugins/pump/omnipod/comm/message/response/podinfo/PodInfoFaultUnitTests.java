package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FaultEventCode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PodInfoFaultUnitTests {
    @Test
    public void testDecoding() {
        PodInfoFault podInfoFault = new PodInfoFault(ByteUtil.fromHexString("059200010000000000000000091912170e")); // From https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/PodInfoTests.swift

        assertEquals(FaultEventCode.BAD_PUMP_REQ_2_STATE, podInfoFault.getFaultEventCode());
        assertTrue(Duration.standardMinutes(1).isEqual(podInfoFault.getActivationTime()));

        DateTime dateTime = podInfoFault.getDateTime();
        assertEquals(2018, dateTime.getYear());
        assertEquals(9, dateTime.getMonthOfYear());
        assertEquals(25, dateTime.getDayOfMonth());
        assertEquals(23, dateTime.getHourOfDay());
        assertEquals(14, dateTime.getMinuteOfHour());
    }
}
