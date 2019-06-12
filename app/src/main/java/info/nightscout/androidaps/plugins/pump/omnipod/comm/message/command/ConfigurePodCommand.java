package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.joda.time.DateTime;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlock;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.MessageBlockType;

// https://github.com/openaps/openomni/wiki/Command-03-Setup-Pod
public class ConfigurePodCommand extends MessageBlock {

    private static byte PACKET_TIMEOUT_LIMIT = 0x04;

    private int lot;
    private int tid;
    private DateTime date;
    private int address;

    //FIXME: We should take care of timezones
    public ConfigurePodCommand(int address, DateTime date, int lot, int tid) {
        this.address = address;
        this.lot = lot;
        this.tid = tid;
        this.date = date;
        encode();
    }

    @Override
    public MessageBlockType getType() {
        return MessageBlockType.SETUP_POD;
    }

    private void encode() {
        encodedData = new byte[0];
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt(address));
        encodedData = ByteUtil.concat(encodedData, new byte[] { //
                (byte)0x14, // unknown
                PACKET_TIMEOUT_LIMIT, //
                (byte)date.monthOfYear().get(), //
                (byte)date.dayOfMonth().get(), //
                (byte)(date.year().get() - 2000), //
                (byte)date.hourOfDay().get(), //
                (byte)date.minuteOfHour().get() //
        });
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt(lot));
        encodedData = ByteUtil.concat(encodedData, ByteUtil.getBytesFromInt(tid));
    }
}
