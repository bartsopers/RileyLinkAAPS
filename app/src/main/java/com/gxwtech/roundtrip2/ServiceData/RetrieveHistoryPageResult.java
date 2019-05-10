package com.gxwtech.roundtrip2.ServiceData;

import android.os.Bundle;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.data.ServiceResult;

/**
 * Created by geoff on 7/2/16.
 */
public class RetrieveHistoryPageResult extends ServiceResult {
    public RetrieveHistoryPageResult() {
    }


    public void setPageNumber(int pageNumber) {
        map.putInt("pageNumber", pageNumber);
    }


    public int getPageNumber() {
        return map.getInt("pageNumber", -1);
    }


    public void setPageBundle(Bundle pageBundle) {
        map.putBundle("pageBundle", pageBundle);
    }


    public Bundle getPageBundle() {
        return map.getBundle("pageBundle");
    }
}
