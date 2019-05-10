package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import android.util.Log;

import com.gxwtech.roundtrip2.ServiceData.ReadPumpClockResult;

import org.joda.time.LocalDateTime;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/9/16.
 */
public class ReadPumpClockTask extends PumpTask {
    private static final String TAG = "ReadPumpClockTask";


    public ReadPumpClockTask() {
    }


    public ReadPumpClockTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {
        LocalDateTime pumpResponse = RileyLinkMedtronicService.getCommunicationManager().getPumpTime();
        if (pumpResponse != null) {
            Log.i(TAG, "ReadPumpClock: " + pumpResponse.toString("HH:mm:ss"));
        } else {
            Log.e(TAG, "handleServiceCommand(" + mTransport.getOriginalCommandName() + ") pumpResponse is null");
        }

        ReadPumpClockResult res = new ReadPumpClockResult();
        res.setTime(pumpResponse);

        getServiceTransport().setServiceResult(res);
    }
}
