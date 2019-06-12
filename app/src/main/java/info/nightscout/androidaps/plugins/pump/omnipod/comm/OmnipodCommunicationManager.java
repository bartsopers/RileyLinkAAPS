package info.nightscout.androidaps.plugins.pump.omnipod.comm;

import android.content.Context;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkCommunicationException;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RLMessageType;


import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigureAlertsCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigurePodCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.VersionResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodMessage;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.OmnipodPacket;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.ErrorResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ErrorResponseType;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepRepeat;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.BeepType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ExpirationAdvisory;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.BolusDeliverySchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.BolusExtraCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PacketType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodProgressState;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodState;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;
import info.nightscout.androidaps.plugins.pump.omnipod.util.Utils;
import info.nightscout.androidaps.utils.SP;

/**
 * Created by andy on 6/29/18.
 */

public class OmnipodCommunicationManager extends RileyLinkCommunicationManager {

    private static final int DEFAULT_ADDRESS = 0xFFFFFFFF;
    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);

    private Integer messageNumber;
    private Integer packetNumber;
    private PodState podState;

    static OmnipodCommunicationManager omnipodCommunicationManager;

    public OmnipodCommunicationManager(Context context, RFSpy rfspy) {
        super(context, rfspy);
        omnipodCommunicationManager = this;
    }

    @Override
    protected void configurePumpSpecificSettings() {
        // TODO
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

    public static OmnipodCommunicationManager getInstance() {
        return omnipodCommunicationManager;
    }

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message) {
        return exchangeMessages(message, null, null);
    }

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = DEFAULT_ADDRESS;
        if (this.podState != null) {
            packetAddress = this.podState.address;
        }
        if (addressOverride != null) {
            packetAddress = addressOverride;
        }

        boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();

        OmnipodPacket response = null;
        while(encodedMessage.length > 0) {
            PacketType packetType = firstPacket? PacketType.PDM : PacketType.CON;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType, podState == null? packetNumber : podState.packetNumber, encodedMessage);
            byte[] encodedMessageInPacket = packet.getEncodedMessage();
            //getting the data remaining to be sent
            encodedMessage = ByteUtil.substring(encodedMessage, encodedMessageInPacket.length, encodedMessage.length - encodedMessageInPacket.length);
            firstPacket = false;
            try {
                response = exchangePackets(packet);
            } catch(Exception ex) {
                throw new OmnipodCommunicationException("Failed to exchange packets", ex);
            }
            //We actually ignore (ack) responses if it is not last packet to send
        }
        if (response == null) {
            throw new OmnipodCommunicationException("Timeout on receive");
        }


        if (response.getPacketType() == PacketType.ACK) {
            increasePacketNumber(1);
            throw new OmnipodCommunicationException("Received ack instead of real response");
        }

        OmnipodMessage receivedMessage = null;
        byte[] receivedMessageData = response.getEncodedMessage();
        while(receivedMessage == null) {
            receivedMessage = OmnipodMessage.decodeMessage(receivedMessageData);
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = createAckPacket(packetAddress, ackAddressOverride);
                try {
                    OmnipodPacket conPacket = exchangePackets(ackForCon, 3, 40);
                    if (conPacket.getPacketType() != PacketType.CON) {
                        throw new OmnipodCommunicationException("Received a non-con packet type: "+ conPacket.getPacketType());
                    }
                    receivedMessageData = ByteUtil.concat(receivedMessageData, conPacket.getEncodedMessage());
                } catch(RileyLinkCommunicationException ex) {
                    throw new OmnipodCommunicationException("RileyLink communication failed", ex);
                }
            }
        }

        increaseMessageNumber(2);

        ackUntilQuiet(packetAddress, ackAddressOverride);

        List<MessageBlock> messageBlocks = receivedMessage.getMessageBlocks();
        if (messageBlocks.size() == 0) {
            throw new OmnipodCommunicationException("Not enough data");
        }

        MessageBlock block = messageBlocks.get(0);
        if (block.getType() == MessageBlockType.ERROR_RESPONSE) {
            ErrorResponse error = (ErrorResponse)block;
            if (error.getErrorResponseType() == ErrorResponseType.BAD_NONCE) {
                LOG.warn("Nonce out-of-sync");

                if (podState != null) {
                    this.podState.resyncNonce(error.getNonceSearchKey(), this.podState.getCurrentNonce(), message.getSequenceNumber());
                }
            }
            throw new OmnipodCommunicationException("Received an error response: "+ error.getErrorResponseType().name());
        }

        return (T) block;
    }

    private void increaseMessageNumber(int increment) {
        if (podState == null) {
            messageNumber = (messageNumber + increment) & 0b1111;
        } else {
            podState.messageNumber = (podState.messageNumber + increment) & 0b1111;
        }
    }

    private void increasePacketNumber(int increment) {
        if (podState == null) {
            packetNumber = (packetNumber + increment) & 0b11111;
        } else {
            podState.packetNumber = (podState.packetNumber + increment) & 0b11111;
        }
    }

    private OmnipodPacket createAckPacket(Integer packetAddress, Integer messageAddress) {
        int pktAddress = DEFAULT_ADDRESS;
        int msgAddress = DEFAULT_ADDRESS;
        if (this.podState != null) {
            pktAddress = msgAddress = podState.address;
        }
        if (packetAddress != null) {
            pktAddress = packetAddress;
        }
        if (messageAddress != null) {
            msgAddress = messageAddress;
        }
        return new OmnipodPacket(pktAddress, PacketType.ACK, podState == null ? packetNumber : podState.packetNumber, ByteUtil.getBytesFromInt(msgAddress));
    }

    private void ackUntilQuiet(Integer packetAddress, Integer messageAddress) {
        OmnipodPacket ack = createAckPacket(packetAddress, messageAddress);
        Boolean quiet = false;
        while(!quiet) {
            try {
                OmnipodPacket response = sendAndListen(ack, 600, 5, 40, OmnipodPacket.class);
                //FIXME: instead of this crappy core we should make a proper timeout handling (exception-based?)
                if (response == null || (!response.isValid() && response.getPacketType() == PacketType.INVALID)) {
                    quiet = true;
                }
            } catch(Exception ex) {
                LOG.warn("Caught exception while trying to ack until quiet: "+ ex.getMessage());
            }
        }
        increasePacketNumber(1);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet) throws RileyLinkCommunicationException {
        return exchangePackets(packet, 0, 250,20000, 127);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int repeatCount, int preambleExtensionMilliseconds) throws RileyLinkCommunicationException {
        return exchangePackets(packet, repeatCount, 250, 20000, preambleExtensionMilliseconds);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int repeatCount, int responseTimeoutMilliseconds, int exchangeTimeoutMilliseconds, int preambleExtensionMilliseconds) throws RileyLinkCommunicationException {
        long timeoutTime = System.currentTimeMillis() + exchangeTimeoutMilliseconds;
        
        while(System.currentTimeMillis() < timeoutTime) {
            OmnipodPacket response = sendAndListen(packet, responseTimeoutMilliseconds, repeatCount, preambleExtensionMilliseconds, OmnipodPacket.class);
            if (response == null || !response.isValid()) {
                continue;
            }
            if (response.getAddress() != packet.getAddress()) {
                continue;
            }
            if (response.getSequenceNumber() != (((podState == null ? packetNumber : podState.packetNumber) + 1) & 0b11111)) {
                continue;
            }

            increasePacketNumber(2);
            return response;
        }
        throw new OmnipodCommunicationException("Timeout when trying to exchange packets");
    }

    public <T extends MessageBlock> T sendCommand(MessageBlock command) {
        int msgAddress = DEFAULT_ADDRESS;
        if (this.podState != null) {
            msgAddress = podState.address;
        }
        OmnipodMessage message = new OmnipodMessage(msgAddress, Collections.singletonList(command), podState == null ? messageNumber : podState.messageNumber);
        return exchangeMessages(message);
    }

    private int nonceValue() {
        if (this.podState == null) {
            throw new OmnipodCommunicationException("Getting nonce without active pod");
        }
        return podState.getCurrentNonce();
    }

    private void advanceToNextNonce() {
        if (this.podState == null) {
            throw new OmnipodCommunicationException("Getting nonce without active pod");
        }
        podState.advanceToNextNonce();
    }

    public void initializePod() {
        if (SP.contains(OmniPodConst.Prefs.POD_STATE)) {
            //FIXME: We should ask "are you sure?"
            // And send the DeactivatePodCommand
            SP.remove(OmniPodConst.Prefs.POD_STATE);
        }

        packetNumber = 0x0A;
        messageNumber = 0;

        int newAddress = 0x1f0b3557;

        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(DEFAULT_ADDRESS, Collections.singletonList(assignAddress), messageNumber);

        VersionResponse configResponse = exchangeMessages(assignAddressMessage, DEFAULT_ADDRESS, newAddress);
        if (configResponse == null) {
            throw new OmnipodCommunicationException("config is null (timeout or wrong answer)");
        }

        DateTime activationDate = DateTime.now();

        //at this point for an unknown reason PDM starts counting messages from 0 again
        messageNumber = 0;
        ConfigurePodCommand confirmPairing = new ConfigurePodCommand(newAddress, activationDate,
                configResponse.lot, configResponse.tid);
        OmnipodMessage confirmPairingMessage = new OmnipodMessage(DEFAULT_ADDRESS,
                Collections.singletonList(confirmPairing), messageNumber);
        VersionResponse config2 = exchangeMessages(confirmPairingMessage, DEFAULT_ADDRESS, newAddress);

        if (config2.podProgressState != PodProgressState.PAIRING_SUCCESS) {
            throw new OmnipodCommunicationException("Pairing failed, state: "+ config2.podProgressState.name());
        }

        this.podState = new PodState(newAddress, activationDate, config2.piVersion,
                config2.pmVersion, config2.lot, config2.tid, packetNumber, messageNumber);

        packetNumber = messageNumber = null;

        ExpirationAdvisory expirationAdvisory =
                new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.RESERVOIR, 50);
        AlertConfiguration lweReservoir =
                new AlertConfiguration(AlertType.LOW_RESERVOIR,true,false,Duration.ZERO,
                expirationAdvisory, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_60_MINUTES);

        int nonce = nonceValue();

        ConfigureAlertsCommand lowReservoirCommand = new ConfigureAlertsCommand(nonce, new AlertConfiguration[] { lweReservoir });
        StatusResponse status = sendCommand(lowReservoirCommand);
        advanceToNextNonce();

        ExpirationAdvisory insertionTimerExpirationAdvisory = new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.TIMER, new Duration(5 * 60 * 1000));

        AlertConfiguration insertionTimer = new AlertConfiguration(AlertType.TIMER_LIMIT,true,false,
                Duration.standardMinutes(55), insertionTimerExpirationAdvisory, BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_5_MINUTES);

        ConfigureAlertsCommand insertionTimerCommand = new ConfigureAlertsCommand(nonceValue(), new AlertConfiguration[] { insertionTimer });
        status = sendCommand(insertionTimerCommand);
        advanceToNextNonce();

        double primeUnits = 2.6;
        BolusDeliverySchedule primeBolus = new BolusDeliverySchedule(primeUnits, Duration.standardSeconds(1));
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(nonceValue(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits);
        OmnipodMessage prime = new OmnipodMessage(newAddress, Arrays.asList(primeCommand, extraBolusCommand), podState.messageNumber);
        status = exchangeMessages(prime);
        advanceToNextNonce();

        Gson gson = Utils.gsonDateTime();

        SP.putString(OmniPodConst.Prefs.POD_STATE, gson.toJson(podState));
    }

    public void finishPrime() {
        if (this.podState == null) {
            throw new IllegalStateException("Pod state cannot be null");
        }

        ExpirationAdvisory expirationAdvisory = new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.TIMER, new Duration(70 * 60 * 60 * 1000 + 58 * 60 * 1000));

        AlertConfiguration alert = new AlertConfiguration(AlertType.EXPIRATION_ADVISORY,true,false,Duration.ZERO, expirationAdvisory,
                BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_3_MINUTES_REPEAT_EVERY_15_MINUTES);

        ConfigureAlertsCommand alertCommand = new ConfigureAlertsCommand(nonceValue(), new AlertConfiguration[] { alert });

        StatusResponse status = sendCommand(alertCommand);

        BasalSchedule schedule = getBasalSchedule();

        setBasalSchedule(schedule, false, Duration.ZERO, Duration.ZERO);
    }

    public void setBasalSchedule(BasalSchedule schedule, boolean confidenceReminder, Duration scheduleOffset, Duration programReminderInterval) {
        //SetInsulinScheduleCommand setBasal = new SetInsulinScheduleCommand(nonceValue(), schedule, scheduleOffset);
        //BasalScheduleExtraCommand extraCommand = new BasalScheduleExtraCommand(confidenceReminder, programReminderInterval);
        //OmnipodMessage basalMessage = new OmnipodMessage(podState.address, new MessageBlock[]{setBasal, extraCommand}, podState.messageNumber);
        //StatusResponse status = exchangeMessages(basalMessage);
    }

    private BasalSchedule getBasalSchedule() {
        // Stub
        // TODO get real basal schedule
        ArrayList<BasalScheduleEntry> basals = new ArrayList<>();
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(30)));    //00:00-00:30
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(30)));   //00:30-01:00
        basals.add(new BasalScheduleEntry(1, Duration.standardMinutes(30)));    //01:00-01:30
        basals.add(new BasalScheduleEntry(11.5, Duration.standardMinutes(30))); //01:30-02:00
        basals.add(new BasalScheduleEntry(2, Duration.standardMinutes(30)));    //20:00-02:30
        basals.add(new BasalScheduleEntry(12.5, Duration.standardMinutes(30))); //02:30-03:00
        basals.add(new BasalScheduleEntry(3, Duration.standardMinutes(30)));    //03:00-03:30
        basals.add(new BasalScheduleEntry(13.5, Duration.standardMinutes(30))); //03:30-04:00
        basals.add(new BasalScheduleEntry(4, Duration.standardMinutes(30)));    //04:00-04:30
        basals.add(new BasalScheduleEntry(14.5, Duration.standardMinutes(30))); //04:30-05:00
        basals.add(new BasalScheduleEntry(5, Duration.standardMinutes(30)));    //05:00-05:30
        basals.add(new BasalScheduleEntry(15.5, Duration.standardMinutes(30))); //05:30-06:00
        basals.add(new BasalScheduleEntry(6, Duration.standardMinutes(30)));    //06:00-06:30
        basals.add(new BasalScheduleEntry(16.5, Duration.standardMinutes(30))); //06:30-07:00
        basals.add(new BasalScheduleEntry(7, Duration.standardMinutes(30)));    //07:00-07:30
        basals.add(new BasalScheduleEntry(17.5, Duration.standardMinutes(30))); //07:30-08:00
        basals.add(new BasalScheduleEntry(8, Duration.standardMinutes(30)));    //08:00-08:30
        basals.add(new BasalScheduleEntry(18.5, Duration.standardMinutes(30))); //08:30-09:00
        basals.add(new BasalScheduleEntry(9, Duration.standardMinutes(30)));    //09:00-09:30
        basals.add(new BasalScheduleEntry(19.5, Duration.standardMinutes(30))); //09:30-10:00
        basals.add(new BasalScheduleEntry(10, Duration.standardMinutes(30)));   //10:00-10:30
        basals.add(new BasalScheduleEntry(1.05, Duration.standardMinutes(30))); //10:30-11:00
        basals.add(new BasalScheduleEntry(11, Duration.standardMinutes(11*60)));//11:00-22:00
        basals.add(new BasalScheduleEntry(1.15, Duration.standardMinutes(2*60)));//22:00-24:00
        return new BasalSchedule(basals);
    }
}
