package com.abinbev.dsa.sync;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.PendingSyncAttachments;
import com.abinbev.dsa.model.SensitiveData__c;
import com.abinbev.dsa.model.Survey_Question__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AttachmentFields;
import com.abinbev.dsa.utils.AbInBevConstants.CaseFields;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyQuestionResponseFields;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyTakerFields;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.abinbev.dsa.utils.AzureUtils;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.table.CloudTable;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.DynamicFetchPreferences;
import com.salesforce.androidsyncengine.datamanager.SFSyncHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.thread.ThreadTools;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineTimingLogger;
import com.salesforce.dsa.utils.CategoryUtils;
import com.salesforce.dsa.utils.DSAConstants;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import okhttp3.OkHttpClient;
import okio.BufferedSink;
import okio.Okio;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_AND_SAVE_THE_MANIFEST_FILE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FETCH_AND_SAVE_THE_USER_PROFILE;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_FLUSH_ATTACHMENTS;
import static com.salesforce.androidsyncengine.utils.SyncProcedure.P_GOING_TO_PERFORM_POST_ACTIONS_FOR_DELTA_SYNC;

/**
 * Created by bduggirala on 1/2/16.
 */
public class CustomSyncHelper extends SFSyncHelper {

    private static final String TAG = CustomSyncHelper.class.getSimpleName();

    private static final String FIELD_FETCH_SQL_FORMAT = "SELECT {%1$s:%2$s} FROM {%1$s}";

    public CustomSyncHelper() {

    }

