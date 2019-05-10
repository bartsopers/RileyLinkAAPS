package info.nightscout.androidaps.plugins.pump.medtronic.defs;

/**
 * Created by andy on 6/11/18.
 */

public enum PumpDeviceState {

    NeverContacted, //
    Sleeping, //
    WakingUp, //
    Active, //
    ErrorWhenCommunicating, //
    TimeoutWhenCommunicating, //
    //ProblemContacting, //
    PumpUnreachable, //
    InvalidConfiguration;

    Integer resourceId = null;


    PumpDeviceState() {

    }


    PumpDeviceState(int resourceId) {
        this.resourceId = resourceId;
    }

}
