package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by Jakub Stefanowski on 22.05.2017.
 */

public class FetchOperationHandler extends QueueOperationHandler {

    private static final String TAG = "FetchOperationUpload";

    private Set<String> sensitiveFields;

    @Override
    public CompositeSubrequest createRequest(QueueUploadHelper helper, QueueObject queueObj) throws SyncException, JSONException {
        String referenceId = createReferenceId(queueObj);
        return helper.getSyncHelper().getFetchCompositeRequest(queueObj.getQuery(), referenceId);
    }

    @Override
    public void handleResponse(QueueUploadHelper helper, QueueObject queueObj, CompositeSubresponse response) throws SyncException, JSONException {
        SmartStoreDataManagerImpl dataManager = helper.getDataManager();
        Context context = helper.getContext();

        JSONObject jsonObject = response.asJSONObject();
        JSONArray records = jsonObject.getJSONArray("records");
        Log.v(TAG, "In fetch record size: " + records.length());

        String objectType = queueObj.getObjectType();
        if (sensitiveFields == null) {
            sensitiveFields = SFObjectMetadata.getSensitiveFieldsWithoutNamespace(context, objectType);
            Log.i(TAG, "sensitive fields: " + sensitiveFields);
        }

        Set<String> localFields = PreferenceUtils.getLocalFields(objectType, context);
        dataManager.insertRecords(objectType, records, false, false, false, localFields, sensitiveFields);
        dataManager.deleteQueueRecords(queueObj.getSoupEntryId());

        helper.addObjectsToFetchDuringAutoSync(objectType);
    }

    @Override
    public void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ErrorObject errorObject, CompositeSubresponse response) throws SyncException, JSONException {
        if (isProcessingHaltedError(response)) return;
        handleError(helper, queueObj);
    }

    @Override
    public void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ServerErrorException serverException) throws SyncException, JSONException {
        handleError(helper, queueObj);
    }

    private void handleError(QueueUploadHelper helper, QueueObject queueObj) {
        // fetch calls are made for error handling scenarios so we can safely ignore them
        // when this happens or we may have a nested sequence of error handling calls
        // if we try to delete a record and if the record deletion fails on server
        // then we try to fetch the record but this record could have been deleted on the server so
        // we can ignore this

        // Delete this entry from queue
        SmartStoreDataManagerImpl dataManager = helper.getDataManager();
        dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
    }
}
