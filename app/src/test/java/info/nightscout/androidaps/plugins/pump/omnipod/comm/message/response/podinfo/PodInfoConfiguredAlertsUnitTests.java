package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import org.junit.Test;

import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;

import static org.junit.Assert.assertEquals;

public class PodInfoConfiguredAlertsUnitTests {
    @Test
    public void testMessageCorrect() {
        byte[] encodedMessage = ByteUtil.fromHexString("010000000000001285000011c700000000119c"); // from https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKitTests/PodInfoTests.swift
        PodInfoConfiguredAlerts podInfoConfiguredAlerts = new PodInfoConfiguredAlerts(encodedMessage);

        List<PodInfoConfiguredAlerts.AlertActivation> alertActivations = podInfoConfiguredAlerts.getAlertActivations();
        assertEquals(8, alertActivations.size());

        assertEquals(BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, alertActivations.get(2).getBeepType());
        assertEquals(18, alertActivations.get(2).getTimeFromPodStart());
        assertEquals(6.65, alertActivations.get(2).getUnitsLeft(), 0.000001);

        assertEquals(BeepType.BEEP, alertActivations.get(4).getBeepType());
        assertEquals(17, alertActivations.get(4).getTimeFromPodStart());
        assertEquals(9.95, alertActivations.get(4).getUnitsLeft(), 0.000001);

        assertEquals(BeepType.BIP_BIP_BIP_BIP_BIP_BIP, alertActivations.get(7).getBeepType());
        assertEquals(17, alertActivations.get(7).getTimeFromPodStart());
        assertEquals(7.8, alertActivations.get(7).getUnitsLeft(), 0.000001);

        assertEmpty(alertActivations.get(0));
        assertEmpty(alertActivations.get(1));
        assertEmpty(alertActivations.get(3));
        assertEmpty(alertActivations.get(5));
        assertEmpty(alertActivations.get(6));
    }

    private void assertEmpty(PodInfoConfiguredAlerts.AlertActivation alertActivation) {
        assertEquals(0, alertActivation.getTimeFromPodStart());
        assertEquals(0.0, alertActivation.getUnitsLeft(), 0.0000001);
    }
}
