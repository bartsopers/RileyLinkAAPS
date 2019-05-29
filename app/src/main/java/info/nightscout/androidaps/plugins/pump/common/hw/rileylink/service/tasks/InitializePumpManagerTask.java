package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.tasks;

import android.util.Log;

import com.gxwtech.roundtrip2.RT2Const;

import info.nightscout.androidaps.logging.L;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs.RileyLinkError;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs.RileyLinkServiceState;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceNotification;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.utils.SP;

/**
 * Created by geoff on 7/9/16.
 * <p>
 * This class is intended to be run by the Service, for the Service. Not intended for clients to run.
 */
public class InitializePumpManagerTask extends ServiceTask {

    private static final String TAG = "InitPumpManagerTask";
    private RileyLinkTargetDevice targetDevice;


    public InitializePumpManagerTask(RileyLinkTargetDevice targetDevice) {
        super();
        this.targetDevice = targetDevice;
    }


    public InitializePumpManagerTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {

        double lastGoodFrequency = SP.getDouble(RileyLinkConst.Prefs.LastGoodDeviceFrequency, 0.0d);
        lastGoodFrequency = Math.round(lastGoodFrequency * 1000d) / 1000d;

        RileyLinkUtil.getRileyLinkServiceData().lastGoodFrequency = lastGoodFrequency;

        if ((lastGoodFrequency > 0.0d)
            && RileyLinkUtil.getRileyLinkCommunicationManager().isValidFrequency(lastGoodFrequency)) {

            RileyLinkUtil.setServiceState(RileyLinkServiceState.RileyLinkReady);

            if (L.isEnabled(L.PUMPCOMM))
                Log.i(TAG, String.format("Setting radio frequency to %.2fMHz", lastGoodFrequency));

            RileyLinkUtil.getRileyLinkCommunicationManager().setRadioFrequencyForPump(lastGoodFrequency);

            boolean foundThePump = RileyLinkUtil.getRileyLinkCommunicationManager().tryToConnectToDevice();

            if (foundThePump) {
                RileyLinkUtil.setServiceState(RileyLinkServiceState.PumpConnectorReady);
                RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpFound), null);
            } else {
                RileyLinkUtil.setServiceState(RileyLinkServiceState.PumpConnectorError,
                    RileyLinkError.NoContactWithDevice);
                RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpLost), null);
            }

            RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_note_Idle), null);
        } else {
            RileyLinkUtil.sendBroadcastMessage(RT2Const.IPC.MSG_PUMP_tunePump);
        }
    }
}
