package info.nightscout.androidaps.plugins.pump.omnipod.defs.state;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;

public abstract class PodState {
    private final int address;
    private int packetNumber;
    private int messageNumber;

    public PodState(int address, int packetNumber, int messageNumber) {
        this.address = address;
        this.packetNumber = packetNumber;
        this.messageNumber = messageNumber;
    }

    public abstract boolean hasNonceState();

    public abstract int getCurrentNonce();

    public abstract void advanceToNextNonce();

    public abstract void resyncNonce(int syncWord, int sentNonce, int sequenceNumber);

    public int getAddress() {
        return address;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public void increaseMessageNumber(int increment) {
        setMessageNumber((messageNumber + increment) & 0b1111);
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public void increasePacketNumber(int increment) {
        setPacketNumber((packetNumber + increment) & 0b11111);
    }
}
