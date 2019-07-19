package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import java.util.Collections;
import java.util.List;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.NonceResyncableMessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

public class AcknowledgeAlertsCommand extends NonceResyncableMessageBlock {

    private int nonce;
    private final List<AlertType> alertTypes;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ACKNOWLEDGE_ALERT;
    }

    public AcknowledgeAlertsCommand(int nonce, List<AlertType> alertTypes) {
        this.nonce = nonce;
        this.alertTypes = alertTypes;
        encode();
    }

    public AcknowledgeAlertsCommand(int nonce, AlertType alertType) {
        this(nonce, Collections.singletonList(alertType));
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        byte alertTypeBits = 0;
        for (AlertType alertType:alertTypes) {
            alertTypeBits |= (0x01 << alertType.getValue());
        }
        encodedData = ByteUtil.concat(encodedData, alertTypeBits);
    }

    @Override
    public int getNonce() {
        return nonce;
    }

    @Override
    public void setNonce(int nonce) {
        this.nonce = nonce;
        encode();
    }
}
