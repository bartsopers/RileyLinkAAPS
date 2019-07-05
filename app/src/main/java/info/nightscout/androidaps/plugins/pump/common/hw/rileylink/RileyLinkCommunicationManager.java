package info.nightscout.androidaps.plugins.pump.common.hw.rileylink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import info.nightscout.androidaps.logging.L;
import info.nightscout.androidaps.plugins.pump.common.data.PumpStatus;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkCommunicationException;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.FrequencyScanResults;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.FrequencyTrial;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RFSpyResponse;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RadioPacket;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RadioResponse;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkBLEError;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkServiceData;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.ServiceTaskExecutor;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks.WakeAndTuneTask;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.medtronic.defs.PumpDeviceState;
import info.nightscout.androidaps.plugins.pump.medtronic.util.MedtronicUtil;
import info.nightscout.androidaps.utils.SP;

/**
 * This is abstract class for RileyLink Communication, this one needs to be extended by specific "Pump" class.
 * <p>
 * Created by andy on 5/10/18.
 */
public abstract class RileyLinkCommunicationManager {

    private static final Logger LOG = LoggerFactory.getLogger(L.PUMPCOMM);

    private static final int SCAN_TIMEOUT = 1500;
    private static final int ALLOWED_PUMP_UNREACHABLE = 10 * 60 * 1000; // 10 minutes

    protected final RFSpy rfspy;
    protected final Context context;
    protected int receiverDeviceAwakeForMinutes = 1; // override this in constructor of specific implementation
    protected String receiverDeviceID; // String representation of receiver device (ex. Pump (xxxxxx) or Pod (yyyyyy))
    protected long lastGoodReceiverCommunicationTime = 0;
    protected PumpStatus pumpStatus;
    protected RileyLinkServiceData rileyLinkServiceData;
    private long nextWakeUpRequired = 0L;

    // internal flag
    private boolean showPumpMessages = true;
    private int timeoutCount = 0;


    public RileyLinkCommunicationManager(Context context, RFSpy rfspy) {
        this.context = context;
        this.rfspy = rfspy;
        this.rileyLinkServiceData = RileyLinkUtil.getRileyLinkServiceData();
        RileyLinkUtil.setRileyLinkCommunicationManager(this);

        configurePumpSpecificSettings();
    }


    protected abstract void configurePumpSpecificSettings();

    protected <E extends RLMessage> E sendAndListen(RLMessage msg, int timeout_ms, Class<E> clazz)
            throws RileyLinkCommunicationException {
        return sendAndListen(msg, timeout_ms, null, clazz);
    }

    protected <E extends RLMessage> E sendAndListen(RLMessage msg, int timeout_ms, Integer extendPreamble_ms, Class<E> clazz)
            throws RileyLinkCommunicationException {
        return sendAndListen(msg, timeout_ms, 0, extendPreamble_ms, clazz);
    }

    // All pump communications go through this function.
    protected <E extends RLMessage> E sendAndListen(RLMessage msg, int timeout_ms, int repeatCount, Integer extendPreamble_ms, Class<E> clazz)
            throws RileyLinkCommunicationException {

        if (showPumpMessages) {
            if (isLogEnabled())
                LOG.info("Sent:" + ByteUtil.shortHexString(msg.getTxData()));
        }

        RFSpyResponse rfSpyResponse = rfspy.transmitThenReceive(new RadioPacket(msg.getTxData()), (byte)0, (byte)repeatCount, (byte)0, (byte)0, timeout_ms, (byte)0, extendPreamble_ms);

        RadioResponse radioResponse = rfSpyResponse.getRadioResponse();

        E response = createResponseMessage(rfSpyResponse.getRadioResponse().getPayload(), clazz);

        if (response.isValid()) {
            // Mark this as the last time we heard from the pump.
            rememberLastGoodDeviceCommunicationTime();
        } else {
            if (isLogEnabled())
                LOG.warn("Response is invalid ! [interrupted={}, timeout={}, unknownCommand={}, invalidParam={}]", rfSpyResponse.wasInterrupted(),
                rfSpyResponse.wasTimeout(), rfSpyResponse.isUnknownCommand(), rfSpyResponse.isInvalidParam());

            if (rfSpyResponse.wasTimeout()) {
                timeoutCount++;

                long diff = System.currentTimeMillis() - pumpStatus.lastConnection;

                if (diff > ALLOWED_PUMP_UNREACHABLE) {
                    if (isLogEnabled())
                        LOG.warn("We reached max time that Pump can be unreachable. Starting Tuning.");
                    //ServiceTaskExecutor.startTask(new WakeAndTuneTask());
                    timeoutCount = 0;
                }

                throw new RileyLinkCommunicationException(RileyLinkBLEError.Timeout);
            } else if (rfSpyResponse.wasInterrupted()) {
                throw new RileyLinkCommunicationException(RileyLinkBLEError.Interrupted);
            }
        }

        if (showPumpMessages) {
            if (isLogEnabled())
                LOG.info("Received:" + ByteUtil.shortHexString(rfSpyResponse.getRadioResponse().getPayload()));
        }

        return response;
    }


