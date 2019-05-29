package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.Duration;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
public class ExpirationAdvisory {

    public Duration timeToExpire;
    public ExpirationType expirationType;
    public double reservoirLevel;

    public enum ExpirationType {
        Reservoir(4),
        Timer(0);
        byte value;

        ExpirationType(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }

    }
    public ExpirationAdvisory(ExpirationType type, double reservoirLevel) {
        if (type != ExpirationType.Reservoir) {
            throw new IllegalArgumentException("Wrong mix of arguments");
        }
        this.expirationType = type;
        this.reservoirLevel = reservoirLevel;

    }
    public ExpirationAdvisory(ExpirationType type, Duration timeToExpire) {
        if (type != ExpirationType.Timer) {
            throw new IllegalArgumentException("Wrong mix of arguments");
        }
        this.expirationType = type;
        this.timeToExpire = timeToExpire;
    }
}
