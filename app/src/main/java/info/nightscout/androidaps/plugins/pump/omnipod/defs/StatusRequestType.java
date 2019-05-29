package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-0E-Status-Request
public enum StatusRequestType {
    Normal ((byte)0x00),
    ExpiredAlert((byte)0x01),
    FaultEvent((byte)0x02),
    DataLog ((byte)0x03),
    FaultData((byte)0x04),
    HardcodedValues ((byte)0x06),
    FlashVariables((byte)0x46), // including state, initialization time, any faults
    RecentFlashLogDump((byte)0x50),
    OlderFlashLogDump((byte)0x51); // but dumps entries before the last 50

    private final byte value;

    StatusRequestType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}