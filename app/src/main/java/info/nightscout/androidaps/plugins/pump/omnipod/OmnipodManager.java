package info.nightscout.androidaps.plugins.pump.omnipod;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.DeactivatePodAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.InsertCannulaAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.PairAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.PrimeAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetBasalScheduleAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.InsertCannulaService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PairService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PrimeService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

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

    public void pairAndPrime() {
        if (podState == null) {
            podState = communicationService.executeAction(new PairAction(new PairService()));
        }
        if (podState.getSetupProgress() == SetupProgress.POD_CONFIGURED) {
            StatusResponse statusResponse = communicationService.executeAction(new PrimeAction(new PrimeService(), podState));
            podState.updateFromStatusResponse(statusResponse);

            // TODO update setup progress after X seconds
        } else {
            throw new IllegalStateException("Illegal setup state: " + podState.getSetupProgress().name());
        }
    }

    public void insertCannula() {
        if (podState == null || podState.getSetupProgress().isBefore(SetupProgress.PRIMING)) { // FIXME is PRIMING the right SetupProgress here?
            throw new IllegalArgumentException("Pod should be paired and primed first");
        } else if (podState.getSetupProgress().isAfter(SetupProgress.STARTING_INSERT_CANNULA)) { // FIXME is STARTING_INSERT_CANNULA the right SetupProgress here?
            throw new IllegalStateException("Illegal setup state: " + podState.getSetupProgress().name());
        }

        StatusResponse statusResponse = communicationService.executeAction(new InsertCannulaAction(new InsertCannulaService(), podState, createStubBasalSchedule()));
        podState.updateFromStatusResponse(statusResponse);

    }

    public void setBasalSchedule(BasalSchedule basalSchedule, boolean confidenceReminder,
                                 Duration scheduleOffset, Duration programReminderInterval) {
        if (!isInitialized()) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new SetBasalScheduleAction(podState, basalSchedule,
                confidenceReminder, scheduleOffset, programReminderInterval));
    }

    public void deactivatePod() {
        //if(!isInitialized()) {
        // TODO
        if(podState == null) {
            throw new IllegalStateException("Pod should be initialized first");
        }
        communicationService.executeAction(new DeactivatePodAction(podState));
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
    }

    private BasalSchedule createStubBasalSchedule() {
        List<BasalScheduleEntry> basals = new ArrayList<>();
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(0)));    //00:00-00:30
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(30)));   //00:30-01:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(60)));    //01:00-01:30
        basals.add(new BasalScheduleEntry(11.5, Duration.standardMinutes(90))); //01:30-02:00
        basals.add(new BasalScheduleEntry(2, Duration.standardMinutes(120)));    //20:00-02:30
        basals.add(new BasalScheduleEntry(12.5, Duration.standardMinutes(150))); //02:30-03:00
        basals.add(new BasalScheduleEntry(3, Duration.standardMinutes(180)));    //03:00-03:30
        basals.add(new BasalScheduleEntry(13.5, Duration.standardMinutes(210))); //03:30-04:00
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(240)));    //04:00-04:30
        basals.add(new BasalScheduleEntry(14.5, Duration.standardMinutes(270))); //04:30-05:00
        basals.add(new BasalScheduleEntry(5, Duration.standardMinutes(300)));    //05:00-05:30
        basals.add(new BasalScheduleEntry(15.5, Duration.standardMinutes(330))); //05:30-06:00
        basals.add(new BasalScheduleEntry(6, Duration.standardMinutes(360)));    //06:00-06:30
        basals.add(new BasalScheduleEntry(16.5, Duration.standardMinutes(390))); //06:30-07:00
        basals.add(new BasalScheduleEntry(7, Duration.standardMinutes(420)));    //07:00-07:30
        basals.add(new BasalScheduleEntry(17.5, Duration.standardMinutes(450))); //07:30-08:00
        basals.add(new BasalScheduleEntry(8, Duration.standardMinutes(480)));    //08:00-08:30
        basals.add(new BasalScheduleEntry(18.5, Duration.standardMinutes(510))); //08:30-09:00
        basals.add(new BasalScheduleEntry(9, Duration.standardMinutes(540)));    //09:00-09:30
        basals.add(new BasalScheduleEntry(19.5, Duration.standardMinutes(570))); //09:30-10:00
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(600)));   //10:00-10:30
        basals.add(new BasalScheduleEntry(1.05, Duration.standardMinutes(630))); //10:30-11:00
        basals.add(new BasalScheduleEntry(11, Duration.standardMinutes(660)));//11:00-22:00
        basals.add(new BasalScheduleEntry(1.15, Duration.standardMinutes(1320)));//22:00-24:00
        return new BasalSchedule(basals);
    }
}