    @Override
    public void preSync(Context context, RestClient client) throws Exception {
        Log.i(TAG, "in preSync");
        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_AND_SAVE_THE_USER_PROFILE);
        fetchAndSaveUserProfile(context, client);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_AND_SAVE_THE_USER_PROFILE);

        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FETCH_AND_SAVE_THE_MANIFEST_FILE);
        fetchAndSaveSyncManifest(context, client);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FETCH_AND_SAVE_THE_MANIFEST_FILE);
    }

    @Override
    public void postDataSync(Context context, RestClient client) {
        Log.i(TAG, "in postDataSync");
        //        boolean deltaSync = PreferenceUtils.getTriggerDeltaSync(context);
//        if (deltaSync) {
//
//            SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_PERFORM_POST_ACTIONS_FOR_DELTA_SYNC);
//            performPostActionForDeltaSync(context);
//            SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_PERFORM_POST_ACTIONS_FOR_DELTA_SYNC);
//
//        }
        SyncEngineTimingLogger.getLogger().startSplit(P_GOING_TO_FLUSH_ATTACHMENTS);
        PendingSyncAttachments.flushAttachments(context);
        SyncEngineTimingLogger.getLogger().endSplit(P_GOING_TO_FLUSH_ATTACHMENTS);
    }

    private void performPostActionForDeltaSync(Context context) {
        String activeConfigId = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(DSAConstants.Constants.ACTIVE_CONFIG_ID, null);
        CategoryUtils.populateCategoryCacheHolder(context, activeConfigId);

        DynamicFetchPreferences.getInstance(context).deleteOldEntries();


        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();

        int deleted = dataManager.deleteOldestTempIds();
        Log.i(TAG, "Deleted temp ids: " + deleted);

        List<SensitiveData__c> sensitiveData = SensitiveData__c.getAll();
        ListIterator<SensitiveData__c> listIterator = sensitiveData.listIterator();
        SensitiveData__c accountSensitiveData = null;

        while (listIterator.hasNext()) {
            SensitiveData__c currentItem = listIterator.next();
            String objectName = currentItem.getObjectName();

            if ("Account".equals(objectName)) {

                accountSensitiveData = currentItem;
                listIterator.remove();

            } else if ("Event".equals(objectName)) {

                listIterator.remove();

            } else if (objectName.endsWith("SurveyTaker__c")) {
                listIterator.remove();

            }
        }


        AzureUtils.fetchSensitiveData(sensitiveData);

        if (accountSensitiveData == null) {
            return;
        }
        HashSet<String> accountIds = new HashSet<>();
        //?--- fix bug start--@{
        //bug describe:
        //  in Visit list->add one out plan visit->search and add one account---=new one do not have marker in map
        // because no latitude and longitude in account
        // so we need get latitude and longitude for account here
        //--code before--
        //List<Event> events = Event.getVisits();
        //for (Event event : events) {
        //    accountIds.add(event.getWhatId());
        //}
        //--code after--
        List<Account> accounts = Account.getAccounts();
        for (Account account : accounts) {
            accountIds.add(account.getId());
        }
        //?--- fix bug end--@}

        AzureUtils.fetchObjectSensitiveData(accountSensitiveData, new ArrayList<>(accountIds));
    }

    @Override
    public void postContentSync(Context context) {
        Log.i(TAG, "in postContentSync");
        // this seems to be getting called even during auto sync
        // which causes the images to disappear until a delta sync actually happens
        boolean deltaSync = PreferenceUtils.getTriggerDeltaSync(context);
        if (deltaSync) {
            PendingSyncAttachments.cleanAttachments();
        }
    }

    @Override
    public void postOptionalContentSync(Context context) {
        super.postOptionalContentSync(context);
        Log.i(TAG, "in postOptionalContentSync");

        //Get sensitive data associated with Accounts in visit list
        HashSet<String> accountIds = new HashSet<>();
        List<Event> events = Event.getVisits();

        for (Event event : events) {
            accountIds.add(event.getWhatId());
        }

        Log.e(TAG, "account list: " + accountIds);

        // Fetching table storage content
//        AzureUtils.fetchAccountRelatedContent(accountIds);

        List<String> questionIds = new ArrayList<>();
        for (Survey_Question__c surveyQuestion : Survey_Question__c.getAll()) {
            questionIds.add(surveyQuestion.getId());
        }

        AzureUtils.fetchSurveyQuestionRelatedContent(questionIds);
    }


    private boolean isSensitiveDataInGroup = false;
    private boolean isSensitiveDataGot = false;
    private List<SensitiveData__c> sensitiveData;
    private HashMap<String, SensitiveData__c> mNormalSensitiveData = new HashMap<>();
    private final static String EXCLUDE_OBJECT_EVENT = "Event";
    @Override
    public void doBeforeGroupFetch(Context context, List<String> objectGroup, boolean isAutoSync) {
        Log.i(TAG, "in doBeforeGroupFetch for: " + objectGroup);
        if (isAutoSync) {
            sensitiveData = null;
            mNormalSensitiveData.clear();
            return;
        }

        if (objectGroup.contains(AbInBevObjects.SENSITIVE_DATA)) {
            isSensitiveDataInGroup = true;
        }
    }

    @Override
    public void doAfterGroupFetch(Context context, List<String> objectGroup, boolean isAutoSync) {
        Log.i(TAG, "in doAfterGroupFetch for: " + objectGroup);

        if (isAutoSync) {
            return;
        }

        if (isSensitiveDataInGroup && (!isSensitiveDataGot)) {
            sensitiveData = SensitiveData__c.getAll();
            ListIterator<SensitiveData__c> listIterator = sensitiveData.listIterator();

            while (listIterator.hasNext()) {
                SensitiveData__c currentItem = listIterator.next();
                String objectName = currentItem.getObjectName();

                if (EXCLUDE_OBJECT_EVENT.equals(objectName)) {
                    continue;
                }

                if (objectName.endsWith(AbInBevObjects.SURVEY_TAKER)) {
                    continue;
                }

                mNormalSensitiveData.put(objectName, currentItem);
            }

            isSensitiveDataGot = true;
        }

        if (!isSensitiveDataGot) {
            return;
        }

        if (mNormalSensitiveData.isEmpty()) {
            return;
        }

        List<SensitiveData__c> temp = new ArrayList<>();
        for (String objectName : mNormalSensitiveData.keySet()) {
            if (!objectGroup.contains(objectName)) {
                continue;
            }

            temp.add(mNormalSensitiveData.get(objectName));
        }

        if (temp.isEmpty()) {
            return;
        }

        ThreadTools.getThreadTools().postFetchSensitiveDataTask(new Runnable() {
            @Override
            public void run() {
                AzureUtils.fetchSensitiveData(temp);
            }
        });


    }


    @Override
    public CloudBlobContainer getAzureContainer() {
        return AzureUtils.getAzureBlobContainer();
    }

    @Override
    public CloudTable getAzureCloudTable() {
        return AzureUtils.getAzureCloudTable();
    }

    @Override
    public List<String> getDynamicContentFetchQueries(String accountId) {
        List<String> queries = new ArrayList<>();
        List<String> orderedObjectsSet = PreferenceUtils.getSortedObjects(ABInBevApp.getAppContext());

        // Take newest account photo
        String query = String.format(FIELD_FETCH_SQL_FORMAT, AbInBevObjects.ATTACHMENT, "_soup");
        query = query.concat(String.format(" WHERE {%s:%s} = '%s' AND {%s:%s} LIKE '%%%s%%' ORDER BY {%s:%s} LIMIT 1",
                AbInBevObjects.ATTACHMENT, AttachmentFields.PARENT_ID, accountId,
                AbInBevObjects.ATTACHMENT, AttachmentFields.NAME, Attachment.getAccountPhotoFileName(),
                AbInBevObjects.ATTACHMENT, AttachmentFields.LAST_MODIFIED_DATE));
        queries.add(query);

        // Take up to 7 attachments
        query = String.format(FIELD_FETCH_SQL_FORMAT, AbInBevObjects.ATTACHMENT, "_soup");
        query = query.concat(String.format(" WHERE {%s:%s} = '%s' AND (NOT {%s:%s} LIKE '%%%s%%') AND (NOT {%s:%s} LIKE '%%%s%%') ORDER BY {%s:%s} LIMIT 7",
                AbInBevObjects.ATTACHMENT, AttachmentFields.PARENT_ID, accountId,
                AbInBevObjects.ATTACHMENT, AttachmentFields.NAME, Attachment.getAccountPhotoFileName(),
                AbInBevObjects.ATTACHMENT, AttachmentFields.NAME, Attachment.getAssetPhotoFileName(),
                AbInBevObjects.ATTACHMENT, AttachmentFields.LAST_MODIFIED_DATE));
        queries.add(query);

        // Take all assets
        query = String.format(FIELD_FETCH_SQL_FORMAT, AbInBevObjects.ATTACHMENT, "_soup");
        query = query.concat(String.format(" WHERE {%s:%s} = '%s' AND {%s:%s} LIKE '%%%s%%'",
                AbInBevObjects.ATTACHMENT, AttachmentFields.PARENT_ID, accountId,
                AbInBevObjects.ATTACHMENT, AttachmentFields.NAME, Attachment.getAssetPhotoFileName()));
        queries.add(query);

        String smartSqlFilter;
        // Take all case attachments
        if (orderedObjectsSet.contains(AbInBevObjects.CASE)) {
            smartSqlFilter = String.format("{%s:%s}='%s'", AbInBevObjects.CASE, CaseFields.ACCOUNT_ID, accountId);
            query = String.format("SELECT {%1$s:_soup} FROM {%1$s} "
                            + "LEFT JOIN {%2$s} ON {%1$s:%3$s} = {%2$s:%4$s} WHERE %5$s",
                    AbInBevObjects.ATTACHMENT,
                    AbInBevObjects.CASE, AttachmentFields.PARENT_ID, "Id", smartSqlFilter);
            queries.add(query);
        } else {
            Log.i(TAG, "not fetching case attachments!");
        }

        // Take all survey attachments
        if (orderedObjectsSet.contains(AbInBevObjects.SURVEY_Question_Response)) {
            smartSqlFilter = String.format("{%s:%s}='%s'", AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.ACCOUNT_ID, accountId);
            String selectSurveyQuestions = String.format("SELECT {%1$s:Id} FROM {%1$s} "
                            + "LEFT JOIN {%2$s} ON {%1$s:%3$s} = {%2$s:%4$s} WHERE %5$s",
                    AbInBevObjects.SURVEY_Question_Response,
                    AbInBevObjects.SURVEY_TAKER, SurveyQuestionResponseFields.SURVEY_TAKER, "Id", smartSqlFilter);

            query = String.format("SELECT {%1$s:_soup} FROM {%1$s} " +
                            "INNER JOIN (%2$s) ON {%1$s:%3$s} = {%4$s:%5$s}",
                    AbInBevObjects.ATTACHMENT, selectSurveyQuestions, AttachmentFields.PARENT_ID, AbInBevObjects.SURVEY_Question_Response, "Id");
            queries.add(query);
        } else {
            Log.i(TAG, "not fetching survey attachments!");
        }

        return queries;
    }

    @Override
    public void postDynamicFetch(String fetchName, Map<String, String> params, Set<String> fetchedObjects) {
        super.postDynamicFetch(fetchName, params, fetchedObjects);

        if (TextUtils.isEmpty(fetchName) || params == null || fetchedObjects.isEmpty()) {
            return;
        }

//        if (DynamicFetch.ACCOUNT_CHECKED_IN.equals(fetchName)) {
//            String accountId = params.get("accountId");
//
//            if (!TextUtils.isEmpty(accountId)) {
//                AzureUtils.fetchAccountRelatedContent(accountId);
//            }
//        }
    }

    private void fetchAndSaveUserProfile(Context context, RestClient client) throws Exception {

        String apiVersion = context.getString(com.salesforce.androidsyncengine.R.string.api_version);
        RestResponse result = null;
        String userProfileFieldName;
        // we have to use some object to see if we should use namespace so ...
        userProfileFieldName = "ONTAP__User_Profile__c";
        // comment above line for and uncomment below line for test and non-namespaced environments
        // userProfileFieldName = "User_Profile__c";

        String query = "SELECT " + userProfileFieldName + " FROM User where Id = '" + client.getClientInfo().userId + "'";
        // String query = "SELECT Country_Alias__c, User_Profile__c, LanguageLocaleKey FROM User where Id = '" + client.getClientInfo().userId + "'";
        try {
            final RestRequest restRequest = RestRequest.getRequestForQuery(apiVersion, query);
            result = client.sendSync(restRequest);
            if (result.isSuccess()) {
                final JSONArray records = result.asJSONObject()
                        .getJSONArray("records");
                JSONObject jsonObject = records.getJSONObject(0);
                //String countryAlias = jsonObject.getString("Country_Alias__c");
                String userProfile = jsonObject.getString(userProfileFieldName);
                Log.i(TAG, "user profile: " + userProfile);
                //String languageLocale = jsonObject.getString("LanguageLocaleKey");
                //AppPreferenceUtils.putCountryAlias(context, countryAlias);
                AppPreferenceUtils.putUserProfile(context, userProfile);
                //AppPreferenceUtils.putLanguageLocale(context, languageLocale);

            } else {
                Log.e(TAG, "error: " + result.toString());
                throw (new Exception("in CustomSyncHelper: " + result.toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "in customSyncHelper :" + e.getMessage());
            // any auth errors and exceptions could be thrown here during full sync
            throw (new SimpleSyncException(e, "in CustomSyncHelper: " + e.getMessage()));
        }
    }

    private void fetchAndSaveSyncManifest(Context context, RestClient client) throws Exception {
        String profileName = AppPreferenceUtils.getUserProfile(context);

        String apiVersion = context.getString(com.salesforce.androidsyncengine.R.string.api_version);
        RestResponse result = null;

        try {

            // delete old file
            File downloadedSyncFile = new File(context.getFilesDir(), ManifestUtils.DOWNLOADED_SYNC_FILE);

            if (downloadedSyncFile.exists()) {
                Log.i(TAG, "deleting old sync file!");
                downloadedSyncFile.delete();
            }

            String query = String.format("SELECT Name, Body FROM Attachment where ParentId IN (Select Id from ONTAP__OnTap_Permission__c where ONTAP__Profile_Name__c = '%s') order by lastmodifieddate desc",
                    profileName);
            Log.i(TAG, "query: " + query);
            final RestRequest restRequest = RestRequest.getRequestForQuery(apiVersion, query);
            result = client.sendSync(restRequest);
            if (result.isSuccess()) {
                final JSONArray records = result.asJSONObject()
                        .getJSONArray("records");
                if (records.length() == 0) {
                    Log.i(TAG, "no sync manifest file available for profile");
                } else {
                    JSONObject jsonObject = records.getJSONObject(0);
                    String name = jsonObject.getString("Name");
                    String body = jsonObject.getString("Body");
                    Log.i(TAG, "data: " + name + ": " + body);
                    downloadSyncFile(context, client, body);
                }
            } else {
                Log.i(TAG, "error getting sync manifest");
                throw ServerErrorException.createFrom(result, context);
            }

        } catch (Exception e) {
            Log.e(TAG, "in customSyncHelper :", e);
            throw new SimpleSyncException(e, e.getMessage());
        }

    }

    private void downloadSyncFile(Context context, RestClient client, String body) throws Exception {
        try {
            URI completeURI = client.getClientInfo().resolveUrl(body);
            URL syncManifestUrl = completeURI.toURL();

            File downloadedSyncFile = new File(context.getFilesDir(), ManifestUtils.DOWNLOADED_SYNC_FILE);

            ABInBevApp appContext = (ABInBevApp) ABInBevApp.getAppContext();
            ClientManager clientManager = appContext.createClientManager();
            OkHttpClient okHttpClient = appContext.createOkHttpClient(clientManager);

            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                    .url(syncManifestUrl)
                    .get()
                    .build();
            okhttp3.Response response = okHttpClient.newCall(httpRequest).execute();

            // Throw exception if result was unsuccessful.
            if (response.code() / 100 != 2) {
                throw new IOException("Incorrect response code while downloading sync manifest: " + response.code());
            }

            // Save download to file
            BufferedSink sink = Okio.buffer(Okio.sink(downloadedSyncFile));
            sink.writeAll(response.body().source());
            sink.close();
            response.close();

            Log.i(TAG, "successfully downloaded sync file!");

        } catch (Exception e) {
            throw (new SimpleSyncException(e, "in CustomSyncHelper.downloadSyncFile: " + e.getMessage()));
        }
    }
}
