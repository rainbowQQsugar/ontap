package com.salesforce.androidsyncengine.syncsteps.localcleanup;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.exceptions.RuntimeSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import rx.functions.Action1;

/**
 * Class that for each provided object name runs a local cleanup.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
class LocalCleanupConsumer implements Action1<String> {

    private static final String TAG = LocalCleanupConsumer.class.getSimpleName();

    private final Context context;

    private final ManifestProcessor manifestProcessor;

    private final Configuration configuration;

    private final SmartStoreDataManagerImpl dataManagerImpl;

    private final DownloadHelper downloadHelper;

    public LocalCleanupConsumer(SyncControls syncControls) {
        this.context = syncControls.getContext();
        this.manifestProcessor = syncControls.getManifestProcessor();
        this.configuration = PreferenceUtils.getConfiguration(context);
        this.dataManagerImpl = syncControls.getDataManager();
        this.downloadHelper = syncControls.getDownloadHelper();
    }

    @Override
    public void call(String objectName) {
        try {
            consume(objectName);
        } catch (SyncException e) {
            throw RuntimeSyncException.wrap(e);
        }
    }

    private void consume(String objectName) throws SyncException {
        try {
            for (String cleanupQuery : getCleanupQueries(objectName)) {
                try {
                    JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(cleanupQuery);
                    if (recordsArray.length() > 0) {
                        deleteLocalRecords(recordsArray, objectName);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "in cleanupQueryList got exception: ", e);
                }
            }

        } catch (ManifestProcessingException e) {
            Log.w(TAG, "Error while processing cleanup query.", e);
            throw new SimpleSyncException(e);

        } catch (Exception e) {
            Log.e(TAG, "got exception: ", e);
        }
    }

    private List<String> getCleanupQueries(String objectName) throws ManifestProcessingException {
        List<String> result = new ArrayList<>();

        SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
        if (objectMetadata != null) {
            String[] cleanupQueryFilters = objectMetadata.getCleanupQueryFilters();

            if (!isEmpty(cleanupQueryFilters)) {
                for (int i = 0; i < cleanupQueryFilters.length; i++) {
                    String filter = manifestProcessor.processLocalQuery(configuration,
                            objectMetadata, cleanupQueryFilters[i]);
                    String smartSql = String.format("SELECT {%s:Id} FROM {%s} WHERE %s",
                            objectName, objectName, filter);

                    Log.i(TAG, "cleanupQuery is: " + smartSql);

                    result.add(smartSql);
                }
            }
        }

        return result;
    }

    private void deleteLocalRecords(JSONArray records, String objectName) {
        Log.i(TAG, "in deleteLocalRecords for: " + objectName + " with length: " + records.length());
        boolean containsContentFiles = PreferenceUtils.containsBinaryField(objectName, context);

        for (int i = 0; i < records.length(); i++) {
            String id = readId(records.optJSONArray(i));

            if (!TextUtils.isEmpty(id)) {
                if (containsContentFiles) {
                    downloadHelper.removeContentFiles(objectName, id);
                }

                dataManagerImpl.deleteRecordWithoutAddingToQueue(objectName, id);
            }
        }
    }

    private static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    private static String readId(JSONArray jsonArray) {
        return jsonArray == null ? null : jsonArray.optString(0);
    }
}
