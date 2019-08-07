package info.nightscout.androidaps.plugins.pump.omnipod.defs;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfo;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoActiveAlerts;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoDataLog;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoFaultAndInitializationTime;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoFaultEvent;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoOlderHighFlashLogDump;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoRecentHighFlashLogDump;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoLowFlashLogDump;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.podinfo.PodInfoTestValues;

// https://github.com/openaps/openomni/wiki/Command-0E-Status-Request
public enum PodInfoType {
    NORMAL((byte)0x00),
    ACTIVE_ALERTS((byte)0x01),
    FAULT_EVENT((byte)0x02),
    DATA_LOG((byte)0x03), // Similar to types $50 & $51. Returns up to the last 60 dwords of data.
    FAULT_AND_INITIALIZATION_TIME((byte)0x05),
    HARDCODED_TEST_VALUES((byte)0x06),
    LOW_FLASH_DUMP_LOG((byte)0x46), // Starting at $4000
    RECENT_HIGH_FLASH_LOG_DUMP((byte)0x50),  // Starting at $4200
    OLDER_HIGH_FLASH_LOG_DUMP((byte)0x51); // Starting at $4200 but dumps entries before the last 50

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
        throw new IllegalArgumentException("Unknown PodInfoType: "+ value);
    }

    public PodInfo decode(byte[] encodedData, int bodyLength) {
        switch(this) {
            case NORMAL:
                // We've never observed a PodInfoResponse with 0x00 subtype
                // Instead, the pod returns a StatusResponse
                throw new UnsupportedOperationException("Cannot decode PodInfoType.NORMAL");
            case ACTIVE_ALERTS:
                return new PodInfoActiveAlerts(encodedData);
            case FAULT_EVENT:
                return new PodInfoFaultEvent(encodedData);
            case DATA_LOG:
                return new PodInfoDataLog(encodedData, bodyLength);
            case FAULT_AND_INITIALIZATION_TIME:
                return new PodInfoFaultAndInitializationTime(encodedData);
            case HARDCODED_TEST_VALUES:
                return new PodInfoTestValues(encodedData);
            case LOW_FLASH_DUMP_LOG:
                return new PodInfoLowFlashLogDump(encodedData);
            case RECENT_HIGH_FLASH_LOG_DUMP:
                return new PodInfoRecentHighFlashLogDump(encodedData, bodyLength);
            case OLDER_HIGH_FLASH_LOG_DUMP:
                return new PodInfoOlderHighFlashLogDump(encodedData);
            default:
                throw new IllegalArgumentException("Cannot decode "+ this.name());
        }
    }
}