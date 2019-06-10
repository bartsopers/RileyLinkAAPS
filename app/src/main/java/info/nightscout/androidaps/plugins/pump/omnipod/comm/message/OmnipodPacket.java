package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationManager;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

/**
 * Created by andy on 6/1/18.
 */
public class OmnipodPacket implements RLMessage {
    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);

    private int packetAddress = 0;
    private PacketType packetType = PacketType.INVALID;
    private int sequenceNumber = 0;
    private byte[] encodedMessage = null;
    private Boolean _isValid = false;

    public OmnipodPacket(byte[] encoded) {
        if (encoded.length < 7) {
            //FIXME: Throw not enough data exception
            return;
        }
        this.packetAddress = ByteUtil.toInt(new Integer(encoded[0]), new Integer(encoded[1]),
                new Integer(encoded[2]), new Integer(encoded[3]), ByteUtil.BitConversion.BIG_ENDIAN);
        this.packetType = PacketType.fromByte((byte)(((int)encoded[4] & 0xFF)>> 5));
        if (this.packetType == null) {
            //FIXME: Log invalid packet type
            return;
        }
        this.sequenceNumber = (encoded[4] & 0b11111);
//        if (packetType == PacketType.ACK) {
//            _isValid = true;
//
//        }
        int crc = OmniCRC.crc8(ByteUtil.substring(encoded,0, encoded.length - 1));
        if (crc != encoded[encoded.length - 1]) {
            LOG.error("OmnipodPacket CRC mismatch");
            //FIXME: Log CRC mismatch
            return;
        }
        this.encodedMessage = ByteUtil.substring(encoded, 5, encoded.length - 1 - 5);
        _isValid = true;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public OmnipodPacket(int packetAddress, PacketType packetType, int packetNumber, byte[] encodedMessage) {
        this.packetAddress = packetAddress;
        this.packetType = packetType;
        this.sequenceNumber = packetNumber;
        this.encodedMessage = encodedMessage;
        if (encodedMessage.length > packetType.maxBodyLength()) {
            this.encodedMessage = ByteUtil.substring(encodedMessage, 0, packetType.maxBodyLength());
        }
        this._isValid = true;
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
        return _isValid;
    }

}
