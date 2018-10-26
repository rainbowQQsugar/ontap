package com.abinbev.dsa.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.utils.PermissionManager;

/**
 * Created by Jakub Stefanowski on 09.11.2016.
 */
public class SyncCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "SyncCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received sync completed intent. Trying to refresh PermissionManager data.");
        PermissionManager.refreshInstance();
    }
}
