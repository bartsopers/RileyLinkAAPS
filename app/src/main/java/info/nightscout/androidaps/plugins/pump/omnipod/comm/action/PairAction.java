package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import org.joda.time.DateTime;

import java.util.Random;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.service.PairService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSetupState;

public class PairAction implements OmnipodAction<PodSessionState> {
    private final PairService service;
    private final int address;

    public PairAction(PairService service, int address) {
        if(service == null) {
            throw new IllegalArgumentException("service cannot be null");
        }
        this.service = service;
        this.address = address;
    }

    public PairAction(PairService service) {
        this(service, generateRandomAddress());
    }

    @Override
    public PodSessionState execute(OmnipodCommunicationService communicationService) {
        PodSetupState setupState = new PodSetupState(address, 0x00, 0x00);

        VersionResponse assignAddressResponse = service.executeAssignAddressCommand(communicationService, setupState);

        DateTime activationDate = DateTime.now();

        VersionResponse confirmPairingResponse = service.executeConfigurePodCommand(communicationService, setupState,
                assignAddressResponse.getLot(), assignAddressResponse.getTid(), activationDate);

        PodSessionState podState = new PodSessionState(address, activationDate, confirmPairingResponse.getPiVersion(),
                confirmPairingResponse.getPmVersion(), confirmPairingResponse.getLot(), confirmPairingResponse.getTid(),
                setupState.getPacketNumber(), setupState.getMessageNumber());
        podState.setSetupProgress(SetupProgress.POD_CONFIGURED);

        return podState;
    }

    private static int generateRandomAddress() {
        return 0x1f000000 | (new Random().nextInt() & 0x000fffff);
    }
}
