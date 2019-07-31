package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.DeactivatePodCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class DeactivatePodAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;

    public DeactivatePodAction(PodSessionState podState) {
        this.podState = podState;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        // TODO suspend delivery

        DeactivatePodCommand deactivatePodCommand = new DeactivatePodCommand(podState.getCurrentNonce());
        return communicationService.sendCommand(StatusResponse.class, podState, deactivatePodCommand);
    }
}
