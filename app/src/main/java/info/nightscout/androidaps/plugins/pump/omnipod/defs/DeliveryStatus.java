package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-1D-Status-response
public enum DeliveryStatus {
    DeliveryInterrupted ( 0),
    BasalRunning ( 1),
    TempBasalRunning ( 2),
    Purging ( 4),
    BolusInProgress ( 5),
    BolusAndTempBasal ( 6);

    byte value;

    DeliveryStatus(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static DeliveryStatus fromByte(byte input) {
        for (DeliveryStatus type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
