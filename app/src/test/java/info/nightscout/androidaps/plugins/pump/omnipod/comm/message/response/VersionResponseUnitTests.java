package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;

import static org.junit.Assert.assertEquals;

public class VersionResponseUnitTests {
    @Test
    public void testVersionResponse() {
        VersionResponse versionResponse = new VersionResponse(ByteUtil.fromHexString("011502070002070002020000a64000097c279c1f08ced2"));

        assertEquals(0x1f08ced2, versionResponse.address);
        assertEquals(42560, versionResponse.lot);
        assertEquals(621607, versionResponse.tid);
        assertEquals("2.7.0", versionResponse.piVersion.toString());
        assertEquals("2.7.0", versionResponse.pmVersion.toString());
    }

    @Test
    public void testLongVersionResponse() {
        VersionResponse versionResponse = new VersionResponse(ByteUtil.fromHexString("011b13881008340a5002070002070002030000a62b000447941f00ee878352"));

        assertEquals(0x1f00ee87, versionResponse.address);
        assertEquals(42539, versionResponse.lot);
        assertEquals(280468, versionResponse.tid);
        assertEquals("2.7.0", versionResponse.piVersion.toString());
        assertEquals("2.7.0", versionResponse.pmVersion.toString());
    }
}
