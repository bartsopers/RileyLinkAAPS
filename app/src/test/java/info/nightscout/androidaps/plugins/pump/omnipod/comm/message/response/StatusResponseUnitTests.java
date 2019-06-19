package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.joda.time.Duration;
import org.junit.Test;

import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSlot;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodAlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;

import info.nightscout.androidaps.Constants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class StatusResponseUnitTests {
    // TODO add /extend tests

    @Test
    public void StatusResponse_EnumsCorrect() {
        byte[] bytes = ByteUtil.fromHexString("00430000000038800000");
        StatusResponse statusResponse = new StatusResponse(bytes);
        assertEquals(DeliveryStatus.PURGING, statusResponse.deliveryStatus);
        assertEquals(PodProgressStatus.PAIRING_SUCCESS, statusResponse.podProgressStatus);
        List<AlertSlot> alertSlots = statusResponse.alerts.getAlertSlots();
    }

    @Test
    public void StatusResponse_SampleCaptureIsCorrect() {
        byte[] bytes = ByteUtil.fromHexString("1d180258f80000146fff");
        StatusResponse statusResponse = new StatusResponse(bytes);


        assertEquals(DeliveryStatus.BASAL_RUNNING, statusResponse.deliveryStatus);
        assertEquals(PodProgressStatus.RUNNING_ABOVE_FIFTY_UNITS, statusResponse.podProgressStatus);
        assertEquals(50, statusResponse.reservoirLevel, 0);
        assertEquals(Duration.standardMinutes(1307).getMillis(), statusResponse.activeTime.getMillis());
        assertEquals(60.05, statusResponse.insulin, 0.05);
        assertEquals(15, statusResponse.podMessageCounter);
        assertEquals(0, statusResponse.insulinNotDelivered, 0);
        assertEquals(0, statusResponse.alerts.getAlertSlots().size());
    }

    @Test
    public void StatusResponse_LargestValuesCorrect() {
        byte[] bytes = ByteUtil.fromHexString("0011ffffffffffffffffff");
        StatusResponse statusResponse = new StatusResponse(bytes);

        assertEquals(Duration.standardMinutes(8191).getMillis(), statusResponse.activeTime.getMillis());
        assertEquals(Constants.POD_PULSE_SIZE * 1023, statusResponse.insulinNotDelivered, 0);
        assertEquals(50, statusResponse.reservoirLevel, 0);
        assertEquals(Constants.POD_PULSE_SIZE * 8191, statusResponse.insulin, 0.05);
        assertEquals(15, statusResponse.podMessageCounter);
    }
}