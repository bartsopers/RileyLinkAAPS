package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import java.util.EnumSet;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class SuspendDeliveryAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;

    public SuspendDeliveryAction(PodSessionState podState) {
        if(podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        this.podState = podState;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        return communicationService.executeAction(new CancelDeliveryAction(podState, EnumSet.allOf(DeliveryType.class)));
    }
}
