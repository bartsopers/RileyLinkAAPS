package info.nightscout.androidaps.plugins.pump.medtronic.comm;

import android.content.Context;
import android.preference.PreferenceManager;

import com.gxwtech.roundtrip2.MainApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import info.AAPSMocker;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicDeviceType;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.utils.SP;

/**
 * Created by andy on 5/13/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MainApp.class, Context.class, SP.class, PreferenceManager.class})
//@Ignore
public class MedtronicCommunicationManagerUTest {


    MedtronicCommunicationManager medtronicCommunicationManager;


    @Mock
    RFSpy rFSpy;


    //@Before
    public void setUp() {
        AAPSMocker.mockMainApp();
        AAPSMocker.mockApplicationContext();
        AAPSMocker.mockSharedPreferences();
        AAPSMocker.mockSP();

        //medtronicCommunicationManager = new MedtronicCommunicationManager(MainApp.instance().getApplicationContext(), rFSpy, false);
    }


    //    @Test
    //    public void getPumpHistoryPage() throws Exception {
    //    }
    //
    //
    //    @Test
    //    public void getAllHistoryPages() throws Exception {
    //    }
    //
    //
    //    @Test
    //    public void getHistoryEventsSinceDate() throws Exception {
    //    }
    //
    //
    //    @Test
    //    public void pressButton() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getPumpRTC() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getPumpModel() throws Exception {
    //
    //
    //        //medtronicCommunicationManager.getPumpModel();
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getPumpISFProfile() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getBolusWizardCarbProfile() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getRemainingBattery() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getRemainingInsulin() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getCurrentBasalRate() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void getProfile() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void setProfile() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void setTBR() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void cancelTBR() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void setBolus() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void cancelBolus() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void setExtendedBolus() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void cancelExtendedBolus() throws Exception {
    //    }
    //
    //
    //    @org.junit.Test
    //    public void updatePumpManagerStatus() throws Exception {
    //    }


    double units = 0.25d;
    int strokesPerUnit = 40;


    @Test
    public void testBolus() {
        //double units = 0.25d;
        //int strokesPerUnit = 40;

        int length;
        int scrollRate;

        if (strokesPerUnit >= 40) {
            length = 2;

            // 40-stroke pumps scroll faster for higher unit values

            if (units > 10)
                scrollRate = 4;
            else if (units > 1)
                scrollRate = 2;
            else
                scrollRate = 1;

        } else {
            length = 1;
            scrollRate = 1;
        }

        int strokes = (int) (units * ((strokesPerUnit * 1.0d) / (scrollRate * 1.0d))) * scrollRate;

        byte[] body = ByteUtil.fromHexString(String.format("%02x%0" + (2 * length) + "x", length, strokes));
        //String data = String.format("%02x%02x", length, strokes);

        System.out.println("Data: " + ByteUtil.getHex(body));

        //MedtronicUtil.getBasalStrokes()


    }


    @Test
    public void setBolusCurrent() {
        //double units = 0.25d;

        MedtronicUtil.setMedtronicPumpModel(MedtronicDeviceType.Medtronic_523_Revel);

        byte[] body = MedtronicUtil.getBolusStrokes(units);

        //System.out.println("Data: " + ByteUtil.getHex(ByteUtil.concat((byte) body.length, body)));
        System.out.println("Data: " + ByteUtil.getHex(body));
    }


}