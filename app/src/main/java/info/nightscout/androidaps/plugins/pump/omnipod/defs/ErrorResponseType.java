package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-06-Error-response-bad-nonce
public enum ErrorResponseType {
    BAD_NONCE((byte)0x14);

    private byte value;

    ErrorResponseType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static ErrorResponseType fromByte(byte value) {
        for (ErrorResponseType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ErrorResponseType: "+ value);
    }
}
