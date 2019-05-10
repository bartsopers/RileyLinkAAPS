package com.gxwtech.roundtrip2.ServiceData;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceMessage;

/**
 * Created by geoff on 7/4/16.
 */
public class ServiceMessageUpdate extends ServiceMessage {
    public ServiceMessageUpdate() {
    }


    public void init() {
        map.putString("ServiceMessageType", "ServiceUpdateMessage");
    }
}
