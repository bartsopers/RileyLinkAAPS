package info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response;

public class FirmwareVersion {
    private int major;
    private int minor;
    private int patch;

    public FirmwareVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", major, minor, patch);
    }
}
