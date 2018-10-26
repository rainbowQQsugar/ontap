package com.salesforce.androidsyncengine.datamanager;

import static com.salesforce.androidsyncengine.datamanager.DataManager.CONSTANTS_ID;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_AUTO_SYNC_MODE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_CHECKING_THE_PREVIOUS_STATE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_DECIDING_THE_MODE_OF_SYNC;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_DELTA_SYNC_MODE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_END_SYNC;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_FULL_SYNC_MODE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_CREATE_SOUPS_FOR_OBJECTS_DEFINED_IN_MANIFEST;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_AND_SAVE_THE_META_DATA_INFO;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_IDS_AND_PURGE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_REQUIRED_CONTENT_FILES_FROM_AZURE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_UPDATE_SYNC_STATUS;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_UPLOAD_DATA_TO_SERVER;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_INITIAL_SOME_RELATED_OBJECTS;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_START_SYNC;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimingLogger;

import com.google.gson.Gson;

import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableOperation;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestRequest.RestMethod;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.smartstore.store.DBOpenHelper;
import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.MobileSyncReporter.SyncType;
import com.salesforce.androidsyncengine.datamanager.exceptions.ConnectionLostException;
import com.salesforce.androidsyncengine.datamanager.exceptions.ResponseErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.UnsupportedEncodingSyncException;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus.SyncStatusStage;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus.SyncStatusState;
import com.salesforce.androidsyncengine.datamanager.soql.DateString;
import com.salesforce.androidsyncengine.datamanager.soql.QueryOp;
import com.salesforce.androidsyncengine.datamanager.synchelper.FetchResponseHandler;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;
import com.salesforce.androidsyncengine.services.DownloadOptionalContentService;
import com.salesforce.androidsyncengine.syncmanifest.ConfigObject;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.FieldToIndex;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessorFactory;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.syncmanifest.SyncDirection;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.syncsteps.ConditionalSyncStep;
import com.salesforce.androidsyncengine.syncsteps.SimpleCondition;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.syncsteps.SyncStep;
import com.salesforce.androidsyncengine.syncsteps.SyncStepGroup;
import com.salesforce.androidsyncengine.syncsteps.fetchdeleted.FetchDeletedSyncStep;
import com.salesforce.androidsyncengine.syncsteps.localcleanup.LocalCleanupSyncStep;
import com.salesforce.androidsyncengine.thread.ThreadTools;
import com.salesforce.androidsyncengine.utils.DeviceNetworkUtils;
import com.salesforce.androidsyncengine.utils.FLog;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.androidsyncengine.utils.SyncEngineTimingLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * SyncEngine: Class that actually does the syncing.
 *
 * @author Babu Duggirala
 */
public class SyncEngine implements SyncControls {

    private static final String TAG = "SyncEngine";

    static final SimpleDateFormat soqlQueryDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    static {
        soqlQueryDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US); // Mon, 14 Apr 2014 21:40:15 GMT

    private static final SyncStatus syncStatus = new SyncStatus();

    private static final String FIELD_FETCH_SQL_FORMAT = "SELECT {%1$s:%2$s} FROM {%1$s}";
    private static final String FIND_BY_FIELD_SQL_FORMAT = "SELECT {%1$s:%2$s} FROM {%1$s} WHERE {%1$s:%3$s} = '%4$s'";

    private Context context;
    private RestClient client;
    private String apiVersion;
    private boolean gotErrors;
    private SyncException lastError;
    private boolean gotFetchErrors;
    private SyncException lastFetchError;
    private SmartStoreDataManagerImpl dataManagerImpl;

    private static long syncStartTime;

    private HashSet<String> objectsToFetchDuringAutoSync;

    private SFSyncHelper customSyncHelper;

    private DownloadHelper downloadHelper;

    private SyncEnginePrefs preferences;

    private SyncHelper syncHelper;

    private ManifestProcessor manifestProcessor;

    //autoSync means just upload records in queue and get all updated records from those objects
    private boolean autoSync;

    private List<String> orderedObjectsSet;

    private final MobileSyncReporter mobileSyncReporter;

    private CloudTable azureTable;

    public SyncEngine(Context context, RestClient client, String apiVersion) {
        this.context = context;
        this.client = client;
        this.apiVersion = apiVersion;
        this.gotErrors = false;
        this.gotFetchErrors = false;
        this.syncHelper = new SyncHelper(client, apiVersion, context);
        this.preferences = new SyncEnginePrefs(context);
        this.downloadHelper = new DownloadHelper(context, client);
        this.mobileSyncReporter = new MobileSyncReporter(syncHelper, context);
        this.dataManagerImpl = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        this.orderedObjectsSet = PreferenceUtils.getSortedObjects(context);

        ManifestProcessorFactory processorFactory = ManifestProcessorFactory.getInstance();
        this.manifestProcessor = processorFactory.createProcessor();

        Log.i(TAG, "In SyncEngine: connection: " + client.getOkHttpClient().connectTimeoutMillis()
                + "\nread timeout: " + client.getOkHttpClient().readTimeoutMillis());
    }

    @Override /* SyncControls */
    public Context getContext() {
        return context;
    }

    @Override /* SyncControls */
    public ManifestProcessor getManifestProcessor() {
        return manifestProcessor;
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
        return dataManagerImpl;
    }

    @Override /* SyncControls */
    public DownloadHelper getDownloadHelper() {
        return downloadHelper;
    }

    @Override /* SyncControls */
    public Set<String> getObjectsToFetchDuringAutoSync() {
        return objectsToFetchDuringAutoSync;
    }

    public void startSync() throws SyncException {
        Log.i(TAG, "Beginning network synchronization");
        SyncEngineTimingLogger.getLogger().addDescription(P_START_SYNC);

        syncStartTime = System.currentTimeMillis();
        syncStatus.setStatus(SyncStatusState.INPROGRESS);
        syncStatus.setDescription("Started Sync");

        Intent syncCompletedBroadcastIntent = new Intent(DataManager.SYNC_STARTED);
        context.sendBroadcast(syncCompletedBroadcastIntent);

        boolean shouldSyncInCurrentNetwork = DeviceNetworkUtils.shouldPerformSync(context);

        if (!shouldSyncInCurrentNetwork) {
            //TODO report internet connection is needed

            return;
        }

        SyncEngineTimingLogger.getLogger().startSplit(P_CHECKING_THE_PREVIOUS_STATE);
        try {
            String syncDataSize = getSyncDataSizes(context, dataManagerImpl);
            Log.i(TAG, syncDataSize);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            // typically this will happen if the user is kicked out due to auth token being revoked
            if (errorMessage.contains("file is encrypted")) {
                // delete the smartstore database and try to reinitalize it
                Log.e(TAG, "trying to delete database");
                DBOpenHelper.deleteAllUserDatabases(context);
                // there is hope
                Log.e(TAG, "deleted database");
                DataManagerFactory.clearDataManager();
                dataManagerImpl = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
                PreferenceUtils.putFullSyncComplete(false, context);
                PreferenceUtils.putFirstLaunchComplete(false, context);
                PreferenceUtils.putManifestVersion(0, context);
            }
        }

        SyncEngineTimingLogger.getLogger().endSplit(P_CHECKING_THE_PREVIOUS_STATE);
        SyncEngineTimingLogger.getLogger().startSplit(P_INITIAL_SOME_RELATED_OBJECTS);
        objectsToFetchDuringAutoSync = new HashSet<>();
        customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(context);

        ensure_current_orgId_valid();

        autoSync = true;
        SyncEngineTimingLogger.getLogger().endSplit(P_INITIAL_SOME_RELATED_OBJECTS);
        SyncEngineTimingLogger.getLogger().startSplit(P_DECIDING_THE_MODE_OF_SYNC);
        if (PreferenceUtils.getManifestVersion(context) == 0) {
            SyncEngineTimingLogger.getLogger().endSplit(P_DECIDING_THE_MODE_OF_SYNC);
            perform_full_sync();
            SyncEngineTimingLogger.getLogger().addDescription(P_END_SYNC);
            return;
        }

        if (PreferenceUtils.getTriggerDeltaSync(context)) {
            SyncEngineTimingLogger.getLogger().endSplit(P_DECIDING_THE_MODE_OF_SYNC);
            perform_delta_sync();
            SyncEngineTimingLogger.getLogger().addDescription(P_END_SYNC);

            return;
        }

        SyncEngineTimingLogger.getLogger().endSplit(P_DECIDING_THE_MODE_OF_SYNC);
        perform_auto_sync();
        SyncEngineTimingLogger.getLogger().addDescription(P_END_SYNC);


    }


