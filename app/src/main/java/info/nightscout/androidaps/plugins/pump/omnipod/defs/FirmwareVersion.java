package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import java.util.Locale;

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
        return String.format(Locale.getDefault(), "%d.%d.%d", major, minor, patch);
    }
}
