package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-0E-Status-Request
public enum StatusRequestType {
    NORMAL((byte)0x00),
    EXPIRED_ALERT((byte)0x01),
    FAULT_EVENT((byte)0x02),
    DATA_LOG((byte)0x03),
    FAULT_DATA((byte)0x04),
    HARDCODED_VALUES((byte)0x06),
    FLASH_VARIABLES((byte)0x46), // including state, initialization time, any faults
    RECENT_FLASH_LOG_DUMP((byte)0x50),
    OLDER_FLASH_LOG_DUMP((byte)0x51); // but dumps entries before the last 50

    private final byte value;

    StatusRequestType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}