package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.data.model.DescribeSObjectResult;
import com.salesforce.androidsyncengine.data.model.Field;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueOperation;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeResponseHandler;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.androidsyncengine.utils.TransactionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.salesforce.androidsyncengine.datamanager.SyncEngine.soqlQueryDateFormat;

/**
 * Created by Jakub Stefanowski on 19.05.2017.
 */

class QueueUploadHelper {

    private static final String TAG = "QueueUploadHelper";

    /** Amount of time that will be added to retry query after failed request. */
    private static final long FAIL_DATE_BUFFER = 25L * 1000L;

    private static final long TRANSACTION_FAIL_DATE_BUFFER = 30L * 60L * 1000L;

    private SmartStoreDataManagerImpl dataManagerImpl;

    private SyncEnginePrefs preferences;

    private SyncStatus syncStatus;

    private SyncHelper syncHelper;

    private Context context;

    private SyncEngine syncEngine;

    private boolean hasErrors = false;

    private long delayBeforeNextBatch = 0;

    private SyncException lastError;

    public QueueUploadHelper(SyncEngine syncEngine) {
        dataManagerImpl = syncEngine.getDataManager();
        preferences = syncEngine.getPreferences();
        syncStatus = syncEngine.getSyncStatus();
        syncHelper = syncEngine.getSyncHelper();
        context = syncEngine.getContext();
        this.syncEngine = syncEngine;
    }

    public Context getContext() {
        return context;
    }

    public SyncHelper getSyncHelper() {
        return syncHelper;
    }

    public SmartStoreDataManagerImpl getDataManager() {
        return dataManagerImpl;
    }

    public SyncEnginePrefs getPreferences() {
        return preferences;
    }

    public SyncException getLastError() {
        return lastError;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    /*package*/ void addObjectsToFetchDuringAutoSync(String objectType) {
        syncEngine.addObjectsToFetchDuringAutoSync(objectType);
    }

    public void requestDelayBeforeNextBatch(long lengthMillis) {
        delayBeforeNextBatch = Math.max(delayBeforeNextBatch, lengthMillis);
    }

    public void uploadQueue() throws SyncException, JSONException {
//        Log.i(TAG, "queueArray:");
//        LongLog.i(TAG, dataManagerImpl.getQueueData().toString(2));

        if (preferences.getFailDate() > 0) {
            handleFailedRequests();
        }

        Map<String, String> localValueToReferencedField = new HashMap<>();
        List<CompositeSubrequest> subrequests = new ArrayList<>();
        List<Long> queueIds = new ArrayList<>();                                                    // Keeps queue id for each subrequest
        boolean isDone = false;
        int i = 0;

        syncStatus.setStage(SyncStatus.SyncStatusStage.UPLOAD_QUEUE);
        syncStatus.setTotalCount(dataManagerImpl.getRecordCount("Queue", null));

        while (!isDone) {
            JSONObject queueRecord = dataManagerImpl.getQueueRecordAt(i);
            boolean sendRequests = false;

            Log.v(TAG, "Queue record " + i + ":");
            syncStatus.setCurrentItem(i);

            if (queueRecord == null) {

                if (subrequests.isEmpty()) {
                    isDone = true;
                }
                else {
                    sendRequests = true;
                }

            }
            else {

                Log.v(TAG, queueRecord.toString(2));
                handleQueueItem(queueRecord, localValueToReferencedField, subrequests, queueIds);
                i++;

                if (!syncHelper.canFitOneRequest(subrequests)) {
                    // Can't send as one request because of the last item.
                    subrequests.remove(subrequests.size() - 1);
                    queueIds.remove(queueIds.size() - 1);
                    sendRequests = true;
                }

            }

            if (sendRequests) {
                boolean isSent = false;
                long sendTime = -1L;

                try {

                    if (delayBeforeNextBatch > 0) {
                        long waitTime = delayBeforeNextBatch;
                        delayBeforeNextBatch = 0;
                        waitFor(waitTime);
                    }

                    sendTime = System.currentTimeMillis();
                    syncHelper.sendCompositeRequests(subrequests, /*allOrNone*/ true);
                    isSent = true;

                    i = 0;
                    syncStatus.setTotalCount(dataManagerImpl.getRecordCount("Queue", null));
                    subrequests.clear();
                    queueIds.clear();
                    localValueToReferencedField.clear();
                }
                catch (ServerErrorException e) {
                    Log.w(TAG, e);

                    // We know that it wasn't saved on server side
                    isSent = true;

                    // Add error object
                    String additionalInfo = String.format(Locale.US, "Failed while sending batch of %d messages.", subrequests.size());
                    ErrorObject errorObject = new ErrorObject("", "BatchRequest", additionalInfo, e.getErrorCode(), e.getMessage(), -1L, "");
                    dataManagerImpl.insertError(errorObject);

                    // Pass server error to subrequests.
                    reportServerErrorToSubrequests(subrequests, e);

                    // Clear queue state and retry from the first item.
                    i = 0;
                    subrequests.clear();
                    queueIds.clear();
                    localValueToReferencedField.clear();
                }
                finally {
                    if (!isSent) {
                        preferences.setFailData(sendTime, queueIds);
                    }
                }
            }
        }
    }

    private void waitFor(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.w(TAG, e);
        }
    }

