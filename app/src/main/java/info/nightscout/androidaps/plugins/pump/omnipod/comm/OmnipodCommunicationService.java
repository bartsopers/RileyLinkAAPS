package info.nightscout.androidaps.plugins.pump.omnipod.comm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkCommunicationException;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RLMessageType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkBLEError;
import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.action.OmnipodAction;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodPacket;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.ErrorResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ErrorResponseType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodState;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.NonceOutOfSyncException;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.OmnipodException;
import info.nightscout.androidaps.plugins.pump.omnipod.exception.PodReturnedErrorResponseException;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationService extends RileyLinkCommunicationManager {

    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationService.class);

    public OmnipodCommunicationService(RFSpy rfspy) {
        super(rfspy);
    }

    @Override
    protected void configurePumpSpecificSettings() {
    }

    @Override
    public boolean tryToConnectToDevice() {
        // TODO
        return false;
    }

    @Override
    public byte[] createPumpMessageContent(RLMessageType type) {
        return new byte[0];
    }

    @Override
    public <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz) {
        return (E) new OmnipodPacket(payload);
    }

    public <T extends MessageBlock> T sendCommand(PodState podState, MessageBlock command) {
        OmnipodMessage message = new OmnipodMessage(podState.getAddress(), Collections.singletonList(command), podState.getMessageNumber());
        return exchangeMessages(podState, message);
    }

    // Convenience method
    public <T> T executeAction(OmnipodAction<T> action) {
        return action.execute(this);
    }

    public <T extends MessageBlock> T exchangeMessages(PodState podState, OmnipodMessage message) {
        return exchangeMessages(podState, message, null, null);
    }

    public <T extends MessageBlock> T exchangeMessages(PodState podState, OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        for(int i = 0; 2 > i; i++) {

            if (podState.hasNonceState() && message.isNonceResyncable()) {
                podState.advanceToNextNonce();
            }

            MessageBlock responseMessageBlock = transportMessages(podState, message, addressOverride, ackAddressOverride);

            try {
                return (T) responseMessageBlock;
            } catch (ClassCastException ex) {
                if (responseMessageBlock.getType() == MessageBlockType.ERROR_RESPONSE) {
                    ErrorResponse error = (ErrorResponse)responseMessageBlock;
                    if (error.getErrorResponseType() == ErrorResponseType.BAD_NONCE) {
                        podState.resyncNonce(error.getNonceSearchKey(), message.getSentNonce(), message.getSequenceNumber());
                        message.resyncNonce(podState.getCurrentNonce());
                    } else {
                        throw new PodReturnedErrorResponseException((ErrorResponse) responseMessageBlock);
                    }
                } else {
                    throw new OmnipodException("Unexpected response type: " + responseMessageBlock.getType().name());
                }

            }
        }

        throw new OmnipodException("Nonce resync failed");
    }

    private MessageBlock transportMessages(PodState podState, OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = podState.getAddress();
        if (addressOverride != null) {
            packetAddress = addressOverride;
        }

        boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();

        OmnipodPacket response = null;
        while (encodedMessage.length > 0) {
            PacketType packetType = firstPacket ? PacketType.PDM : PacketType.CON;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType, podState.getPacketNumber(), encodedMessage);
            byte[] encodedMessageInPacket = packet.getEncodedMessage();
            //getting the data remaining to be sent
            encodedMessage = ByteUtil.substring(encodedMessage, encodedMessageInPacket.length, encodedMessage.length - encodedMessageInPacket.length);
            firstPacket = false;
            try {
                response = exchangePackets(podState, packet);
            } catch (Exception ex) {
                throw new OmnipodException("Failed to exchange packets", ex);
            }
            //We actually ignore (ack) responses if it is not last packet to send
        }

        if (response.getPacketType() == PacketType.ACK) {
            podState.increasePacketNumber(1);
            throw new OmnipodException("Received ack instead of real response");
        }

        OmnipodMessage receivedMessage = null;
        byte[] receivedMessageData = response.getEncodedMessage();
        while (receivedMessage == null) {
            try {
                receivedMessage = OmnipodMessage.decodeMessage(receivedMessageData);
            } catch(OmnipodException ex) {
                // Message is (probably) not complete yet
                LOG.debug("Ignoring exception in exchangeMessages: "+ ex.getClass().getName() +": "+ ex.getMessage());
            }
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = createAckPacket(podState, packetAddress, ackAddressOverride);
                try {
                    OmnipodPacket conPacket = exchangePackets(podState, ackForCon, 3, 40);
                    if (conPacket.getPacketType() != PacketType.CON) {
                        throw new OmnipodException("Received a non-con packet type: " + conPacket.getPacketType());
                    }
                    receivedMessageData = ByteUtil.concat(receivedMessageData, conPacket.getEncodedMessage());
                } catch (RileyLinkCommunicationException ex) {
                    throw new OmnipodException("RileyLink communication failed", ex);
                }
            }
        }

        podState.increaseMessageNumber(2);

        ackUntilQuiet(podState, packetAddress, ackAddressOverride);

        List<MessageBlock> messageBlocks = receivedMessage.getMessageBlocks();

        if (messageBlocks.size() == 0) {
            throw new OmnipodException("Not enough data");
        }

        return messageBlocks.get(0);
    }

    private OmnipodPacket createAckPacket(PodState podState, Integer packetAddress, Integer messageAddress) {
        int pktAddress = podState.getAddress();
        int msgAddress = podState.getAddress();
        if (packetAddress != null) {
            pktAddress = packetAddress;
        }
        if (messageAddress != null) {
            msgAddress = messageAddress;
        }
        return new OmnipodPacket(pktAddress, PacketType.ACK, podState.getPacketNumber(), ByteUtil.getBytesFromInt(msgAddress));
    }

    private void ackUntilQuiet(PodState podState, Integer packetAddress, Integer messageAddress) {
        OmnipodPacket ack = createAckPacket(podState, packetAddress, messageAddress);
        boolean quiet = false;
        while (!quiet) try {
            sendAndListen(ack, 300, 0, 0, 40, OmnipodPacket.class);
        } catch (RileyLinkCommunicationException ex) {
            if (RileyLinkBLEError.Timeout.equals(ex.getErrorCode())) {
                quiet = true;
            } else {
                ex.printStackTrace();
            }
        }

        podState.increasePacketNumber(1);
    }

    private OmnipodPacket exchangePackets(PodState podState, OmnipodPacket packet) throws RileyLinkCommunicationException {
        return exchangePackets(podState, packet, 0, 250, 20000, 127);
    }

    private OmnipodPacket exchangePackets(PodState podState, OmnipodPacket packet, int repeatCount, int preambleExtensionMilliseconds) throws RileyLinkCommunicationException {
        return exchangePackets(podState, packet, repeatCount, 250, 20000, preambleExtensionMilliseconds);
    }

    private OmnipodPacket exchangePackets(PodState podState, OmnipodPacket packet, int repeatCount, int responseTimeoutMilliseconds, int exchangeTimeoutMilliseconds, int preambleExtensionMilliseconds) throws RileyLinkCommunicationException {
        long timeoutTime = System.currentTimeMillis() + exchangeTimeoutMilliseconds;

        while (System.currentTimeMillis() < timeoutTime) {
            OmnipodPacket response = sendAndListen(packet, responseTimeoutMilliseconds, repeatCount, 9, preambleExtensionMilliseconds, OmnipodPacket.class);
            if (response == null || !response.isValid()) {
                continue;
            }
            if (response.getAddress() != packet.getAddress()) {
                continue;
            }
            if (response.getSequenceNumber() != ((podState.getPacketNumber() + 1) & 0b11111)) {
                continue;
            }

            podState.increasePacketNumber(2);
            return response;
        }
        throw new OmnipodException("Timeout when trying to exchange packets");
    }
}
