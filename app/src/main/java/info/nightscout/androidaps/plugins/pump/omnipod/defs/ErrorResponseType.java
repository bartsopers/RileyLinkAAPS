package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-06-Error-response-bad-nonce
public enum ErrorResponseType {
    BadNonce(0x14);

    byte value;

    ErrorResponseType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static ErrorResponseType fromByte(byte input) {
        for (ErrorResponseType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
