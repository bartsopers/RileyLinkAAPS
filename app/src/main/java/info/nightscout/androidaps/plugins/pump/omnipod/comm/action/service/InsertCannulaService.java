package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Arrays;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.BolusAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.ConfigureAlertsAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetBasalScheduleAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfigurationFactory;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class InsertCannulaService {
    public StatusResponse programInitialBasalSchedule(OmnipodCommunicationService communicationService,
                                                      PodSessionState podState, BasalSchedule basalSchedule) {
        return communicationService.executeAction(new SetBasalScheduleAction(podState, basalSchedule,
                true, podState.getScheduleOffset(), false));
    }

    public StatusResponse executeExpirationRemindersAlertCommand(OmnipodCommunicationService communicationService,
                                                                 PodSessionState podState) {
        DateTime endOfServiceTime = podState.getActivatedAt().plus(Constants.SERVICE_DURATION);

        Duration timeUntilExpirationAdvisoryAlarm = new Duration(DateTime.now(),
                endOfServiceTime.minus(Constants.END_OF_SERVICE_IMMINENT_WINDOW).minus(Constants.EXPIRATION_ADVISORY_WINDOW));
        Duration timeUntilShutdownImminentAlarm = new Duration(DateTime.now(),
                endOfServiceTime.minus(Constants.END_OF_SERVICE_IMMINENT_WINDOW));

        AlertConfiguration expirationAdvisoryAlertConfiguration = AlertConfigurationFactory.createExpirationAdvisoryAlertConfiguration(
                timeUntilExpirationAdvisoryAlarm, Constants.EXPIRATION_ADVISORY_WINDOW);
        AlertConfiguration shutdownImminentAlertConfiguration = AlertConfigurationFactory.createShutdownImminentAlertConfiguration(
                timeUntilShutdownImminentAlarm);
        AlertConfiguration autoOffAlertConfiguration = AlertConfigurationFactory.createAutoOffAlertConfiguration(
                false, Duration.ZERO);

        List<AlertConfiguration> alertConfigurations = Arrays.asList( //
                expirationAdvisoryAlertConfiguration, //
                shutdownImminentAlertConfiguration, //
                autoOffAlertConfiguration //
        );

        return new ConfigureAlertsAction(podState, alertConfigurations).execute(communicationService);
    }

    public StatusResponse executeInsertionBolusCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        return communicationService.executeAction(new BolusAction(podState, Constants.POD_CANNULA_INSERTION_BOLUS_UNITS,
                Duration.standardSeconds(1), false, false));
    }
}
