package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum AlertType {
    AUTO_OFF((byte)0x00),
    END_OF_SERVICE((byte)0x02),
    EXPIRATION_ADVISORY((byte)0x03),
    LOW_RESERVOIR((byte)0x04),
    SUSPEND_IN_PROGRESS((byte)0x05),
    SUSPEND_ENDED((byte)0x06),
    TIMER_LIMIT((byte)0x07);

    private byte value;

    AlertType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static AlertType fromByte(byte value) {
        for (AlertType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AlertType: "+ value);
    }


}
