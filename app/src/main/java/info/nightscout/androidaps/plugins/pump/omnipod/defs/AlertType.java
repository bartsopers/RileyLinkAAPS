package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum AlertType {
    AutoOff(0),
    EndOfService(2),
    ExpirationAdvisory(3),
    LowReservoir(4),
    SuspendInProgress(5),
    SuspendEnded(6),
    TimerLimit(7);

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
