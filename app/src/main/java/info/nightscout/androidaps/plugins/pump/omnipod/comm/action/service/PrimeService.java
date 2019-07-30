package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.Duration;

import java.util.Arrays;
import java.util.Collections;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.ConfigureAlertsAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.FaultConfigCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.TimerAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class PrimeService {
    public StatusResponse executeDisableTab5Sub16FaultConfigCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        FaultConfigCommand faultConfigCommand = new FaultConfigCommand(podState.getCurrentNonce(), (byte) 0x00, (byte) 0x00);
        OmnipodMessage faultConfigMessage = new OmnipodMessage(podState.getAddress(), Collections.singletonList(faultConfigCommand), podState.getMessageNumber());
        return communicationService.exchangeMessages(StatusResponse.class, podState, faultConfigMessage);
    }

    public StatusResponse executeFinishSetupReminderAlertCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        TimerAlertTrigger finishSetupReminderTrigger = new TimerAlertTrigger(Duration.standardDays(5));

        AlertConfiguration finishSetupReminderAlertConfiguration = new AlertConfiguration(AlertType.TIMER_LIMIT, true, false,
                Duration.standardMinutes(55), finishSetupReminderTrigger, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_5_MINUTES);

        return communicationService.executeAction(new ConfigureAlertsAction(podState, Collections.singletonList(finishSetupReminderAlertConfiguration)));
    }

    // TODO maybe we should replace this with a BolusAction?
    public StatusResponse executePrimeBolusCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        double primeUnits = 2.6;
        Duration timeBetweenPulses = Duration.standardSeconds(1);
        BolusDeliverySchedule primeBolus = new BolusDeliverySchedule(primeUnits, timeBetweenPulses);
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(podState.getCurrentNonce(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits, timeBetweenPulses);
        OmnipodMessage primeMessage = new OmnipodMessage(podState.getAddress(), Arrays.asList(primeCommand, extraBolusCommand), podState.getMessageNumber());
        StatusResponse statusResponse = communicationService.exchangeMessages(StatusResponse.class, podState, primeMessage);
        return statusResponse;
    }
}
