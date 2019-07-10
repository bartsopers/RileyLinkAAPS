package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.Duration;

import java.util.Arrays;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BasalScheduleExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class SetBasalScheduleAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final BasalSchedule schedule;
    private final boolean confidenceReminder;
    private final Duration scheduleOffset;
    private final Duration programReminderInterval;

    public SetBasalScheduleAction(PodSessionState podState, BasalSchedule schedule,
                                  boolean confidenceReminder, Duration scheduleOffset, Duration programReminderInterval) {
        this.podState = podState;
        this.schedule = schedule;
        this.confidenceReminder = confidenceReminder;
        this.scheduleOffset = scheduleOffset;
        this.programReminderInterval = programReminderInterval;
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        SetInsulinScheduleCommand setBasal = new SetInsulinScheduleCommand(podState.getCurrentNonce(), schedule, scheduleOffset);
        BasalScheduleExtraCommand extraCommand = new BasalScheduleExtraCommand(schedule, scheduleOffset,
                true, confidenceReminder, programReminderInterval);
        OmnipodMessage basalMessage = new OmnipodMessage(podState.getAddress(), Arrays.asList(setBasal, extraCommand),
                podState.getMessageNumber());

        return communicationService.exchangeMessages(podState, basalMessage);
    }
}
