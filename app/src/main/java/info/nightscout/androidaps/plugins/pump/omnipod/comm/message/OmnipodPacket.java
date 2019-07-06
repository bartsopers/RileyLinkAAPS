package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

/**
 * Created by andy on 6/1/18.
 */
public class OmnipodPacket implements RLMessage {
    private static final Logger LOG = LoggerFactory.getLogger(OmnipodPacket.class);

    private int packetAddress = 0;
    private PacketType packetType = PacketType.INVALID;
    private int sequenceNumber = 0;
    private byte[] encodedMessage = null;
    private boolean valid = false;

    public OmnipodPacket(byte[] encoded) {
        if (encoded.length < 7) {
            return;
        }
        this.packetAddress = ByteUtil.toInt(new Integer(encoded[0]), new Integer(encoded[1]),
                new Integer(encoded[2]), new Integer(encoded[3]), ByteUtil.BitConversion.BIG_ENDIAN);
        this.packetType = PacketType.fromByte((byte)(((int)encoded[4] & 0xFF)>> 5));
        if (this.packetType == null) {
            throw new OmnipodEncodingException("Invalid packet type");
        }
        this.sequenceNumber = (encoded[4] & 0b11111);
//        if (packetType == PacketType.ACK) {
//            valid = true;
//
//        }
        int crc = OmniCRC.crc8(ByteUtil.substring(encoded,0, encoded.length - 1));
        if (crc != encoded[encoded.length - 1]) {
            throw new OmnipodEncodingException("CRC mismatch");
        }
        this.encodedMessage = ByteUtil.substring(encoded, 5, encoded.length - 1 - 5);
        valid = true;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public OmnipodPacket(int packetAddress, PacketType packetType, int packetNumber, byte[] encodedMessage) {
        this.packetAddress = packetAddress;
        this.packetType = packetType;
        this.sequenceNumber = packetNumber;
        this.encodedMessage = encodedMessage;
        if (encodedMessage.length > packetType.getMaxBodyLength()) {
            this.encodedMessage = ByteUtil.substring(encodedMessage, 0, packetType.getMaxBodyLength());
        }
        this.valid = true;
    }

    public int getAddress() {
        return packetAddress;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getEncodedMessage() {
        return encodedMessage;
    }

    @Override
    public byte[] getTxData() {
        byte[] output = new byte[0];
        output = ByteUtil.concat(output, ByteUtil.getBytesFromInt(this.packetAddress));
        output = ByteUtil.concat(output, (byte)((this.packetType.getValue() << 5) + (sequenceNumber & 0b11111)));
        output = ByteUtil.concat(output, encodedMessage);
        output = ByteUtil.concat(output, OmniCRC.crc8(output));
        return output;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

}
