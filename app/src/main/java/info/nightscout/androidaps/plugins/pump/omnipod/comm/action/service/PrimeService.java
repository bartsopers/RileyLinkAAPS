package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.Duration;

import java.util.Collections;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.BolusAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.ConfigureAlertsAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.FaultConfigCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfigurationFactory;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;

public class PrimeService {

    public StatusResponse executeDisableTab5Sub16FaultConfigCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        FaultConfigCommand faultConfigCommand = new FaultConfigCommand(podState.getCurrentNonce(), (byte) 0x00, (byte) 0x00);
        OmnipodMessage faultConfigMessage = new OmnipodMessage(podState.getAddress(),
                Collections.singletonList(faultConfigCommand), podState.getMessageNumber());
        return communicationService.exchangeMessages(StatusResponse.class, podState, faultConfigMessage);
    }

    public StatusResponse executeFinishSetupReminderAlertCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        AlertConfiguration finishSetupReminderAlertConfiguration = AlertConfigurationFactory.createFinishSetupReminderAlertConfiguration();
        return communicationService.executeAction(new ConfigureAlertsAction(podState, Collections.singletonList(finishSetupReminderAlertConfiguration)));
    }

    public StatusResponse executePrimeBolusCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        return communicationService.executeAction(new BolusAction(podState, Constants.POD_PRIME_BOLUS_UNITS, Duration.standardSeconds(1)));
    }
}
