package info.nightscout.androidaps.plugins.pump.common.defs;

/**
 * Created by andy on 10/15/18.
 */

public enum PumpDriverState {

    NotInitialized, //
    Connecting, //
    Initialized, //
    Busy, //
    Suspended, //
    ;

    public static boolean isConnected(PumpDriverState pumpState) {
        return pumpState == Initialized || pumpState == Busy || pumpState == Suspended;
    }
}