    private void ensure_current_orgId_valid() {
        String currentOrgId = client.getClientInfo().orgId;
        String prefOrgId = PreferenceUtils.getOrgId(context);

        if (prefOrgId == null) {
            PreferenceUtils.putOrgId(currentOrgId, context);
        } else if (!prefOrgId.equals(currentOrgId)) {
            // TODO: Delete all the content files associated with the old OrgId
            PreferenceUtils.putOrgId(currentOrgId, context);
        }
    }

    private boolean areUpdatedRecordsFetchedSuccessfully() throws SyncException {

        try {
            fetchUpdatedRecords();
        } catch (IOException e) {
            throw ConnectionLostException.create(e, context);
        } catch (JSONException e) {
            throw new SimpleSyncException(e, "Unable to read response for update request: " + e.getMessage());
        }

        if (gotFetchErrors) {
            // send a broadcast
            Log.e(TAG, "sending sync engine error broadcast after fetchUpdatedRecords");

            String message = lastFetchError != null ? lastFetchError.getMessage() : "Fetch Error!";

            SyncEngine.setSyncStatus(SyncStatus.SyncStatusState.NOT_SYNCING, SyncStatus.SyncStatusStage.NOT_APPLICABLE, message);

            Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_ERROR);
            broadcastIntent.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, message);
            context.sendBroadcast(broadcastIntent);

