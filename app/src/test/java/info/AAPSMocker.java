package info;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gxwtech.roundtrip2.MainApp;

import org.powermock.api.mockito.PowerMockito;

import java.util.Locale;

import info.nightscout.androidaps.utils.SP;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by mike on 23.03.2018.
 */

public class AAPSMocker {
    //private static Profile profile;
    //private static ProfileStore profileStore;
    public static final String TESTPROFILENAME = "someProfile";
    public static Intent intentSent = null;
    private static String validProfile = "{\"dia\":\"3\",\"carbratio\":[{\"time\":\"00:00\",\"value\":\"30\"}],\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"},{\"time\":\"2:00\",\"value\":\"110\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}";

    public static void mockStrings() {
        Locale.setDefault(new Locale("en", "US"));

//        when(MainApp.gs(R.string.closed_loop_disabled_on_dev_branch)).thenReturn("Running dev version. Closed loop is disabled.");
//        when(MainApp.gs(R.string.closedmodedisabledinpreferences)).thenReturn("Closed loop mode disabled in preferences");
//        when(MainApp.gs(R.string.objectivenotstarted)).thenReturn("Objective %d not started");
//        when(MainApp.gs(R.string.novalidbasalrate)).thenReturn("No valid basal rate read from pump");
//        when(MainApp.gs(R.string.autosensdisabledinpreferences)).thenReturn("Autosens disabled in preferences");
//        when(MainApp.gs(R.string.smbdisabledinpreferences)).thenReturn("SMB disabled in preferences");
//        when(MainApp.gs(R.string.limitingbasalratio)).thenReturn("Limiting basal rate to %.2f U/h because of %s");
//        when(MainApp.gs(R.string.pumplimit)).thenReturn("pump limit");
//        when(MainApp.gs(R.string.itmustbepositivevalue)).thenReturn("it must be positive value");
//        when(MainApp.gs(R.string.maxvalueinpreferences)).thenReturn("max value in preferences");
//        when(MainApp.gs(R.string.maxbasalmultiplier)).thenReturn("max basal multiplier");
//        when(MainApp.gs(R.string.maxdailybasalmultiplier)).thenReturn("max daily basal multiplier");
//        when(MainApp.gs(R.string.limitingpercentrate)).thenReturn("Limiting percent rate to %d%% because of %s");
//        when(MainApp.gs(R.string.pumplimit)).thenReturn("pump limit");
//        when(MainApp.gs(R.string.limitingbolus)).thenReturn("Limiting bolus to %.1f U because of %s");
//        when(MainApp.gs(R.string.hardlimit)).thenReturn("hard limit");
//        when(MainApp.gs(R.string.key_child)).thenReturn("child");
//        when(MainApp.gs(R.string.limitingcarbs)).thenReturn("Limiting carbs to %d g because of %s");
//        when(MainApp.gs(R.string.limitingiob)).thenReturn("Limiting IOB to %.1f U because of %s");
//        when(MainApp.gs(R.string.pumpisnottempbasalcapable)).thenReturn("Pump is not temp basal capable");
//        when(MainApp.gs(R.string.loop)).thenReturn("Loop");
//        when(MainApp.gs(R.string.loop_shortname)).thenReturn("LOOP");
//        when(MainApp.gs(R.string.smbalwaysdisabled)).thenReturn("SMB always and after carbs disabled because active BG source doesn\\'t support advanced filtering");
//        when(MainApp.gs(R.string.smbnotallowedinopenloopmode)).thenReturn("SMB not allowed in open loop mode");
//        when(MainApp.gs(R.string.Glimp)).thenReturn("Glimp");
//        when(MainApp.gs(R.string.glucose)).thenReturn("Glucose");
//        when(MainApp.gs(R.string.delta)).thenReturn("Delta");
//        when(MainApp.gs(R.string.short_avgdelta)).thenReturn("Short avg. delta");
//        when(MainApp.gs(R.string.long_avgdelta)).thenReturn("Long avg. delta");
//        when(MainApp.gs(R.string.zerovalueinprofile)).thenReturn("INVALID profile: %s");
//        when(MainApp.gs(R.string.success)).thenReturn("Success");
//        when(MainApp.gs(R.string.enacted)).thenReturn("Enacted");
//        when(MainApp.gs(R.string.comment)).thenReturn("Comment");
//        when(MainApp.gs(R.string.smb_shortname)).thenReturn("SMB");
//        when(MainApp.gs(R.string.canceltemp)).thenReturn("Cancel temp basal");
//        when(MainApp.gs(R.string.duration)).thenReturn("Duration");
//        when(MainApp.gs(R.string.percent)).thenReturn("Percent");
//        when(MainApp.gs(R.string.absolute)).thenReturn("Absolute");
//        when(MainApp.gs(R.string.waitingforpumpresult)).thenReturn("Waiting for result");
//        when(MainApp.gs(R.string.insulin_unit_shortname)).thenReturn("U");
//        when(MainApp.gs(R.string.minimalbasalvaluereplaced)).thenReturn("Basal value replaced by minimal supported value");
//        when(MainApp.gs(R.string.basalprofilenotaligned)).thenReturn("Basal values not aligned to hours: %s");
//        when(MainApp.gs(R.string.minago)).thenReturn("%d min ago");
//        when(MainApp.gs(R.string.hoursago)).thenReturn("%.1fh ago");
//        when(MainApp.gs(R.string.careportal_profileswitch)).thenReturn("Profile Switch");
    }

    public static MainApp mockMainApp() {
        PowerMockito.mockStatic(MainApp.class);
        MainApp mainApp = mock(MainApp.class);
        when(MainApp.instance()).thenReturn(mainApp);
        return mainApp;
    }


    public static void mockSP() {
        PowerMockito.mockStatic(SP.class);
        when(SP.getLong(anyInt(), anyLong())).thenReturn(0L);
        when(SP.getBoolean(anyInt(), anyBoolean())).thenReturn(false);
        when(SP.getInt(anyInt(), anyInt())).thenReturn(0);
    }

    public static void mockApplicationContext() {
        Context context = mock(Context.class);
        when(MainApp.instance().getApplicationContext()).thenReturn(context);
    }

    public static void mockSharedPreferences() {
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        //PreferenceManager preferenceManager = mock(PreferenceManager.class);

        PowerMockito.mockStatic(PreferenceManager.class);

        when(PreferenceManager.getDefaultSharedPreferences(any(Context.class))).thenReturn(sharedPreferences);
    }

}
