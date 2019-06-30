package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfo;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoConfiguredAlerts;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoDataLog;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoFault;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoFaultEvent;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoResetStatus;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoTester;

// https://github.com/openaps/openomni/wiki/Command-0E-Status-Request
public enum PodInfoType {
    NORMAL((byte)0x00),
    CONFIGURED_ALERTS((byte)0x01),
    FAULT_EVENTS((byte)0x02),
    DATA_LOG((byte)0x03),
    FAULT((byte)0x05),
    HARDCODED_TEST_VALUES((byte)0x06),
    RESET_STATUS((byte)0x46), // including state, initialization time, any faults
    RECENT_FLASH_LOG_DUMP((byte)0x50),
    OLDER_FLASH_LOG_DUMP((byte)0x51); // but dumps entries before the last 50

    private final byte value;

    PodInfoType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static PodInfoType fromByte(byte value) {
        for (PodInfoType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("PodInfoType not implemented: "+ value);
    }

    public PodInfo decode(byte[] encodedData) {
        switch(this) {
            case NORMAL:
                // TODO Refactor StatusResponse to also allow a PodInfo subclass for
                //  PodInfoType.NORMAL, as the contents of these responses should be identical
                throw new UnsupportedOperationException("Cannot decode PodInfoType.NORMAL");
            case CONFIGURED_ALERTS:
                return new PodInfoConfiguredAlerts(encodedData);
            case FAULT_EVENTS:
                return new PodInfoFaultEvent(encodedData);
            case DATA_LOG:
                return new PodInfoDataLog(encodedData);
            case FAULT:
                return new PodInfoFault(encodedData);
            case HARDCODED_TEST_VALUES:
                return new PodInfoTester(encodedData);
            case RESET_STATUS:
                return new PodInfoResetStatus(encodedData);
            case RECENT_FLASH_LOG_DUMP:
            case OLDER_FLASH_LOG_DUMP:
                // TODO
            default:
                throw new IllegalArgumentException("PodInfoType not implemented: "+ encodedData);
        }
    }
}