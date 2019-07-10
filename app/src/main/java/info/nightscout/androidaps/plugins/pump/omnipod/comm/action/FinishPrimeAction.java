package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.Duration;

import java.util.Collections;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigureAlertsCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.TimerAlertTrigger;

public class FinishPrimeAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;

    public FinishPrimeAction(PodSessionState podState) {
        this.podState = podState;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        if (this.podState == null) {
            throw new IllegalStateException("Pod state cannot be null");
        }

        TimerAlertTrigger expirationAlertTrigger = new TimerAlertTrigger(Duration.standardHours(70).plus(Duration.standardMinutes(58)));

        AlertConfiguration expirationAlertConfiguration = new AlertConfiguration(AlertType.EXPIRATION_ADVISORY,
                true, false, Duration.ZERO, expirationAlertTrigger,
                BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_15_MINUTES);

        ConfigureAlertsCommand alertCommand = new ConfigureAlertsCommand(podState.getCurrentNonce(), Collections.singletonList(expirationAlertConfiguration));

        return communicationService.sendCommand(podState, alertCommand);
    }
}
