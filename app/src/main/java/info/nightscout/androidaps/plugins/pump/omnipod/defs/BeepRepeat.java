package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum BeepRepeat {
    Once (0),
    EveryMinuteFor3MinutesRepeatEvery60Minutes(1),
    EveryMinuteFor15minutes (2),
    EveryMinuteFor3MinutesRepeatEvery15Minutes(3),
    Every3MinutesDelayed (4),
    Every60Minutes (5),
    Every15Minutes (6),
    Every15MinutesDelayed (7),
    Every5Minutes (8);

    byte value;

    BeepRepeat(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }
}
