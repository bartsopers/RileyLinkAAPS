package info.nightscout.androidaps.plugins.PumpOmnipod.comm.message;

import org.apache.commons.lang3.NotImplementedException;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.ble.defs.RLMessage;
import info.nightscout.androidaps.plugins.PumpCommon.utils.ByteUtil;
import info.nightscout.androidaps.plugins.PumpCommon.utils.CRC;
import info.nightscout.androidaps.plugins.PumpOmnipod.defs.PacketType;

/**
 * Created by andy on 6/1/18.
 */
// FIXME: This needs to be changed. this is just copy of MedtronicPumpMessage, so I imagine this file will have different structure
// in Omnipod I assume
public class OmnipodPacket implements RLMessage {

    private int packetAddress = 0;
    private PacketType packetType = PacketType.Invalid;
    private int sequenceNumber = 0;
    private byte[] encodedMessage = null;
    private Boolean _isValid = false;

    public OmnipodPacket(byte[] encoded) {
        if (encoded.length < 7) {
            //FIXME: Throw not enough data exception
        }
        this.packetAddress = ByteUtil.toInt(
                new Integer(encoded[0])
                , new Integer(encoded[1])
                , new Integer(encoded[2])
                , new Integer(encoded[3])
                , ByteUtil.BitConversion.BIG_ENDIAN);
        this.packetType = PacketType.fromByte((byte)(encoded[4] >> 5));
        if (this.packetType == null) {
            //FIXME: Log invalid packet type
            return;
        }
        this.sequenceNumber = encoded[4] & 0b11111;
        int crc = CRC.crc8(ByteUtil.substring(encoded,0, encoded.length - 1));
        if (crc != encoded[encoded.length - 1]) {
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
        if (encodedMessage.length > packetType.MaxBodyLength())
            this.encodedMessage = ByteUtil.substring(encodedMessage, 0,  packetType.MaxBodyLength());
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
        output = ByteUtil.concat(output, (byte)((this.packetType.getValue() << 5) + sequenceNumber & 0b11111));
        output = ByteUtil.concat(output, encodedMessage);
        output = ByteUtil.concat(output, CRC.crc8(output));
        return output;

    }

    @Override
    public boolean isValid() {
        return _isValid;
    }


}
