package com.abinbev.dsa.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.activity.LoginActivity;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;

/**
 * Service which can perform logout on separate thread. Empty intent is enough to start it.
 *
 * Created by Jakub Stefanowski on 18.01.2017.
 */

public class LogoutService extends IntentService {

    private static final String TAG = "LogoutService";

    public LogoutService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        clearLocalData();
        localLogOut();
        salesforceSDKLogOut();
        redirectToLogInScreen();
    }

    private void clearLocalData() {
        try {
            DataManager dataManager = DataManagerFactory.getDataManager();
            if (dataManager != null) {
                dataManager.clearLocalData(this, true);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error when calling DataManager clearLocalData()");
        }
    }

    private void localLogOut() {
        try {
            ClientManager clientManager = new ClientManager(this, SalesforceSDKManager.getInstance().getAccountType(),
                    SalesforceSDKManager.getInstance().getLoginOptions(),
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

            RestClient client = clientManager.peekRestClient();
            DataManager dataManager = DataManagerFactory.getDataManager();
            if (dataManager != null) {
                dataManager.logout(this, client);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error when calling DataManager logout()");
        }
    }

    private void salesforceSDKLogOut() {
        try {
            SmartStoreSDKManager.getInstance().logout(null, false);
        }
        catch (Exception e) {
            Log.e(TAG, "Error when calling SmartStoreSDKManager logout()");
        }
    }

    private void redirectToLogInScreen() {
        Intent activityIntent = new Intent(this, LoginActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activityIntent);
    }
}
