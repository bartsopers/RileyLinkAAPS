package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;

public class CancelDeliveryCommand extends MessageBlock {

    private int nonce;
    private BeepType beepType;
    private boolean cancelBolus;
    private boolean cancelTempBasal;
    private boolean cancelBasalProgram;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.CancelDelivery;
    }

    public CancelDeliveryCommand(int nonce, BeepType beepType, boolean cancelBolus,
                                 boolean cancelTempBasal, boolean cancelBasalProgram) {
        this.nonce = nonce;
        this.beepType = beepType;
        this.cancelBolus = cancelBolus;
        this.cancelTempBasal = cancelTempBasal;
        this.cancelBasalProgram = cancelBasalProgram;
        encode();
    }

    private void encode() {
        encodedData = new byte[5];
        System.arraycopy(ByteUtil.getBytesFromInt(nonce),0,encodedData,0,4);
        byte beepTypeValue = beepType.getValue();
        if (beepTypeValue > 8) beepTypeValue = 0;
        encodedData[4] = (byte)((beepTypeValue & 0x0F) << 4);
        if (cancelBolus) {
            encodedData[4] |= 4;
        }
        if (cancelTempBasal) {
            encodedData[4] |= 2;
        }
        if (cancelBasalProgram) {
            encodedData[4] |= 1;
        }
    }
}