    public abstract <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz);


    public void wakeUp(boolean force) {
        wakeUp(receiverDeviceAwakeForMinutes, force);
    }

    // FIXME change wakeup
    // TODO we might need to fix this. Maybe make pump awake for shorter time (battery factor for pump) - Andy
    public void wakeUp(int duration_minutes, boolean force) {
        // If it has been longer than n minutes, do wakeup. Otherwise assume pump is still awake.
        // **** FIXME: this wakeup doesn't seem to work well... must revisit
        // receiverDeviceAwakeForMinutes = duration_minutes;

        MedtronicUtil.setPumpDeviceState(PumpDeviceState.WakingUp);

        if (force)
            nextWakeUpRequired = 0L;

        if (System.currentTimeMillis() > nextWakeUpRequired) {
            if (isLogEnabled())
                LOG.info("Waking pump...");

            byte[] pumpMsgContent = createPumpMessageContent(RLMessageType.ReadSimpleData); // simple
            RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(pumpMsgContent), (byte)0, (byte)200,
                (byte)0, (byte)0, 25000, (byte)0);
            if (isLogEnabled())
                LOG.info("wakeup: raw response is " + ByteUtil.shortHexString(resp.getRaw()));

            // FIXME wakeUp successful !!!!!!!!!!!!!!!!!!

            nextWakeUpRequired = System.currentTimeMillis() + (receiverDeviceAwakeForMinutes * 60 * 1000);
        } else {
            if (isLogEnabled())
                LOG.trace("Last pump communication was recent, not waking pump.");
        }

        // long lastGoodPlus = getLastGoodReceiverCommunicationTime() + (receiverDeviceAwakeForMinutes * 60 * 1000);
        //
        // if (System.currentTimeMillis() > lastGoodPlus || force) {
        // LOG.info("Waking pump...");
        //
        // byte[] pumpMsgContent = createPumpMessageContent(RLMessageType.PowerOn);
        // RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(pumpMsgContent), (byte) 0, (byte) 200, (byte)
        // 0, (byte) 0, 15000, (byte) 0);
        // LOG.info("wakeup: raw response is " + ByteUtil.shortHexString(resp.getRaw()));
        // } else {
        // LOG.trace("Last pump communication was recent, not waking pump.");
        // }
    }


    public void setRadioFrequencyForPump(double freqMHz) {
        rfspy.setBaseFrequency(freqMHz);
    }


    public double tuneForDevice() {
        return scanForDevice(RileyLinkUtil.getRileyLinkTargetFrequency().getScanFrequencies());
    }


    /**
     * If user changes pump and one pump is running in US freq, and other in WW, then previously set frequency would be
     * invalid,
     * so we would need to retune. This checks that saved frequency is correct range.
     *
     * @param frequency
     * @return
     */
    public boolean isValidFrequency(double frequency) {

        double[] scanFrequencies = RileyLinkUtil.getRileyLinkTargetFrequency().getScanFrequencies();

        if (scanFrequencies.length == 1) {
            return RileyLinkUtil.isSame(scanFrequencies[0], frequency);
        } else {
            return (scanFrequencies[0] <= frequency && scanFrequencies[scanFrequencies.length - 1] >= frequency);
        }
    }


    /**
     * Do device connection, with wakeup
     *
     * @return
     */
    public abstract boolean tryToConnectToDevice();


    public double scanForDevice(double[] frequencies) {
        if (isLogEnabled())
            LOG.info("Scanning for receiver ({})", receiverDeviceID);
        wakeUp(receiverDeviceAwakeForMinutes, false);
        FrequencyScanResults results = new FrequencyScanResults();

        for (int i = 0; i < frequencies.length; i++) {
            int tries = 3;
            FrequencyTrial trial = new FrequencyTrial();
            trial.frequencyMHz = frequencies[i];
            rfspy.setBaseFrequency(frequencies[i]);

            int sumRSSI = 0;
            for (int j = 0; j < tries; j++) {

                byte[] pumpMsgContent = createPumpMessageContent(RLMessageType.ReadSimpleData);
                RFSpyResponse resp = rfspy.transmitThenReceive(new RadioPacket(pumpMsgContent), (byte)0, (byte)0,
                    (byte)0, (byte)0, 1000, (byte)0);
                if (resp.wasTimeout()) {
                    if (isLogEnabled())
                        LOG.error("scanForPump: Failed to find pump at frequency {}", frequencies[i]);
                } else if (resp.looksLikeRadioPacket()) {
                    RadioResponse radioResponse = new RadioResponse();

                    try {

                        radioResponse.init(resp.getRaw());

                        if (radioResponse.isValid()) {
                            sumRSSI += radioResponse.rssi;
                            trial.rssiList.add(radioResponse.rssi);
                            trial.successes++;
                        } else {
                            if (isLogEnabled())
                                LOG.warn("Failed to parse radio response: " + ByteUtil.shortHexString(resp.getRaw()));
                            trial.rssiList.add(-99);
                        }

                    } catch (RileyLinkCommunicationException rle) {
                        if (isLogEnabled())
                            LOG.warn("Failed to decode radio response: " + ByteUtil.shortHexString(resp.getRaw()));
                        trial.rssiList.add(-99);
                    }

                } else {
                    if (isLogEnabled())
                        LOG.error("scanForPump: raw response is " + ByteUtil.shortHexString(resp.getRaw()));
                    trial.rssiList.add(-99);
                }
                trial.tries++;
            }
            sumRSSI += -99.0 * (trial.tries - trial.successes);
            trial.averageRSSI2 = (double)(sumRSSI) / (double)(trial.tries);

            trial.calculateAverage();

            results.trials.add(trial);
        }

        results.dateTime = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder("Scan results:\n");

        for (int k = 0; k < results.trials.size(); k++) {
            FrequencyTrial one = results.trials.get(k);

            stringBuilder.append(String.format("Scan Result[%s]: Freq=%s, avg RSSI = %s\n", "" + k, ""
                + one.frequencyMHz, "" + one.averageRSSI + ", RSSIs =" + one.rssiList + ", averageRSSI_Old="
                + one.averageRSSI2));
        }

        LOG.info(stringBuilder.toString());

        results.sort(); // sorts in ascending order

        FrequencyTrial bestTrial = results.trials.get(results.trials.size() - 1);
        results.bestFrequencyMHz = bestTrial.frequencyMHz;
        if (bestTrial.successes > 0) {
            rfspy.setBaseFrequency(results.bestFrequencyMHz);
            if (isLogEnabled())
                LOG.debug("Best frequency found: " + results.bestFrequencyMHz);
            return results.bestFrequencyMHz;
        } else {
            if (isLogEnabled())
                LOG.error("No pump response during scan.");
            return 0.0;
        }
    }


    public abstract byte[] createPumpMessageContent(RLMessageType type);

    protected void rememberLastGoodDeviceCommunicationTime() {
        lastGoodReceiverCommunicationTime = System.currentTimeMillis();

        SP.putLong(RileyLinkConst.Prefs.LastGoodDeviceCommunicationTime, lastGoodReceiverCommunicationTime);

        if(pumpStatus != null) {
            pumpStatus.setLastCommunicationToNow();
        }
    }


    public void clearNotConnectedCount() {
        if (rfspy != null) {
            rfspy.notConnectedCount = 0;
        }
    }

    private boolean isLogEnabled() {
        return L.isEnabled(L.PUMPCOMM);
    }

}
