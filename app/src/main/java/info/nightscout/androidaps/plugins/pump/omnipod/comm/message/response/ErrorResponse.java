package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ErrorResponseType;

public class ErrorResponse extends MessageBlock {
    private final ErrorResponseType errorResponseType;
    private final int nonceSearchKey;

    public ErrorResponse(byte[] encodedData) {
        this.errorResponseType = ErrorResponseType.fromByte(encodedData[2]);
        this.nonceSearchKey = ByteUtil.makeUnsignedShort((int)encodedData[3], (int)encodedData[4]);
        int length = encodedData[1] + 2;
        this.encodedData = ByteUtil.substring(encodedData, 1, length - 1);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ERROR_RESPONSE;
    }

    public ErrorResponseType getErrorResponseType() {
        return errorResponseType;
    }
    public int getNonceSearchKey() {
        return nonceSearchKey;
    }
}
