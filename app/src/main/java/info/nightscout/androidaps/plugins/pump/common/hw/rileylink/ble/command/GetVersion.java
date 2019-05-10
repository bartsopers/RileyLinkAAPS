package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.command;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkCommandType;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.ble.defs.RileyLinkFirmwareVersion;

public class GetVersion extends RileyLinkCommand {

    public GetVersion(RileyLinkFirmwareVersion version) {
        super(version);
    }


    @Override
    public RileyLinkCommandType getCommandType() {
        return RileyLinkCommandType.GetVersion;
    }


    @Override
    public byte[] getRaw() {
        return super.getRawSimple();
    }
}
