package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-1D-Status-response
public enum DeliveryStatus {
    DELIVERY_INTERRUPTED( 0),
    BASAL_RUNNING( 1),
    TEMP_BASAL_RUNNING( 2),
    PURGING( 4),
    BOLUS_IN_PROGRESS( 5),
    BOLUS_AND_TEMP_BASAL( 6);

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
