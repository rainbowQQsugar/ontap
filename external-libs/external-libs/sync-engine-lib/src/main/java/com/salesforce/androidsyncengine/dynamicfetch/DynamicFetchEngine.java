package com.salesforce.androidsyncengine.dynamicfetch;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.DynamicFetchPreferences;
import com.salesforce.androidsyncengine.datamanager.SFSyncHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.SyncEngine;
import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus.SyncStatusStage;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus.SyncStatusState;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.DynamicFetchConfig;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessorFactory;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.syncsteps.BasicObservableSyncStep;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.syncsteps.fetchdeleted.FetchDeletedConsumer;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;

import static com.salesforce.androidsyncengine.utils.BundleUtils.toBundle;

/**
 * Created by Jakub Stefanowski on 11.04.2017.
 */

public class DynamicFetchEngine implements SyncControls {

    private static final String TAG = "DynamicFetchEngine";

    private final Context context;

    private final SmartStoreDataManagerImpl dataManager;

    private final DynamicFetchPreferences preferences;

    private final SFSyncHelper sFSyncHelper;

    private final ManifestProcessor processor;

    private final SyncHelper syncHelper;

    private final String apiVersion;

    private final SyncStatus syncStatus;

    private final DownloadHelper downloadHelper;

    public DynamicFetchEngine(Context context, SFSyncHelper sFSyncHelper) {
        this.context = context.getApplicationContext();
        this.dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        this.preferences = DynamicFetchPreferences.getInstance(this.context);
        this.sFSyncHelper = sFSyncHelper;
        this.processor = ManifestProcessorFactory.getInstance().createProcessor();
        this.apiVersion = context.getString(R.string.api_version);
        this.syncHelper = createSyncHelper(context, apiVersion);
        this.syncStatus = new SyncStatus();
        this.downloadHelper = new DownloadHelper(context, syncHelper.getClient());
    }

