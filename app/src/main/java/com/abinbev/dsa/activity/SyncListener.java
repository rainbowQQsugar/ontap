package com.abinbev.dsa.activity;

/**
 * Created by wandersonblough on 1/14/16.
 */
public interface SyncListener {

    void onSyncCompleted();

    void onSyncError(String message);

    void onSyncFailure(String message);
}
