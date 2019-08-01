package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.Duration;

import java.util.Arrays;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class BolusAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final double units;
    private final Duration timeBetweenPulses;

    public BolusAction(PodSessionState podState, double units, Duration timeBetweenPulses) {
        this.podState = podState;
        this.units = units;
        this.timeBetweenPulses = timeBetweenPulses;
    }

    public BolusAction(PodSessionState podState, double units) {
        this(podState, units, Duration.standardSeconds(2));
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        BolusDeliverySchedule bolusDeliverySchedule = new BolusDeliverySchedule(units, timeBetweenPulses);
        SetInsulinScheduleCommand setInsulinScheduleCommand = new SetInsulinScheduleCommand(
                podState.getCurrentNonce(), bolusDeliverySchedule);
        BolusExtraCommand bolusExtraCommand = new BolusExtraCommand(units, timeBetweenPulses);
        OmnipodMessage primeBolusMessage = new OmnipodMessage(podState.getAddress(),
                Arrays.asList(setInsulinScheduleCommand, bolusExtraCommand), podState.getMessageNumber());
        return communicationService.exchangeMessages(StatusResponse.class, podState, primeBolusMessage);
    }
}
