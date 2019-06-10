package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public enum BeepRepeat {
    ONCE(0),
    EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_60_MINUTES(1),
    EVERY_MINUTE_FOR_15_MINUTES(2),
    EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_15_MINUTES(3),
    EVERY_3_MINUTES_DELAYED(4),
    EVERY_60_MINUTES(5),
    EVERY_15_MINUTES(6),
    EVERY_15_MINUTES_DELAYED(7),
    EVERY_5_MINUTES(8);

    byte value;

    BeepRepeat(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }
}
