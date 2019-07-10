package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationException;
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
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.TimerAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.UnitsRemainingAlertTrigger;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;

public class InitializePodAction implements OmnipodAction<PodSessionState> {
    // FIXME extract methods
    @Override
    public PodSessionState execute(OmnipodCommunicationService communicationService) {
        PodSetupState setupState = new PodSetupState(Constants.DEFAULT_ADDRESS, 0x00, 0x00);

        int newAddress = generateRandomAddress();

        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(Constants.DEFAULT_ADDRESS, Collections.singletonList(assignAddress), setupState.getMessageNumber());

        VersionResponse configResponse = communicationService.exchangeMessages(setupState, assignAddressMessage, Constants.DEFAULT_ADDRESS, newAddress);
        if (configResponse == null) {
            throw new OmnipodCommunicationException("config is null (timeout or wrong answer)");
        }

        DateTime activationDate = DateTime.now();

        //at this point for an unknown reason PDM starts counting messages from 0 again
        setupState.setMessageNumber(0x00);
        ConfigurePodCommand confirmPairing = new ConfigurePodCommand(newAddress, activationDate,
                configResponse.lot, configResponse.tid);
        OmnipodMessage confirmPairingMessage = new OmnipodMessage(Constants.DEFAULT_ADDRESS,
                Collections.singletonList(confirmPairing), setupState.getMessageNumber());
        VersionResponse config2 = communicationService.exchangeMessages(setupState, confirmPairingMessage, Constants.DEFAULT_ADDRESS, newAddress);

        if (config2.podProgressStatus != PodProgressStatus.PAIRING_SUCCESS) {
            throw new OmnipodCommunicationException("Pairing failed, state: " + config2.podProgressStatus.name());
        }

        PodSessionState podState = new PodSessionState(newAddress, activationDate, config2.piVersion,
                config2.pmVersion, config2.lot, config2.tid, setupState.getPacketNumber(), setupState.getMessageNumber());

        UnitsRemainingAlertTrigger lowReservoirAlertTrigger = new UnitsRemainingAlertTrigger(50.0D);

        AlertConfiguration lowReservoirAlertConfiguration =
                new AlertConfiguration(AlertType.LOW_RESERVOIR, true, false, Duration.ZERO,
                        lowReservoirAlertTrigger, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_60_MINUTES);

        int nonce = podState.getCurrentNonce();

        ConfigureAlertsCommand lowReservoirCommand = new ConfigureAlertsCommand(nonce, Collections.singletonList(lowReservoirAlertConfiguration));
        communicationService.sendCommand(podState, lowReservoirCommand);
        podState.advanceToNextNonce();

        TimerAlertTrigger insertionAlertTrigger = new TimerAlertTrigger(Duration.standardDays(5));

        AlertConfiguration insertionTimer = new AlertConfiguration(AlertType.TIMER_LIMIT, true, false,
                Duration.standardMinutes(55), insertionAlertTrigger, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_5_MINUTES);

        ConfigureAlertsCommand insertionTimerCommand = new ConfigureAlertsCommand(podState.getCurrentNonce(), Collections.singletonList(insertionTimer));
        communicationService.sendCommand(podState, insertionTimerCommand);
        podState.advanceToNextNonce();

        double primeUnits = 2.6;
        BolusDeliverySchedule primeBolus = new BolusDeliverySchedule(primeUnits, Duration.standardSeconds(1));
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(podState.getCurrentNonce(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits);
        OmnipodMessage prime = new OmnipodMessage(newAddress, Arrays.asList(primeCommand, extraBolusCommand), podState.getMessageNumber());
        communicationService.exchangeMessages(podState, prime);
        podState.advanceToNextNonce();

        return podState;
    }

    private int generateRandomAddress() {
        return 0x1f000000 | (new Random().nextInt() & 0x000fffff);
    }
}
