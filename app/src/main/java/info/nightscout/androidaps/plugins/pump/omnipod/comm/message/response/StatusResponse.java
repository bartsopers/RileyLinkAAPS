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
    public final Duration timeActive;
    public final Double reservoirLevel;
    public final double insulin;
    public final double insulinNotDelivered;
    public final byte podMessageCounter;
    public final AlertSet alerts;

    public StatusResponse(byte[] encodedData) {
        if(encodedData.length < MINIMUM_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Not enough data");
        }

        this.deliveryStatus = DeliveryStatus.fromByte((byte) (ByteUtil.convertUnsignedByteToInt(encodedData[1]) >>> 4));
        this.podProgressStatus = PodProgressStatus.fromByte((byte) (encodedData[1] & 0x0F));

        int minutes = ((encodedData[7] & 0x7F) << 6) | ((encodedData[8] & 0xFC) >>> 2);
        this.timeActive = Duration.standardMinutes(minutes);

        int highInsulinBits = (encodedData[2] & 0xF) << 9;
        int middleInsulinBits = ByteUtil.convertUnsignedByteToInt(encodedData[3]) << 1;
        int lowInsulinBits = ByteUtil.convertUnsignedByteToInt(encodedData[4]) >>> 7;
        this.insulin = Constants.POD_PULSE_SIZE * (highInsulinBits | middleInsulinBits | lowInsulinBits);
        this.podMessageCounter = (byte) ((encodedData[4] >>> 3) & 0xf);

        this.insulinNotDelivered = Constants.POD_PULSE_SIZE * (((encodedData[4] & 0x03) << 8) | ByteUtil.convertUnsignedByteToInt(encodedData[5]));
        this.alerts = new AlertSet((byte) (((encodedData[6] & 0x7f) << 1) | (ByteUtil.convertUnsignedByteToInt(encodedData[7]) >>> 7)));

        double reservoirValue = (((encodedData[8] & 0x3) << 8) + ByteUtil.convertUnsignedByteToInt(encodedData[9])) * Constants.POD_PULSE_SIZE;
        if(reservoirValue > Constants.MAX_RESERVOIR_READING) {
            reservoirLevel = null;
        } else {
            reservoirLevel = reservoirValue;
        }
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.STATUS_RESPONSE;
    }
}
