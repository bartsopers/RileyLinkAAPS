package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.FirmwareVersion;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

public class PodState {

    public int address;
    public DateTime activatedAt;
    public FirmwareVersion piVersion;
    public FirmwareVersion PmVersion;
    public int lot;
    public int tid;
    public int messageNumber;
    public int packetNumber;
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
}
