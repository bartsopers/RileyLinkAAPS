package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public enum PacketType {
    INVALID((byte)0),
    POD((byte)0b111),
    PDM((byte)0b101),
    CON((byte)0b100),
    ACK((byte)0b010);

    byte value;

    PacketType(byte value) {
        this.value = value;
    }

    public static PacketType fromByte(byte value) {
        for (PacketType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PacketType: "+ value);
    }

    public int maxBodyLength() {
        switch(this) {
            case ACK:
                return 4;
            case CON:
            case PDM:
            case POD:
                return 31;
            default:
                return 0;
        }
    }

    public byte getValue() {
        return value;
    }

}
