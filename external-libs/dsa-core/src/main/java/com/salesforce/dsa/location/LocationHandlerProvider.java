package com.salesforce.dsa.location;

import com.salesforce.dsa.BuildConfig;

/**
 * Created by jstafanowski on 23.02.18.
 */

public final class LocationHandlerProvider {

    private LocationHandlerProvider() {}

    public static LocationHandler createLocationHandler(LocationReceiver receiver) {
        if (BuildConfig.CHINA_BUILD) {
            return new AMapLocationHandler(receiver);
        }
        else {
            return new GmsLocationHandler(receiver);
        }
    }
}
