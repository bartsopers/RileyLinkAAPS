package info.nightscout.androidaps.plugins.pump.medtronic.comm.data.history2;

import android.util.Log;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;

/**
 * Created by andy on 6/4/18.
 */

public class MedtronicDecoderUTest {


    @Test
    public void testDecoding() {
        byte[] rawData = {(byte) 0x7c, 00};
        // A7 31 65 51 73 02 06 8F 00
        // 06 8E

        //byte cf = new Byte(8e);

        float value = MedtronicUtil.makeUnsignedShort(0x06, (byte) 0x8F) / 10.0f;

        System.out.println("Value: " + value);


        float value2 = MedtronicUtil.makeUnsignedShort((byte) 0x8F, 0x06) / 10.0f;

        System.out.println("Value: " + value2);

        // 02 06 = 153.8
        // 06 8f = 205.4
        // 8f 00 =


    }


    @Test
    public void testMultiFrames() {

        int[] content = new int[145];

        for(int i = 0; i < content.length; i++) {
            content[i] = i + 1;
        }

        Log.d("test", "Array: " + displayArray(content));

        int start = 0;
        int frameNr = 1;
        int len = 0;

        do {

            if (start + 64 > content.length) {
                len = content.length - start;

                if (len == 0)
                    break;
            } else {
                len = 64;
            }

            int frame[] = new int[65];

            frame[0] = frameNr;

            System.arraycopy(content, start, frame, 1, len);

            Log.d("Test", "Sending frame #" + frameNr);

            Log.d("Test", displayArray(frame));

            start += 64;
            frameNr++;

            if (len != 64) {
                break;
            }


        } while (true);

    }


    public String displayArray(int[] array) {
        StringBuffer sb = new StringBuffer();

        for(int i : array) {
            sb.append(" ");
            sb.append(i);
        }

        return sb.toString();
    }


}
