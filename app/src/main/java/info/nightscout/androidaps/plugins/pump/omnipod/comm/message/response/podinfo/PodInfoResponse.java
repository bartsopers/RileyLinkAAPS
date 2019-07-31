package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

public class PodInfoResponse extends MessageBlock {
    private final PodInfoType subType;
    private final PodInfo podInfo;

    public PodInfoResponse(byte[] encodedData) {
        this.encodedData = ByteUtil.substring(encodedData, 2, encodedData[1]);
        subType = PodInfoType.fromByte(encodedData[2]);
        podInfo = subType.decode(ByteUtil.substring(encodedData, 2, ByteUtil.convertUnsignedByteToInt(encodedData[1])));
    }

    public PodInfoType getSubType() {
        return subType;
    }

    public <T extends PodInfo> T getPodInfo() {
        return (T)podInfo;
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.POD_INFO_RESPONSE;
    }

    @Override
    public String toString() {
        return "PodInfoResponse{" +
                "subType=" + subType.name() +
                ", podInfo=" + podInfo.toString() +
                '}';
    }
}