    private static SyncHelper createSyncHelper(Context context, String apiVersion) {
        ClientManager clientManager = new ClientManager(context,
                SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

        RestClient client = clientManager.peekRestClient();
        return new SyncHelper(client, apiVersion, context);
    }

    public Set<String> fetchData(String fetchName, Map<String, String> params) throws SyncException, ManifestProcessingException {
        try {
            broadcastSyncStarted(fetchName, params);
            Set<String> fetchedObjects = fetchRecords(fetchName, params);

            fetchDeletedRecords(fetchName, fetchedObjects, params);

            sFSyncHelper.postDynamicFetch(fetchName, params, fetchedObjects);

            return fetchedObjects;
        }
        finally {
            broadcastSyncCompleted(fetchName, params);
        }
    }

    public static void fetchInBackground(Context context, String fetchName, Map<String, String> params) {
        SyncUtils.TriggerDynamicFetch(context, fetchName, params);

        if (!DeviceNetworkUtils.isConnected(context)) {
            String message = context.getString(R.string.sync_failure_message);
            context.sendBroadcast(new Intent(DataManager.DYNAMIC_FETCH_ERROR)
                    .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
                    .putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, message)
                    .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS, toBundle(params)));
        }
    }

    public boolean requiresFetch(String fetchName, Map<String, String> params) {
        if (TextUtils.isEmpty(fetchName)) return false;

        List<String> sortedObjects = PreferenceUtils.getSortedObjects(context);

        for (String objectName : sortedObjects) {

            SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
            if (objectMetadata == null) {
                throw new IllegalStateException("There is no SFObjectMetadata for: " + objectName);
            }

            DynamicFetchConfig fetchConfig = objectMetadata.getDynamicFetchByName(fetchName);
            if (fetchConfig != null) {

                if (requiresFetch(objectName, fetchConfig, params)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void broadcastSyncStarted(String fetchName, Map<String, String> params) {
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setStatus(SyncStatusState.INPROGRESS);
        syncStatus.setStage(SyncStatusStage.FETCH_RECORDS);
        syncStatus.setDescription("Started Sync");
        context.sendBroadcast(
                new Intent(DataManager.DYNAMIC_FETCH_STARTED)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_STATUS, syncStatus)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS, toBundle(params)));
    }

    private void broadcastFetchProgress(String fetchName, Map<String, String> params, String objectName, int recordsCount) {
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setStatus(SyncStatusState.INPROGRESS);
        syncStatus.setStage(SyncStatusStage.FETCH_RECORDS);
        syncStatus.setDescription("Fetching records for object: " + objectName + " - " + recordsCount);
        context.sendBroadcast(
                new Intent(DataManager.DYNAMIC_FETCH_PROGRESS)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_STATUS, syncStatus)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS, toBundle(params)));
    }

    private void broadcastGetDeleted(String fetchName, String objectName, Map<String, String> params) {
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setStatus(SyncStatusState.INPROGRESS);
        syncStatus.setStage(SyncStatusStage.GET_DELETED);
        syncStatus.setDescription("Fetching list of deleted records for object: " + objectName);
        context.sendBroadcast(
                new Intent(DataManager.DYNAMIC_FETCH_PROGRESS)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_STATUS, syncStatus)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS, toBundle(params)));
    }

    private void broadcastSyncCompleted(String fetchName, Map<String, String> params) {
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setStatus(SyncStatusState.COMPLETED);
        context.sendBroadcast(
                new Intent(DataManager.DYNAMIC_FETCH_COMPLETED)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_STATUS, syncStatus)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_NAME, fetchName)
                        .putExtra(DataManager.EXTRAS_DYNAMIC_FETCH_PARAMS, toBundle(params)));
    }

    private boolean dynamicFetchFromServer(SFObjectMetadata objectMetadata, DynamicFetchConfig fetchConfig, Map<String, String> params) throws SyncException {
        String objectName = objectMetadata.getNameWitoutNameSpace();
        String fetchName = fetchConfig.getName();

        preferences.storePendingFetch(objectName, fetchName, params);
        boolean success = false;

        try {
            Configuration configuration = PreferenceUtils.getConfiguration(context);

            List<String> queries = objectMetadata.getDynamicFetchQuery(context, processor, configuration, fetchName, params);
            broadcastFetchProgress(fetchName, params, objectName, 0);

            for (String query : queries) {
                SyncEngine.fetchRecordsForObject(context, syncHelper, dataManager, sFSyncHelper,
                        query, objectName, objectMetadata.getFieldsCount(),
                        size -> broadcastFetchProgress(fetchName, params, objectName, size));
            }
            success = true;
        } catch (ManifestProcessingException e) {
            throw new SimpleSyncException(e);
        } finally {
            if (success) {
                preferences.storeFinishedFetch(objectName, fetchName, params, System.currentTimeMillis());
            } else {
                preferences.storeFailedFetch(objectName, fetchName, params);
            }
        }

        return success;
    }

    private boolean requiresFetch(String objectName, DynamicFetchConfig fetchConfig, Map<String, String> params) {
        String fetchName = fetchConfig.getName();
        long fetchValidity = fetchConfig.getFetchValidity() * 1000L; // convert to milliseconds

        return !preferences.isFetchPending(objectName, fetchName, params) &&
                (preferences.getLastFetchTime(objectName, fetchName, params) + fetchValidity < System.currentTimeMillis());
    }

    /**
     * Fetches records for provided fetch name. Returns names of objects that were successfully
     * fetched.
     */
    private Set<String> fetchRecords(String fetchName, Map<String, String> params) throws SyncException, ManifestProcessingException {
        if (TextUtils.isEmpty(fetchName)) return Collections.emptySet();

        List<String> sortedObjects = PreferenceUtils.getSortedObjects(context);
        Set<String> fetchedObjectNames = new HashSet<>();

        for (String objectName : sortedObjects) {

            SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
            if (objectMetadata == null) {
                throw new IllegalStateException("There is no SFObjectMetadata for: " + objectName);
            }

            DynamicFetchConfig fetchConfig = objectMetadata.getDynamicFetchByName(fetchName);
            if (fetchConfig != null) {

                if (requiresFetch(objectName, fetchConfig, params)) {
                    boolean success = dynamicFetchFromServer(objectMetadata, fetchConfig, params);
                    if (success) {
                        fetchedObjectNames.add(objectName);
                    }
                } else {
                    Log.d(TAG, objectName + "." + fetchName + " doesn't require fetch.");
                }
            }
        }

        return fetchedObjectNames;
    }

    private void fetchDeletedRecords(final String fetchName, final Set<String> objectNames, Map<String, String> params) throws SyncException {
        new BasicObservableSyncStep()
                .setObservable(Observable.from(objectNames)
                        .filter(objectName -> shouldFetchDeletedRecords(objectName, fetchName))
                        .doOnNext(objectName -> broadcastGetDeleted(fetchName, objectName, params))
                        .compose(new FetchDeletedConsumer(this)))
                .execute(this);
    }

    private boolean shouldFetchDeletedRecords(String objectName, String fetchName) {
        SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
        if (objectMetadata != null) {
            DynamicFetchConfig fetchConfig = objectMetadata.getDynamicFetchByName(fetchName);
            if (fetchConfig != null) {
                return fetchConfig.isFetchDeletedRecords();
            }
        }

        return false;
    }

    @Override /* SyncControls */
    public Context getContext() {
        return context;
    }

    @Override /* SyncControls */
    public ManifestProcessor getManifestProcessor() {
        return processor;
    }

    @Override /* SyncControls */
    public SyncHelper getSyncHelper() {
        return syncHelper;
    }

    @Override /* SyncControls */
    public SyncStatus getStatus() {
        return syncStatus;
    }

    @Override /* SyncControls */
    public String getApiVersion() {
        return apiVersion;
    }

    @Override /* SyncControls */
    public SmartStoreDataManagerImpl getDataManager() {
        return dataManager;
    }

    @Override /* SyncControls */
    public DownloadHelper getDownloadHelper() {
        return downloadHelper;
    }

    @Override /* SyncControls */
    public Set<String> getObjectsToFetchDuringAutoSync() {
        return Collections.emptySet();
    }
}
