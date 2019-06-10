package info.nightscout.androidaps.plugins.pump.omnipod.util;

/**
 * Created by andy on 5/12/18.
 */

public class OmniPodConst {

    static final String Prefix = "AAPS.OmniPod.";

    public class Prefs {

        public static final String PREF_PREFIX = "pref_";

        public static final String RILEY_LINK_ADDRESS = PREF_PREFIX + "rileylink_mac";
        public static final String POD_STATE = PREF_PREFIX + "pod_state";
        public static final String LAST_GOOD_PUMP_COMMUNICATION_TIME = Prefix + "last_good_pump_communication_time";
    }
}
