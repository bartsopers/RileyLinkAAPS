package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.DateTime;

import java.util.Random;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.InitializePodService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;

public class InitializePodAction implements OmnipodAction<PodSessionState> {
    private final int address;
    private final InitializePodService service;

    public InitializePodAction(InitializePodService service, int address) {
        this.service = service;
        this.address = address;
    }

    public InitializePodAction(InitializePodService service) {
        this(service, generateRandomAddress());
    }

    @Override
    public PodSessionState execute(OmnipodCommunicationService communicationService) {
        PodSetupState setupState = new PodSetupState(address, 0x00, 0x00);

        VersionResponse assignAddressResponse = service.executeAssignAddressCommand(communicationService, setupState);

        DateTime activationDate = DateTime.now();

        VersionResponse confirmPairingResponse = service.executeConfirmPairingCommand(communicationService, setupState,
                assignAddressResponse.getLot(), assignAddressResponse.getTid(), activationDate);

        PodSessionState podState = new PodSessionState(address, activationDate, confirmPairingResponse.getPiVersion(),
                confirmPairingResponse.getPmVersion(), confirmPairingResponse.getLot(), confirmPairingResponse.getTid(),
                setupState.getPacketNumber(), setupState.getMessageNumber());

        service.executeConfigureLowReservoirAlertCommand(communicationService, podState);
        service.executeConfigureInsertionAlertCommand(communicationService, podState);
        service.executePrimeBolus(communicationService, podState);

        return podState;
    }

    private static int generateRandomAddress() {
        return 0x1f000000 | (new Random().nextInt() & 0x000fffff);
    }
}
