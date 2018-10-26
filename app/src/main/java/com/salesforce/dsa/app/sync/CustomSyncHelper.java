package com.salesforce.dsa.app.sync;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.datamanager.SFSyncHelper;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;

/**
 * Created by bduggirala on 1/2/16.
 */
public class CustomSyncHelper extends SFSyncHelper {

    private static final String TAG = CustomSyncHelper.class.getSimpleName();

    public CustomSyncHelper() {

    }

    @Override
    public void preSync(Context context, RestClient client) {
        Log.i(TAG, "in preSync");
    }

    @Override
    public void postDataSync(Context context, RestClient client) {
        Log.i(TAG, "in postDataSync");
        String activeConfigId = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DSAConstants.Constants.ACTIVE_CONFIG_ID, null);
        CategoryUtils.populateCategoryCacheHolder(context, activeConfigId);
    }

    @Override
    public void postContentSync(Context context) {
        Log.i(TAG, "in postContentSync");
    }

}
