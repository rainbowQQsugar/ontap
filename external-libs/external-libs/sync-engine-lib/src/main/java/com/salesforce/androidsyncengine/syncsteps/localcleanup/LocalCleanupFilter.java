package com.salesforce.androidsyncengine.syncsteps.localcleanup;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import rx.functions.Func1;

/**
 * Class that filters out objects for which the local cleanup should not be executed.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
class LocalCleanupFilter implements Func1<String, Boolean> {

    private final SyncControls syncControls;

    public LocalCleanupFilter(SyncControls syncControls) {
        this.syncControls = syncControls;
    }

    @Override
    public Boolean call(String objectName) {
        return  hasRefreshTime(syncControls, objectName);
    }

    private boolean hasRefreshTime(SyncControls syncControls, String objectName) {
        return PreferenceUtils.getLastRefreshTime(objectName, syncControls.getContext()) != null;
    }
}
