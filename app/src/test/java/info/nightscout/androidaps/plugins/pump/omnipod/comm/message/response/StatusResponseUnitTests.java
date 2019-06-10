package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.joda.time.Duration;
import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodAlarm;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressState;

import info.nightscout.androidaps.Constants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class StatusResponseUnitTests {
    @Test
    public void StatusResponse_EnumsCorrect() throws Exception {
        byte[] bytes = ByteUtil.fromHexString("00430000000038800000");
        StatusResponse statusResponse = new StatusResponse(bytes);
        assertEquals(DeliveryStatus.PURGING, statusResponse.deliveryStatus);
        assertEquals(PodProgressState.PAIRING_SUCCESS, statusResponse.podProgressState);
        assertTrue(statusResponse.alarms.getFlags().contains(PodAlarm.PodAlarmType.SUSPENDED));
        assertTrue(statusResponse.alarms.getFlags().contains(PodAlarm.PodAlarmType.SUSPEND_EXPIRED));
        assertTrue(statusResponse.alarms.getFlags().contains(PodAlarm.PodAlarmType.BELOW_FIFTY_UNITS));
        assertTrue(statusResponse.alarms.getFlags().contains(PodAlarm.PodAlarmType.UNKNOWN_BIT_1));
    }

    @Test
    public void StatusResponse_SampleCaptureIsCorrect() throws Exception {
        byte[] bytes = ByteUtil.fromHexString("1d180258f80000146fff");
        StatusResponse statusResponse = new StatusResponse(bytes);


        assertEquals(DeliveryStatus.BASAL_RUNNING, statusResponse.deliveryStatus);
        assertEquals(PodProgressState.RUNNING_ABOVE_FIFTY_UNITS, statusResponse.podProgressState);
        assertEquals(50, statusResponse.reservoirLevel, 0);
        assertEquals(Duration.standardMinutes(1307).getMillis(), statusResponse.activeTime.getMillis());
        assertEquals(60.05, statusResponse.insulin, 0.05);
        assertEquals(15, statusResponse.podMessageCounter);
        assertEquals(0, statusResponse.insulinNotDelivered, 0);
        assertEquals(0, statusResponse.alarms.getFlags().size());
    }

    @Test
    public void StatusResponse_LargestValuesCorrect() throws Exception {
        byte[] bytes = ByteUtil.fromHexString("0011ffffffffffffffffff");
        StatusResponse statusResponse = new StatusResponse(bytes);

        assertEquals(Duration.standardMinutes(8191).getMillis(), statusResponse.activeTime.getMillis());
        assertEquals(Constants.PodPulseSize * 1023, statusResponse.insulinNotDelivered, 0);
        assertEquals(50, statusResponse.reservoirLevel, 0);
        assertEquals(Constants.PodPulseSize * 8191, statusResponse.insulin, 0.05);
        assertEquals(15, statusResponse.podMessageCounter);
    }
}