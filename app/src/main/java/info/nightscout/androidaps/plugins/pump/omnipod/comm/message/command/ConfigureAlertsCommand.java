package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.AlertConfiguration;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

public class ConfigureAlertsCommand extends MessageBlock {


    private int nonce;
    private AlertConfiguration[] configurations;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfigureAlerts;
    }

    public ConfigureAlertsCommand(int nonce, AlertConfiguration[] configurations) {
        this.nonce = nonce;
        this.configurations = configurations;
        encode();
    }

    private void encode() {
        encodedData = ByteUtil.getBytesFromInt(nonce);
        for (AlertConfiguration config : configurations) {
            encodedData = ByteUtil.concat(encodedData, config.getRawData());
        }
    }
}
