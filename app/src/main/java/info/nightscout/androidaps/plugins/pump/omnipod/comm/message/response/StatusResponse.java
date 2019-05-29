package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodAlarm;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressState;

public class StatusResponse extends MessageBlock {
    public final DeliveryStatus deliveryStatus;
    public final PodProgressState podProgressState;
    public final Duration activeTime;
    public final double insulin;
    public final double insulinNotDelivered;
    public final byte podMessageCounter;
    public final PodAlarm alarms;
    public final double reservoirLevel;

    public StatusResponse(byte[] encodedData) {
        this.deliveryStatus = DeliveryStatus.fromByte((byte) ((encodedData[1] & 0xF0) >>> 4));
        this.podProgressState = PodProgressState.fromByte((byte) (encodedData[1] & 0x0F));
        int minutes = ((encodedData[7] & 0x7F) << 6) | ((encodedData[8] & 0xFC) >>> 2);
        this.activeTime = Duration.standardMinutes(minutes);

        int highInsulinBits = (encodedData[2] & 0x0F) << 9;
        int middleInsulinBits = (encodedData[3] & 0xFF) << 1;
        int lowInsulinBits = (encodedData[4] & 0x80) >>> 7;
        this.insulin = Constants.PodPulseSize * (highInsulinBits | middleInsulinBits | lowInsulinBits);
        this.podMessageCounter = (byte) ((encodedData[4] & 0x78) >>> 3);

        this.insulinNotDelivered = Constants.PodPulseSize * (((encodedData[4] & 0x03) << 8) | (encodedData[5] & 0xFF));
        this.alarms = new PodAlarm((byte) (((encodedData[6] & 0x7f) << 1) | ((encodedData[7] & 0x80) >>> 7)));

        int resHighBits = ((encodedData[8] & 0x03) << 6);
        int resLowBits = ((encodedData[9] & 0xFC) >>> 2);

        this.reservoirLevel = Math.round((double)((resHighBits | resLowBits)) * 50 / 255);

        this.encodedData = ByteUtil.substring(encodedData, 1, 9);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.StatusResponse;
    }
}
