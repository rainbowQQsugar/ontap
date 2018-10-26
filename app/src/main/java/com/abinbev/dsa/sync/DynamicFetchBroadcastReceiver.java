package com.abinbev.dsa.sync;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.utils.BundleUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mewa on 6/28/17.
 */

public class DynamicFetchBroadcastReceiver extends RegisteredReceiver<DynamicFetchBroadcastListener> {

    private DynamicFetchBroadcastListener listener;

    private Set<String> dynamicFetchNames = new HashSet<>();

    private boolean registered;

    public DynamicFetchBroadcastReceiver() {
    }

    public void clearDynamicFetchNames() {
        dynamicFetchNames.clear();
    }

    public void addDynamicFetchName(String dynamicFetchName) {
        dynamicFetchNames.add(dynamicFetchName);
    }

    public void removeDynamicFetchName(String dynamicFetchName) {
        dynamicFetchNames.remove(dynamicFetchName);
    }

    @Override
    public void register(Context context, DynamicFetchBroadcastListener listener) {
        this.listener = listener;
        register(context);
    }

    private synchronized void register(Context context) {
        if (registered)
            return;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_STARTED);
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_PROGRESS);
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_COMPLETED);
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_ERROR);
        context.registerReceiver(this, intentFilter);
        registered = true;
    }

    @Override
    public synchronized void unregister(Context context, DynamicFetchBroadcastListener listener) {
        if (registered) {
            registered = false;
            context.unregisterReceiver(this);
            this.listener = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null) {
            String fetchName = intent.getStringExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME);

            if (dynamicFetchNames.isEmpty() || dynamicFetchNames.contains(fetchName)) {
                SyncStatus syncStatus = intent.getParcelableExtra(DataManager.EXTRAS_DYNAMIC_FETCH_STATUS);
                Bundle paramsBundle = intent.getBundleExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS);
                Map<String, String> params = BundleUtils.toStringMap(paramsBundle);

                switch (intent.getAction()) {
                    case DataManager.DYNAMIC_FETCH_STARTED:
                        listener.onDynamicFetchStarted(fetchName, syncStatus, params);
                        break;
                    case DataManager.DYNAMIC_FETCH_PROGRESS:
                        listener.onDynamicFetchProgress(fetchName, syncStatus, params);
                        break;
                    case DataManager.DYNAMIC_FETCH_COMPLETED:
                        listener.onDynamicFetchCompleted(fetchName, syncStatus, params);
                        break;
                    case DataManager.DYNAMIC_FETCH_ERROR:
                        String errorMessage = intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE);
                        listener.onDynamicFetchError(fetchName, errorMessage, params);
                        break;
                }
            }
        }
    }
}
