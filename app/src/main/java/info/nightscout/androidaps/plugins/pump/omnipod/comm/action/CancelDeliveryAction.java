package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import java.util.EnumSet;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.CancelDeliveryCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class CancelDeliveryAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final EnumSet<DeliveryType> deliveryTypes;

    public CancelDeliveryAction(PodSessionState podState, EnumSet<DeliveryType> deliveryTypes) {
        if(podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        if(deliveryTypes == null) {
            throw new IllegalArgumentException("Delivery types cannot be null");
        }
        this.podState = podState;
        this.deliveryTypes = deliveryTypes;
    }

    public CancelDeliveryAction(PodSessionState podState, DeliveryType deliveryType) {
        this(podState, EnumSet.of(deliveryType));
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        return communicationService.sendCommand(StatusResponse.class, podState,
                new CancelDeliveryCommand(podState.getCurrentNonce(), BeepType.BEEEP, deliveryTypes));
    }
}
