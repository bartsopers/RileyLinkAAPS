package info.nightscout.androidaps.plugins.pump.omnipod.exception;

public class OmnipodCommunicationException extends RuntimeException {
    public OmnipodCommunicationException(String message) {
        super(message);
    }

    public OmnipodCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
