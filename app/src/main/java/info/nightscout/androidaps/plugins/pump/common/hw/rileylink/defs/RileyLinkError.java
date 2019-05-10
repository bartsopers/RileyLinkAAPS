package info.nightscout.androidaps.plugins.pump.common.hw.rileylink.defs;

/**
 * Created by andy on 14/05/2018.
 */

public enum RileyLinkError {

    ParametersNotSetOrInvalid, //
    GattDiscoveryFailed, //
    TuneUpOfDeviceFailed, //
    UnableToObtainBluetoothAdapter, //
    BluetoothDisabled, //
    GattDeviceNotFound, //
    RileyLinkUnreachable, //
    DeviceIsNotRileyLink, //

    NoBluetoothAdapter, //

    // Device
    NoContactWithDevice

}
