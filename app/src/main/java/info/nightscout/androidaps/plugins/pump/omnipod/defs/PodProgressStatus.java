package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Pod-Progress-State
public enum PodProgressStatus {
    INITIAL_VALUE((byte)0x00),
    TANK_POWER_ACTIVATED((byte)0x01),
    TANK_FILL_COMPLETED((byte)0x02),
    PAIRING_SUCCESS((byte)0x03),
    PURGING((byte)0x04),
    READY_FOR_INJECTION((byte)0x05),
    INJECTION_DONE((byte)0x06),
    PRIMING_CANNULA((byte)0x07),
    RUNNING_ABOVE_FIFTY_UNITS((byte)0x08),
    RUNNING_BELOW_FIFTY_UNITS((byte)0x09),
    ERROR_EVENT_OCCURRED_SHUTTING_DOWN((byte)0x0d),
    DELAYED_PRIME((byte)0x0e),
    INACTIVE((byte)0x0f);

    private byte value;

    PodProgressStatus(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static PodProgressStatus fromByte(byte value) {
        for (PodProgressStatus type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PodProgressStatus: "+ value);
    }
}
