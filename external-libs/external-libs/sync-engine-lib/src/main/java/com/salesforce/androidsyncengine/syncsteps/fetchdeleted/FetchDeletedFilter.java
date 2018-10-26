package com.salesforce.androidsyncengine.syncsteps.fetchdeleted;

import android.content.Context;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import rx.functions.Func1;

/**
 * Class that filters out objects that shouldn't be included in {@link FetchDeletedSyncStep}.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
class FetchDeletedFilter implements Func1<String, Boolean> {

    private final SyncControls syncControls;

    public FetchDeletedFilter(SyncControls syncControls) {
        this.syncControls = syncControls;
    }

    @Override
    public Boolean call(String objectName) {
        Context context = syncControls.getContext();
        return  // Check if it was refreshed at least once.
                PreferenceUtils.getLastRefreshTime(objectName, context) != null
                // Check if is replicable.
                && PreferenceUtils.getReplicable(objectName, context)
                // Check if fetch for deleted is enabled.
                && PreferenceUtils.getCheckForDeleted(objectName, context);
    }
}
