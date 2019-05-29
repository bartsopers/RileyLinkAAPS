package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class StatusError extends MessageBlock {
    public StatusError(byte[] encodedData) {
        throw new NotImplementedException("StatusError");
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.StatusError;
    }
}
