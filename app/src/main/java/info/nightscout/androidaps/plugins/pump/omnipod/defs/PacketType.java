package info.nightscout.androidaps.plugins.pump.omnipod.defs;

public enum PacketType {
    INVALID(0),
    POD(0b111),
    PDM(0b101),
    CON(0b100),
    ACK(0b010);

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
