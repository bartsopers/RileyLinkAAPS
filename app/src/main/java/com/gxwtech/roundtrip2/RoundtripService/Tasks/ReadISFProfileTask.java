package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;

/**
 * Created by geoff on 7/10/16.
 */
public class ReadISFProfileTask extends PumpTask {

    public ReadISFProfileTask() {
    }


    public ReadISFProfileTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void preOp() {
    }


    @Override
    public void run() {
        // //ISFTable table = RileyLinkMedtronicService.getCommunicationManager().getPumpISFProfile();
        // ServiceResult result = getServiceTransport().getServiceResult();
        // if (table.isValid()) {
        // // convert from ISFTable to ISFProfile
        // Bundle map = result.getMap();
        // map.putIntArray("times", table.getTimes());
        // map.putFloatArray("rates", table.getRates());
        // map.putString("ValidDate", TimeFormat.standardFormatter().print(table.getValidDate()));
        // result.setMap(map);
        // result.setResultOK();
        // getServiceTransport().setServiceResult(result);
        // }
    }

}
