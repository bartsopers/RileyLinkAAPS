package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PrimeService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class PrimeAction implements OmnipodAction<StatusResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(PrimeAction.class);

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
        if(podState.getSetupProgress().isBefore(SetupProgress.POD_CONFIGURED)) {
            throw new IllegalStateException("Pod should be paired first");
        }
        if (podState.getSetupProgress().isBefore(SetupProgress.STARTING_PRIME)) {
            service.executeDisableTab5Sub16FaultConfigCommand(communicationService, podState);
            service.executeFinishSetupReminderAlertCommand(communicationService, podState);
            podState.setSetupProgress(SetupProgress.STARTING_PRIME);
        }

        if(podState.getSetupProgress().isBefore(SetupProgress.PRIMING)) {
            StatusResponse statusResponse = service.executePrimeBolusCommand(communicationService, podState);
            podState.updateFromStatusResponse(statusResponse);

            podState.setSetupProgress(SetupProgress.PRIMING);

            return statusResponse;
        } else if(podState.getSetupProgress().equals(SetupProgress.PRIMING)) {
            // Check status
            StatusResponse statusResponse = communicationService.executeAction(new GetStatusAction(podState));
            updatePrimingStatus(podState, statusResponse);
            return statusResponse;
        } else {
            throw new IllegalStateException("Illegal setup progress: "+ podState.getSetupProgress().name());
        }
    }

    public static void updatePrimingStatus(PodSessionState podState, StatusResponse statusResponse) {
        if(podState.getSetupProgress().equals(SetupProgress.PRIMING) && statusResponse.getPodProgressStatus().equals(PodProgressStatus.READY_FOR_BASAL_SCHEDULE)) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Updating SetupProgress from PRIMING to READY_FOR_BASAL_SCHEDULE");
            }
            podState.setSetupProgress(SetupProgress.PRIMING_FINISHED);
        }
    }
}
