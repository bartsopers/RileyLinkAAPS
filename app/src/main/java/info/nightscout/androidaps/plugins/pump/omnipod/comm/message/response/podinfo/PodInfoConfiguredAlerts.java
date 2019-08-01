package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSlot;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

// TODO make this closer match the wiki description
public class PodInfoConfiguredAlerts extends PodInfo {
    private static final int MINIMUM_MESSAGE_LENGTH = 11;

    private final byte[] word278; // Unknown use
    private final List<AlertActivation> alertActivations;

    public PodInfoConfiguredAlerts(byte[] encodedData) {
        super(encodedData);

        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        word278 = ByteUtil.substring(encodedData, 1, 2);

        alertActivations = new ArrayList<>();

        for(byte alarmType = 0; alarmType < AlertSlot.values().length; alarmType++) {
            BeepType beepType = BeepType.fromByte(alarmType);
            byte timeFromStart = encodedData[3 + 2 * alarmType];
            double unitsLeft = ByteUtil.convertUnsignedByteToInt(encodedData[4 + 2 * alarmType]) * Constants.POD_PULSE_SIZE;
            alertActivations.add(new AlertActivation(beepType, timeFromStart, unitsLeft));
        }
    }

    @Override
    public PodInfoType getType() {
        return PodInfoType.CONFIGURED_ALERTS;
    }

    public byte[] getWord278() {
        return word278;
    }

    public List<AlertActivation> getAlertActivations() {
        return new ArrayList<>(alertActivations);
    }

    public static class AlertActivation {
        private final BeepType beepType;
        private final byte timeFromPodStart;
        private final double unitsLeft;

        private AlertActivation(BeepType beepType, byte timeFromPodStart, double unitsLeft) {
            this.beepType = beepType;
            this.timeFromPodStart = timeFromPodStart;
            this.unitsLeft = unitsLeft;
        }

        public BeepType getBeepType() {
            return beepType;
        }

        public byte getTimeFromPodStart() {
            return timeFromPodStart;
        }

        public double getUnitsLeft() {
            return unitsLeft;
        }

        @Override
        public String toString() {
            return "AlertActivation{" +
                    "beepType=" + beepType +
                    ", timeFromPodStart=" + timeFromPodStart +
                    ", unitsLeft=" + unitsLeft +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PodInfoConfiguredAlerts{" +
                "word278=" + Arrays.toString(word278) +
                ", alertActivations=" + alertActivations +
                '}';
    }
}
