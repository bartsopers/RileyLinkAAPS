package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;

import static org.junit.Assert.assertArrayEquals;

public class GetStatusCommandUnitTests {
    @Test
    public void testPodInfoTypeNormal() {
        GetStatusCommand getStatusCommand = new GetStatusCommand(PodInfoType.NORMAL);

        assertArrayEquals(ByteUtil.fromHexString("0e0100"), getStatusCommand.getRawData());
    }

    @Test
    public void testPodInfoTypeConfiguredAlerts() {
        GetStatusCommand getStatusCommand = new GetStatusCommand(PodInfoType.ACTIVE_ALERTS);

        assertArrayEquals(ByteUtil.fromHexString("0e0101"), getStatusCommand.getRawData());
    }

    @Test
    public void testPodInfoTypeFaultEvents() {
        GetStatusCommand getStatusCommand = new GetStatusCommand(PodInfoType.FAULT_EVENT);

        assertArrayEquals(ByteUtil.fromHexString("0e0102"), getStatusCommand.getRawData());
    }

    @Test
    public void testPodInfoTypeResetStatus() {
        GetStatusCommand getStatusCommand = new GetStatusCommand(PodInfoType.LOW_FLASH_DUMP_LOG);

        assertArrayEquals(ByteUtil.fromHexString("0e0146"), getStatusCommand.getRawData());
    }
}
