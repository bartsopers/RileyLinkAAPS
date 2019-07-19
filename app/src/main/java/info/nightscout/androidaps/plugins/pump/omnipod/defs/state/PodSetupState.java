package info.nightscout.androidaps.plugins.pump.omnipod.defs.state;

public class PodSetupState extends PodState {
    public PodSetupState(int address, int packetNumber, int messageNumber) {
        super(address, packetNumber, messageNumber);
    }

    @Override
    public boolean hasNonceState() {
        return false;
    }

    @Override
    public int getCurrentNonce() {
        throw new UnsupportedOperationException("PodSetupState does not have a nonce state");
    }

    @Override
    public void advanceToNextNonce() {
        throw new UnsupportedOperationException("PodSetupState does not have a nonce state");
    }

    @Override
    public void resyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
        throw new UnsupportedOperationException("PodSetupState does not have a nonce state");
    }
}
