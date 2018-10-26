package com.abinbev.dsa.sync;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.datamanager.DataManager;

/**
 * Created by mewa on 6/28/17.
 */

public class DefaultSyncBroadcastReceiver extends RegisteredReceiver<SyncListener> {
    private SyncListener syncListener;
    private boolean registered;

    public DefaultSyncBroadcastReceiver() {
    }

    @Override
    public void register(Context context, SyncListener listener) {
        this.syncListener = listener;
        register(context);
    }

    private synchronized void register(Context context) {
        if (registered)
            return;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.SYNC_ENGINE_ERROR);
        intentFilter.addAction(DataManager.SYNC_COMPLETED);
        intentFilter.addAction(DataManager.SYNC_ENGINE_FAILURE);
        intentFilter.addAction(DataManager.DYNAMIC_FETCH_COMPLETED);
        context.registerReceiver(this, intentFilter);
        registered = true;
    }

    @Override
    public synchronized void unregister(Context context, SyncListener listener) {
        if (registered) {
            registered = false;
            context.unregisterReceiver(this);
            syncListener = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (syncListener != null) {
            switch (intent.getAction()) {
                case DataManager.SYNC_COMPLETED:
                    syncListener.onSyncCompleted();
                    break;
                case DataManager.SYNC_ENGINE_ERROR:
                    syncListener.onSyncError(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    break;
                case DataManager.SYNC_ENGINE_FAILURE:
                    syncListener.onSyncFailure(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    break;
                case DataManager.DYNAMIC_FETCH_COMPLETED:
                    syncListener.onSyncCompleted();
                default:
            }
        }
    }
}
