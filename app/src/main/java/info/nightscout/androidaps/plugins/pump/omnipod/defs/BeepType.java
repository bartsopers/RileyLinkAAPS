package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum BeepType {
    None (0),
    FourBeeps (1),
    FourBipBeeps (2),
    TwoBips (3),
    OneBeep (4),
    ThreeBeeps (5),
    OneLongBeep (6),
    SixBips (7),
    TwoLongBeeps (8);

    byte value;

    BeepType(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

}
