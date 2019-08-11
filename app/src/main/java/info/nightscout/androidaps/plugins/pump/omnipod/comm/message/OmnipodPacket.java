package info.nightscout.androidaps.plugins.pump.omnipod.comm.message;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.CrcMismatchException;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.OmnipodException;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniCRC;

/**
 * Created by andy on 6/1/18.
 */
public class OmnipodPacket implements RLMessage {
    private int packetAddress = 0;
    private PacketType packetType = PacketType.INVALID;
    private int sequenceNumber = 0;
    private byte[] encodedMessage = null;
    private boolean valid = false;

    public OmnipodPacket(byte[] encoded) {
        if (encoded.length < 7) {
            return;
        }
        this.packetAddress = ByteUtil.toInt((int) encoded[0], (int) encoded[1],
                (int) encoded[2], (int) encoded[3], ByteUtil.BitConversion.BIG_ENDIAN);
        try {
            this.packetType = PacketType.fromByte((byte) (((int) encoded[4] & 0xFF) >> 5));
        } catch (IllegalArgumentException ex) {
            throw new OmnipodException("Invalid packet type", ex);
        }
        this.sequenceNumber = (encoded[4] & 0b11111);
        byte crc = OmniCRC.crc8(ByteUtil.substring(encoded, 0, encoded.length - 1));
        if (crc != encoded[encoded.length - 1]) {
            throw new CrcMismatchException("CRC mismatch: " +
                    ByteUtil.shortHexString(new byte[]{crc}) + " <> " +
                    ByteUtil.shortHexString(new byte[]{encoded[encoded.length - 1]}) +
                    " (packetType=" + packetType.name() + ",packetLength=" + encoded.length + ")");
        }
        this.encodedMessage = ByteUtil.substring(encoded, 5, encoded.length - 1 - 5);
        valid = true;
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

    public PacketType getPacketType() {
        return packetType;
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
        output = ByteUtil.concat(output, (byte) ((this.packetType.getValue() << 5) + (sequenceNumber & 0b11111)));
        output = ByteUtil.concat(output, encodedMessage);
        output = ByteUtil.concat(output, OmniCRC.crc8(output));
        return output;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

}
