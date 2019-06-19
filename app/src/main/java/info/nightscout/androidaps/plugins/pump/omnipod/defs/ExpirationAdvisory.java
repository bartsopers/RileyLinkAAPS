package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.Duration;

// https://github.com/openaps/openomni/wiki/Command-19-Configure-Alerts
// TODO can this be replaced by AlertTrigger?
public class ExpirationAdvisory {

    public Duration timeToExpire;
    public ExpirationType expirationType;
    public double reservoirLevel;

    public enum ExpirationType {
        RESERVOIR(4),
        TIMER(0);
        
        byte value;

        ExpirationType(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }
    }

    public ExpirationAdvisory(ExpirationType type, double reservoirLevel) {

        if (type != ExpirationType.RESERVOIR) {
            throw new IllegalArgumentException("Wrong mix of arguments");
        }
        this.expirationType = type;
        this.reservoirLevel = reservoirLevel;

    }
    public ExpirationAdvisory(ExpirationType type, Duration timeToExpire) {

        if (type != ExpirationType.TIMER) {
            throw new IllegalArgumentException("Wrong mix of arguments");
        }
        this.expirationType = type;
        this.timeToExpire = timeToExpire;
    }
}
