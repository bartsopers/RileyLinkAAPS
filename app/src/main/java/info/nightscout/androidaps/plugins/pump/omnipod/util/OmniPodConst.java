package info.nightscout.androidaps.plugins.pump.omnipod.util;

/**
 * Created by andy on 5/12/18.
 */

public class OmniPodConst {

    static final String Prefix = "AAPS.OmniPod.";

    public class Prefs {
        public static final String PrefPrefix = "pref_omnipod_";
        public static final String RileyLinkAddress = PrefPrefix + "rileylink_mac";
        public static final String PodState = PrefPrefix + "pod_state";

        public static final String LastGoodPumpCommunicationTime = Prefix + "lastGoodPumpCommunicationTime";
    }
}
