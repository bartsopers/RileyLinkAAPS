package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.Duration;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.SetTempBasalService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class SetTempBasalAction implements OmnipodAction<StatusResponse> {
    private final SetTempBasalService service;
    private final PodSessionState podState;
    private final double rate;
    private final Duration duration;

    public SetTempBasalAction(SetTempBasalService service, PodSessionState podState, double rate, Duration duration) {
        this.service = service;
        this.podState = podState;
        this.rate = rate;
        this.duration = duration;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        StatusResponse statusResponse = service.cancelTempBasal(communicationService, podState);

        if(statusResponse.getDeliveryStatus() != DeliveryStatus.NORMAL) {
            throw new IllegalStateException("Illegal delivery status: "+
                    statusResponse.getDeliveryStatus().name());
        }

        return service.executeTempBasalCommand(communicationService, podState, rate, duration);
    }
}
