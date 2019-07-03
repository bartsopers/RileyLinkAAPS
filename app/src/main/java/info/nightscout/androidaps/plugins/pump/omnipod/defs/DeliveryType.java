package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-1D-Status-response
public enum DeliveryType {
    NONE((byte)0x00),
    BASAL((byte)0x01),
    TEMP_BASAL((byte)0x02),
    BOLUS((byte)0x04),
    EXTENDED_BOLUS((byte)0x08);
	
    private byte value;

    DeliveryType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static DeliveryType fromByte(byte value) {
        for (DeliveryType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DeliveryType: "+ value);
    }
}
