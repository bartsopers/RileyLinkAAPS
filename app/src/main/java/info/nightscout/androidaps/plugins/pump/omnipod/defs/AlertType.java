package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum AlertType {
    AUTO_OFF(0),
    END_OF_SERVICE(2),
    EXPIRATION_ADVISORY(3),
    LOW_RESERVOIR(4),
    SUSPEND_IN_PROGRESS(5),
    SUSPEND_ENDED(6),
    TIMER_LIMIT(7);

    byte value;

    AlertType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static AlertType fromByte(byte input) {
        for (AlertType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }


}
