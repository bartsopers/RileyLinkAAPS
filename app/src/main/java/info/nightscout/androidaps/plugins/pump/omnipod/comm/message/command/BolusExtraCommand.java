package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class BolusExtraCommand extends MessageBlock {
    private final double units;
    private final byte byte2;
    private final byte[] unknownPart;

    public BolusExtraCommand(double units, byte byte2, byte[] unknownPart) {
        this.units = units;
        this.byte2 = byte2;
        this.unknownPart = unknownPart;
        encode();
    }

    private void encode() {
        encodedData = new byte[] {byte2};
        encodedData = ByteUtil.concat(encodedData,
                ByteUtil.substring(ByteUtil.getBytesFromInt(
                        (int) (units / Constants.PodPulseSize * 10)), 2, 2));
        encodedData = ByteUtil.concat(encodedData, unknownPart);
        encodedData = ByteUtil.concat(encodedData, new byte[6]);
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.BolusExtra;
    }
}
