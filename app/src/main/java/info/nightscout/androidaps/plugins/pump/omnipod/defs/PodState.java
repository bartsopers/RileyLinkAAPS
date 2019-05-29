package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.FirmwareVersion;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

public class PodState {

    public int Address;
    public DateTime ActivatedAt;
    public FirmwareVersion PiVersion;
    public FirmwareVersion PmVersion;
    public int Lot;
    public int Tid;
    public int messageNumber;
    public int packetNumber;
    private NonceState nonceState;

    public PodState(int address, DateTime activatedAt,FirmwareVersion piVersion, FirmwareVersion pmVersion, int lot, int tid, int packetNumber, int messageNumber) {
        this.Address = address;
        this.ActivatedAt = activatedAt;
        this.PiVersion = piVersion;
        this.PmVersion = pmVersion;
        this.Lot = lot;
        this.Tid = tid;
        this.packetNumber = packetNumber;
        this.messageNumber = messageNumber;
        this.nonceState = new NonceState(lot, tid);
    }

    public void resyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
        int sum = (sentNonce & 0xFFFF)
                + OmniCRC.crc16lookup[sequenceNumber]
                + (this.Lot & 0xFFFF)
                + (this.Tid & 0xFFFF);
        int seed = ((sum & 0xFFFF) ^ syncWord);
        this.nonceState = new NonceState(Lot, Tid, (byte)(seed & 0xFF));
    }

    public int getCurrentNonce() {
        return nonceState.getCurrentNonce();
    }

    public void advanceToNextNonce() {
        nonceState.advanceToNextNonce();
    }
}
