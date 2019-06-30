package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.MessageBlockType;

// TODO implement
public class BeepConfigCommand extends MessageBlock {
    @Override
    public MessageBlockType getType() {
        return MessageBlockType.BEEP_CONFIG;
    }
}
