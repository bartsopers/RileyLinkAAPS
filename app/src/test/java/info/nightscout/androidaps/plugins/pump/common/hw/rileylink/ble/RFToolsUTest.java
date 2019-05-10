package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble;

import org.junit.Test;

import android.util.Log;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkTargetFrequency;
import info.nightscout.androidaps.plugins.pump.common.utils.HexDump;
import info.nightscout.androidaps.plugins.pump.common.utils.StringUtil;

/**
 * Created by andy on 7/1/18.
 */
public class RFToolsUTest {

    String TAG = "RFToolsUTest";


    @Test
    public void decode4b6b() throws Exception {

        String[] dataSent = {

            // "19050000000000000000b4030000a9659a6b19b199c555b2c000", //
            "79050000000000000000b4030000a9659a6b19b199c571c9a555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555556000", //
            "79050000000000000000b4030000a9659a6b19b199c5725555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555ac000", //
            "79050000000000000000b4030000a9659a6b19b199c6a355555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555570e000"

        };
        // 0x19, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        for (int i = 0; i < dataSent.length; i++) {
            Log.d(TAG, "Entry " + i);

            // createByteArray(dataSent[i], 28);

            // , 0x00
            byte[] bytes = RFTools.decode4b6b(createByteArray(dataSent[i], 28));

            System.out.println(HexDump.toHexStringDisplayable(bytes));
        }

    }


    private byte[] createByteArray(String dataFull, int startIndex) {

        String data = dataFull.substring(startIndex);

        data = data.substring(0, data.length() - 4);

        System.out.println("Data: " + data);

        // List<Byte> outList = new ArrayList<>();
        // for(int i = 0; i < data.length(); i += 2) {
        // String nn = "0x" + data.substring(i, i + 2);
        //
        //
        // outList.add(Byte.parse(nn));
        // }
        //
        // byte[] outArray = new byte[outList.size()];
        //
        // for(int i = 0; i < outList.size(); i++) {
        // outArray[i] = outList.get(i);
        // }

        int len = data.length();
        byte[] outArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            outArray[i / 2] = (byte)((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i + 1),
                16));
        }

        return outArray;
    }


    @Test
    public void getVersion() throws Exception {
        byte[] bytes = { 0x62, 0x67, 0x5F, 0x72, 0x66, 0x73, 0x70, 0x79, 0x20, 0x30, 0x2E, 0x39 };

        System.out.println(StringUtil.fromBytes(bytes));
    }


    @Test
    public void getFrequency() {
        RileyLinkTargetFrequency frew = RileyLinkTargetFrequency.Medtronic_US;

        for (double v : frew.getScanFrequencies()) {
            System.out.println("v=" + v);
        }
    }

}
