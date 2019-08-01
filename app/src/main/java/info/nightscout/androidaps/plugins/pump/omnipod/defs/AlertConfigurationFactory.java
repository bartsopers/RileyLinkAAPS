package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import org.joda.time.Duration;

public class AlertConfigurationFactory {
    public static AlertConfiguration createExpirationAdvisoryAlertConfiguration(Duration timeUntilAlert, Duration duration) {
        return new AlertConfiguration(AlertSlot.SLOT7, true, false, duration,
                new TimerAlertTrigger(timeUntilAlert), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_60_MINUTES);
    }

    public static AlertConfiguration createShutdownImminentAlertConfiguration(Duration timeUntilAlert) {
        return new AlertConfiguration(AlertSlot.SLOT2, true, false, Duration.ZERO,
                new TimerAlertTrigger(timeUntilAlert), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_15_MINUTES);
    }

    public static AlertConfiguration createAutoOffAlertConfiguration(boolean active, Duration countdownDuration) {
        return new AlertConfiguration(AlertSlot.SLOT0, active, true,
                Duration.standardMinutes(15), new TimerAlertTrigger(countdownDuration), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_MINUTE_FOR_15_MINUTES);
    }

    public static AlertConfiguration createFinishSetupReminderAlertConfiguration() {
        return new AlertConfiguration(AlertSlot.SLOT7, true, false,
                Duration.standardMinutes(55), new TimerAlertTrigger(Duration.standardMinutes(5)), BeepType.BIP_BEEP_BIP_BEEP_BIP_BEEP_BIP_BEEP, BeepRepeat.EVERY_5_MINUTES);
    }
}
