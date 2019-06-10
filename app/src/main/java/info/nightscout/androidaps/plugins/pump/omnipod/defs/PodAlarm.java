package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import java.util.EnumSet;

public class PodAlarm {
    // FIXME https://github.com/ps2/rileylink_ios/blob/omnipod-testing/OmniKit/Model/AlertSlot.swift
    public enum PodAlarmType {
        POD_EXPIRED( 0b10000000),
        SUSPEND_EXPIRED( 0b01000000),
        SUSPENDED( 0b00100000),
        BELOW_FIFTY_UNITS( 0b00010000),
        ONE_HOUR_EXPIRY( 0b00001000),
        POD_DEACTIVATED( 0b00000100),
        UNKNOWN_BIT_2( 0b00000010),
        UNKNOWN_BIT_1( 0b00000001);

        public final byte value;

        PodAlarmType(int flag) {
            this.value = (byte) flag;
        }
    }

    private final byte value;
    private final EnumSet<PodAlarmType> flags = EnumSet.noneOf(PodAlarmType.class);

    public PodAlarm(byte value) {
        this.value = value;
        for(PodAlarmType a: PodAlarmType.values()) {
            if ((value & a.value) > 0) {
                flags.add(a);
            }
        }
    }

    public byte getAsByte() {
        return this.value;
    }

    public EnumSet<PodAlarmType> getFlags() {
        return flags;
    }
}
