package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum BeepType {
    NONE(0),
    FOUR_BEEPS(1),
    FOUR_BIP_BEEPS(2),
    TWO_BIPS(3),
    ONE_BEEP(4),
    THREE_BEEPS(5),
    ONE_LONG_BEEP(6),
    SIX_BIPS(7),
    TWO_LONG_BEEPS(8);

    byte value;

    BeepType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

}
