package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationManager;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

public class OmnipodMessage {

    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    private int address;
    private MessageBlock[] messageBlocks;
    private int sequenceNumber;

    public OmnipodMessage(int address, MessageBlock[] messageBlocks, int sequenceNumber) {
        this.address = address;
        this.messageBlocks = messageBlocks;
        this.sequenceNumber = sequenceNumber;
    }

    public byte[] getEncoded() {
        byte[] encodedData = new byte[0];
        for (int i = 0; i < messageBlocks.length; i++) {
            encodedData = ByteUtil.concat(encodedData, messageBlocks[i].getRawData());
        }

        byte[] header = new byte[0];
        //right before the message blocks we have 6 bits of seqNum and 10 bits of length
        header = ByteUtil.concat(header, ByteUtil.getBytesFromInt(address));
        header = ByteUtil.concat(header, (byte) (((sequenceNumber & 0x1F) << 2) + ((encodedData.length >> 8) & 0x03)));
        header = ByteUtil.concat(header, (byte)(encodedData.length & 0xFF));
        encodedData = ByteUtil.concat(header, encodedData);
        String myString = ByteUtil.shortHexString(encodedData);
        int crc = OmniCRC.crc16(encodedData);
        encodedData = ByteUtil.concat(encodedData, ByteUtil.substring(ByteUtil.getBytesFromInt(crc), 2,2));
        return encodedData;
    }

    public MessageBlock[] getMessageBlocks() {
        return messageBlocks;
    }
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public static OmnipodMessage TryDecode(byte[] data) {
        if (data.length < 10) {
            //FIXME: Throw exception of not enough data or at least log
            return null;
        }

        int address = ByteUtil.toInt(new Integer(data[0]), new Integer(data[1]), new Integer(data[2]),
                new Integer(data[3]), ByteUtil.BitConversion.BIG_ENDIAN);
        byte b9 = data[4];
        byte bodyLength = data[5];
        if (data.length - 8 < bodyLength) {
            //FIXME: Throw or log: not enough data
            LOG.info("OmnipodMessage: not enough data (" + (data.length - 8) + " instead of " + bodyLength + ")");
            return null;
        }
        int sequenceNumber = (((int)b9 >> 2) & 0b11111);
        int crc = ByteUtil.toInt(data[data.length - 2], data[data.length - 1]);
        int calculatedCrc = OmniCRC.crc16(ByteUtil.substring(data, 0, data.length - 2));
        if (crc != calculatedCrc) {
            //FIXME: Throw or log CRC error
            LOG.warn("OmnipodMessage: CRC mismatch");
            return null;
        }
        MessageBlock[] blocks = decodeBlocks(ByteUtil.substring(data, 6, data.length - 6 - 2));
        if (blocks == null || blocks.length == 0) {
            //FIXME: Throw/log no blocks decoded
            LOG.warn("OmnipodMessage: No blocks decoded");
            return null;
        }

        OmnipodMessage result = new OmnipodMessage(address, blocks, sequenceNumber);
        return result;
    }

    private static MessageBlock[] decodeBlocks(byte[] data) {
        ArrayList<MessageBlock> blocks = new  ArrayList<MessageBlock>();
        int index = 0;
        while (index < data.length) {
            MessageBlockType blockType = MessageBlockType.fromByte(data[index]);
            MessageBlock block = blockType.decode(data);
            if (block == null) {
                //FIXME: Throw/log: unknown block
                LOG.warn("OmnipodMessage: Unknown block type: " + data[index]);
                return null;
            }
            blocks.add(block);
            index += block.getRawData().length;
        }

        MessageBlock[] result = new MessageBlock[blocks.size()];
        return blocks.toArray(result);
    }
}
