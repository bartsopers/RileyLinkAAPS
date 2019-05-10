package com.gxwtech.roundtrip2.RoundtripService.Tasks;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceResult;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.PumpTask;

/**
 * Created by geoff on 7/10/16.
 */
public class ReadBolusWizardCarbProfileTask extends PumpTask {

    public ReadBolusWizardCarbProfileTask() {
        super();
    }


    public ReadBolusWizardCarbProfileTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {
        // PumpMessage msg = RileyLinkMedtronicService.getCommunicationManager().getBolusWizardCarbProfile();
        ServiceResult result = getServiceTransport().getServiceResult();
        // interpret msg here.
        getServiceTransport().setServiceResult(result);
    }
}
