package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;

import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * @author usanaga
 */
public interface DataManager {

    /**
     * This method needs to be called before you perform any operations on the DataManager. This will
     * initialize the local store if it is not initialized and do any upgrades to the local store if
     * required.
     */
    void init(Context context);

    void clearLocalData(Context context, boolean shouldClearContent);

    boolean getEncryptionStatus(Context context);

    boolean isOnline();

    /**
     * Update a record in the local store and queue a request to the server
     *
     * @param objectName        Name of the SFDC Object
     * @param Id                Saleforce ID of the record to be updated
     * @param updatedFieldsJSON JSONObject that contains the fields that have been updated
     * @return true if the request was successfully queued
     */
    boolean updateRecord(String objectName, String Id, JSONObject updatedFieldsJSON);

    /**
     * Delete a record from the local store and queue a request to the server
     *
     * @param objectName Name of the SFDC Object
     * @param Id         Salesforce ID of the record to be deleted
     * @return true if the request was successfully queued
     */
    boolean deleteRecord(String objectName, String Id);

    /**
     * Create a record in the local store with a temporary Id and queue a request to create it on the server.
     * Once the sync comple
     *
     * @param objectName Name of the SFDC Object
     * @param objectJSON JSONObject containing the fields for the new object. This JSONObject should not
     *                   contain the Id field.
     * @return String -- record ID if the request was successfully queued
     */
    String createRecord(String objectName, JSONObject objectJSON);

    /**
     * @param objectName Name of the SFDC Object
     * @param field      Field within the object to be ordered by
     * @return JSONArray that contains all the records
     */
    JSONArray fetchAllRecords(String objectName, String field);

    /**
     * @param objectName Name of the SFDC Object
     * @param field      Field within the object to be ordered by
     * @param pageIndex  the page number to fetch
     * @param pageSize   page size to use
     * @return JSONArray that contains all the records
     */
    JSONArray fetchRecords(String objectName, String field, int pageIndex, int pageSize);


    /**
     * @param objectName Name of the SFDC Object
     * @param field      Field within the object to be ordered by
     * @return number of records
     */
    int getRecordCount(String objectName, String field);

    JSONArray fetchSmartSQLQuery(String smartSql, int pageIndex, int pageSize);

    JSONArray fetchAllSmartSQLQuery(String smartSql);

    JSONObject exactQuery(String soupName, String path,
                          String exactMatchKey);

    JSONArray fetchMatchingRecords(String soupName, String path, String matchKey, int pageSize);

    void logout(Context context, RestClient client);


    /**
     * @return Returns a list of all errors that have been encountered
     */
    List<ErrorObject> getErrors();

    /**
     * Clear all error messages
     */
    void clearErrors();

    /**
     * @return Return the current state of sync
     */
    SyncStatus getSyncStatus();

    /**
     * @param objectName
     * @param id
     * @return file path if downloaded/exists or null
     */
    String getFilePath(Context context, String objectName, String id);

    /**
     * Delete the records from the queue
     *
     * @param soupEntryIds - values that are passed via the ErrorObject to the client app
     */
    void deleteQueueRecordFromClient(Long soupEntryId, boolean removeAllIfCreate);

    QueueObject getQueueRecordFromClient(Long soupEntryId);

    String getSalesforceIdFromTemporaryId(String temporaryId);

    /**
     * @return Returns the list of all item in the Queue
     */
    List<QueueObject> getQueueRecords();

    boolean isFirstSyncComplete(Context context);

    SmartStore getSmartStore();

    String CONSTANTS_ID = "Id";

    // public static final String SYNC_ENGINE = "SyncEngine";

    String SYNC_STARTED = "com.salesforce.androidsyncengine.SYNC_STARTED";

    String SYNC_ENGINE_FAILURE = "com.salesforce.androidsyncengine.SYNC_ENGINE_FAILURE";

    String SYNC_ENGINE_ERROR = "com.salesforce.androidsyncengine.SYNC_ENGINE_ERROR";

    String SYNC_COMPLETED = "com.salesforce.androidsyncengine.SYNC_COMPLETED";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    String DYNAMIC_FETCH_STARTED = "com.salesforce.androidsyncengine.DYNAMIC_FETCH_STARTED";

    String DYNAMIC_FETCH_PROGRESS = "com.salesforce.androidsyncengine.DYNAMIC_FETCH_PROGRESS";

    String DYNAMIC_FETCH_COMPLETED = "com.salesforce.androidsyncengine.DYNAMIC_FETCH_COMPLETED";

    String DYNAMIC_FETCH_ERROR = "com.salesforce.androidsyncengine.DYNAMIC_FETCH_ERROR";

    String EXTRAS_DYNAMIC_FETCH_STATUS = "com.salesforce.androidsyncengine.EXTRAS_DYNAMIC_FETCH_STATUS";

    String EXTRAS_DYNAMIC_FETCH_NAME = "com.salesforce.androidsyncengine.EXTRAS_DYNAMIC_FETCH_NAME";

    String EXTRAS_DYNAMIC_FETCH_PARAMS = "com.salesforce.androidsyncengine.EXTRAS_DYNAMIC_FETCH_PARAMS";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    String EXTRAS_ERROR_READABLE_MESSAGE = "readableMessage";

    String SYNC_ENGINE_CONTENT_FILES_RECEIVED = "com.salesforce.androidsyncengine.SYNC_ENGINE_CONTENT_FILES_RECEIVED";

    String SYNC_ENGINE_AUTHORITY = "com.salesforce.androidsyncengine.provider";

    // constants for sync UI
    String FETCH_CONFIGURATION_STARTED = "com.salesforce.androidsyncengine.FETCH_CONFIGURATION_STARTED";
    String FETCH_CONFIGURATION_COMPLETED = "com.salesforce.androidsyncengine.FETCH_CONFIGURATION_COMPLETED";
    String FETCH_METADATA_STARTED = "com.salesforce.androidsyncengine.FETCH_METADATA_STARTED";
    String FETCH_METADATA_COMPLETED = "com.salesforce.androidsyncengine.FETCH_METADATA_COMPLETED";
    String FETCH_CONTENT_STARTED = "com.salesforce.androidsyncengine.FETCH_CONTENT_STARTED";
    String FETCH_CONTENT_COMPLETED = "com.salesforce.androidsyncengine.FETCH_CONTENT_COMPLETED";
    String SYNC_FINISHING = "com.salesforce.androidsyncengine.SYNC_FINISHING";
    String SYNC_FINISHED = "com.salesforce.androidsyncengine.SYNC_FINISHED";


}