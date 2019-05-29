package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public enum PacketType {
    Invalid(0),
    Pod(0b111),
    Pdm(0b101),
    Con(0b100),
    Ack(0b010);

    byte value;

    PacketType(int value) {
        this.value = (byte) value;
    }

    public static PacketType fromByte(byte input) {
        for (PacketType type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }

    public int maxBodyLength() {
        switch(this) {
            case Ack:
                return 4;
            case Con:
            case Pdm:
            case Pod:
                return 31;
            default:
                return 0;
        }
    }

    public byte getValue() {
        return value;
    }

}
