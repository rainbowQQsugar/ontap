/**
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2013 Salesforce. All rights reserved.
 */

package com.salesforce.dsa.location;

import android.app.Activity;
import android.location.Location;

public interface LocationReceiver {
    Activity getReceivingActivity();

    int getFailureResolutionRequestCode();

    void handleUnresolvedError(int errorCode);

    void onNewLocationReceived(Location location);

    void onConnected();
}
