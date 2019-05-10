package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.common.utils.CRC;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.MedtronicDeviceType;

/**
 * Created by geoff on 6/18/16.
 */
@Deprecated
public class PumpHistoryPage {
    public byte[] data = new byte[0];
    private MedtronicDeviceType model = MedtronicDeviceType.Medtronic_522;
    private int pageNumber;


    public PumpHistoryPage() {
    }


    public PumpHistoryPage(byte[] data, MedtronicDeviceType model, int pageNumber) {
        init(data, model, pageNumber);
    }


    public void init(byte[] data, MedtronicDeviceType model, int pageNumber) {
        this.data = data;
        this.model = model;
        this.pageNumber = pageNumber;
    }


    public int getPageNumber() {
        return pageNumber;
    }


    public boolean isValid() {
        return isCRCValid();
    }


    public boolean isCRCValid() {
        if (data == null) {
            return false;
        }
        if (data.length < 3) {
            return false;
        }
        byte[] crc16 = CRC.calculate16CCITT(ByteUtil.substring(data, 0, data.length - 2));
        if (crc16 == null) {
            return false;
        }
        if ((crc16[0] == data[data.length - 2]) && (crc16[1] == data[data.length - 1])) {
            return true;
        }
        return false;
    }
}
