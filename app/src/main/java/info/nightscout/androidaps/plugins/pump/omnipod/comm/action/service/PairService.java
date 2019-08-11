package info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service;

import org.joda.time.DateTime;

import java.util.Collections;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigurePodCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.OmnipodException;

public class PairService {
    public VersionResponse executeAssignAddressCommand(OmnipodCommunicationService communicationService, PodSetupState setupState) {
        AssignAddressCommand assignAddress = new AssignAddressCommand(setupState.getAddress());
        OmnipodMessage assignAddressMessage = new OmnipodMessage(Constants.DEFAULT_ADDRESS,
                Collections.singletonList(assignAddress), setupState.getMessageNumber());

        return communicationService.exchangeMessages(VersionResponse.class, setupState, assignAddressMessage,
                Constants.DEFAULT_ADDRESS, setupState.getAddress());
    }

    public VersionResponse executeConfigurePodCommand(OmnipodCommunicationService communicationService,
                                                      PodSetupState setupState, int lot, int tid, DateTime activationDate) {
        // at this point for an unknown reason PDM starts counting messages from 0 again
        setupState.setMessageNumber(0x00);

        ConfigurePodCommand configurePodCommand = new ConfigurePodCommand(setupState.getAddress(), activationDate,
                lot, tid);
        OmnipodMessage message = new OmnipodMessage(Constants.DEFAULT_ADDRESS,
                Collections.singletonList(configurePodCommand), setupState.getMessageNumber());
        VersionResponse configurePodResponse = communicationService.exchangeMessages(VersionResponse.class, setupState,
                message, Constants.DEFAULT_ADDRESS, setupState.getAddress());

        if (configurePodResponse.getPodProgressStatus() != PodProgressStatus.PAIRING_SUCCESS) {
            throw new OmnipodException("Pairing failed, state: " + configurePodResponse.getPodProgressStatus().name());
        }

        return configurePodResponse;
    }
}
