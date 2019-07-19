package info.nightscout.androidaps.plugins.pump.omnipod.defs.state;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.FirmwareVersion;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.NonceState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.SetupProgress;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;
import info.nightscout.androidaps.plugins.pump.omnipod.util.Utils;
import info.nightscout.androidaps.utils.SP;

public class PodSessionState extends PodState {
    private final DateTime activatedAt;
    private final FirmwareVersion piVersion;
    private final FirmwareVersion PmVersion;
    private final int lot;
    private final int tid;

    private NonceState nonceState;
    private SetupProgress setupProgress;

    public PodSessionState(int address, DateTime activatedAt, FirmwareVersion piVersion,
                           FirmwareVersion pmVersion, int lot, int tid, int packetNumber, int messageNumber) {
        super(address, messageNumber, packetNumber);
        if (setupProgress == null) {
            throw new IllegalArgumentException("Setup progress cannot be null");
        }
        this.setupProgress = SetupProgress.ADDRESS_ASSIGNED;
        this.activatedAt = activatedAt;
        this.piVersion = piVersion;
        this.PmVersion = pmVersion;
        this.lot = lot;
        this.tid = tid;
        this.nonceState = new NonceState(lot, tid);
        store();
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

    public void resyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
        int sum = (sentNonce & 0xFFFF)
                + OmniCRC.crc16lookup[sequenceNumber]
                + (this.lot & 0xFFFF)
                + (this.tid & 0xFFFF);
        int seed = ((sum & 0xFFFF) ^ syncWord);

        this.nonceState = new NonceState(lot, tid, (byte) (seed & 0xFF));
    }

    public int getCurrentNonce() {
        return nonceState.getCurrentNonce();
    }

    public void advanceToNextNonce() {
        nonceState.advanceToNextNonce();
    }

    public SetupProgress getSetupProgress() {
        return setupProgress;
    }

    public void setSetupProgress(SetupProgress setupProgress) {
        if (setupProgress == null) {
            throw new IllegalArgumentException("Setup state cannot be null");
        }
        this.setupProgress = setupProgress;
        store();
    }

    public boolean hasNonceState() {
        return true;
    }

    @Override
    public String toString() {
        return "PodSessionState{" +
                "address=" + getAddress() +
                ", activatedAt=" + activatedAt +
                ", piVersion=" + piVersion +
                ", PmVersion=" + PmVersion +
                ", lot=" + lot +
                ", tid=" + tid +
                ", setupProgress="+ setupProgress.name() +
                ", messageNumber=" + getMessageNumber() +
                ", packetNumber=" + getPacketNumber() +
                ", nonce=" + nonceState.getCurrentNonce() +
                '}';
    }

    @Override
    public void setPacketNumber(int packetNumber) {
        super.setPacketNumber(packetNumber);
        store();
    }

    @Override
    public void setMessageNumber(int messageNumber) {
        super.setMessageNumber(messageNumber);
        store();
    }

    public void updateFromStatusResponse(StatusResponse statusResponse) {
        // TODO
    }

    private void store() {
        Gson gson = Utils.gsonDateTime();
        SP.putString(OmniPodConst.Prefs.POD_STATE, gson.toJson(this));
    }
}
