package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.InsertCannulaService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class InsertCannulaAction implements OmnipodAction<StatusResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(InsertCannulaAction.class);

    private final PodSessionState podState;
    private final InsertCannulaService service;
    private final BasalSchedule initialBasalSchedule;

    public InsertCannulaAction(InsertCannulaService insertCannulaService, PodSessionState podState, BasalSchedule initialBasalSchedule) {
        if (insertCannulaService == null) {
            throw new IllegalArgumentException("Insert cannula service cannot be null");
        }
        if (podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        if (initialBasalSchedule == null) {
            throw new IllegalArgumentException("Initial basal schedule cannot be null");
        }
        this.service = insertCannulaService;
        this.podState = podState;
        this.initialBasalSchedule = initialBasalSchedule;
    }

    public static void updateCannulaInsertionStatus(PodSessionState podState, StatusResponse statusResponse) {
        if (podState.getSetupProgress().equals(SetupProgress.CANNULA_INSERTING) &&
                statusResponse.getPodProgressStatus().isReadyForDelivery()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updating SetupProgress from CANNULA_INSERTING to COMPLETED");
            }
            podState.setSetupProgress(SetupProgress.COMPLETED);
        }
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        if (podState.getSetupProgress().isBefore(SetupProgress.PRIMING_FINISHED)) {
            throw new IllegalStateException("Pod should be primed first");
        }

        if (podState.getSetupProgress().isBefore(SetupProgress.INITIAL_BASAL_SCHEDULE_SET)) {
            service.programInitialBasalSchedule(communicationService, podState, initialBasalSchedule);
            podState.setSetupProgress(SetupProgress.INITIAL_BASAL_SCHEDULE_SET);
        }
        if (podState.getSetupProgress().isBefore(SetupProgress.STARTING_INSERT_CANNULA)) {
            service.executeExpirationRemindersAlertCommand(communicationService, podState);
            podState.setSetupProgress(SetupProgress.STARTING_INSERT_CANNULA);
        }

        if (podState.getSetupProgress().isBefore(SetupProgress.CANNULA_INSERTING)) {
            StatusResponse statusResponse = service.executeInsertionBolusCommand(communicationService, podState);
            podState.setSetupProgress(SetupProgress.CANNULA_INSERTING);
            return statusResponse;
        } else if (podState.getSetupProgress().equals(SetupProgress.CANNULA_INSERTING)) {
            // Check status
            StatusResponse statusResponse = communicationService.executeAction(new GetStatusAction(podState));
            updateCannulaInsertionStatus(podState, statusResponse);
            return statusResponse;
        } else {
            throw new IllegalStateException("Illegal setup progress: " + podState.getSetupProgress().name());
        }
    }
}
