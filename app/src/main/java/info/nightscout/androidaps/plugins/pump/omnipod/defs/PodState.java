package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

public class PodState {

    private final int address;
    private final DateTime activatedAt;
    private final FirmwareVersion piVersion;
    private final FirmwareVersion PmVersion;
    private final int lot;
    private final int tid;

    private int messageNumber;
    private int packetNumber;
    private NonceState nonceState;

    public PodState(int address, DateTime activatedAt, FirmwareVersion piVersion,
                    FirmwareVersion pmVersion, int lot, int tid, int packetNumber, int messageNumber) {
        this.address = address;
        this.activatedAt = activatedAt;
        this.piVersion = piVersion;
        this.PmVersion = pmVersion;
        this.lot = lot;
        this.tid = tid;
        this.packetNumber = packetNumber;
        this.messageNumber = messageNumber;
        this.nonceState = new NonceState(lot, tid);
    }

    public int getAddress() {
        return address;
    }

    public DateTime getActivatedAt() {
        return activatedAt;
    }

    public FirmwareVersion getPiVersion() {
        return piVersion;
    }

    public FirmwareVersion getPmVersion() {
        return PmVersion;
    }

    public int getLot() {
        return lot;
    }

    public int getTid() {
        return tid;
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

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public void resyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
        int sum = (sentNonce & 0xFFFF)
                + OmniCRC.crc16lookup[sequenceNumber]
                + (this.lot & 0xFFFF)
                + (this.tid & 0xFFFF);
        int seed = ((sum & 0xFFFF) ^ syncWord);

        this.nonceState = new NonceState(lot, tid, (byte)(seed & 0xFF));
    }

    public int getCurrentNonce() {
        return nonceState.getCurrentNonce();
    }

    public void advanceToNextNonce() {
        nonceState.advanceToNextNonce();
    }

    @Override
    public String toString() {
        return "PodState{" +
                "address=" + address +
                ", activatedAt=" + activatedAt +
                ", piVersion=" + piVersion +
                ", PmVersion=" + PmVersion +
                ", lot=" + lot +
                ", tid=" + tid +
                ", messageNumber=" + messageNumber +
                ", packetNumber=" + packetNumber +
                ", nonce=" + nonceState.getCurrentNonce() +
                '}';
    }
}
