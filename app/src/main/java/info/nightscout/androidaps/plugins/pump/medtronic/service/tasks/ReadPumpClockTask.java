package info.nightscout.androidaps.plugins.pump.medtronic.service.tasks;

import org.joda.time.LocalDateTime;

import android.util.Log;

import com.gxwtech.roundtrip2.ServiceData.ReadPumpClockResult;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;
import info.nightscout.androidaps.plugins.pump.medtronic.data.dto.ClockDTO;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/9/16.
 */
@Deprecated
public class ReadPumpClockTask extends PumpTask {

    private static final String TAG = "ReadPumpClockTask";


    public ReadPumpClockTask() {
    }


    public ReadPumpClockTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {
        ClockDTO pumpResponse = RileyLinkMedtronicService.getCommunicationManager().getPumpTime();
        if (pumpResponse != null) {
            Log.i(TAG, "ReadPumpClock: " + pumpResponse.pumpTime.toString("HH:mm:ss"));
        } else {
            Log.e(TAG, "handleServiceCommand(" + mTransport.getOriginalCommandName() + ") pumpResponse is null");
        }

        ReadPumpClockResult res = new ReadPumpClockResult();
        res.setTime(pumpResponse.pumpTime);

        getServiceTransport().setServiceResult(res);
    }
}
