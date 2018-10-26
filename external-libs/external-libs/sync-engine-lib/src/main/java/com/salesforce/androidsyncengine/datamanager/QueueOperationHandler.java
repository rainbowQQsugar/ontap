package com.salesforce.androidsyncengine.datamanager;

import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Jakub Stefanowski on 19.05.2017.
 */

abstract class QueueOperationHandler {

//    private static final String TAG = "QueueOperationHandler";

    protected static final int QUEUE_RETRY_MAX_ATTEMPTS = 2;
    protected static final int QUEUE_RETRY_ON_LOCK_ATTEMPTS = 4;

    protected static final long LOCK_ERROR_DELAY = 30_000L;

    private static final String PROCESSING_HALTED_ERROR = "PROCESSING_HALTED";

    public abstract CompositeSubrequest createRequest(QueueUploadHelper helper, QueueObject queueObj) throws SyncException, JSONException;

    public abstract void handleResponse(QueueUploadHelper helper, QueueObject queueObj, CompositeSubresponse response) throws SyncException, JSONException;

    public abstract void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ErrorObject errorObject, CompositeSubresponse response) throws SyncException, JSONException;

    public abstract void handleServerError(QueueUploadHelper helper, QueueObject queueObj, ServerErrorException serverException) throws SyncException, JSONException;

    public void updateReferenceMap(String referenceId, QueueObject queueObj, Map<String, String> localValueToReferencedField) {

    }

    protected static String createReferenceId(QueueObject queueObj) {
        return "ref_" + queueObj.getSoupEntryId();
    }

    /**
     * Processing halted is an error which happens when previous request from batch has failed.
     * Usually this error should be ignored.
     */
    protected static boolean isProcessingHaltedError(CompositeSubresponse response) {
        if (response == null) return false;

        if (!response.isSuccess()) {
            JSONObject jsonObject = response.asJSONArray().optJSONObject(0);
            if (jsonObject != null) {
                return PROCESSING_HALTED_ERROR.equals(jsonObject.optString("errorCode"));
            }
        }

        return false;
    }

    protected boolean isLockedError(ErrorObject errorObject) {
        if (errorObject == null) {
            return false;
        }

        String errorCode = errorObject.getErrorCode();
        return "ENTITY_IS_LOCKED".equals(errorCode) || "UNABLE_TO_LOCK_ROW".equals(errorCode);
    }

    protected int getQueueRetryMaxAttempts(ErrorObject errorObject) {
        if (isLockedError(errorObject)) {
            return QUEUE_RETRY_ON_LOCK_ATTEMPTS;
        } else {
            return QUEUE_RETRY_MAX_ATTEMPTS;
        }
    }
}
