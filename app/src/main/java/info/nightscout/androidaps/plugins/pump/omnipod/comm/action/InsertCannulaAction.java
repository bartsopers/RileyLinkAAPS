package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.InsertCannulaService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class InsertCannulaAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final InsertCannulaService service;
    private final BasalSchedule initialBasalSchedule;

    public InsertCannulaAction(InsertCannulaService service, PodSessionState podState, BasalSchedule initialBasalSchedule) {
        if(service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        if(podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        if(initialBasalSchedule == null) {
            throw new IllegalArgumentException("Initial basal schedule cannot be null");
        }
        this.service = service;
        this.podState = podState;
        this.initialBasalSchedule = initialBasalSchedule;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        podState.setSetupProgress(SetupProgress.SETTING_INITIAL_BASAL_SCHEDULE);
        service.programInitialBasalSchedule(communicationService, podState, initialBasalSchedule);
        podState.setSetupProgress(SetupProgress.INITIAL_BASAL_SCHEDULE_SET);
        service.executeExpirationRemindersAlertCommand(communicationService, podState);
        podState.setSetupProgress(SetupProgress.STARTING_INSERT_CANNULA);
        StatusResponse statusResponse = service.executeInsertionBolusCommand(communicationService, podState);
        podState.setSetupProgress(SetupProgress.CANNULA_INSERTING);
        return statusResponse;
    }
}
