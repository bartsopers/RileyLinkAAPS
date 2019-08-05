package info.nightscout.androidaps.plugins.pump.omnipod;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.AcknowledgeAlertsAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.BolusAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.CancelDeliveryAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.DeactivatePodAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.GetStatusAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.InsertCannulaAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.PairAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.PrimeAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetBasalScheduleAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetTempBasalAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.InsertCannulaService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PairService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PrimeService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.SetTempBasalService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;
import info.nightscout.androidaps.utils.SP;

public class OmnipodManager {
    private final OmnipodCommunicationService communicationService;
    private PodSessionState podState;

    public OmnipodManager(OmnipodCommunicationService communicationService, PodSessionState podState) {
        if (communicationService == null) {
            throw new IllegalArgumentException("Communication service cannot be null");
        }
        this.communicationService = communicationService;
        this.podState = podState;
    }

    public OmnipodManager(OmnipodCommunicationService communicationService) {
        this(communicationService, null);
    }

    public OmnipodCommunicationService getCommunicationService() {
        return communicationService;
    }

    public StatusResponse getStatus() {
        if(podState == null) {
            throw new IllegalStateException("Pod should be paired first");
        }
        return communicationService.executeAction(new GetStatusAction(podState));
    }

    public void acknowledgeAlerts() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new AcknowledgeAlertsAction(podState, podState.getActiveAlerts()));
    }

    public void pairAndPrime() {
        if (podState == null) {
            podState = communicationService.executeAction(new PairAction(new PairService()));
        }
        if (podState.getSetupProgress().isBefore(SetupProgress.PRIMING_FINISHED)) {
            communicationService.executeAction(new PrimeAction(new PrimeService(), podState));

            executeDelayed(() -> {
                StatusResponse delayedStatusResponse = communicationService.executeAction(new GetStatusAction(podState));
                PrimeAction.updatePrimingStatus(podState, delayedStatusResponse);
            }, Constants.POD_PRIME_DURATION);
        } else {
            throw new IllegalStateException("Illegal setup state: " + podState.getSetupProgress().name());
        }
    }

    public void insertCannula() {
        if (podState == null || podState.getSetupProgress().isBefore(SetupProgress.PRIMING_FINISHED)) {
            throw new IllegalArgumentException("Pod should be paired and primed first");
        } else if (podState.getSetupProgress().isAfter(SetupProgress.CANNULA_INSERTING)) {
            throw new IllegalStateException("Illegal setup state: " + podState.getSetupProgress().name());
        }

        communicationService.executeAction(new InsertCannulaAction(new InsertCannulaService(), podState, createStubBasalSchedule()));

        executeDelayed(() -> {
                StatusResponse delayedStatusResponse = communicationService.executeAction(new GetStatusAction(podState));
                InsertCannulaAction.updateCannulaInsertionStatus(podState, delayedStatusResponse);
        }, Constants.POD_CANNULA_INSERTION_DURATION);
    }

    public void setBasalSchedule(BasalSchedule basalSchedule, boolean confidenceReminder) {
        if (!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new SetBasalScheduleAction(podState, basalSchedule,
                confidenceReminder, podState.getScheduleOffset(), true));
    }

    public void setTempBasal(double rate, Duration duration) {
        if (!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new SetTempBasalAction(new SetTempBasalService(),
                podState, rate, duration, true, true));
    }

    public void cancelTempBasal() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new CancelDeliveryAction(podState, DeliveryType.TEMP_BASAL, true));
    }

    public void bolus(double units) {
        if (!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new BolusAction(podState, units, true, true));
    }

    public void cancelBolus() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new CancelDeliveryAction(podState, DeliveryType.BOLUS, true));
    }

    public void suspendDelivery() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new CancelDeliveryAction(podState, EnumSet.allOf(DeliveryType.class), true));
    }

    public void resumeDelivery() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new SetBasalScheduleAction(podState, podState.getBasalSchedule(),
                true, podState.getScheduleOffset(), true));
    }

    public void setTime() {
        if(!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        // Suspend delivery
        communicationService.executeAction(new CancelDeliveryAction(podState, EnumSet.allOf(DeliveryType.class), false));

        // Joda seems to cache the default time zone, so we use the JVM's
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        podState.setTimeZone(DateTimeZone.getDefault());

        // Resume delivery
        communicationService.executeAction(new SetBasalScheduleAction(podState, podState.getBasalSchedule(),
                true, podState.getScheduleOffset(), true));
    }

    public DateTime getTime() {
        return podState.getTime();
    }

    public void deactivatePod() {
        if (podState == null) {
            throw new IllegalStateException("Pod should be paired first");
        }
        communicationService.executeAction(new DeactivatePodAction(podState, true));
        resetPodState();
    }

    public boolean isInitialized() {
        return podState != null && podState.getSetupProgress() == SetupProgress.COMPLETED;
    }

    public String getPodStateAsString() {
        return podState == null ? "null" : podState.toString();
    }

    public void resetPodState() {
        podState = null;
        SP.remove(OmniPodConst.Prefs.POD_STATE);
    }

    private BasalSchedule createStubBasalSchedule() {
        List<BasalScheduleEntry> basals = new ArrayList<>();
        basals.add(new BasalScheduleEntry(0.55, Duration.standardMinutes(0)));    //00:00-00:30
        basals.add(new BasalScheduleEntry(2.05, Duration.standardMinutes(30)));   //00:30-01:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(60)));    //01:00-01:30
        basals.add(new BasalScheduleEntry(1.3, Duration.standardMinutes(90))); //01:30-02:00
        basals.add(new BasalScheduleEntry(2, Duration.standardMinutes(120)));    //20:00-02:30
        basals.add(new BasalScheduleEntry(1.5, Duration.standardMinutes(150))); //02:30-03:00
        basals.add(new BasalScheduleEntry(0.85, Duration.standardMinutes(180)));    //03:00-03:30
        basals.add(new BasalScheduleEntry(0.7, Duration.standardMinutes(210))); //03:30-04:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(240)));    //04:00-04:30
        basals.add(new BasalScheduleEntry(1.5, Duration.standardMinutes(270))); //04:30-05:00
        basals.add(new BasalScheduleEntry(0.55, Duration.standardMinutes(300)));    //05:00-05:30
        basals.add(new BasalScheduleEntry(1.5, Duration.standardMinutes(330))); //05:30-06:00
        basals.add(new BasalScheduleEntry(1.3, Duration.standardMinutes(360)));    //06:00-06:30
        basals.add(new BasalScheduleEntry(0.65, Duration.standardMinutes(390))); //06:30-07:00
        basals.add(new BasalScheduleEntry(0.8, Duration.standardMinutes(420)));    //07:00-07:30
        basals.add(new BasalScheduleEntry(0.5, Duration.standardMinutes(450))); //07:30-08:00
        basals.add(new BasalScheduleEntry(1.45, Duration.standardMinutes(480)));    //08:00-08:30
        basals.add(new BasalScheduleEntry(1.1, Duration.standardMinutes(510))); //08:30-09:00
        basals.add(new BasalScheduleEntry(0.3, Duration.standardMinutes(540)));    //09:00-09:30
        basals.add(new BasalScheduleEntry(1.5, Duration.standardMinutes(570))); //09:30-10:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(600)));   //10:00-10:30
        basals.add(new BasalScheduleEntry(1.05, Duration.standardMinutes(630))); //10:30-11:00
        basals.add(new BasalScheduleEntry(0.85, Duration.standardMinutes(660)));//11:00-22:00
        basals.add(new BasalScheduleEntry(1.15, Duration.standardMinutes(1320)));//22:00-24:00
        return new BasalSchedule(basals);
    }

    private void executeDelayed(Runnable r, Duration timeout) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(r, timeout.getMillis(), TimeUnit.MILLISECONDS);
    }
}