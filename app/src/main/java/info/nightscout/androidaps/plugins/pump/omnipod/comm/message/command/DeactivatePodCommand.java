package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.NonceResyncableMessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

public class DeactivatePodCommand extends NonceResyncableMessageBlock {
    private int nonce;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.DEACTIVATE_POD;
    }

    public DeactivatePodCommand(int nonce) {
        this.nonce = nonce;
        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
    }

    @Override
    public int getNonce() {
        return nonce;
    }

    @Override
    public void setNonce(int nonce) {
        this.nonce = nonce;
        encode();
    }
}
