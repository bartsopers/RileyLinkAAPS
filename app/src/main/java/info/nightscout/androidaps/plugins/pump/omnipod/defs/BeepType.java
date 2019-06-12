package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum BeepType {
    NO_BEEP(0),
    BEEP_BEEP_BEEP_BEEP(1),
    BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP(2),
    BIP_BIP(3),
    BEEP(4),
    BEEP_BEEP_BEEP(5),
    BEEEEEEP(6),
    BIP_BIP_BIP_BIP_BIP_BIP(7),
    BEEEP_BEEEP(8);

    byte value;

    BeepType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

}
