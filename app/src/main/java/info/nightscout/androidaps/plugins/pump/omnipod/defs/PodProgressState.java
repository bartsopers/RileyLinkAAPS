package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Pod-Progress-State
public enum PodProgressState {
    INITIAL_VALUE(0),
    TANK_POWER_ACTIVATED(1),
    TANK_FILL_COMPLETED(2),
    PAIRING_SUCCESS(3),
    PURGING( 4),
    READY_FOR_INJECTION( 5),
    INJECTION_DONE( 6),
    PRIMING_CANNULA( 7),
    RUNNING_ABOVE_FIFTY_UNITS( 8),
    RUNNING_BELOW_FIFTY_UNITS( 9),
    ERROR_EVENT_OCCURRED_SHUTTING_DOWN(13),
    FAILED_TO_INITIALIZE_IN_TIME_SHUTTING_DOWN( 14 ),
    INACTIVE( 15);

    byte value;

    PodProgressState(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static PodProgressState fromByte(byte input) {
        for (PodProgressState type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
