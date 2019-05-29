package info.nightscout.androidaps.plugins.pump.omnipod.comm;

import android.content.Context;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Random;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkCommunicationManager;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RFSpy;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.RileyLinkCommunicationException;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.data.RLMessage;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RLMessageType;


import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AssignAddressCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfigureAlertsCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.ConfirmPairingCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.SetInsulinScheduleCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.ConfigResponse;
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
import info.nightscout.androidaps.plugins.pump.omnipod.defs.InsulinSchedule.Bolus;
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

    private static final int defaultAddress = 0xFFFFFFFF;
    private Integer messageNumber;
    private Integer packetNumber;
    private PodState podState;

    private static final Logger LOG = LoggerFactory.getLogger(OmnipodCommunicationManager.class);
    private boolean showPumpMessages;
    static OmnipodCommunicationManager omnipodCommunicationManager;

    public OmnipodCommunicationManager(Context context, RFSpy rfspy) {
        super(context, rfspy);
        omnipodCommunicationManager = this;
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

    //FIXME: This one should be refactored as it sends/listens to raw packets and not messages
    @Override
    public <E extends RLMessage> E createResponseMessage(byte[] payload, Class<E> clazz) {
        OmnipodPacket pumpMessage = new OmnipodPacket(payload);
        return (E)pumpMessage;
    }

//    // All pump communications go through this function.
//    protected PodMessage sendAndListen(RLMessage msg, int timeout_ms) {
//
//        return sendAndListen(msg, timeout_ms, PodMessage.class);
//    }

    public static OmnipodCommunicationManager getInstance() {
        return omnipodCommunicationManager;
    }

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message) {
        return exchangeMessages(message, null, null);
    }

    protected <T extends MessageBlock> T exchangeMessages(OmnipodMessage message, Integer addressOverride, Integer ackAddressOverride) {
        int packetAddress = defaultAddress;
        if (this.podState != null) {
            packetAddress = this.podState.Address;
        }
        if (addressOverride != null) {
            packetAddress = addressOverride;
        }

        boolean firstPacket = true;
        byte[] encodedMessage = message.getEncoded();

        OmnipodPacket response = null;
        while(encodedMessage.length > 0) {
            PacketType packetType = firstPacket? PacketType.Pdm : PacketType.Con;
            OmnipodPacket packet = new OmnipodPacket(packetAddress, packetType,podState == null? packetNumber : podState.packetNumber, encodedMessage);
            byte[] encodedMessageInPacket = packet.getEncodedMessage();
            //getting the data remaining to be sent
            encodedMessage = ByteUtil.substring(encodedMessage, encodedMessageInPacket.length, encodedMessage.length - encodedMessageInPacket.length);
            firstPacket = false;
            try {
                response = exchangePackets(packet);
            } catch(Exception ex) {
                return null;
                // FIXME
            }
            //We actually ignore (ack) responses if it is not last packet to send
        }
        if (response == null) {
            LOG.debug("Timeout on receive");
            //FIXME: Log timeout
            return null;
        }

        if (response.getPacketType() == PacketType.Ack) {
            LOG.warn("Received ack instead of real response");
            //FIXME: we received ack instead of real response, something is wrong
            incrementPacketNumber(1);
            return null;
        }

        OmnipodMessage receivedMessage = null;
        byte[] receivedMessageData = response.getEncodedMessage();
        while(receivedMessage == null) {
            receivedMessage = OmnipodMessage.TryDecode(receivedMessageData);
            if (receivedMessage == null) {
                OmnipodPacket ackForCon = makeAckPacket(packetAddress, ackAddressOverride);
                try {
                    OmnipodPacket conPacket = exchangePackets(ackForCon, 3, 40);
                    if (conPacket.getPacketType() != PacketType.Con) {
                        LOG.debug("Received a non-con packet type:", conPacket.getPacketType());
                        //FIXME: We should throw an error as we expect only continuation packets
                        return null;
                    }
                    receivedMessageData = ByteUtil.concat(receivedMessageData, conPacket.getEncodedMessage());
                } catch(RileyLinkCommunicationException ex) {
                    // FIXME
                    return null;
                }
            }
        }
        incrementMessageNumber(2);

        ackUntilQuiet(packetAddress, ackAddressOverride);

        MessageBlock[] messageBlocks = receivedMessage.getMessageBlocks();
        if (messageBlocks.length == 0) {
            LOG.debug("Not enough data");
            //FIXME: We should throw an error of not enough data
            return null;
        }

        MessageBlock block = messageBlocks[0];
        if (block.getType() == MessageBlockType.ErrorResponse) {
            ErrorResponse error = (ErrorResponse)block;
            if (error.getErrorResponseType() == ErrorResponseType.BadNonce) {
                LOG.debug("Nonce out-of-sync");
                //FIXME: Log that we have nonce out-of-sync
                if (podState != null) {
                    this.podState.resyncNonce(error.getNonceSearchKey(), this.podState.getCurrentNonce(), message.getSequenceNumber());
                }
            }
            //FIXME: we should log and throw the error
            return null;
        }

        T responseBlock = (T) block;
        //FIXME: Log content here
        return responseBlock;
    }

    private void incrementMessageNumber(int increment) {
        if (podState == null) {
            messageNumber = (messageNumber + increment) & 0b1111;
        } else {
            podState.messageNumber = (podState.messageNumber + increment) & 0b1111;
        }
    }

    private void incrementPacketNumber(int increment) {
        if (podState == null) {
            packetNumber = (packetNumber + increment) & 0b11111;
        } else {
            podState.packetNumber = (podState.packetNumber + increment) & 0b11111;
        }
    }

    private OmnipodPacket makeAckPacket(Integer packetAddress, Integer messageAddress) {
        int pktAddress = defaultAddress;
        int msgAddress = defaultAddress;
        if (this.podState != null) {
            pktAddress = msgAddress = podState.Address;
        }
        if (packetAddress != null) {
            pktAddress = packetAddress;
        }
        if (messageAddress != null) {
            msgAddress = messageAddress;
        }
        return new OmnipodPacket(pktAddress, PacketType.Ack, podState == null ? packetNumber : podState.packetNumber, ByteUtil.getBytesFromInt(msgAddress));
    }

    private void ackUntilQuiet(Integer packetAddress, Integer messageAddress) {
        OmnipodPacket ack = makeAckPacket(packetAddress, messageAddress);
        Boolean quiet = false;
        while(!quiet) {
            try {
                OmnipodPacket response = sendAndListen(ack, 600, 5, 40, OmnipodPacket.class);
                //FIXME: instead of this crappy core we should make a proper timeout handling (exception-based?)
                if (response == null || (!response.isValid() && response.getPacketType() == PacketType.Invalid)) {
                    quiet = true;
                }
            } catch(Exception ex) {
                // FIXME
            }
        }
        incrementPacketNumber(1);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet) throws RileyLinkCommunicationException {
        return exchangePackets(packet, 0, 250,20000, 127);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int repeatCount, int preambleExtension_ms) throws RileyLinkCommunicationException {
        return exchangePackets(packet, repeatCount, 250, 20000, preambleExtension_ms);
    }

    private OmnipodPacket exchangePackets(OmnipodPacket packet, int repeatCount, int responseTimeout_ms, int exchangeTimeout_ms, int preambleExtension_ms) throws RileyLinkCommunicationException {
        int radioRetriesCount = 20;
        long timeoutTime = System.currentTimeMillis() + exchangeTimeout_ms;
        while(System.currentTimeMillis() < timeoutTime) {
            OmnipodPacket response = sendAndListen(packet, responseTimeout_ms, repeatCount, preambleExtension_ms, OmnipodPacket.class);
            if (response == null || !response.isValid()) {
                continue;
            }
            if (response.getAddress() != packet.getAddress()) {
                continue;
            }
            if (response.getSequenceNumber() != (((podState == null ? packetNumber : podState.packetNumber) + 1) & 0b11111)) {
                continue;
            }

            incrementPacketNumber(2);
            return response;

        }
        //FIXME: throw timeout (no response) exception
        return null;
    }

    public <T extends MessageBlock> T sendCommand(MessageBlock command) {
        int msgAddress = defaultAddress;
        if (this.podState != null) {
            msgAddress = podState.Address;
        }
        OmnipodMessage message = new OmnipodMessage(msgAddress, new MessageBlock[]{command}, podState == null ? messageNumber : podState.messageNumber);
        return exchangeMessages(message);
    }

    private int nonceValue() {
        if (this.podState == null) {
            //FIXME: we should have a set of application-level meaningfull exceptions
            throw new IllegalArgumentException("Getting nonce without active pod");
        }
        return podState.getCurrentNonce();
    }

    private void advanceToNextNonce() {
        if (this.podState == null) {
            //FIXME: we should have a set of application-level meaningfull exceptions
            throw new IllegalArgumentException("Getting nonce without active pod");
        }
        podState.advanceToNextNonce();
    }

    public Object initializePod() {
        if (SP.contains(OmniPodConst.Prefs.PodState)) {
            //FIXME: We should ask "are you sure?"
            SP.remove(OmniPodConst.Prefs.PodState);
        }

        Random rnd = new Random();
        int newAddress = rnd.nextInt();
        this.packetNumber = 0x0A;
        this.messageNumber = 0;
        newAddress = 0x1f05e70b;
        //LOG.debug("New address: " + ByteUtil.shortHexString());
        newAddress = (newAddress & 0x001fffff) | 0x1f000000;
        AssignAddressCommand assignAddress = new AssignAddressCommand(newAddress);
        OmnipodMessage assignAddressMessage = new OmnipodMessage(defaultAddress, new MessageBlock[] { assignAddress }, messageNumber);

        ConfigResponse config = exchangeMessages(assignAddressMessage, defaultAddress, newAddress);
        if (config == null) {
            LOG.debug("config is null (timeout or wrong answer)");
            //FIXME: show communication timeout
            // FIXME throw exception
            return null;
        }

        //DateTime activationDate = DateTime.now();
        DateTime activationDate = new DateTime(2018,8,18,20,5);

        //at this point for an unknown reason PDM starts counting messages from 0 again
        messageNumber = 0;
        ConfirmPairingCommand confirmPairing = new ConfirmPairingCommand(newAddress, activationDate,
                config.lot, config.tid);
        OmnipodMessage confirmPairingMessage = new OmnipodMessage(defaultAddress,
                new MessageBlock[] { confirmPairing }, messageNumber);
        ConfigResponse config2 = exchangeMessages(confirmPairingMessage, defaultAddress, newAddress);
        if (config == null) {
            LOG.debug("Second command timeout, that's bad");
            // FIXME throw exception
            return null;
        }

        if (config2.podProgressState != PodProgressState.PairingSuccess) {
            //FIXME: Log invalid data (we should have received a paired-state response
            LOG.error("Invalid pairing state");
            // FIXME throw exception
            return null;
        }

        this.podState = new PodState(newAddress, activationDate, config2.piVersion,
                config2.pmVersion, config2.lot, config2.tid, packetNumber, messageNumber);

        packetNumber = messageNumber = null;

        ExpirationAdvisory expirationAdvisory =
                new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.Reservoir, 50);
        AlertConfiguration lweReservoir =
                new AlertConfiguration(AlertType.LowReservoir,true,false,0,
                expirationAdvisory, BeepRepeat.EveryMinuteFor3MinutesRepeatEvery60Minutes, BeepType.FourBipBeeps);

        int nonce = nonceValue();

        ConfigureAlertsCommand lowReservoirCommand = new ConfigureAlertsCommand(nonce, new AlertConfiguration[] { lweReservoir });
        StatusResponse status = sendCommand(lowReservoirCommand);
        advanceToNextNonce();

        ExpirationAdvisory insertionTimerExpirationAdvisory = new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.Timer, new Duration(5 * 60 * 1000));

        AlertConfiguration insertionTimer = new AlertConfiguration(AlertType.TimerLimit,true,false,
                55, insertionTimerExpirationAdvisory, BeepRepeat.Every5Minutes, BeepType.FourBipBeeps);

        ConfigureAlertsCommand insertionTimerCommand = new ConfigureAlertsCommand(nonceValue(), new AlertConfiguration[] { insertionTimer });
        status = sendCommand(insertionTimerCommand);
        advanceToNextNonce();

        double primeUnits = 2.6;
        Bolus primeBolus = new Bolus(primeUnits, 8);
        SetInsulinScheduleCommand primeCommand = new SetInsulinScheduleCommand(nonceValue(), primeBolus);
        BolusExtraCommand extraBolusCommand = new BolusExtraCommand(primeUnits, (byte) 0, ByteUtil.fromHexString("000186a0"));
        OmnipodMessage prime = new OmnipodMessage(newAddress, new MessageBlock[]{primeCommand, extraBolusCommand}, podState.messageNumber);
        status = exchangeMessages(prime);
        advanceToNextNonce();

        Gson gson = Utils.gsonDateTime();
        String s = gson.toJson(podState);
        SP.putString(OmniPodConst.Prefs.PodState, s);

        // FIXME return what?
        return "OK";
    }

    public Object finishPrime() {
        if (this.podState == null) {
            String serialized = "{\"ActivatedAt\":\"2018-08-18T20:05:00.000Z\",\"Address\":520480523,\"Lot\":43687,\"PiVersion\":{\"major\":2,\"minor\":7,\"patch\":0},\"PmVersion\":{\"major\":2,\"minor\":7,\"patch\":0},\"Tid\":630145,\"nonceState\":{\"index\":3,\"table\":[79745181,413876894,1799221675,-381140322,1859143840,497915028,-1194513883,1557972065,1353375686,-736718945,541733452,-833859995,1587621096,-543494224,426786215,-1655021558,-1174888418,-1772703871,0,0,0]}}";
            Gson gson = Utils.gsonDateTime();
            this.podState = gson.fromJson(serialized, PodState.class);
        }

        ExpirationAdvisory expirationAdvisory = new ExpirationAdvisory(ExpirationAdvisory.ExpirationType.Timer, new Duration(70 * 60 * 60 * 1000 + 58 * 60 * 1000));

        AlertConfiguration alert = new AlertConfiguration(AlertType.ExpirationAdvisory,true,false,0, expirationAdvisory,
                BeepRepeat.EveryMinuteFor3MinutesRepeatEvery15Minutes, BeepType.FourBipBeeps);

        ConfigureAlertsCommand alertCommand = new ConfigureAlertsCommand(nonceValue(), new AlertConfiguration[] { alert });

        StatusResponse status = sendCommand(alertCommand);

        //Here goes new alarm settings and basal schedule set
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
        BasalSchedule schedule = new BasalSchedule(basals.toArray(new BasalScheduleEntry[0]));

        setBasalSchedule(schedule, false, Duration.ZERO, Duration.ZERO);

        // FIXME return what?
        return "OK";
    }

    public void setBasalSchedule(BasalSchedule schedule, boolean confidenceReminder, Duration scheduleOffset, Duration programReminderInterval) {
        SetInsulinScheduleCommand setBasal = new SetInsulinScheduleCommand(nonceValue(), schedule, scheduleOffset);
        //BasalScheduleExtraCommand extraCommand = new BasalScheduleExtraCommand(confidenceReminder, programReminderInterval,);
        //OmnipodMessage basalMessage = new OmnipodMessage(podState.Address, new MessageBlock[]{setBasal, extraCommand}, podState.messageNumber);
        //StatusResponse status = exchangeMessages(basalMessage);
    }
}
