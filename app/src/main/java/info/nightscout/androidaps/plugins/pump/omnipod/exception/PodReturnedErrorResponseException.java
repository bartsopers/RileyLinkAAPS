package info.nightscout.androidaps.plugins.pump.omnipod.exception;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.ErrorResponse;

public class PodReturnedErrorResponseException extends OmnipodException {
    public PodReturnedErrorResponseException(ErrorResponse errorResponse) {
        super("Pod returned error response: "+ errorResponse.getType().name());
    }

    public PodReturnedErrorResponseException(ErrorResponse errorResponse, Throwable cause) {
        super("Pod returned error response: "+ errorResponse.getType().name(), cause);
    }
}
