package info.nightscout.androidaps.plugins.pump.omnipod;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.FinishPrimeAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.InitializePodAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.SetBasalScheduleAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalScheduleEntry;
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

    public void initializePod() {
        podState = communicationService.executeAction(new InitializePodAction());
    }

    public StatusResponse finishPrime() {
        if (!isInitialized()) {
            throw new IllegalArgumentException("Pod should be initialized first");
        }
        communicationService.executeAction(new FinishPrimeAction(podState));

        // FIXME obtain real basal schedule
        StatusResponse statusResponse = setBasalSchedule(createStubBasalSchedule(), false, Duration.ZERO, Duration.ZERO);
        return statusResponse;
    }

    public StatusResponse setBasalSchedule(BasalSchedule basalSchedule, boolean confidenceReminder,
                                           Duration scheduleOffset, Duration programReminderInterval) {
        if (!isInitialized()) {
            throw new IllegalArgumentException("Pod should be initialized first");
        }
        StatusResponse statusResponse = communicationService.executeAction(new SetBasalScheduleAction(podState, basalSchedule,
                confidenceReminder, scheduleOffset, programReminderInterval));
        return statusResponse;
    }

    public boolean isInitialized() {
        return podState != null;
    }

    public String getPodStateAsString() {
        return podState.toString();
    }

    private BasalSchedule createStubBasalSchedule() {
        List<BasalScheduleEntry> basals = new ArrayList<>();
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(30)));    //00:00-00:30
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(30)));   //00:30-01:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(30)));    //01:00-01:30
        basals.add(new BasalScheduleEntry(11.5, Duration.standardMinutes(30))); //01:30-02:00
        basals.add(new BasalScheduleEntry(2, Duration.standardMinutes(30)));    //20:00-02:30
        basals.add(new BasalScheduleEntry(12.5, Duration.standardMinutes(30))); //02:30-03:00
        basals.add(new BasalScheduleEntry(3, Duration.standardMinutes(30)));    //03:00-03:30
        basals.add(new BasalScheduleEntry(13.5, Duration.standardMinutes(30))); //03:30-04:00
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(30)));    //04:00-04:30
        basals.add(new BasalScheduleEntry(14.5, Duration.standardMinutes(30))); //04:30-05:00
        basals.add(new BasalScheduleEntry(5, Duration.standardMinutes(30)));    //05:00-05:30
        basals.add(new BasalScheduleEntry(15.5, Duration.standardMinutes(30))); //05:30-06:00
        basals.add(new BasalScheduleEntry(6, Duration.standardMinutes(30)));    //06:00-06:30
        basals.add(new BasalScheduleEntry(16.5, Duration.standardMinutes(30))); //06:30-07:00
        basals.add(new BasalScheduleEntry(7, Duration.standardMinutes(30)));    //07:00-07:30
        basals.add(new BasalScheduleEntry(17.5, Duration.standardMinutes(30))); //07:30-08:00
        basals.add(new BasalScheduleEntry(8, Duration.standardMinutes(30)));    //08:00-08:30
        basals.add(new BasalScheduleEntry(18.5, Duration.standardMinutes(30))); //08:30-09:00
        basals.add(new BasalScheduleEntry(9, Duration.standardMinutes(30)));    //09:00-09:30
        basals.add(new BasalScheduleEntry(19.5, Duration.standardMinutes(30))); //09:30-10:00
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(30)));   //10:00-10:30
        basals.add(new BasalScheduleEntry(1.05, Duration.standardMinutes(30))); //10:30-11:00
        basals.add(new BasalScheduleEntry(11, Duration.standardMinutes(11 * 60)));//11:00-22:00
        basals.add(new BasalScheduleEntry(1.15, Duration.standardMinutes(2 * 60)));//22:00-24:00
        return new BasalSchedule(basals);
    }
}