    private void reportServerErrorToSubrequests(List<CompositeSubrequest> subrequests, ServerErrorException e) throws JSONException, SyncException {
        if (e == null || subrequests == null || subrequests.isEmpty()) return;

        for (CompositeSubrequest subrequest : subrequests) {
            UploadResponseHandler responseHandler = (UploadResponseHandler) subrequest.getResponseHandler();
            if (responseHandler != null) {
                responseHandler.handleServerError(e);
            }
        }
    }

    private JSONObject updateWithServerId(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) return null;

        // Update local id with server id
        String jsonString = jsonObject.toString();
        jsonString = dataManagerImpl.updatedWithServerIdJsonString(jsonString);
        return new JSONObject(jsonString);
    }

    private void handleQueueItem(JSONObject recordJson, Map<String, String> localValueToReferencedField, List<CompositeSubrequest> subrequests, List<Long> queueIds) throws JSONException, SyncException {
        recordJson = updateWithServerId(recordJson);
        QueueObject queueObj = new QueueObject(recordJson);

        // Add transaction id if this object allows one. It will be used on send failure
        // to check if request got to the server.
        addTransactionIdIfNeeded(queueObj);

        // Replace any local values.
        QueueObject queueObjWithReferences = new QueueObject(queueObj);
        replaceLocalValues(queueObjWithReferences, localValueToReferencedField);


        if (dataManagerImpl.isClientId(queueObjWithReferences.getId())
                && !QueueOperation.CREATE.equals(queueObjWithReferences.getOperation())) {
            Log.e(TAG, "Skipping queue record using local id: " + queueObjWithReferences.getId());
            return;
        }

        syncStatus.setDescription("" + queueObj.getOperation() + " on object: "
                + queueObj.getObjectType() + " with id: " + queueObj.getId());

        QueueOperationHandler operationHandler = getOperationHandler(queueObj.getOperation());

        CompositeSubrequest request = operationHandler.createRequest(this, queueObjWithReferences);
        operationHandler.updateReferenceMap(request.getReferenceId(), queueObjWithReferences, localValueToReferencedField);

        request.setResponseHandler(new QueueUploadHelper.UploadResponseHandler(this, queueObj, operationHandler));
        subrequests.add(request);
        queueIds.add(queueObj.getSoupEntryId());
    }

    public void setLastError(SyncException lastError) {
        this.lastError = lastError;
        this.hasErrors = lastError != null;
    }

    private static ErrorObject createErrorObject(QueueObject queueObj, CompositeSubresponse result) {
        JSONObject errorInfo = new JSONObject();
        try {
            errorInfo = result.asJSONArray().getJSONObject(0);
        }
        catch (Exception e) {
            Log.e(TAG, "Got exception: " + e.toString(), e);
        }

        String Id = queueObj.getId();
        String objectType = queueObj.getObjectType();
        QueueOperation operation = queueObj.getOperation();

        String additionalInfo = String.format("%s failed on object: %s for id: %s ", operation, objectType, Id);
        String errorCode = errorInfo.optString("errorCode");
        String errorMessage = errorInfo.optString("message");
        String fieldJson = null;
        if (queueObj.getFieldsJson() != null) fieldJson = queueObj.getFieldsJson().toString();

        return new ErrorObject(Id, objectType, additionalInfo, errorCode, errorMessage, queueObj.getSoupEntryId(), fieldJson);
    }

    /** Returns true if QueueObject was updated. */
    private boolean addTransactionIdIfNeeded(QueueObject queueObj) throws JSONException {

        // Fetch doesn't need transaction id.
        if (QueueOperation.FETCH.equals(queueObj.getOperation())) return false;

        // If there already is a transaction Id don't update it.
        JSONObject fields = queueObj.getFieldsJson();
        if (fields == null) return false;

        if (fields.has(SyncEngineConstants.StdFields.TRANSACTION_ID)) return false;

        SFObjectMetadata meta = PreferenceUtils.getMetadataObject(queueObj.getObjectType(), context);
        String transactionIdField = ManifestUtils.getNamespaceSupportedFieldName(
                queueObj.getObjectType(), SyncEngineConstants.StdFields.TRANSACTION_ID, context);

        // Check if object contains transaction id field.
        if (!meta.containsField(transactionIdField)) return false;

        long transactionId = TransactionUtils.generateTransactionId();
        fields.put(SyncEngineConstants.StdFields.TRANSACTION_ID, transactionId);

        dataManagerImpl.updateQueueRecordFields(queueObj.getSoupEntryId(), fields);

        return true;
    }

    public void handleFailedRequests() throws SyncException, JSONException {
        List<Long> recordIds = preferences.getFailedSoupIds();
        List<CompositeSubrequest> subrequests = new ArrayList<>();
        Set<String> localIds = new HashSet<>();

        Log.i(TAG, "Handling " + recordIds + " failed requests.");

        while (!recordIds.isEmpty()) {
            Long recordId = recordIds.remove(0);
            JSONObject jsonRecord = dataManagerImpl.getQueueJsonFromClient(recordId);
            boolean sendRequests = false;

            if (jsonRecord != null) {
                jsonRecord = updateWithServerId(jsonRecord);

                Log.v(TAG, "QueueObj: " + jsonRecord.toString());

                QueueObject queueObj = new QueueObject(jsonRecord);
                QueueOperation operation = queueObj.getOperation();

                if (QueueOperation.CREATE.equals(operation) || QueueOperation.UPDATE.equals(operation)) {
                    String query = createLookupQuery(queueObj);
                    Log.v(TAG, "Lookup query: " + query);

                    String referenceId = "ref_" + queueObj.getSoupEntryId();
                    CompositeSubrequest subrequest = syncHelper.getFetchCompositeRequest(query, referenceId);
                    subrequest.setResponseHandler(new LookupResponseHandler(query, queueObj, this));

                    if (containsLocalValues(queueObj, localIds)) {
                        if (!subrequests.isEmpty()) {
                            Log.v(TAG, "Has local values. Will be added to the next batch.");
                            recordIds.add(0, recordId); // We need to reload that with updated server ids.
                            sendRequests = true;
                        }
                        else {
                            Log.v(TAG, "Has local values and would be first item in the batch. Therefore the item will be ignored.");
                        }
                    }
                    else {

                        if (QueueOperation.CREATE.equals(operation)) {

                            if (subrequests.isEmpty()) {
                                localIds.add(queueObj.getId());
                                subrequests.add(subrequest);
                            }
                            else {
                                // For safety reasons it would be better to have create request as a
                                // first item of batch.
                                recordIds.add(0, recordId);
                                sendRequests = true;
                            }
                        }
                        else {
                            subrequests.add(subrequest);
                        }
                    }
                }
            }

            if (sendRequests || recordIds.isEmpty()) {
                try {
                    Log.v(TAG, "Sending " + subrequests.size() + " subrequests.");
                    syncHelper.sendCompositeRequests(subrequests, /* allOrNone */ false);
                }
                catch (ServerErrorException e) {
                    // Ignore the error. In such case we will retry to send the original requests.
                    Log.w(TAG, "Sending lookup queries was unsuccessful.", e);
                }
                subrequests.clear();
                preferences.updateIds(recordIds);
            }
        }

        preferences.clear();
    }

    private String createLookupQuery(QueueObject queueObject) {
        if (queueObject == null) return null;

        JSONObject fieldsJson = queueObject.getFieldsJson();
        StringBuilder filterBuilder = new StringBuilder();

        // Use transaction id to check if the object already exists.
        if (fieldsJson.has(SyncEngineConstants.StdFields.TRANSACTION_ID)) {

            Date date = new Date(preferences.getFailDate() - TRANSACTION_FAIL_DATE_BUFFER);
            long transactionId = fieldsJson.optLong(SyncEngineConstants.StdFields.TRANSACTION_ID);

            String transactionFieldName = ManifestUtils.getNamespaceSupportedFieldName(
                    queueObject.getObjectType(),
                    SyncEngineConstants.StdFields.TRANSACTION_ID,
                    context);
            filterBuilder.append(String.format("%1$s = %2$d.0",  transactionFieldName, transactionId)); // We keep it as long but the server needs a double.

            filterBuilder.append(String.format(" AND %1$s > %2$s",
                    SyncEngineConstants.StdFields.LAST_MODIFIED_DATE,
                    soqlQueryDateFormat.format(date)));

        }
        else {
            if (QueueOperation.CREATE.equals(queueObject.getOperation())) {
                Date startDate = new Date(preferences.getFailDate() - FAIL_DATE_BUFFER);
                Date endDate = new Date(preferences.getFailDate() + FAIL_DATE_BUFFER);
                filterBuilder.append(String.format("%1$s = '%2$s'",
                        SyncEngineConstants.StdFields.CREATED_BY_ID, syncHelper.getUserId()));

                filterBuilder.append(String.format(" AND %1$s > %2$s AND %1$s < %3$s",
                        SyncEngineConstants.StdFields.CREATED_DATE,
                        soqlQueryDateFormat.format(startDate),
                        soqlQueryDateFormat.format(endDate)));
            } else if (QueueOperation.UPDATE.equals(queueObject.getOperation())) {
                Date date = new Date(preferences.getFailDate() - FAIL_DATE_BUFFER);
                filterBuilder.append(String.format("%1$s > %2$s",
                        SyncEngineConstants.StdFields.LAST_MODIFIED_DATE,
                        soqlQueryDateFormat.format(date)));
            } else {
                throw new IllegalArgumentException("Lookup query works only for CREATE and UPDATE " +
                        "operations. Not for: " + queueObject.getOperation());
            }

            DescribeSObjectResult metadata = MetaDataProvider.getMetaData(context, new Gson(), queueObject.getObjectType());

            if (metadata != null) {
                List<Field> fields = metadata.getFields();

                Iterator<String> iterator = fieldsJson.keys();
                while(iterator.hasNext()) {
                    String fieldName = iterator.next();
                    String value = String.valueOf(fieldsJson.opt(fieldName));
                    String namespacedFieldName = ManifestUtils.getNamespaceSupportedFieldName(
                            queueObject.getObjectType(), fieldName, context);

                    Field field = findField(fields, namespacedFieldName);
                    if (field != null && "reference".equals(field.getType())) {

                        if (value == null) {
                            filterBuilder.append(String.format(" AND %1$s = null", namespacedFieldName));
                        }
                        else {
                            filterBuilder.append(String.format(" AND %1$s = '%2$s'", namespacedFieldName, value));
                        }
                    }
                }
            }
        }

        return String.format("SELECT %1$s FROM %2$s WHERE %3$s",
                SyncEngineConstants.StdFields.ID,
                ManifestUtils.getNamespaceSupportedObjectName(queueObject.getObjectType(), context),
                filterBuilder.toString());
    }

    private static Field findField(List<Field> fields, String fieldName) {
        if (fields == null || fields.isEmpty() || TextUtils.isEmpty(fieldName)) return null;

        for (Field field : fields) {
            if (fieldName.equals(field.getName())) return field;
        }

        return null;
    }

    protected static void replaceLocalValues(QueueObject queueObj, Map<String, String> localValueToReferencedField) throws JSONException {
        JSONObject fieldsJson = queueObj.getFieldsJson();
        replaceLocalValues(fieldsJson, localValueToReferencedField);

        String updatedQuery = replaceLocalValues(queueObj.getQuery(), localValueToReferencedField);
        queueObj.setQuery(updatedQuery);

        String updatedId = localValueToReferencedField.get(queueObj.getId());
        if (updatedId != null) {
            queueObj.setId(updatedId);
        }
    }

    protected static boolean containsLocalValues(QueueObject queueObj, Set<String> localValues) throws JSONException {
        return localValues.contains(queueObj.getId()) ||
                containsLocalValues(queueObj.getQuery(), localValues) ||
                containsLocalValues(queueObj.getFieldsJson(), localValues);
    }

    private static void replaceLocalValues(JSONObject jsonObject, Map<String, String> localValueToReferencedField) throws JSONException {
        if (jsonObject == null) return;

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object obj = jsonObject.opt(key);

            if (obj instanceof String) {
                String referencedField = localValueToReferencedField.get(obj);
                if (referencedField != null) {
                    jsonObject.putOpt(key, referencedField);
                }
            }
            else if (obj instanceof JSONObject) {
                replaceLocalValues((JSONObject) obj, localValueToReferencedField);
            }
            else if (obj instanceof JSONArray) {
                replaceLocalValues((JSONArray) obj, localValueToReferencedField);
            }

            // ignore other data types
        }
    }

    private static boolean containsLocalValues(JSONObject jsonObject, Set<String> localValues) throws JSONException {
        if (jsonObject == null) return false;

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object obj = jsonObject.opt(key);

            if (obj instanceof String) {
                if (localValues.contains(obj)) return true;
            }
            else if (obj instanceof JSONObject) {
                if (containsLocalValues((JSONObject) obj, localValues)) return true;
            }
            else if (obj instanceof JSONArray) {
                if (containsLocalValues((JSONArray) obj, localValues)) return true;
            }

            // ignore other data types
        }

        return false;
    }

    private static void replaceLocalValues(JSONArray jsonArray, Map<String, String> localValueToReferencedField) throws JSONException {
        if (jsonArray == null) return;

        for (int i = 0; i < jsonArray.length(); i++) {
            Object obj = jsonArray.opt(i);

            if (obj instanceof String) {
                String referencedField = localValueToReferencedField.get(obj);
                if (referencedField != null) {
                    jsonArray.put(i, referencedField);
                }
            }
            else if (obj instanceof JSONObject) {
                replaceLocalValues((JSONObject) obj, localValueToReferencedField);
            }
            else if (obj instanceof JSONArray) {
                replaceLocalValues((JSONArray) obj, localValueToReferencedField);
            }

            // ignore other data types
        }
    }

    private static boolean containsLocalValues(JSONArray jsonArray, Set<String> localValues) throws JSONException {
        if (jsonArray == null) return false;

        for (int i = 0; i < jsonArray.length(); i++) {
            Object obj = jsonArray.opt(i);

            if (obj instanceof String) {
                if (localValues.contains(obj)) return true;
            }
            else if (obj instanceof JSONObject) {
                if (containsLocalValues((JSONObject) obj, localValues)) return true;
            }
            else if (obj instanceof JSONArray) {
                if (containsLocalValues((JSONArray) obj, localValues)) return true;
            }

            // ignore other data types
        }

        return false;
    }

    private static String replaceLocalValues(String str, Map<String, String> localValueToReferencedField) {
        if (TextUtils.isEmpty(str)) return str;

        for (Map.Entry<String, String> entry : localValueToReferencedField.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }

        return str;
    }

    private static boolean containsLocalValues(String str, Set<String> localValues) {
        if (TextUtils.isEmpty(str)) return false;

        for (String entry : localValues) {
            if (str.contains(entry)) return true;
        }

        return false;
    }

    private CreateOperationHandler createHandler = new CreateOperationHandler();

    private UpdateOperationHandler updateHandler = new UpdateOperationHandler();

    private FetchOperationHandler fetchHandler = new FetchOperationHandler();

    private DeleteOperationHandler deleteHandler = new DeleteOperationHandler();

    private QueueOperationHandler getOperationHandler(QueueOperation operation) {
        switch (operation) {
            case CREATE:
                return createHandler;
            case UPDATE:
                return updateHandler;
            case DELETE:
                return deleteHandler;
            case FETCH:
                return fetchHandler;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    private static class LookupResponseHandler implements CompositeResponseHandler {

        private final String query;

        private final QueueObject queueObj;

        private final QueueUploadHelper uploadHelper;

        private LookupResponseHandler(String query, QueueObject queueObj, QueueUploadHelper uploadHelper) {
            this.query = query;
            this.queueObj = queueObj;
            this.uploadHelper = uploadHelper;
        }

        @Override
        public CompositeSubrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, CompositeSubrequest subrequest, CompositeSubresponse subresponse) throws SyncException {
            if (!subresponse.isSuccess()) {
                Log.w(TAG, "Performing id lookup with query: " + query + " was unsuccessful. " +
                        "Response: " + subresponse.asJSONArray());
            } else {
                Log.i(TAG, "Received response for Id look up. Last batch was successful.");
                JSONObject responseJson = subresponse.asJSONObject();
                int totalSize = responseJson.optInt("totalSize");

                SmartStoreDataManagerImpl dataManager = uploadHelper.getDataManager();
                Context context = uploadHelper.getContext();

                if (totalSize < 1) {
                    // There is no id for our fields so we assume that last batch request was unsuccessful.
                    Log.i(TAG, "No look up result received from server. Will retry last failed batch.");
                }
                else if (totalSize == 1) {
                    // We assume that last batch request was successful.
                    if (QueueOperation.CREATE.equals(queueObj.getOperation())) {
                        JSONArray records = responseJson.optJSONArray("records");
                        String serverId = records.optJSONObject(0).optString(SyncEngineConstants.StdFields.ID);

                        if (!TextUtils.isEmpty(serverId)) {
                            dataManager.updateWithServerId(queueObj, serverId, context);
                        }
                    }

                    uploadHelper.addObjectsToFetchDuringAutoSync(queueObj.getObjectType());
                    dataManager.deleteQueueRecords(queueObj.getSoupEntryId());
                }
                else {
                    String errorMessage = "Id lookup query: " + query + " returned too many results: " + totalSize;
                    Log.e(TAG, errorMessage);
                    //TODO: Figure out a better way to handle this scenario
                    // The below error entry is more for information purposes but is causing confusion to the user so we are
                    // not going to show the below error to the user.
                    // dataManager.insertError(new ErrorObject("",
                    //        queueObj.getObjectType(), "", "", errorMessage, null, null));
                }
            }

            return null;
        }
    }


    private static class UploadResponseHandler implements CompositeResponseHandler {

        private final QueueUploadHelper queueUploadHelper;

        private final QueueObject queueObj;

        private final QueueOperationHandler operationHandler;

        private UploadResponseHandler(QueueUploadHelper queueUploadHelper, QueueObject queueObj, QueueOperationHandler operationHandler) {
            this.queueUploadHelper = queueUploadHelper;
            this.queueObj = queueObj;
            this.operationHandler = operationHandler;
        }

        @Override
        public CompositeSubrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, CompositeSubrequest subrequest, CompositeSubresponse subresponse) throws SyncException {
            try {
                if (subresponse == null) {  // In some cases we are not sending any requests, i.e. when no field has changed.
                    operationHandler.handleResponse(queueUploadHelper, queueObj, null);
                }
                else if (subresponse.isSuccess()) {
                    operationHandler.handleResponse(queueUploadHelper, queueObj, subresponse);
                }
                else {
                    queueUploadHelper.setLastError(ServerErrorException
                            .createFrom(subresponse, queueUploadHelper.getContext()));
                    ErrorObject errorObject = createErrorObject(queueObj, subresponse);
                    operationHandler.handleServerError(queueUploadHelper, queueObj, errorObject, subresponse);
                }
            }
            catch (JSONException e) {
                Log.w(TAG, e);
                throw new SimpleSyncException(e, "Error while parsing response.");
            }
            return null;
        }

        public void handleServerError(ServerErrorException e) throws JSONException, SyncException {
            operationHandler.handleServerError(queueUploadHelper, queueObj, e);
        }
    }
}
