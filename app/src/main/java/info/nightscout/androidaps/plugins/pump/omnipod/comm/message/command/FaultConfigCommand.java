package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

public class FaultConfigCommand extends MessageBlock {
    private final int nonce;
    private final byte tab5sub16;
    private final byte tab5sub17;

    public FaultConfigCommand(int nonce, byte tab5sub16, byte tab5sub17) {
        this.nonce = nonce;
        this.tab5sub16 = tab5sub16;
        this.tab5sub17 = tab5sub17;

        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        encodedData = ByteUtil.concat(encodedData, tab5sub16);
        encodedData = ByteUtil.concat(encodedData, tab5sub17);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.FAULT_CONFIG;
    }
}