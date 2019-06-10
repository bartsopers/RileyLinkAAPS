package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class PodInfoResponse extends MessageBlock {
    public PodInfoResponse(byte[] encodedData) {
        throw new NotImplementedException("PodInfoResponse");
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.POD_INFO_RESPONSE;
    }
}
