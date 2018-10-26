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
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.json.JSONException;

import java.util.Arrays;

import static com.salesforce.androidsyncengine.datamanager.DataManager.CONSTANTS_ID;
import static com.salesforce.androidsyncengine.syncmanifest.ManifestUtils.getNamespaceSupportedObjectName;

/**
 * Created by Jakub Stefanowski on 22.05.2017.
 */

public class DeleteOperationHandler extends QueueOperationHandler {

    private static final String TAG = "DeleteOperationUpload";

    @Override
    public CompositeSubrequest createRequest(QueueUploadHelper helper, QueueObject queueObj) throws SyncException, JSONException {
        String serverName = getNamespaceSupportedObjectName(queueObj.getObjectType(), helper.getContext());
        String referenceId = createReferenceId(queueObj);
        return helper.getSyncHelper().getDeleteCompositeRequest(queueObj, serverName, referenceId);
    }

    @Override
    public void handleResponse(QueueUploadHelper helper, QueueObject queueObj, CompositeSubresponse response) throws SyncException, JSONException {
        SmartStoreDataManagerImpl dataManager = helper.getDataManager();
        dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
        helper.addObjectsToFetchDuringAutoSync(queueObj.getObjectType());
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

        String Id = queueObj.getId();
        String objectType = queueObj.getObjectType();

        if (errorObject != null) {
            // Insert into error store
            Log.e(TAG, "errorObject is: " + errorObject.toString());
            dataManager.insertError(errorObject);
        }

        // update the retryCount
        dataManager.incrementQueueRecordRetryCount(queueObj.getSoupEntryId());

        QueueObject newQueueObj = new QueueObject(Id, objectType, QueueOperation.FETCH);

        SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectType, context);
        FilterObject filterObject = new FilterObject();
        filterObject.setField(CONSTANTS_ID);
        filterObject.setValue(Id);
        filterObject.setOp(QueryOp.eq);
        objectMetadata.setFilterObjects(Arrays.asList(filterObject));

        // Use simplified query since we have set custom filters.
        String query = objectMetadata.getSimpleQuery(context);

        newQueueObj.setQuery(query);
        Log.e(TAG, "delete failure. creating query to refetch data. query: " + query);

        dataManager.insertRecordIntoQueue(newQueueObj);

        // Delete this entry from queue
        // Without this call on delete failures we keep adding to the queue
        dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
    }
}
