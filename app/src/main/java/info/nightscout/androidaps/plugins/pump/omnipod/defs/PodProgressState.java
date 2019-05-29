package info.nightscout.androidaps.plugins.pump.omnipod.defs;

// https://github.com/openaps/openomni/wiki/Pod-Progress-State
public enum PodProgressState {
    Initialized (0),
    TankPowerActivated (1),
    TankFillCompleted (2),
    PairingSuccess (3),
    Priming ( 4),
    ReadyForInjection ( 5),
    InjectionStarted ( 6),
    InjectionDone ( 7),
    AboveFiftyUnits ( 8),
    BelowFiftyUnits ( 9),
    ErrorEventLoggedShuttingDown (13),
    DelayedPrime ( 14 ),
    Inactive ( 15);

    byte value;

    PodProgressState(int value) {
        this.value = (byte)value;
    }

    public byte getValue() {
        return value;
    }

    public static PodProgressState fromByte(byte input) {
        for (PodProgressState type : values()) {
            if (type.value == input) {
                return type;
            }
        }
        return null;
    }
}
