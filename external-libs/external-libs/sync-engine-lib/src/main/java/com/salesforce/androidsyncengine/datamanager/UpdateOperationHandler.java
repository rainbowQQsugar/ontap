package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueOperation;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.soql.QueryOp;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.DummyCompositeSubrequest;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.salesforce.androidsyncengine.datamanager.DataManager.CONSTANTS_ID;

/**
 * Created by Jakub Stefanowski on 22.05.2017.
 */

public class UpdateOperationHandler extends QueueOperationHandler {

    private static final String TAG = "UpdateOperationUpload";

    @Override
    public CompositeSubrequest createRequest(QueueUploadHelper helper, QueueObject queueObj) throws SyncException, JSONException {
        Context context = helper.getContext();
        String objectType = queueObj.getObjectType();

        Map<String, Object> updatedFields = getUpdatedFields(context, queueObj);
        Log.v(TAG, "params: " + objectType + ", " + queueObj.getId() + ", " + updatedFields);

        if (!updatedFields.isEmpty()) {
            updatedFields = convertFieldNames(context, queueObj, updatedFields);

            JSONObject data = new JSONObject(updatedFields);
            String serverName = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
            String referenceId = createReferenceId(queueObj);
            return helper.getSyncHelper().getUpdateCompositeRequest(queueObj, data, serverName, referenceId);
        }
        else {
            // No need to send any request if there is no updated fields.
            return new DummyCompositeSubrequest();
        }
    }

    private Map<String, Object> convertFieldNames(Context context, QueueObject queueObj, Map<String, Object> fields) {
        Map<String, Object> nameSpacedFields = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            key = ManifestUtils.getNamespaceSupportedFieldName(queueObj.getObjectType(), key, context);
            nameSpacedFields.put(key, entry.getValue());
        }

        return nameSpacedFields;
    }

    private Map<String, Object> getUpdatedFields(Context context, QueueObject queueObj) throws JSONException {
        JSONObject elementField = queueObj.getFieldsJson();
        Log.v(TAG, "params: " + queueObj.getObjectType() + ", " + queueObj.getId() + ", " + elementField);

        // map to hold changed fields
        Map<String, Object> updatedFields = new HashMap<String, Object>();

        // iterate over all changed fields and only gather non-local ones
        Set<String> localFields = PreferenceUtils.getLocalFields(queueObj.getObjectType(), context);
        Iterator temp = elementField.keys();
        while (temp.hasNext()) {
            String currentKey = (String) temp.next();
            // Only add if elements if not part of localfields
            if (localFields == null || !localFields.contains(currentKey)) {
                updatedFields.put(currentKey, elementField.get(currentKey));
            }
        }

        return updatedFields;
    }

    @Override
    public void handleResponse(QueueUploadHelper helper, QueueObject queueObj, CompositeSubresponse response) throws SyncException, JSONException {
        SmartStoreDataManagerImpl dataManager = helper.getDataManager();
        dataManager.deleteQueueRecords(queueObj.getSoupEntryId());

        if (response != null) {
            helper.addObjectsToFetchDuringAutoSync(queueObj.getObjectType());
        }
    }

    @Override
    public void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ErrorObject errorObject, CompositeSubresponse response) throws SyncException, JSONException {
        if (isProcessingHaltedError(response)) return;
        handleError(helper, queueObj, errorObject);
    }

    @Override
    public void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ServerErrorException serverException) throws SyncException, JSONException {
        handleError(helper, queueObj, null);
    }

    private void handleError(QueueUploadHelper helper, QueueObject queueObj, ErrorObject errorObject) {
        SmartStoreDataManagerImpl dataManager = helper.getDataManager();
        Context context = helper.getContext();
        String objectType = queueObj.getObjectType();

        // update the retryCount
        dataManager.incrementQueueRecordRetryCount(queueObj.getSoupEntryId());

        // the below retryCount is before we incremented it and updated the database
        if (queueObj.getRetryCount() + 1 >= getQueueRetryMaxAttempts(errorObject)) {

            if (errorObject != null) {
                // Insert into error store
                Log.e(TAG, "errorObject is: " + errorObject.toString());
                dataManager.insertError(errorObject);
            }

            // if it is a delete or update failure then try to fetch this record
            // Add a fetch operation to queue instead of sending request here
            dataManager.deleteQueueRecordFromClient(queueObj.getSoupEntryId(), false);

            QueueObject newQueueObj = new QueueObject(queueObj.getId(), objectType, QueueOperation.FETCH);

            SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectType, context);
            FilterObject filterObject = new FilterObject();
            filterObject.setField(CONSTANTS_ID);
            filterObject.setValue(queueObj.getId());
            filterObject.setOp(QueryOp.eq);
            objectMetadata.setFilterObjects(Arrays.asList(filterObject));

            // Use simplified query since we have set custom filters.
            String query = objectMetadata.getSimpleQuery(context);

            newQueueObj.setQuery(query);
            Log.e(TAG, "update failure. creating query to refetch data. query: " + query);

            dataManager.insertRecordIntoQueue(newQueueObj);
        }  else if (isLockedError(errorObject)) {
            helper.requestDelayBeforeNextBatch(LOCK_ERROR_DELAY);
        }
    }
}
