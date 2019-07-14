package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Arrays;
import java.util.Collections;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.OmnipodCommunicationException;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigureAlertsCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigurePodCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.TimerAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.UnitsRemainingAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;

public class InitializePodService {

    public VersionResponse executeAssignAddressCommand(OmnipodCommunicationService communicationService, PodSetupState setupState) {
        AssignAddressCommand assignAddress = new AssignAddressCommand(setupState.getAddress());
        OmnipodMessage assignAddressMessage = new OmnipodMessage(Constants.DEFAULT_ADDRESS,
                Collections.singletonList(assignAddress), setupState.getMessageNumber());

        return communicationService.exchangeMessages(setupState, assignAddressMessage, Constants.DEFAULT_ADDRESS, setupState.getAddress());
    }

    public VersionResponse executeConfirmPairingCommand(OmnipodCommunicationService communicationService,
                                                         PodSetupState setupState, int lot, int tid, DateTime activationDate) {
        //at this point for an unknown reason PDM starts counting messages from 0 again
        setupState.setMessageNumber(0x00);
        ConfigurePodCommand confirmPairing = new ConfigurePodCommand(setupState.getAddress(), activationDate,
                lot, tid);
        OmnipodMessage confirmPairingMessage = new OmnipodMessage(Constants.DEFAULT_ADDRESS,
                Collections.singletonList(confirmPairing), setupState.getMessageNumber());
        VersionResponse confirmPairingResponse = communicationService.exchangeMessages(setupState,
                confirmPairingMessage, Constants.DEFAULT_ADDRESS, setupState.getAddress());

        if (confirmPairingResponse.getPodProgressStatus() != PodProgressStatus.PAIRING_SUCCESS) {
            throw new OmnipodCommunicationException("Pairing failed, state: " + confirmPairingResponse.getPodProgressStatus().name());
        }

        return confirmPairingResponse;
    }

    public void executeConfigureLowReservoirAlertCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        UnitsRemainingAlertTrigger lowReservoirAlertTrigger = new UnitsRemainingAlertTrigger(50.0D);

        AlertConfiguration lowReservoirAlertConfiguration =
                new AlertConfiguration(AlertType.LOW_RESERVOIR, true, false, Duration.ZERO,
                        lowReservoirAlertTrigger, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP,
                        BeepRepeat.EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_60_MINUTES);

        int nonce = podState.getCurrentNonce();

        ConfigureAlertsCommand lowReservoirCommand = new ConfigureAlertsCommand(nonce, Collections.singletonList(lowReservoirAlertConfiguration));
        communicationService.sendCommand(podState, lowReservoirCommand);
        podState.advanceToNextNonce();
    }

    public void executeConfigureInsertionAlertCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        TimerAlertTrigger insertionAlertTrigger = new TimerAlertTrigger(Duration.standardDays(5));

        AlertConfiguration insertionTimer = new AlertConfiguration(AlertType.TIMER_LIMIT, true, false,
                Duration.standardMinutes(55), insertionAlertTrigger, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_5_MINUTES);

        ConfigureAlertsCommand insertionTimerCommand = new ConfigureAlertsCommand(podState.getCurrentNonce(), Collections.singletonList(insertionTimer));
        communicationService.sendCommand(podState, insertionTimerCommand);
        podState.advanceToNextNonce();
    }

    // TODO maybe we should replace this with a BolusAction?
    public void executePrimeBolusCommand(OmnipodCommunicationService communicationService, PodSessionState podState) {
        double primeUnits = 2.6;
        BolusDeliverySchedule primeBolus = new BolusDeliverySchedule(primeUnits, Duration.standardSeconds(1));
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(podState.getCurrentNonce(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits);
        OmnipodMessage prime = new OmnipodMessage(podState.getAddress(), Arrays.asList(primeCommand, extraBolusCommand), podState.getMessageNumber());
        communicationService.exchangeMessages(podState, prime);
        podState.advanceToNextNonce();
    }
}