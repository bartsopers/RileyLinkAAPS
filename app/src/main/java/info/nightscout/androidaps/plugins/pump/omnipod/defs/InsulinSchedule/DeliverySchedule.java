package info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.IRawRepresentable;

public abstract class DeliverySchedule implements IRawRepresentable {

    public abstract InsulinScheduleType getType();

    public abstract int getChecksum();
}
