package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.joda.time.Duration;
import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;

import info.nightscout.androidaps.Constants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StatusResponseUnitTests {
    // TODO add /extend tests

    @Test
    public void StatusResponse_EnumsCorrect() {
        byte[] bytes = ByteUtil.fromHexString("00430000000038800000");
        StatusResponse statusResponse = new StatusResponse(bytes);
        assertEquals(DeliveryStatus.PRIMING, statusResponse.deliveryStatus);
        assertEquals(PodProgressStatus.PAIRING_SUCCESS, statusResponse.podProgressStatus);
        assertEquals(4, statusResponse.alerts.getAlertSlots().size());
        // TODO add assertions on alert slots contents
    }

    @Test
    public void StatusResponse_SampleCaptureIsCorrect() {
        byte[] bytes = ByteUtil.fromHexString("1d180258f80000146fff"); // From https://github.com/openaps/openomni/wiki/Command-1D-Status-response
        StatusResponse statusResponse = new StatusResponse(bytes);

        assertEquals(DeliveryStatus.NORMAL, statusResponse.deliveryStatus);
        assertEquals(PodProgressStatus.RUNNING_ABOVE_FIFTY_UNITS, statusResponse.podProgressStatus);
        assertNull("Reservoir level should be null", statusResponse.reservoirLevel);
        assertEquals(Duration.standardMinutes(1307).getMillis(), statusResponse.timeActive.getMillis());
        assertEquals(60.05, statusResponse.insulin, 0.000001);
        assertEquals(15, statusResponse.podMessageCounter);
        assertEquals(0, statusResponse.insulinNotDelivered, 0.000001);
        assertEquals(0, statusResponse.alerts.getAlertSlots().size());
    }

    @Test
    public void StatusResponse_LargestValuesCorrect() {
        byte[] bytes = ByteUtil.fromHexString("0011ffffffffffffffffff");
        StatusResponse statusResponse = new StatusResponse(bytes);

        assertEquals(Duration.standardMinutes(8191).getMillis(), statusResponse.timeActive.getMillis());
        assertEquals(Constants.POD_PULSE_SIZE * 1023, statusResponse.insulinNotDelivered, 0.000001);
        assertNull("Reservoir level should be null", statusResponse.reservoirLevel);
        assertEquals(Constants.POD_PULSE_SIZE * 8191, statusResponse.insulin, 0.0000001);
        assertEquals(15, statusResponse.podMessageCounter);
        assertEquals(8, statusResponse.alerts.getAlertSlots().size());
    }
}