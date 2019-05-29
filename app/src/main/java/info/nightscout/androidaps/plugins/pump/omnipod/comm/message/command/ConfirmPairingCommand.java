package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

// https://github.com/openaps/openomni/wiki/Command-03-Setup-Pod
public class ConfirmPairingCommand extends MessageBlock {

    private int lot;
    private int tid;
    private DateTime date;
    private int address;

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.ConfirmPairing;
    }
    //FIXME: We should take care of timezones
    public ConfirmPairingCommand(int address, DateTime date, int lot, int tid) {
        this.address = address;
        this.lot = lot;
        this.tid = tid;
        this.date = date;
        encode();
    }

    private void encode() {
        byte[] data = new byte[0];
        data = ByteUtil.concat(data, ByteUtil.getBytesFromInt(address));
        data = ByteUtil.concat(data, new byte[] {(byte)0x14, (byte)0x04});
        data = ByteUtil.concat(data, new byte[] {(byte)date.monthOfYear().get(),
                (byte)(date.dayOfMonth().get()), (byte)(date.year().get() - 2000),
                (byte)date.hourOfDay().get(), (byte)date.minuteOfHour().get()});
        data = ByteUtil.concat(data, ByteUtil.getBytesFromInt(lot));
        data = ByteUtil.concat(data, ByteUtil.getBytesFromInt(tid));
        encodedData = data;
    }
}
