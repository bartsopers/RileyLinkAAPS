package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.ServiceData.FetchPumpHistoryResult;

import java.util.ArrayList;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.Page;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/16/16.
 */
public class FetchPumpHistoryTask extends PumpTask {
    public FetchPumpHistoryTask() {
    }


    public FetchPumpHistoryTask(ServiceTransport transport) {
        super(transport);
    }


    private FetchPumpHistoryResult result = new FetchPumpHistoryResult();


    @Override
    public void run() {
        ArrayList<Page> ra = new ArrayList<>();
        for(int i = 0; i < 16; i++) {
            Page page = RileyLinkMedtronicService.getCommunicationManager().getPumpHistoryPage(i);
            if (page != null) {
                ra.add(page);
                RileyLinkMedtronicService.getInstance().saveHistoryPage(i, page);
            }
        }

        result.setMap(getServiceTransport().getServiceResult().getMap());
        result.setResultOK();
        result.setPageArray(ra);
        getServiceTransport().setServiceResult(result);
    }


}
