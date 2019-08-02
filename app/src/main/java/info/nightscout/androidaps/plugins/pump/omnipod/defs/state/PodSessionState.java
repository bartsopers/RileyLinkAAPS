package info.nightscout.androidaps.plugins.pump.omnipod.defs.state;

import com.google.gson.Gson;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSet;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
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
    private final FirmwareVersion pmVersion;
    private final int lot;
    private final int tid;
    private boolean suspended;

    private NonceState nonceState;
    private SetupProgress setupProgress;
    private AlertSet activeAlerts;

    public PodSessionState(int address, DateTime activatedAt, FirmwareVersion piVersion,
                           FirmwareVersion pmVersion, int lot, int tid, int packetNumber, int messageNumber) {
        super(address, messageNumber, packetNumber);
        this.setupProgress = SetupProgress.ADDRESS_ASSIGNED;
        this.activatedAt = activatedAt;
        this.piVersion = piVersion;
        this.pmVersion = pmVersion;
        this.lot = lot;
        this.tid = tid;
        this.nonceState = new NonceState(lot, tid);
        suspended = false;
        store();
    }

    public DateTime getActivatedAt() {
        return activatedAt;
    }

    public FirmwareVersion getPiVersion() {
        return piVersion;
    }

    public FirmwareVersion getPmVersion() {
        return pmVersion;
    }

    public int getLot() {
        return lot;
    }

    public int getTid() {
        return tid;
    }

    public synchronized void resyncNonce(int syncWord, int sentNonce, int sequenceNumber) {
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

    public synchronized void advanceToNextNonce() {
        nonceState.advanceToNextNonce();
    }

    public SetupProgress getSetupProgress() {
        return setupProgress;
    }

    public synchronized void setSetupProgress(SetupProgress setupProgress) {
        if (setupProgress == null) {
            throw new IllegalArgumentException("Setup state cannot be null");
        }
        this.setupProgress = setupProgress;
        store();
    }
    public boolean isSuspended() {
        return suspended;
    }

    public boolean hasActiveAlerts() {
        return activeAlerts != null;
    }

    public AlertSet getActiveAlerts() {
        return activeAlerts;
    }

    public boolean hasNonceState() {
        return true;
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

    @Override
    public void updateFromStatusResponse(StatusResponse statusResponse) {
        suspended = (statusResponse.getDeliveryStatus() == DeliveryStatus.SUSPENDED);
        activeAlerts = statusResponse.getAlerts();
    }

    private void store() {
        Gson gson = Utils.gsonDateTime();
        SP.putString(OmniPodConst.Prefs.POD_STATE, gson.toJson(this));
    }

    @Override
    public String toString() {
        return "PodSessionState{" +
                "activatedAt=" + activatedAt +
                ", piVersion=" + piVersion +
                ", pmVersion=" + pmVersion +
                ", lot=" + lot +
                ", tid=" + tid +
                ", suspended=" + suspended +
                ", nonceState=" + nonceState +
                ", setupProgress=" + setupProgress +
                ", activeAlerts=" + activeAlerts +
                ", address=" + address +
                ", packetNumber=" + packetNumber +
                ", messageNumber=" + messageNumber +
                ", faultEvent=" + faultEvent +
                '}';
    }
}
