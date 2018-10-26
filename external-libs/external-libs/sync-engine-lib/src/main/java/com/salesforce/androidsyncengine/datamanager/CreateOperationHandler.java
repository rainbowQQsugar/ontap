package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.DummyCompositeSubrequest;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jakub Stefanowski on 22.05.2017.
 */

public class CreateOperationHandler extends QueueOperationHandler {

    private static final String TAG = "CreateOperationUpload";

    private static final String SMALL_ID_FIELD = "id";

    @Override
    public CompositeSubrequest createRequest(QueueUploadHelper helper, QueueObject queueObj) throws SyncException, JSONException {
        Context context = helper.getContext();
        String objectType = queueObj.getObjectType();

        Map<String, Object> updatedFields = getUpdatedFields(context, queueObj);
        Log.v(TAG, "params: " + objectType + ", " + queueObj.getId() + ", " + updatedFields);

        if (!updatedFields.isEmpty()) {
            updatedFields = convertFieldNames(context, queueObj, updatedFields);

            JSONObject requestData = new JSONObject(updatedFields);
            String serverName = ManifestUtils.getNamespaceSupportedObjectName(objectType, context);
            String referenceId = createReferenceId(queueObj);
            return helper.getSyncHelper().getCreateCompositeRequest(queueObj, requestData, serverName, referenceId);
        }
        else {
            // No need to send any request if there is no updated fields.
            return new DummyCompositeSubrequest();
        }
    }

    private Map<String, Object> convertFieldNames(Context context, QueueObject queueObj, Map<String, Object> fields) {
        Map<String, Object> nameSpacedFields = new HashMap<>();
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
        Map<String, Object> updatedFields = new HashMap<>();

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

        if (response == null) {
            dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
        }
        else {
            Context context = helper.getContext();
            JSONObject jsonObject = response.asJSONObject();
            String serverId = jsonObject.getString(SMALL_ID_FIELD);

            dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
            dataManager.updateWithServerId(queueObj, serverId, context);

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

        // update the retryCount
        dataManager.incrementQueueRecordRetryCount(queueObj.getSoupEntryId());

        // the below retryCount is before we incremented it and updated the database
        if (queueObj.getRetryCount() + 1 >= getQueueRetryMaxAttempts(errorObject)) {

            // we want to create only one error not multiple entries
            if (errorObject != null) {
                // Insert into error store
                dataManager.insertError(errorObject);
            }

            // if it is a create record failure then just delete it from the smartstore
            // without adding it to the queue
            Log.e(TAG, "create failure. delete frome queue and delete from smartstore.");
            dataManager.deleteQueueRecordFromClient(queueObj.getSoupEntryId(), true);
            dataManager.deleteRecordWithoutAddingToQueue(queueObj.getObjectType(), queueObj.getId());
        } else if (isLockedError(errorObject)) {
            helper.requestDelayBeforeNextBatch(LOCK_ERROR_DELAY);
        }
    }

    @Override
    public void updateReferenceMap(String referenceId, QueueObject queueObj, Map<String, String> localValueToReferencedField) {
        super.updateReferenceMap(referenceId, queueObj, localValueToReferencedField);
        localValueToReferencedField.put(queueObj.getId(), String.format("@{%s.id}", referenceId));
    }
}
