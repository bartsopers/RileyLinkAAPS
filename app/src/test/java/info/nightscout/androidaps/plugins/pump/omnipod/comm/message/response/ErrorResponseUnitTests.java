package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

import org.junit.Test;

import info.nightscout.androidaps.plugins.pump.common.utils.ByteUtil;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.ErrorResponseType;

import static org.junit.Assert.assertEquals;

public class ErrorResponseUnitTests {
    @Test
    public void testDecoding() {
        ErrorResponse errorResponse = new ErrorResponse(ByteUtil.fromHexString("060314fa92"));

        assertEquals(ErrorResponseType.BAD_NONCE, errorResponse.getErrorResponseType());
        // TODO add assertion one nonce search key (obtain captures first)
    }
}
