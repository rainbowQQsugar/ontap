package com.salesforce.androidsyncengine.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.SFSyncHelper;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;

/**
 * Created by Adam Chodera on 14.06.2017.
 */

public class DownloadOptionalContentService extends IntentService {

    private static final String TAG = DownloadOptionalContentService.class.getSimpleName();

    public DownloadOptionalContentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG, "onHandleIntent");

        try {
            ClientManager clientManager = new ClientManager(this,
                    SalesforceSDKManager.getInstance().getAccountType(),
                    SalesforceSDKManager.getInstance().getLoginOptions(),
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();
            String apiVersion = getString(R.string.api_version);

            SyncEngine syncEngine = new SyncEngine(this, client, apiVersion);

            SFSyncHelper customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(this);
            syncEngine.setAzureContainer(customSyncHelper.getAzureContainer());

            syncEngine.fetchOptionalContentFiles();
            customSyncHelper.postOptionalContentSync(this);
        } catch (Exception e) {
            Log.e(TAG, "got exception fetchOptionalContent: ", e);
        }
    }
}
