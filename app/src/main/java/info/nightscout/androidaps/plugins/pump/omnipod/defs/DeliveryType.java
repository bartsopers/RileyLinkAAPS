package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-1D-Status-response
public enum DeliveryType {
    None ( 0),
    Basal ( 1),
    TempBasal ( 2),
    Bolus ( 4),
    ExtendedBolus ( 8);
	
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