            Intent syncFailureIntent = new Intent(DataManager.SYNC_ENGINE_FAILURE);
            syncFailureIntent.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, message);
            context.sendBroadcast(syncFailureIntent);

            return false;
        }

        return true;

    }

    private void perform_full_sync() throws SyncException {
        SyncEngineTimingLogger.getLogger().startSplit(P_FULL_SYNC_MODE);
        // Report sync start
        String userId = client.getClientInfo().userId;
        mobileSyncReporter.reportSyncStart(syncStartTime, userId, SyncType.FULL);

        long manifestStartTime = System.currentTimeMillis();
        SFSyncHelper baseObject = null;
        syncStatus.setDescription("in Presync");

        try {
            customSyncHelper.preSync(context, client);
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e, "Pre-synchronization has failed: " + e.getMessage());
        }

        syncStatus.setDescription("Fetching MetaData");
        ManifestUtils manifestUtils = ManifestUtils.getInstance(context);

        dataManagerImpl.clearLocalData(context, false);
        DataManagerFactory.clearDataManager();

        dataManagerImpl = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        manifestUtils.invalidateManifest();

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_AND_SAVE_THE_META_DATA_INFO);
        try {
            manifestUtils.fetchAndSaveMetadataPrefs();
        } catch (Exception e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e, "Fetch and save metadata has failed. " + e.getMessage());
        }
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_AND_SAVE_THE_META_DATA_INFO);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_CREATE_SOUPS_FOR_OBJECTS_DEFINED_IN_MANIFEST);
        dataManagerImpl.createManifestObjectsSoups(context);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_CREATE_SOUPS_FOR_OBJECTS_DEFINED_IN_MANIFEST);

        autoSync = false;
        long manifestEndTime = System.currentTimeMillis();

        Log.i(TAG, "manifest download time: " + (manifestEndTime - manifestStartTime));
        context.sendBroadcast(new Intent(DataManager.FETCH_METADATA_COMPLETED));
        context.sendBroadcast(new Intent(DataManager.FETCH_CONFIGURATION_STARTED));

        orderedObjectsSet = PreferenceUtils.getSortedObjects(context);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);
        if (!areUpdatedRecordsFetchedSuccessfully()) {
            return;
        }
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_REQUIRED_CONTENT_FILES_FROM_AZURE);
        // Don't worry about content files during auto-sync
        downloadHelper.setAzureContainer(customSyncHelper.getAzureContainer());
        fetchRequiredContentFiles();
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_REQUIRED_CONTENT_FILES_FROM_AZURE);

        // Start optional files download.
        context.startService(new Intent(context, DownloadOptionalContentService.class));

        PreferenceUtils.putFirstLaunchComplete(true, context);

        customSyncHelper.postDataSync(context, client);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_UPDATE_SYNC_STATUS);
        // no need to check other file content etc during auto sync
        updateSyncStatus(context, 0 /* files left */);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_UPDATE_SYNC_STATUS);

        try {
            ThreadTools.getThreadTools().waitUntilSensitiveDataTaskDone();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        SyncEngineTimingLogger.getLogger().endSplit(P_FULL_SYNC_MODE);
    }


    private void perform_delta_sync() throws SyncException {
        SyncEngineTimingLogger.getLogger().startSplit(P_DELTA_SYNC_MODE);
        // Report sync start
        String userId = client.getClientInfo().userId;
        mobileSyncReporter.reportSyncStart(syncStartTime, userId, SyncType.DELTA);
        autoSync = false;

        objectsToFetchDuringAutoSync = new HashSet<>();
        orderedObjectsSet = PreferenceUtils.getSortedObjects(context);


        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_UPLOAD_DATA_TO_SERVER);
        uploadQueueToServer();
        SyncStep cleanupSyncStep = new ConditionalSyncStep()
                // Run only if not autoSync.
                .setCondition(new SimpleCondition(/* accept: */!autoSync))
                // Construct sync step group.
                .setSyncStep(new SyncStepGroup()
                        .add(new FetchDeletedSyncStep())
                        .add(new LocalCleanupSyncStep()));

        cleanupSyncStep.execute(this);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_UPLOAD_DATA_TO_SERVER);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_IDS_AND_PURGE);
        fetchIDsAndPurge(context, client, dataManagerImpl);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_IDS_AND_PURGE);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);
        if (!areUpdatedRecordsFetchedSuccessfully()) {
            return;
        }
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_REQUIRED_CONTENT_FILES_FROM_AZURE);
        // Don't worry about content files during auto-sync
        downloadHelper.setAzureContainer(customSyncHelper.getAzureContainer());
        fetchRequiredContentFiles();
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_REQUIRED_CONTENT_FILES_FROM_AZURE);

        // Start optional files download.
        context.startService(new Intent(context, DownloadOptionalContentService.class));
        if (gotErrors) {
            // send a broadcast
            Log.e(TAG, "sending sync engine error broadcast after fetchContentFiles");
            Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_ERROR);
            if (lastError != null)
                broadcastIntent.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, lastError.getMessage());
            context.sendBroadcast(broadcastIntent);
        }


        customSyncHelper.postDataSync(context, client);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_UPDATE_SYNC_STATUS);
        // no need to check other file content etc during auto sync
        updateSyncStatus(context, 0 /* files left */);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_UPDATE_SYNC_STATUS);

        try {
            ThreadTools.getThreadTools().waitUntilSensitiveDataTaskDone();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        SyncEngineTimingLogger.getLogger().endSplit(P_DELTA_SYNC_MODE);
    }

    private void perform_auto_sync() throws SyncException {
        SyncEngineTimingLogger.getLogger().startSplit(P_AUTO_SYNC_MODE);
        objectsToFetchDuringAutoSync = new HashSet<>();

        orderedObjectsSet = PreferenceUtils.getSortedObjects(context);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_UPLOAD_DATA_TO_SERVER);

        // TODO: save last full/delta sync time and calculate whether we should be auto-syncing

        uploadQueueToServer();

        // TODO: Add the list of dependent objects to Hashset so that any triggers that
        // update data in other objects are fetched
        // objectsToFetchDuringSync
        // validate whether this is necessary or not ....\
        // autosync needs to be quick ...

        // Constructing sync step responsible for fetching deleted records and running local
        // cleanup.
        SyncStep cleanupSyncStep = new ConditionalSyncStep()
                // Run only if not autoSync.
                .setCondition(new SimpleCondition(/* accept: */!autoSync))
                // Construct sync step group.
                .setSyncStep(new SyncStepGroup()
                        .add(new FetchDeletedSyncStep())
                        .add(new LocalCleanupSyncStep()));

        cleanupSyncStep.execute(this);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_UPLOAD_DATA_TO_SERVER);
        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);
        if (!areUpdatedRecordsFetchedSuccessfully()) {
            return;
        }
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_UPDATED_RECORDS_FROM_SALESFORCE);

        // Don't worry about content files during auto-sync

        if (gotErrors) {
            // send a broadcast
            Log.e(TAG, "sending sync engine error broadcast after fetchContentFiles");
            Intent broadcastIntent = new Intent(DataManager.SYNC_ENGINE_ERROR);
            if (lastError != null)
                broadcastIntent.putExtra(DataManager.EXTRAS_ERROR_READABLE_MESSAGE, lastError.getMessage());
            context.sendBroadcast(broadcastIntent);
        }
        customSyncHelper.postDataSync(context, client);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_UPDATE_SYNC_STATUS);
        // no need to check other file content etc during auto sync
        syncStatus.setStatus(SyncStatusState.COMPLETED);
        syncStatus.setDescription("Sync is complete");
        Intent syncIntent = new Intent(DataManager.SYNC_COMPLETED);
        context.sendBroadcast(syncIntent);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_UPDATE_SYNC_STATUS);

        SyncEngineTimingLogger.getLogger().endSplit(P_AUTO_SYNC_MODE);
    }

    public SyncEnginePrefs getPreferences() {
        return preferences;
    }

    public void startAccountContentSync(String accountId) throws SyncException {

        Log.i(TAG, "Beginning network synchronization");
        syncStatus.setStatus(SyncStatusState.INPROGRESS);
        syncStatus.setDescription("Started Sync");

        Intent syncCompletedBroadcastIntent = new Intent(DataManager.SYNC_STARTED);
        context.sendBroadcast(syncCompletedBroadcastIntent);
        dataManagerImpl = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        boolean shouldSyncInCurrentNetwork = DeviceNetworkUtils.shouldPerformSync(context);

        if (shouldSyncInCurrentNetwork) {

            fetchAccountContentFiles(accountId);
            // no need to check other file content etc during auto sync
            if (autoSync) {
                syncStatus.setStatus(SyncStatusState.COMPLETED);
                syncStatus.setDescription("Sync is complete");
                Intent syncIntent = new Intent(DataManager.SYNC_COMPLETED);
                context.sendBroadcast(syncIntent);
            } else {
                updateSyncStatus(context, 0 /* files left */);
            }
        } else {
            Log.i(TAG, "Attachment sync should not be performed in current network");
        }
    }

    public static SyncStatus getSyncStatus() {
        return syncStatus;
    }

    public void updateSyncStatus(Context context, int filesToDownload) {
        Log.v(TAG, "filesToDownloadCount: " + filesToDownload);

        if (filesToDownload == 0) {
            Log.v(TAG, "setting full sync complete");

            SFSyncHelper customSyncHelper;
            customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(context);
            customSyncHelper.postContentSync(context);
//			context.sendBroadcast(new Intent(DataManager.FETCH_CONTENT_COMPLETED));

            Log.v(TAG, "Fetch Sync finishing");
            context.sendBroadcast(new Intent(DataManager.SYNC_FINISHING));

            PreferenceUtils.putTriggerDeltaSync(false, context);
            PreferenceUtils.putFullSyncComplete(true, context);

            syncStatus.setStatus(SyncStatusState.COMPLETED);
            syncStatus.setDescription("Sync is complete");
            Intent syncCompletedBroadcastIntent = new Intent(DataManager.SYNC_COMPLETED);
            context.sendBroadcast(syncCompletedBroadcastIntent);

            context.sendBroadcast(new Intent(DataManager.SYNC_FINISHED));
            Log.i(TAG, "Sync content completed in " + (System.currentTimeMillis() - syncStartTime) + "ms");
            syncStatus.setDescription("sync completed in " + (System.currentTimeMillis() - syncStartTime) + "ms");
            mobileSyncReporter.reportSyncEnd(System.currentTimeMillis());

        } else {
            syncStatus.setStage(SyncStatusStage.FETCH_CONTENT);
            syncStatus.setCurrentItem(0);
            syncStatus.setTotalCount(filesToDownload);
            syncStatus.setDescription("Downloading content/attachment files. Files left: " + filesToDownload);
        }

        Log.i(TAG, syncStatus.toString());
    }

    public void addObjectsToFetchDuringAutoSync(String objectType) {
        if (!TextUtils.isEmpty(objectType)) {
            objectsToFetchDuringAutoSync.add(objectType);
        }
    }

    private void uploadQueueToServer() throws SyncException {
        try {
            // test upload first entry to Azure
//			JSONObject queueRecord = dataManagerImpl.getQueueRecordAt(0);
//			if (queueRecord !=null) {
//				uploadRecordToAzure(new QueueObject(queueRecord));
//			}

            QueueUploadHelper uploadHelper = new QueueUploadHelper(this);
            uploadHelper.uploadQueue();
            gotErrors = uploadHelper.hasErrors();
            lastError = uploadHelper.getLastError();

            uploadAzureQueue();
        } catch (JSONException e) {
            Log.w(TAG, e);
        }
    }

    private void uploadAzureQueue() throws JSONException {
        boolean isDone = false;
        azureTable = customSyncHelper.getAzureCloudTable();

        while (!isDone) {
            JSONObject jsonObject = dataManagerImpl.getAzureQueueRecordAt(0);

            if (jsonObject == null) {
                isDone = true;
            } else {
                QueueObject queueObject = new QueueObject(jsonObject);
                uploadRecordToAzure(queueObject);
                dataManagerImpl.deleteAzureQueueRecords(queueObject.getSoupEntryId());
            }
        }
    }

    private void fetchUpdatedRecords() throws IOException, JSONException, SyncException {
        List<Subrequest> subrequests = new ArrayList<>();
        Map<String, String> headers = QUERY_HTTP_HEADERS;

        // get list of objects saved in manifest based on dependency order
        List<String> orderedObjectsSet = PreferenceUtils.getSortedObjects(context);

        List<List<String>> objectGroups = PreferenceUtils.getGroupsForBatchSync(context);
        Configuration configuration = PreferenceUtils.getConfiguration(context);

        syncStatus.setStage(SyncStatusStage.FETCH_UPDATED_RECORDS);
        syncStatus.setTotalCount(orderedObjectsSet.size());
        int objectIndex = 0;
        Set<String> objectNames = null;
        boolean firstLaunchComplete = PreferenceUtils.getFirstLaunchComplete(context);
        if (firstLaunchComplete) {
            try {
                objectNames = checkLastModify(orderedObjectsSet,configuration);
            } catch (ResponseErrorException e) {
                e.printStackTrace();
                objectNames=null;
            }
        }

        for (List<String> objectGroup : objectGroups) {
            customSyncHelper.doBeforeGroupFetch(context, objectGroup, autoSync);

            for (String objectName : objectGroup) {

                if (autoSync) {
                    if (!objectsToFetchDuringAutoSync.contains(objectName)) continue;
                }

                Log.i(TAG, "Fetching records for object: " + objectName);
                syncStatus.setDescription("Fetching records for object: " + objectName);

                // if specified sync direction doesn't include pull, then ignore
                SyncDirection syncDirection = PreferenceUtils.getSyncDirection(objectName, context);
                if (syncDirection == SyncDirection.UP || syncDirection == SyncDirection.NONE) {
                    continue;
                }
                SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
                final String objectNameWithNameSpace=objectMetadata.getName();
                if (firstLaunchComplete && objectNames != null && !objectNames.contains(objectNameWithNameSpace)) {
                    continue;
                }

                syncStatus.setCurrentItem(objectIndex);
                objectIndex++;

                String objectLastRefreshTimeString = PreferenceUtils.getLastRefreshTime(objectName, context);
                // objectLastRefreshTimeString = dummyDate();
                boolean objectFullSync = (objectLastRefreshTimeString == null);
                if (objectFullSync) {
                    checkAndClearDataIfRequired(objectName, dataManagerImpl);
                }

                // Removed using isDateQuery as an option. We seem to run into more problems when we delete and recreate the database
                // Now, we are using a combination of fetch and cleanup filters to achieve the same affect


                List<FilterObject> additionalFilters = customSyncHelper.getAdditionalFilters(context, objectMetadata.getNameWitoutNameSpace());
                Log.i(TAG, "got additionalFilters size: " + additionalFilters.size());
                if (additionalFilters.size() > 0) {
                    Log.i(TAG, "got additionalFilters size: " + additionalFilters.size());
                    objectMetadata.addFilterObjects(additionalFilters);
                }

                List<String> allQueries = new ArrayList<>();

                // Run queries related with dynamic fetch. They don't require adding lastModifiedDate.
                Map<String, Object> params = new HashMap<>();
                params.put("lastRefreshTime", objectLastRefreshTimeString);
                allQueries.addAll(getDynamicFetchSyncQueries(context, manifestProcessor, configuration, objectMetadata, params));

                // Add all regular fetch queries.
                boolean addLastModifiedFilter = !objectFullSync;

                List<FilterObject> lastModifiedFilterList = new ArrayList<>();

                if (addLastModifiedFilter) {
                    FilterObject lastModifiedDate = new FilterObject();
                    lastModifiedDate.setOp(QueryOp.gt);
                    lastModifiedDate.setField("lastModifiedDate");
                    lastModifiedDate.setValue(new DateString(objectLastRefreshTimeString));

                    lastModifiedFilterList.add(lastModifiedDate);
                    objectMetadata.addFilterObjects(lastModifiedFilterList);
                }

                allQueries.addAll(getSyncQueries(context, manifestProcessor, configuration, objectMetadata));

                // Adding mechanism to run additional queries on an object
                List<String> additionalQueryFilters = customSyncHelper.getAdditionalQueryFilters(context, objectMetadata.getNameWitoutNameSpace());
                if (!additionalQueryFilters.isEmpty()) {
                    Log.i(TAG, "running additional queries on object: " + objectName);
                    for (String queryFilter : additionalQueryFilters) {
                        objectMetadata.setFilterObjects(lastModifiedFilterList);
                        String query = objectMetadata.getSimpleQuery(context, queryFilter);
                        allQueries.add(query);
                    }
                }

                boolean firstQuery = true;
                // Convert all queries to subrequests.
                for (String query : allQueries) {
                    Log.v(TAG, "query is: " + query);
                    Subrequest subrequest = syncHelper.getFetchSubrequest(query);
                    subrequest.setResponseHandler(new FetchUpdatedRecordsResponseHandler(context,
                            syncHelper, objectName, objectFullSync, autoSync, firstQuery, dataManagerImpl));

                    subrequests.add(subrequest);

                    if (firstQuery) {
                        firstQuery = false;
                    }
                }
            }
            try {
                syncHelper.sendBatchRequests(subrequests, true, headers, MAX_REQUESTS_PER_BATCH);
                subrequests.clear();
            } catch (ServerErrorException e) {
                Log.w(TAG, e);
                lastFetchError = e;
                gotFetchErrors = true;
                return;
            }
            customSyncHelper.doAfterGroupFetch(context, objectGroup, autoSync);
        }
    }

    private Set<String> checkLastModify(List<String> orderedObjectsSet,Configuration configuration) throws IOException, JSONException, SyncException, ResponseErrorException {
        List<Map<String, String>> list = new LinkedList<>();
        Set<String> data = new HashSet<>();
        for (String objectName : orderedObjectsSet) {
            List<Map<String, String>> parameters = buildCheckLastModifyHead(objectName,configuration);
            list.addAll(parameters);
        }
        Gson gson = new Gson();
        JSONArray jsonArray;
        RestRequest request = syncHelper.checkLastModify(gson.toJson(list));
        RestResponse response = client.sendSync(request);
        if (response.isSuccess()) {

            JSONObject object = response.asJSONObject();
            jsonArray = object.optJSONArray(SyncEngineConstants.StdFields.ITEMS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                if (jsonObject.getBoolean(SyncEngineConstants.StdFields.IS_MODIFIED)) {
                    data.add(jsonObject.get(SyncEngineConstants.StdFields.SOBJECT_NAME).toString());
                }
            }
        }else{
            throw new ResponseErrorException("checkLastModify response error. e:"+response.toString());
        }
        return data;
    }

    @NonNull
    private List<Map<String, String>> buildCheckLastModifyHead(String objectName,Configuration configuration)throws SyncException {
        List<Map<String, String>> mapList=new LinkedList<>();
        SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
        List<String> filters=getCheckLastModifyFilter(context, manifestProcessor, configuration, objectMetadata);
        if(filters==null||filters.isEmpty()){
            Map<String, String> parameters = new HashMap<>();
            String objectLastRefreshTimeString = PreferenceUtils.getLastRefreshTime(objectName, context);
            parameters.put(SyncEngineConstants.StdFields.SOBJECT_NAME, objectMetadata.getName());
            parameters.put(SyncEngineConstants.StdFields.LAST_MODIFY_TIME, objectLastRefreshTimeString);
            mapList.add(parameters);
        }else{
            for(String filter:filters){
                Map<String, String> parameters = new HashMap<>();
                String objectLastRefreshTimeString = PreferenceUtils.getLastRefreshTime(objectName, context);
                parameters.put(SyncEngineConstants.StdFields.SOBJECT_NAME, objectMetadata.getName());
                parameters.put(SyncEngineConstants.StdFields.LAST_MODIFY_TIME, objectLastRefreshTimeString);
                parameters.put(SyncEngineConstants.StdFields.FILTER, filter);
                mapList.add(parameters);
            }
        }

        return mapList;
    }

    // We are setting that because we cannot have more than 10 nextRecordsUrls at the same time.
    private static final int MAX_REQUESTS_PER_BATCH = 9;

    public void fetchRequiredContentFiles() throws SimpleSyncException {
        context.sendBroadcast(new Intent(DataManager.FETCH_CONTENT_STARTED));
        Log.e(TAG, "Fetch content started");

        //TODO: If the account size is small we download the content this value is used on Attachment object
        //but this gets confusing during testing
        int allAccountsCount = getAccountsCount();

        List<String> orderedObjectsSet = PreferenceUtils.getSortedObjects(context);
        Configuration configuration = PreferenceUtils.getConfiguration(context);
        List<ContentFileInfo> requiredFiles = new ArrayList<ContentFileInfo>();
        for (String objectName : orderedObjectsSet) {
            if (!PreferenceUtils.containsBinaryField(objectName, context)) {
                continue;
            }

            String binaryField = PreferenceUtils.getBinaryField(objectName, context);
            ExtraData extraData = new ExtraData();

            //Fetch all is ok since we are just fetching only the Ids
            String query = String.format(FIELD_FETCH_SQL_FORMAT, objectName, CONSTANTS_ID);
            Set<String> requiredFilesFilters = PreferenceUtils.getRequiredFilesFilters(objectName, context);
            Set<String> additionalFilesFilters = PreferenceUtils.getAdditionalFilesFilters(objectName, context);

            if (isEmpty(requiredFilesFilters) && isEmpty(additionalFilesFilters)) {
                // By default all files are required.
                addAllContentFileInfo(objectName, binaryField, query, allAccountsCount, extraData, requiredFiles);

            } else {
                SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);

                if (isEmpty(requiredFilesFilters)) {
                    continue;
                }

                for (String filter : requiredFilesFilters) {
                    try {
                        filter = manifestProcessor.processLocalQuery(configuration, objectMetadata, filter);
                        String updatedQuery = query;
                        if (!TextUtils.isEmpty(filter)) {
                            updatedQuery += " WHERE " + filter;
                        }

                        addAllContentFileInfo(objectName, binaryField, updatedQuery, allAccountsCount, extraData, requiredFiles);
                    } catch (ManifestProcessingException e) {
                        Log.w(TAG, "Error while processing required files query.", e);
                        throw new SimpleSyncException(e);
                    }
                }
            }
        }

        Log.i(TAG, "Files that will be downloaded during sync: " + requiredFiles.size());

        for (int i = 0; i < requiredFiles.size(); i++) {

            updateSyncStatus(context, requiredFiles.size() - i);

            ContentFileInfo info = requiredFiles.get(i);

            try {
                downloadHelper.download(info.objectName, info.id, info.binaryUrl);
            } catch (IOException e) {
                Log.w(TAG, e);
                // In case of error add the download to the download queue.
                Log.v(TAG, "Couldn't download content file. Adding it to the download queue.");
                downloadHelper.addToDownloadQueue(info.objectName, info.id, info.binaryUrl);
            }
        }

        Log.v(TAG, "Fetch content completed");
        context.sendBroadcast(new Intent(DataManager.FETCH_CONTENT_COMPLETED));
    }

    public void fetchOptionalContentFiles() throws SimpleSyncException {
        int allAccountsCount = getAccountsCount();

        List<String> orderedObjectsSet = PreferenceUtils.getSortedObjects(context);
        Configuration configuration = PreferenceUtils.getConfiguration(context);
        List<ContentFileInfo> additionalFiles = new ArrayList<ContentFileInfo>();

        for (String objectName : orderedObjectsSet) {

            if (PreferenceUtils.containsBinaryField(objectName, context)) {
                String binaryField = PreferenceUtils.getBinaryField(objectName, context);
                ExtraData extraData = new ExtraData();

                //Fetch all is ok since we are just fetching only the Ids
                String query = String.format(FIELD_FETCH_SQL_FORMAT, objectName, CONSTANTS_ID);
                Set<String> additionalFilesFilters = PreferenceUtils.getAdditionalFilesFilters(objectName, context);

                SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);

                if (!isEmpty(additionalFilesFilters)) {
                    for (String filter : additionalFilesFilters) {
                        try {
                            filter = manifestProcessor.processLocalQuery(configuration, objectMetadata, filter);
                            String updatedQuery = query;
                            if (!TextUtils.isEmpty(filter)) {
                                updatedQuery += " WHERE " + filter;
                            }
                            addAllContentFileInfo(objectName, binaryField, updatedQuery, allAccountsCount, extraData, additionalFiles);
                        } catch (ManifestProcessingException e) {
                            Log.w(TAG, "Error while processing additional files query.", e);
                            throw new SimpleSyncException(e);
                        }
                    }
                }
            }
        }

        Log.i(TAG, "Files that will be downloaded in background: " + additionalFiles.size());

        for (ContentFileInfo info : additionalFiles) {
            downloadHelper.addToDownloadQueue(info.objectName, info.id, info.binaryUrl);
        }

        Log.v(TAG, "Fetch optional content completed");
    }

    private static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    private void addAllContentFileInfo(String objectName, String binaryField, String query, int allAccountsCount, ExtraData extraData, Collection<ContentFileInfo> container) {
        JSONArray jsonArray = dataManagerImpl.fetchAllSmartSQLQuery(query);
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String id = jsonArray.getJSONArray(i).getString(0);
                if (extraData.contentFilesAdded.contains(id)) {
                    Log.v(TAG, "Duplicated " + objectName + " id: " + id);
                    continue;
                }

                JSONObject jsonObject = dataManagerImpl.exactQuery(objectName, CONSTANTS_ID, id);
                String binaryUrl = jsonObject.getString(binaryField);

                Log.v(TAG, "binary field value is: " + binaryUrl);

                if (shouldDownloadFile(objectName, jsonObject, extraData, allAccountsCount)) {

                    container.add(new ContentFileInfo(objectName, id, binaryUrl));
                    extraData.contentFilesAdded.add(id);

                }

            } catch (JSONException e) {
                Log.w(TAG, e);
            }
        }
    }

    public void setAzureContainer(final CloudBlobContainer azureContainer) {
        downloadHelper.setAzureContainer(azureContainer);
    }

    private static class ContentFileInfo {

        String objectName;

        String id;

        String binaryUrl;

        public ContentFileInfo(String objectName, String id, String binaryUrl) {
            this.objectName = objectName;
            this.id = id;
            this.binaryUrl = binaryUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContentFileInfo info = (ContentFileInfo) o;

            if (objectName != null ? !objectName.equals(info.objectName) : info.objectName != null)
                return false;
            return id != null ? id.equals(info.id) : info.id == null;

        }

        @Override
        public int hashCode() {
            int result = objectName != null ? objectName.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }

    public static int getAccountsCount() {
        String smartSql = String.format("SELECT count() FROM {%1$s}", OBJECT_ACCOUNT);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        int result = 0;
        try {
            if (recordsArray.length() > 0) {
                result = recordsArray.getJSONArray(0).getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting accounts", e);
        }
        return result;
    }

    public void fetchAccountContentFiles(String accountId) {
        context.sendBroadcast(new Intent(DataManager.FETCH_CONTENT_STARTED));
        Log.e(TAG, "Fetch content started");

        SFSyncHelper customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(context);
        List<String> queries = customSyncHelper.getDynamicContentFetchQueries(accountId);
        int downloadCount = 0;

        for (String query : queries) {
            JSONArray jsonArray = dataManagerImpl.fetchAllSmartSQLQuery(query);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONArray(i).getJSONObject(0);
                        JSONObject attributes = jsonObject.getJSONObject("attributes");

                        String id = jsonObject.getString("Id");
                        String objectName = attributes.getString("type");
                        String binaryField = PreferenceUtils.getBinaryField(objectName, context);
                        String binaryUrl = jsonObject.getString(binaryField);

                        Log.v(TAG, "id: " + id + " type: " + objectName + " binaryUrl: " + binaryUrl);
                        downloadHelper.addToDownloadQueue(objectName, id, binaryUrl);
                        downloadCount++;
                    } catch (JSONException e) {
                        Log.w(TAG, e);
                    }
                }
            }
        }

        Log.v(TAG, "Files added to download queue: " + downloadCount);
        Log.v(TAG, "Fetch content completed");
        context.sendBroadcast(new Intent(DataManager.FETCH_CONTENT_COMPLETED));
    }

    private static final String OBJECT_ATTACHMENT = "Attachment";
    private static final String OBJECT_EVENT = "Event";
    private static final String OBJECT_ACCOUNT = "Account";
    private static final String OBJECT_CASE = "Case_Force__c";
    private static final String OBJECT_SURVEY_QUESTION_RESPONSE = "SurveyQuestionResponse__c";
    private static final String OBJECT_SURVEY_TAKER = "SurveyTaker__c";
    private static final int MAX_ACCOUNT_PHOTOS_NUM = 150; //TODO take it from config
    private static final int MAX_ATTACHMENTS = 7; //TODO take it from config
    private static final boolean DEBUG_DOWNLOAD_FILTER = false;
    private static final String TAG_DOWNLOAD_FILTER = "DownloadFilter";

    private static class ExtraData {
        Set<String> accountsWithPhoto = new HashSet<String>();
        Set<String> contentFilesAdded = new HashSet<String>();
        Map<String, Integer> attachmentsNum = new HashMap<String, Integer>();
    }

    private boolean shouldDownloadFile(String objectName, JSONObject jsonObject, ExtraData extraData, int allAccountsCount) {
        // Always download if this is not an Attachment
        if (!OBJECT_ATTACHMENT.equals(objectName)) {
            if (DEBUG_DOWNLOAD_FILTER) {
                Log.i(TAG_DOWNLOAD_FILTER, "Object is not an Attachment: " + objectName + ". Downloading.");
            }
            return true;
        }

        String parentId = jsonObject.optString("ParentId");

        // Check if this is an account attachment
        JSONObject accountJson = getObjectById(OBJECT_ACCOUNT, parentId);
        if (accountJson != null) {
            if (DEBUG_DOWNLOAD_FILTER) {
                Log.i(TAG_DOWNLOAD_FILTER, "Found account: " + parentId);
            }

            // Check if this is an account photo
            if (isAccountPhoto(jsonObject)) {
                boolean downloadAllAccountPhotos = allAccountsCount < MAX_ACCOUNT_PHOTOS_NUM;
                if (downloadAllAccountPhotos) {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "Download each account photo. Downloading.");
                    }

                    return true;
                } else if (existsEventFor(parentId)) {
                    if (extraData.accountsWithPhoto.contains(parentId)) {
                        if (DEBUG_DOWNLOAD_FILTER) {
                            Log.i(TAG_DOWNLOAD_FILTER, "There is newer photo for account: " + parentId + ". Not downloading.");
                        }

                        return false;
                    } else {
                        if (DEBUG_DOWNLOAD_FILTER) {
                            Log.i(TAG_DOWNLOAD_FILTER, "This is the newest photo for account: " + parentId + ". Downloading.");
                        }

                        extraData.accountsWithPhoto.add(parentId);
                        return true;
                    }
                } else {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "No event for account: " + parentId + ". Not downloading.");
                    }
                    return false;
                }
            }

            // Check if there is an event for this account
            if (existsEventFor(parentId)) {

                // Download all asset photos
                if (isAssetPhoto(jsonObject)) {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "Exists event for account: " + parentId + ". Downloading.");
                    }
                    return true;
                }
                // Download only newest attachments
                else {
                    Integer number = extraData.attachmentsNum.get(parentId);
                    if (number == null) number = 0;

                    if (number < MAX_ATTACHMENTS) {
                        if (DEBUG_DOWNLOAD_FILTER) {
                            Log.i(TAG_DOWNLOAD_FILTER, "Downloaded " + number + " attachments for account: " + parentId + " where max is: " + MAX_ATTACHMENTS + ". Downloading.");
                        }

                        extraData.attachmentsNum.put(parentId, number + 1);
                        return true;
                    } else {
                        if (DEBUG_DOWNLOAD_FILTER) {
                            Log.i(TAG_DOWNLOAD_FILTER, "Too many attachments downloaded for account: " + parentId + ". Not downloading.");
                        }

                        return false;
                    }
                }
            } else {
                if (DEBUG_DOWNLOAD_FILTER) {
                    Log.i(TAG_DOWNLOAD_FILTER, "No event for account: " + parentId + ". Not downloading.");
                }
                return false;
            }
        }

        // Check if this is a case attachment
        JSONObject caseJson = getObjectById(OBJECT_CASE, parentId);
        if (caseJson != null) {
            if (DEBUG_DOWNLOAD_FILTER) {
                Log.i(TAG_DOWNLOAD_FILTER, "Found case: " + parentId);
            }

            String status = caseJson.optString("Status__c");
            if ("Closed".equals(status)) {
                if (DEBUG_DOWNLOAD_FILTER) {
                    Log.i(TAG_DOWNLOAD_FILTER, "Closed case: " + parentId + ". Downloading.");
                }
                return true;
            } else {
                // Check if there is an event for this account.
                String accountId = caseJson.optString("Account__c");
                if (existsEventFor(accountId)) {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "Exists event for case: " + parentId + ". Downloading.");
                    }
                    return true;
                } else {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "No event for case: " + parentId + ". Not downloading.");
                    }
                    return false;
                }
            }
        }

        // Check if this is a question attachment
        JSONObject questionJson = getObjectById(OBJECT_SURVEY_QUESTION_RESPONSE, parentId);
        if (questionJson != null) {
            if (DEBUG_DOWNLOAD_FILTER) {
                Log.i(TAG_DOWNLOAD_FILTER, "Found question response: " + parentId);
            }

            String surveyTakerId = questionJson.optString("SurveyTaker__c");
            JSONObject surveyTakerJson = getObjectById(OBJECT_SURVEY_TAKER, surveyTakerId);

            if (surveyTakerJson != null) {
                String accountId = surveyTakerJson.optString("Account__c");

                // Check if there is an event for this account.
                if (existsEventFor(accountId)) {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "Exists event for question response: " + parentId + ". Downloading.");
                    }
                    return true;
                } else {
                    if (DEBUG_DOWNLOAD_FILTER) {
                        Log.i(TAG_DOWNLOAD_FILTER, "No event for question response: " + parentId + ". Not downloading.");
                    }
                    return false;
                }
            } else {
                if (DEBUG_DOWNLOAD_FILTER) {
                    Log.i(TAG_DOWNLOAD_FILTER, "Missing survey taker with id: " + surveyTakerId + ". Not downloading.");
                }
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if exists Event for chosen accountId.
     */
    private boolean existsEventFor(String accountId) {
        if (TextUtils.isEmpty(accountId)) return false;

        String smartQuery = String.format(FIELD_FETCH_SQL_FORMAT, OBJECT_EVENT, "Id");
        String condition = String.format("{%1$s:%2$s} = '%4$s' OR {%1$s:%3$s} = '%4$s'",
                OBJECT_EVENT, "AccountId", "WhatId", accountId);
        JSONArray jsonArray = dataManagerImpl.fetchAllSmartSQLQuery(smartQuery + " WHERE " + condition);

        return jsonArray != null && jsonArray.length() > 0;
    }

    /**
     * Checks if attachment object is an account photo.
     */
    private boolean isAccountPhoto(JSONObject jsonObject) {
        if (jsonObject == null) return false;

        JSONObject attributes = jsonObject.optJSONObject("attributes");
        if (attributes == null || !OBJECT_ATTACHMENT.equals(attributes.optString("type"))) {
            return false;
        }

        String fileName = jsonObject.optString(SyncEngineConstants.StdFields.NAME);
        if (TextUtils.isEmpty(fileName)) {
            return false;
        } else {
            return fileName.startsWith("AccountPhoto");
        }
    }

    /**
     * Checks if attachment object is an account photo.
     */
    private boolean isAssetPhoto(JSONObject jsonObject) {
        if (jsonObject == null) return false;

        JSONObject attributes = jsonObject.optJSONObject("attributes");
        if (attributes == null || !OBJECT_ATTACHMENT.equals(attributes.optString("type"))) {
            return false;
        }

        String fileName = jsonObject.optString(SyncEngineConstants.StdFields.NAME);
        if (TextUtils.isEmpty(fileName)) {
            return false;
        } else {
            return fileName.startsWith("AssetPhoto");
        }
    }

    private JSONObject getObjectById(String objectName, String id) {

        if (!orderedObjectsSet.contains(objectName)) return null;

        if (TextUtils.isEmpty(id)) return null;

        String smartQuery = String.format(FIND_BY_FIELD_SQL_FORMAT, objectName, "_soup", "Id", id);
        JSONArray jsonArray = dataManagerImpl.fetchAllSmartSQLQuery(smartQuery);

        return jsonArray != null && jsonArray.length() > 0 ? jsonArray.optJSONArray(0).optJSONObject(0) : null;
    }

    private static List<String> getSyncQueries(Context context, ManifestProcessor processor, Configuration configuration, SFObjectMetadata objectMetadata) throws SyncException {
        try {
            return objectMetadata.getSyncQuery(context, processor, configuration);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e);
        }
    }
    private static List<String> getCheckLastModifyFilter(Context context, ManifestProcessor processor, Configuration configuration, SFObjectMetadata objectMetadata) throws SyncException {
        try {
            return objectMetadata.getCheckLastModifyQueryFilter(context, processor, configuration);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e);
        }
    }

    private static List<String> getDynamicFetchSyncQueries(Context context, ManifestProcessor processor, Configuration configuration, SFObjectMetadata objectMetadata,
                                                           Map<String, Object> params) throws SyncException {
        try {
            return objectMetadata.getDynamicFetchSyncQuery(context, processor, configuration, params);
        } catch (ManifestProcessingException e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e);
        }
    }

    private static List<String> getAllSyncQueries(Context context, ManifestProcessor processor, Configuration configuration, SFObjectMetadata objectMetadata,
                                                  Map<String, Object> params) throws SyncException {
        try {
            List<String> result = new ArrayList<String>();
            result.addAll(objectMetadata.getSyncQuery(context, processor, configuration));
            result.addAll(objectMetadata.getDynamicFetchSyncQuery(context, processor, configuration, params));
            return result;
        } catch (ManifestProcessingException e) {
            Log.w(TAG, e);
            throw new SimpleSyncException(e);
        }
    }


    private static class FetchUpdatedRecordsResponseHandler extends FetchResponseHandler {

        private final TimingLogger timings = new TimingLogger("Timing", "fetchRecords");

        private final boolean isFullSync;

        private final boolean isFirstQuery;

        private final boolean isAutoSync;

        private final SmartStoreDataManagerImpl dataManagerImpl;

        private final String objectType;

        private int recordsCount = 0;

        private int recordsSet = 0;

        private String lastRefreshTimeValue;

        private Set<String> sensitiveFields;

        protected FetchUpdatedRecordsResponseHandler(Context context, SyncHelper syncHelper,
                                                     String objectType, boolean isFullSync,
                                                     boolean isAutoSync, boolean isFirstQuery,
                                                     SmartStoreDataManagerImpl dataManager) {
            super(context, syncHelper);
            this.objectType = objectType;
            this.isAutoSync = isAutoSync;
            this.isFullSync = isFullSync;
            this.isFirstQuery = isFirstQuery;
            this.dataManagerImpl = dataManager;
        }

        @Override
        public Subrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, Subrequest subrequest, Subresponse subresponse) throws SyncException {
            if (lastRefreshTimeValue == null) {
                lastRefreshTimeValue = getServerDateString(originalResponse);
            }
            return super.handleResponse(originalRequest, originalResponse, subrequest, subresponse);
        }

        @Override
        protected void handleRecords(JSONArray records, int startIndex, boolean isLast) throws SyncException {
            Log.d(TAG, "start index: " + startIndex + " record size: " + records.length() + " is last: " + isLast);

            if (sensitiveFields == null) {
                sensitiveFields = isFullSync ?
                        Collections.emptySet() : SFObjectMetadata.getSensitiveFieldsWithoutNamespace(getContext(), objectType);
                Log.i(TAG, "sensitive fields: " + sensitiveFields);
            }

            recordsCount += records.length();
            syncStatus.setDescription("Fetching records for object: " + objectType + " - " + recordsCount);

            Set<String> localFields = PreferenceUtils.getLocalFields(objectType, getContext());
            // insert the records into smart store
            // if it is not autoSync then the user cannot do anything in the UI so let us speed up sync
            dataManagerImpl.insertRecords(objectType, records, isFirstQuery, isFullSync, !isAutoSync, localFields, sensitiveFields);

            timings.addSplit("fetchRecords set " + recordsSet);
            recordsSet++;

            if (isLast) {
                finalizeFetch();
            }
        }

        private void finalizeFetch() {
            timings.dumpToLog();
            if (PreferenceUtils.getFullSyncComplete(getContext())) {
                String lastSyncTime = PreferenceUtils.getLastRefreshTime(objectType, getContext());
                if (lastSyncTime != null)
                    PreferenceUtils.putPreviousRefreshTime(objectType, lastSyncTime, getContext());
            }
            PreferenceUtils.putLastRefreshTime(objectType, lastRefreshTimeValue, getContext());
        }

        @Override
        protected boolean isLargeResponse(int totalSize) {
            return totalSize >= BATCH_SIZE; // TODO handle situation if i.e. 501
        }
    }

    public interface FetchRecordsProgress {
        void onRecordsFetched(int size);
    }

    public static boolean fetchRecordsForObject(Context context, SyncHelper syncHelper, SmartStoreDataManagerImpl dataManager, SFSyncHelper customSyncHelper, String soqlOrPath, final String objectType, int numberOfFields, FetchRecordsProgress fetchProgress)
            throws SyncException {

        boolean success = false;
        boolean isSoql = true;
        boolean firstSetOfRecords = true;
        DownloadHelper downloadHelper = new DownloadHelper(context, syncHelper.getClient());
        downloadHelper.setAzureContainer(customSyncHelper.getAzureContainer());

        boolean containsContentFiles = PreferenceUtils.containsBinaryField(objectType, context);
        String binaryField = PreferenceUtils.getBinaryField(objectType, context);

        TimingLogger timings = new TimingLogger("Timing", "fetchRecords");
        int currentSet = 1;

        Log.i(TAG, "objectType: " + objectType + " fieldSize: " + numberOfFields);

        Set<String> sensitiveFields = SFObjectMetadata.getSensitiveFieldsWithoutNamespace(context, objectType);
        Log.i(TAG, "sensitive fields: " + sensitiveFields);

        while (soqlOrPath != null) {
            Log.d(TAG, "soqlOrPath: " + soqlOrPath);

            // create a request based on whether it is soql or a url
            final RestRequest restRequest = isSoql ? getRequestForQuery(syncHelper.getApiVersion(), soqlOrPath, numberOfFields)
                    : new RestRequest(RestMethod.GET, soqlOrPath,
                    (numberOfFields > MAXFIELD_COUNT_FOR_BATCHING ? QUERY_HTTP_HEADERS : null));

            Log.d(TAG, "in sendRequest before sendSync");
            // perform the rest call
            RestResponse result = syncHelper.sendRawRequest(restRequest);
            //Log.e(TAG, result.asString());
            Log.d(TAG, "in sendRequest after sendSync");

            try {

                // check if the call was a success
                if (result.isSuccess()) {
                    // if this was a soql fetch, lets update the last refreshed time
                    if (firstSetOfRecords) {
                        success = true;
                    }

                    // get the records from the response
                    final JSONArray records = result.asJSONObject().getJSONArray("records");
                    Log.d(TAG, "record size: " + records.length());
                    if (fetchProgress != null) {
                        fetchProgress.onRecordsFetched(records.length());
                    }

                    Set<String> localFields = PreferenceUtils.getLocalFields(objectType, context);

                    // insert the records into smart store
                    // if it is not autoSync then the user cannot do anything in the UI so let us speed up sync
                    dataManager.insertRecords(objectType, records, /*skipDuplicatesCheck*/ false, /*firstSync*/ false, /*speedSync*/ true, localFields, sensitiveFields);
                    if (containsContentFiles) {
                        downloadBinaryContent(downloadHelper, objectType, records, binaryField);
                    }

                    // check if we have more records to fetch
                    String nextRecordsUrl = result.asJSONObject().optString("nextRecordsUrl", null);

                    // if we have more records to fetch, make sure we get those
                    if (!GuavaUtils.isNullOrEmpty(nextRecordsUrl)) {
                        Log.i(TAG, "nextRecordsUrl is: " + nextRecordsUrl);
                        soqlOrPath = nextRecordsUrl;
                        isSoql = false;
                        firstSetOfRecords = false;
                    } else {
                        soqlOrPath = null;
                    }

                    timings.addSplit("fetchRecords set " + currentSet);
                    currentSet++;

                } else {
                    Log.e(TAG, "in sendRequest: failed to get data. objectType: "
                            + objectType + "\nresult: "
                            + asStringSilently(result) + "\nsoql: " + soqlOrPath);
                    throw ServerErrorException.createFrom(result, context);
                }
            } catch (IOException e) {
                Log.w(TAG, e);
                throw ConnectionLostException.create(e, context);
            } catch (JSONException e) {
                Log.w(TAG, e);
                throw new SimpleSyncException(e, "Unable to read response for dynamic fetch: " + e.getMessage());
            }
        }

        timings.dumpToLog();

        return success;
    }

    private static void downloadBinaryContent(DownloadHelper downloadHelper, String objectName, JSONArray records, String binaryField) {
        if (records == null || records.length() == 0) return;

        for (int i = 0; i < records.length(); i++) {
            JSONObject object = records.optJSONObject(i);

            if (object != null) {
                String id = object.optString("Id");
                String binaryUrl = object.optString(binaryField);

                if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(binaryField)) {
                    downloadHelper.addToDownloadQueue(objectName, id, binaryUrl);
                }
            }
        }
    }

    private static String asStringSilently(RestResponse response) {
        try {
            return response.asString();
        } catch (IOException e) {
            return "";
        }
    }

    private static String getServerDateString(RestResponse response) {
        return getServerDateString(response.getAllHeaders());
    }

    private static String getServerDateString(Map<String, List<String>> headers) {
        String dateString = null;
        try {
            Log.i(TAG, "headers: " + headers.toString());
            String dateValue = headers.get("Date").get(0);
            if (dateValue == null) {
                Date serverDate = new Date();
                dateString = soqlQueryDateFormat.format(serverDate);
                return dateString;
            }
            Date serverDate = serverDateFormat.parse(dateValue);
            dateString = soqlQueryDateFormat.format(serverDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateString;
    }

    protected void fetchIDsAndPurge(Context context, RestClient client, SmartStoreDataManagerImpl dataManagerImpl) {
        // check if the difference between current time and last time purge took place
        // if the difference is less than the frequency mentioned in the manifest return
        // else proceed
        long lastPurgeTime = PreferenceUtils.getLastPurgeTime(context);
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        if (currentTimeInSeconds - lastPurgeTime < PreferenceUtils.getPurgeFrequency(context)) {
            return;
        }

        try {
            Configuration configuration = PreferenceUtils.getConfiguration(context);
            String apiVersion = context.getString(R.string.api_version);

            List<String> orderedObjectsSet = PreferenceUtils.getSortedObjects(context);

            for (String objectName : orderedObjectsSet) {

                syncStatus.setDescription("In fetch Ids and Purge for object: " + objectName);
                if (autoSync) {
                    if (!objectsToFetchDuringAutoSync.contains(objectName)) continue;
                }

                boolean isPurgeEnabled = PreferenceUtils.getPurgeEnabled(objectName, context);
                if (!isPurgeEnabled) {
                    continue;
                }

                // get only the Ids here
//				String prefsQuery = PreferenceUtils.getSyncQuery(objectName, context);
                SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);

                ArrayList<String> fieldsToFetch = new ArrayList<String>();
                fieldsToFetch.add(CONSTANTS_ID);
                objectMetadata.setFieldsToFetch(fieldsToFetch);

                List<FilterObject> additionalFilters = customSyncHelper.getAdditionalFilters(context, objectMetadata.getNameWitoutNameSpace());
                if (additionalFilters.size() > 0) {
                    objectMetadata.addFilterObjects(additionalFilters);
                }

                String objectLastRefreshTimeString = PreferenceUtils.getLastRefreshTime(objectName, context);
                Map<String, Object> params = new HashMap<>();
                params.put("lastRefreshTime", objectLastRefreshTimeString);

                List<String> queries = getAllSyncQueries(context, manifestProcessor, configuration, objectMetadata, params);
                for (String objectQuery : queries) {
                    // TODO: This check is probably not necessary
                    // getDeleted will fetch the deleted ids for other kinds of objects
                    // but it may not get the all the deleted records if it has been a while
                    // For demo, the below logic makes sense but in the real world
                    // this should not be present
                    boolean replicable = PreferenceUtils.getReplicable(objectName, context);
                    if (replicable) {
                        continue;
                    }

                    boolean containsContentFiles = PreferenceUtils.containsBinaryField(objectName, context);
                    boolean success = true;
                    boolean firstSetOfRecords = true;
//					String lastPurgeTimeValue = null;
                    long lastPurgeTimeInMilliseconds = 0;
                    HashMap<String, String> objectIDMap = new HashMap<String, String>();
                    while (objectQuery != null) {

                        // create a request based on whether it is soql or a url
                        final RestRequest restRequest = firstSetOfRecords ? RestRequest.getRequestForQuery(apiVersion, objectQuery)
                                : new RestRequest(RestMethod.GET, objectQuery);
                        RestResponse result = syncHelper.sendRawRequest(restRequest);
                        // check if the call was a success
                        if (result.isSuccess()) {
                            // get the records from the response
                            final JSONArray records = result.asJSONObject()
                                    .getJSONArray("records");
                            Log.d(TAG, "record size: " + records.length());

                            // add record Ids to hashmap
                            for (int i = 0; i < records.length(); i++) {
                                String id = records.getJSONObject(i).getString(CONSTANTS_ID);
                                objectIDMap.put(id, id);
                            }

                            if (firstSetOfRecords) {
                                success = true;
//								lastPurgeTimeValue = getServerDateString(result);
                                lastPurgeTimeInMilliseconds = System.currentTimeMillis();
                                PreferenceUtils.putLastPurgeTime(lastPurgeTimeInMilliseconds, context);
                            }

                            // check if we have more records to fetch
                            String nextRecordsUrl = result.asJSONObject()
                                    .optString("nextRecordsUrl", null);

                            // if we have more records to fetch, make sure we get
                            // those
                            if (!GuavaUtils.isNullOrEmpty(nextRecordsUrl)) {
                                Log.i(TAG, "nextRecordsUrl is: " + nextRecordsUrl);
                                objectQuery = nextRecordsUrl;
                                firstSetOfRecords = false;
                            } else {
                                objectQuery = null;
                            }

                        } else {
                            Log.e(TAG,
                                    "in sendRequest: failed to get data. objectType: "
                                            + objectName + " query: " + objectQuery
                                            + "result: " + result.asString());
                            success = false;
                            objectQuery = null; // nullify soql to prevent infinite
                            // loop in case of error
                        }
                    }

                    if (success) {

                        // go thru smartstore and remove records not in the map;

                        //Fetch all is ok since we are just fetching only the Ids
                        JSONArray records = dataManagerImpl.fetchAllSmartSQLQuery(String.format(FIELD_FETCH_SQL_FORMAT, objectName, CONSTANTS_ID));
                        for (int i = 0; i < records.length(); i++) {
                            String Id = records.getJSONArray(i).getString(0);
                            // it is not in the map it is deleted
                            if (!objectIDMap.containsKey(Id)) {
                                Log.v(TAG, "in purge, deleting object with id: " + Id);
                                if (containsContentFiles) {
                                    downloadHelper.removeContentFiles(objectName, Id);
                                }
                                dataManagerImpl.deleteRecordWithoutAddingToQueue(objectName, Id);
                            }
                        }

                        // Change to lastPurgeTime but are we going to use it
                        // PreferenceUtils.putLastRefreshTime(objectType, lastRefreshTimeValue, context);
                    }

                }

            }

            // send a broadcast to say that data is updated, we should use a different broadcast for this
//			Log.v(TAG,
//					"sending file received broadcast");
//			Intent broadcastIntent = new Intent(
//					DataManager.SYNC_ENGINE_CONTENT_FILES_RECEIVED);
//			context.sendBroadcast(broadcastIntent);

        } catch (Exception e) {
            Log.e(TAG, "in purge. got exception");
            e.printStackTrace();
        }

    }

    // Quick fix to address OOM errors caused by fetching records that have too many fields and data
    // TODO: Compute batch size using object meta data and query filters rather than hardcoding to 500

    private static final int BATCH_SIZE = 500;

    private static final Map<String, String> QUERY_HTTP_HEADERS;

    private static final int MAXFIELD_COUNT_FOR_BATCHING = 50;

    static {
        Map<String, String> h = new HashMap<String, String>();
        h.put("Sforce-Query-Options", "batchSize=" + BATCH_SIZE);
        QUERY_HTTP_HEADERS = Collections.unmodifiableMap(h);
    }

    private static RestRequest getRequestForQuery(String apiVersion, String q, int numberOfFields) throws UnsupportedEncodingSyncException {
        StringBuilder path = new StringBuilder(String.format("/services/data/%s/query", apiVersion));
        path.append("?q=");
        path.append(urlEncode(q, HTTP.UTF_8));
        if (numberOfFields > MAXFIELD_COUNT_FOR_BATCHING) {
            return new RestRequest(RestMethod.GET, path.toString(), QUERY_HTTP_HEADERS);
        } else {
            // let the server decide on the batch size
            return new RestRequest(RestMethod.GET, path.toString());
        }
    }

    private static String urlEncode(String s, String charsetName) throws UnsupportedEncodingSyncException {
        try {
            return URLEncoder.encode(s, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingSyncException(e, "Charset: " + charsetName + " is not supported.");
        }
    }

    public static void setSyncStatus(SyncStatusState status, SyncStatusStage stage, String description) {
        syncStatus.setStatus(status);
        syncStatus.setStage(stage);
        syncStatus.setDescription(description);
    }


    public static String getSyncDataSizes(Context context, DataManager dataManager) {
        StringBuffer sb = new StringBuffer();
        SmartStore smartStore = dataManager.getSmartStore();
        sb.append("Database size: " +
                android.text.format.Formatter.formatShortFileSize(context, smartStore.getDatabaseSize()));
        Log.i(TAG, "Database size: " +
                android.text.format.Formatter.formatShortFileSize(context, smartStore.getDatabaseSize()));
        sb.append("\n");
        List<String> soupNames = smartStore.getAllSoupNames();
        for (String soupName : soupNames) {
            int count = dataManager.getRecordCount(soupName, "Id");
            sb.append(soupName).append(": ").append(count).append("\n");
        }
        return sb.toString();
    }

    private void checkAndClearDataIfRequired(String objectName, DataManager dataManager) {
        // This will prevent duplicates when the full sync fails in the middle of record fetching

        int count = dataManager.getRecordCount(objectName, "Id");

        if (count == 0) return;
        else {
            Log.e(TAG, "non zero count during full sync. count is: " + count);
            SmartStore smartStore = dataManager.getSmartStore();
            smartStore.dropSoup(objectName);
            ManifestUtils manifestUtils = ManifestUtils.getInstance(context);
            manifestUtils.invalidateManifest();

            for (ConfigObject configObject : manifestUtils.getManifest().getObjects()) {
                if (objectName.equals(configObject.getObjectName())) {


                    FieldToIndex[] fieldsToIndex = configObject.getFieldsToIndex();
                    IndexSpec[] indexSpec = null;
                    if (fieldsToIndex != null) {
                        indexSpec = new IndexSpec[fieldsToIndex.length];
                        for (int i = 0; i < fieldsToIndex.length; i++) {
                            FieldToIndex fieldToIndex = fieldsToIndex[i];
                            indexSpec[i] = new IndexSpec(fieldToIndex.getName(), SmartStore.Type.valueOf(fieldToIndex.getType()));
                        }
                    }
                    smartStore.registerSoup(objectName, indexSpec);
                    break;
                }
            }

            count = dataManager.getRecordCount(objectName, "Id");
            Log.e(TAG, "count after recreate is: " + count);

        }

    }

    private void uploadRecordToAzure(QueueObject queueObj) {

        try {
            String partitionKey = queueObj.getObjectType();
            String rowKey = queueObj.getId();
            JSONObject elementField = queueObj.getFieldsJson();

            DynamicTableEntity dynamicTableEntity = new DynamicTableEntity(partitionKey, rowKey);

            HashMap<String, EntityProperty> properties = new HashMap<String, EntityProperty>();

            Iterator temp = elementField.keys();
            while (temp.hasNext()) {
                String currentKey = (String) temp.next();
                Object currentValue = elementField.get(currentKey);
                if (currentValue instanceof String) {
                    properties.put(currentKey, new EntityProperty((String) elementField.get(currentKey)));
                } else if (currentValue instanceof Integer) {
                    properties.put(currentKey, new EntityProperty((Integer) elementField.get(currentKey)));
                } else if (currentValue instanceof Long) {
                    properties.put(currentKey, new EntityProperty((Long) elementField.get(currentKey)));
                } else if (currentValue instanceof Double) {
                    properties.put(currentKey, new EntityProperty((Double) elementField.get(currentKey)));
                }
            }

            dynamicTableEntity.setProperties(properties);

            // Create an operation to add the new event
            TableOperation insertEvent = TableOperation.insertOrMerge(dynamicTableEntity);

            Log.e("Babu", "about to insert into Azure: " + queueObj.toJson());
            // Submit the operation to the table service.
            azureTable.execute(insertEvent);
        } catch (Exception e) {
            //got exception ...
            Log.e(TAG, "got exception during insert record into Azure: " + e.getMessage());
        }
    }
}

