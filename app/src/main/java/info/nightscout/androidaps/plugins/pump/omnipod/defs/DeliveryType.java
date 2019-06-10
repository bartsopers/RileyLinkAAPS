package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-1D-Status-response
public enum DeliveryType {
    NONE( 0),
    BASAL ( 1),
    TEMP_BASAL ( 2),
    BOLUS ( 4),
    EXTENDED_BOLUS( 8);
	
    byte value;

    DeliveryType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static DeliveryType fromByte(byte input) {
        for (DeliveryType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
