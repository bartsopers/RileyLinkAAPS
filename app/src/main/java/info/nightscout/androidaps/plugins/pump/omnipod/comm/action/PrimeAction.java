package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PrimeService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class PrimeAction implements OmnipodAction<StatusResponse> {
    private final PrimeService service;
    private final PodSessionState podState;

    public PrimeAction(PrimeService service, PodSessionState podState) {
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null");
        }
        if (podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        this.service = service;
        this.podState = podState;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        if (podState.getSetupProgress().isBefore(SetupProgress.STARTING_PRIME)) {
            service.executeDisableTab5Sub16FaultConfigCommand(communicationService, podState);
            service.executeFinishSetupReminderAlertCommand(communicationService, podState);
        }

        podState.setSetupProgress(SetupProgress.STARTING_PRIME);

        StatusResponse statusResponse = service.executePrimeBolusCommand(communicationService, podState);
        podState.updateFromStatusResponse(statusResponse);

        podState.setSetupProgress(SetupProgress.PRIMING);
        return statusResponse;
    }
}
