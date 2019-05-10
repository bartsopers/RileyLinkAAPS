package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import com.gxwtech.roundtrip2.ServiceData.RetrieveHistoryPageResult;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;
import info.nightscout.androidaps.plugins.pump.medtronic.comm.data.Page;
import info.nightscout.androidaps.plugins.pump.medtronic.service.RileyLinkMedtronicService;

/**
 * Created by geoff on 7/9/16.
 */
public class RetrieveHistoryPageTask extends PumpTask {
    public RetrieveHistoryPageTask() {
    }


    public RetrieveHistoryPageTask(ServiceTransport transport) {
        super(transport);
    }


    private Page page;
    private RetrieveHistoryPageResult result;
    private int pageNumber;


    @Override
    public void preOp() {
        // This is to avoid allocating any memory from async thread, though I'm not sure it's necessary.
        RetrieveHistoryPageResult result = new RetrieveHistoryPageResult();
        getServiceTransport().setServiceResult(result);
    }


    @Override
    public void run() {
        pageNumber = mTransport.getServiceCommand().getMap().getInt("pageNumber");
        page = RileyLinkMedtronicService.getCommunicationManager().getPumpHistoryPage(pageNumber);
        result = (RetrieveHistoryPageResult) getServiceTransport().getServiceResult();
        result.setResultOK();
        result.setPageNumber(pageNumber);
        result.setPageBundle(page.pack());
    }

}
