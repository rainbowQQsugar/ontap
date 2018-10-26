/**
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce. All rights reserved.
 */

package com.salesforce.dsa.location;

import com.google.android.gms.maps.model.LatLng;

public interface LocationHandler {

    void connect();

    void disconnect();

    void unregisterReceiver();

    LatLng getCurrentLatLng();

    String analyzeErrorCode(int errorCode);
}
