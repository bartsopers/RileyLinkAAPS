package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ErrorResponseType;

public class ErrorResponse extends MessageBlock {
    private ErrorResponseType errorResponseType;
    private int nonceSearchKey;

    public ErrorResponse(byte[] encodedData) {
        this.errorResponseType = ErrorResponseType.fromByte(encodedData[2]);
        this.nonceSearchKey = ByteUtil.toInt(new Integer(encodedData[3]), new Integer(encodedData[4]),
                new Integer(encodedData[5]), new Integer(encodedData[6]), ByteUtil.BitConversion.BIG_ENDIAN);
        int length = encodedData[1] + 2;
        this.encodedData = ByteUtil.substring(encodedData, 1, length - 1);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ErrorResponse;
    }


    public ErrorResponseType getErrorResponseType() {
        return errorResponseType;
    }
    public int getNonceSearchKey() {
        return nonceSearchKey;
    }
}
