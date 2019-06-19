package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.joda.time.Duration;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSet;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.DeliveryStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressStatus;

public class StatusResponse extends MessageBlock {
    private static final int MINIMUM_MESSAGE_LENGTH = 10;

    public final DeliveryStatus deliveryStatus;
    public final PodProgressStatus podProgressStatus;
    public final Duration activeTime;
    public final double insulin;
    public final double insulinNotDelivered;
    public final byte podMessageCounter;
    public final AlertSet alerts;
    public final double reservoirLevel;

    public StatusResponse(byte[] encodedData) {
        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        this.deliveryStatus = DeliveryStatus.fromByte((byte) ((encodedData[1] & 0xF0) >>> 4));
        this.podProgressStatus = PodProgressStatus.fromByte((byte) (encodedData[1] & 0x0F));
        int minutes = ((encodedData[7] & 0x7F) << 6) | ((encodedData[8] & 0xFC) >>> 2);
        this.activeTime = Duration.standardMinutes(minutes);

        int highInsulinBits = (encodedData[2] & 0x0F) << 9;
        int middleInsulinBits = (encodedData[3] & 0xFF) << 1;
        int lowInsulinBits = (encodedData[4] & 0x80) >>> 7;
        this.insulin = Constants.POD_PULSE_SIZE * (highInsulinBits | middleInsulinBits | lowInsulinBits);
        this.podMessageCounter = (byte) ((encodedData[4] & 0x78) >>> 3);

        this.insulinNotDelivered = Constants.POD_PULSE_SIZE * (((encodedData[4] & 0x03) << 8) | (encodedData[5] & 0xFF));
        this.alerts = new AlertSet((byte) (((encodedData[6] & 0x7f) << 1) | (encodedData[7] >> 7)));

        int resHighBits = ((encodedData[8] & 0x03) << 6);
        int resLowBits = ((encodedData[9] & 0xFC) >>> 2);

        this.reservoirLevel = Math.round((double)((resHighBits | resLowBits)) * 50 / 255);

        this.encodedData = ByteUtil.substring(encodedData, 1, 9);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.STATUS_RESPONSE;
    }
}
