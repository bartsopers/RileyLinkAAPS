package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.omnipod.defs.StatusRequestType;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class GetStatusCommand extends MessageBlock {
    private final StatusRequestType statusRequestType;

    public GetStatusCommand(StatusRequestType statusRequestType) {
        this.statusRequestType = statusRequestType;
        encode();
    }

    private void encode() {
        encodedData = new byte[] {statusRequestType.getValue()};
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.GetStatus;
    }
}
