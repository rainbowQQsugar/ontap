package com.abinbev.dsa.sync;

import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import java.util.Map;

/**
 * Created by Jakub Stefanowski on 17.10.2017.
 */

public interface DynamicFetchBroadcastListener {

    void onDynamicFetchStarted(String fetchName, SyncStatus syncStatus, Map<String, String> params);
    void onDynamicFetchProgress(String fetchName, SyncStatus syncStatus, Map<String, String> params);
    void onDynamicFetchCompleted(String fetchName, SyncStatus syncStatus, Map<String, String> params);
    void onDynamicFetchError(String fetchName, String message, Map<String, String> params);
}
