package com.abinbev.dsa.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.SyncUtils;

public class TriggerReceiver extends BroadcastReceiver {

    public static String TAG = "com.abinbev.dsa.receiver.TriggerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG, "TriggerReceiver");
        SyncUtils.TriggerRefresh(context);

    }
}
