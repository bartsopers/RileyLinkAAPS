package info.nightscout.androidaps.plugins.pump.common.util;

/**
 * Created by andy on 10/21/18.
 */

public class ByteTestsAbstract {

    // its data in format 00010A
    public byte[] createByteArrayWithLast4(String dataFull, int startIndex) {

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

}
