package com.abinbev.dsa.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.view.SyncProgressView;
import com.abinbev.dsa.utils.crashreport.CrashReportManager;
import com.abinbev.dsa.utils.crashreport.CrashReportManagerProvider;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.security.PasscodeManager;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsdk.util.EventsObservable;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.app.sync.ClearAllTempTemporaryFilesTask;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;

/**
 * Created by wandersonblough on 1/24/16.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    @Bind(R.id.sync_progress_view)
    SyncProgressView syncProgressView;

    private PasscodeManager passcodeManager;
    private SyncListener syncListener;
    private boolean alreadyStarted;

    // Timeouts.
    private static final int CONNECT_TIMEOUT = 45;
    private static final int READ_TIMEOUT = 60;


    private BroadcastReceiver syncEngineBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DataManager.SYNC_COMPLETED:
                    Log.d(TAG, "onReceive: SYNC COMPLETE");
                    if (syncListener != null) {
                        syncListener.onSyncCompleted();
                    }
                    boolean showQueueDialog = !isRequestQueueEmpty();
                    goToMainScreen(showQueueDialog);
                    break;
                case DataManager.SYNC_ENGINE_ERROR:
                    Log.e(TAG, "onReceive: SYNC_ERROR");
                    if (syncListener != null) {
                        syncListener.onSyncError(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }
                    break;
                case DataManager.SYNC_ENGINE_FAILURE:
                    Log.e(TAG, "onReceive: SYNC_FAILURE");
                    if (syncListener != null) {
                        syncListener.onSyncFailure(intent.getStringExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE));
                    }
                    break;
                case ClientManager.ACCESS_TOKEN_REFRESH_INTENT:
                    Log.d(TAG, "onReceive: ACCESS_TOKEN_REFRESH_INTENT");
                    break;
                case ClientManager.INSTANCE_URL_UPDATE_INTENT:
                    Log.d(TAG, "onReceive: INSTANCE_URL_UPDATE_INTENT");
                    break;
                case ClientManager.ACCESS_TOKEN_REVOKE_INTENT:
                    // TODO: We should redirect the user to login activity in this case
                    Log.d(TAG, "onReceive: ACCESS_TOKEN_REVOKE_INTENT");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        syncProgressView.setVisibility(View.GONE);

        // Gets an instance of the passcode manager.
        passcodeManager = SalesforceSDKManager.getInstance().getPasscodeManager();

        // Lets observers know that activity creation is complete.
        EventsObservable.get().notifyEvent(EventsObservable.EventType.MainActivityCreateComplete, this);
        syncListener = syncProgressView;
        Log.e(TAG, "onCreate: ");
        ClearAllTempTemporaryFilesTask clearAllTempTemporaryFilesTask = new ClearAllTempTemporaryFilesTask();
        clearAllTempTemporaryFilesTask.execute(getExternalFilesDir(null).getAbsolutePath());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!syncProgressView.isInProgress()) {
            passcodeChallenge();
        }
        registerSyncEngineReceiver();
    }

    @Override
    protected void onPause() {
        passcodeManager.onPause(this);
        unregisterReceiver(syncEngineBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        passcodeManager.reset(this);
        super.onDestroy();
    }

    private void registerSyncEngineReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataManager.SYNC_COMPLETED);
        intentFilter.addAction(DataManager.SYNC_ENGINE_ERROR);
        intentFilter.addAction(DataManager.SYNC_ENGINE_FAILURE);
        intentFilter.addAction(ClientManager.ACCESS_TOKEN_REFRESH_INTENT);
        intentFilter.addAction(ClientManager.INSTANCE_URL_UPDATE_INTENT);
        intentFilter.addAction(ClientManager.ACCESS_TOKEN_REVOKE_INTENT);
        registerReceiver(syncEngineBroadcastReceiver, intentFilter);
    }

    private void passcodeChallenge() {
        // Brings up the passcode screen if needed.
        if (passcodeManager.onResume(this)) {

            // work around to avoid a memory leak when we call getRestClient with the activity

            try {
                ClientManager clientManager = new ClientManager(getApplicationContext(),
                        SalesforceSDKManager.getInstance().getAccountType(),
                        SalesforceSDKManager.getInstance().getLoginOptions(),
                        SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

                RestClient client = clientManager.peekRestClient();
                if (client != null) {
                    onResumeClient(client);
                    return;
                }
            } catch (Exception e) {
                // continue with the below flow if we get any exceptions or if the client is null
            }


            // Gets login options.
            final String accountType = SalesforceSDKManager.getInstance().getAccountType();
            final ClientManager.LoginOptions loginOptions = SalesforceSDKManager.getInstance().getLoginOptions();

            // Gets a rest client.
            new ClientManager(getApplicationContext(), accountType, loginOptions,
                    SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked()).getRestClient(this, new ClientManager.RestClientCallback() {

                @Override
                public void authenticatedRestClient(RestClient client) {
                    if (client == null) {
                        SalesforceSDKManager.getInstance().logout(LoginActivity.this);
                        return;
                    }
                    onResumeClient(client);

                    // Lets observers know that rendition is complete.
                    EventsObservable.get().notifyEvent(EventsObservable.EventType.RenditionComplete);
                }
            });
        }
    }

    private void onResumeClient(RestClient client) {
        Log.i(TAG, "in onResumeClient!");

        OkHttpClient okHttpClient = client.getOkHttpClient();
                OkHttpClient.Builder builder = okHttpClient.newBuilder()
                                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
                client.setOkHttpClient(builder.build());

        // this is to handle the case that the screen is powered off and powered on
        // after the first sync is completed.
        if (DataManagerFactory.getDataManager().isFirstSyncComplete(this)) {
            goToMainScreen();
            return;
        }

        if (alreadyStarted) {
            return;
        } else {
            alreadyStarted = true;
        }

        logUser(client);
        syncProgressView.setVisibility(View.VISIBLE);
        syncProgressView.initDataManger();

        try {
            if (DataManagerFactory.getDataManager().isFirstSyncComplete(this)) {
                goToMainScreen();
            } else {
                syncProgressView.startSync(false, true);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Manifest Error: ", e);
            Toast.makeText(this, "Invalid manifest file provided. Please check logcat for errors.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "got exception! " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void goToMainScreen() {
        goToMainScreen(false);
    }

    public void goToMainScreen(boolean showQueueDialog) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(UserDetailsActivity.ARG_SHOW_QUEUE_DIALOG, showQueueDialog);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void logUser(RestClient client) {
        CrashReportManager crManager = CrashReportManagerProvider.getInstance();
        crManager.setUserDetails(client.getClientInfo().username, client.getClientInfo().userId);
    }

    public void logoutUser() {
        syncProgressView.setLoggingOut();

        ClientManager clientManager = new ClientManager(LoginActivity.this, SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

        RestClient client = clientManager.peekRestClient();
        DataManager dataManager = DataManagerFactory.getDataManager();
        if (dataManager != null) {
            dataManager.clearLocalData(LoginActivity.this, true);
            dataManager.logout(LoginActivity.this, client);
        }

        SmartStoreSDKManager.getInstance().logout(LoginActivity.this, false);
        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private boolean isRequestQueueEmpty() {
        return DataManagerFactory.getDataManager().getRecordCount("Queue", null) < 1;
    }
}
