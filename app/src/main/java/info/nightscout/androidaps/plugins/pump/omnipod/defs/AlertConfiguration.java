package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.Duration;

public class AlertConfiguration {
    public static final int LENGTH = 6;

    private final AlertSlot alertSlot;
    private final Duration duration;
    private final AlertTrigger alertTrigger;
    private final BeepRepeat beepRepeat;
    private final BeepType beepType;
    private final boolean active;
    private final boolean autoOffModifier;

    public AlertConfiguration(AlertSlot alertSlot, Duration duration, AlertTrigger alertTrigger,
                              BeepRepeat beepRepeat, BeepType beepType, boolean active, boolean autoOffModifier) {
        this.alertSlot = alertSlot;
        this.duration = duration;
        this.alertTrigger = alertTrigger;
        this.beepRepeat = beepRepeat;
        this.beepType = beepType;
        this.active = active;
        this.autoOffModifier = autoOffModifier;
    }

    public AlertConfiguration(AlertSlot alertSlot, Duration duration, AlertTrigger alertTrigger,
                              BeepRepeat beepRepeat, BeepType beepType) {
        this(alertSlot, duration, alertTrigger, beepRepeat, beepType, true, false);
    }

    public AlertSlot getAlertSlot() {
        return alertSlot;
    }

    public Duration getDuration() {
        return duration;
    }

    public AlertTrigger<?> getAlertTrigger() {
        return alertTrigger;
    }

    public BeepRepeat getBeepRepeat() {
        return beepRepeat;
    }

    public BeepType getBeepType() {
        return beepType;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAutoOffModifier() {
        return autoOffModifier;
    }
}
