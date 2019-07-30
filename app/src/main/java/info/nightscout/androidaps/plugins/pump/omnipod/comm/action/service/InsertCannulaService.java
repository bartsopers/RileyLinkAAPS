package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Arrays;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.ConfigureAlertsAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetBasalScheduleAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.TimerAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class InsertCannulaService {
    public StatusResponse programInitialBasalSchedule(OmnipodCommunicationService communicationService, PodSessionState podState, BasalSchedule basalSchedule) {
        DateTime now = DateTime.now();
        Duration scheduleOffset = new Duration(new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0, 0), now);
        return communicationService.executeAction(new SetBasalScheduleAction(podState, basalSchedule, false, scheduleOffset, Duration.ZERO));
    }

    public StatusResponse executeExpirationRemindersAlertCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        DateTime endOfServiceTime = podState.getActivatedAt().plus(Constants.SERVICE_DURATION);

        Duration timeUntilExpirationAdvisory = new Duration(DateTime.now(),
                endOfServiceTime.minus(Constants.END_OF_SERVICE_IMMINENT_WINDOW).minus(Constants.EXPIRATION_ADVISORY_WINDOW));
        // FIXME make AlertType match wiki description
        AlertConfiguration expirationAdvisoryAlarm = new AlertConfiguration(AlertType.TIMER_LIMIT, true, false, Constants.EXPIRATION_ADVISORY_WINDOW,
                new TimerAlertTrigger(timeUntilExpirationAdvisory), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_60_MINUTES);

        Duration timeUntilShutdownImminentAlarm = new Duration(DateTime.now(), endOfServiceTime.minus(Constants.END_OF_SERVICE_IMMINENT_WINDOW));
        AlertConfiguration shutdownImminentAlarm = new AlertConfiguration(AlertType.END_OF_SERVICE, true, false, Duration.ZERO,
                new TimerAlertTrigger(timeUntilShutdownImminentAlarm), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_15_MINUTES);

        AlertConfiguration autoOffAlarm = new AlertConfiguration(AlertType.AUTO_OFF, false, true,
                Duration.standardMinutes(15), new TimerAlertTrigger(Duration.ZERO), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_15_MINUTES);

        return new ConfigureAlertsAction(podState, Arrays.asList(expirationAdvisoryAlarm, shutdownImminentAlarm, autoOffAlarm)).execute(communicationService);
    }

    // TODO maybe we should replace this with a BolusAction?
    public StatusResponse executeInsertionBolusCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        double insertionBolusUnits = 0.5;
        Duration timeBetweenPulses = Duration.standardSeconds(1);

        BolusDeliverySchedule insertionBolusDeliverySchedule = new BolusDeliverySchedule(insertionBolusUnits, timeBetweenPulses);
        SetInsulinScheduleCommand insertionBolusCommand = new SetInsulinScheduleCommand(podState.getCurrentNonce(), insertionBolusDeliverySchedule);
        BolusExtraCommand insertionBolusExtraCommand = new BolusExtraCommand(insertionBolusUnits, timeBetweenPulses);
        OmnipodMessage insertionBolusMessage = new OmnipodMessage(podState.getAddress(), Arrays.asList(insertionBolusCommand, insertionBolusExtraCommand), podState.getMessageNumber());
        StatusResponse statusResponse = communicationService.exchangeMessages(StatusResponse.class, podState, insertionBolusMessage);
        return statusResponse;
    }
}
