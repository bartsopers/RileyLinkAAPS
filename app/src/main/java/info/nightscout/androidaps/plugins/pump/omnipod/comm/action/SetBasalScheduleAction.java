package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Arrays;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BasalScheduleExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class SetBasalScheduleAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final BasalSchedule schedule;
    private final boolean confidenceReminder;
    private final Duration scheduleOffset;

    public SetBasalScheduleAction(PodSessionState podState, BasalSchedule schedule,
                                  boolean confidenceReminder, Duration scheduleOffset) {
        this.podState = podState;
        this.schedule = schedule;
        this.confidenceReminder = confidenceReminder;
        this.scheduleOffset = scheduleOffset;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        SetInsulinScheduleCommand setBasal = new SetInsulinScheduleCommand(podState.getCurrentNonce(), schedule, scheduleOffset);
        BasalScheduleExtraCommand extraCommand = new BasalScheduleExtraCommand(schedule, scheduleOffset,
                true, confidenceReminder, Duration.ZERO);
        OmnipodMessage basalMessage = new OmnipodMessage(podState.getAddress(), Arrays.asList(setBasal, extraCommand),
                podState.getMessageNumber());

        return communicationService.exchangeMessages(StatusResponse.class, podState, basalMessage);
    }

    public static Duration calculateScheduleOffset() {
        DateTime now = DateTime.now();
        return new Duration(new DateTime(now.getYear(), now.getMonthOfYear(),
                now.getDayOfMonth(), 0, 0, 0), now);
    }
}
