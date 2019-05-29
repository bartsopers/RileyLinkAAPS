package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class DeactivatePodCommand extends MessageBlock {
    private int nonce;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.DeactivatePod;
    }

    public DeactivatePodCommand(int nonce) {
        this.nonce = nonce;
        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
    }

}
