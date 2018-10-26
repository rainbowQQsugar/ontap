package com.abinbev.dsa.model;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AttachmentUtils;
import com.abinbev.dsa.utils.AzureUtils;
import com.abinbev.dsa.utils.FileUtils;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.BODY;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.BODY_LENGTH;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.CONTENT_TYPE;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.DESCRIPTION;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.IS_PRIVATE;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.PARENT_ID;

/**
 * Created by wandersonblough on 12/2/15.
 */
public class Attachment extends com.salesforce.dsa.data.model.Attachment {

    public static final String TAG = Attachment.class.getSimpleName();

    public static final String DUMMY_ID = "123";

    public enum ParentType {
        PARENT_ACCOUNT, PARENT_EVENT, PARENT_ASSET, PARENT_CASE, PARENT_SURVEY_QR
    }

    private String filePath;

    public Attachment() {

    }

    public Attachment(JSONObject jsonObject) {
        super(jsonObject);
    }

    public String getBody() {
        return getStringValueForKey(BODY);
    }

    public String getBodyLength() {
        return getStringValueForKey(BODY_LENGTH);
    }

    public String getContentType() {
        return getStringValueForKey(CONTENT_TYPE);
    }

    public String getDescription() {
        return getStringValueForKey(DESCRIPTION);
    }

    public boolean getIsPrivate() {
        return getBooleanValueForKey(IS_PRIVATE);
    }

    public String getParentId() {
        return getStringValueForKey(PARENT_ID);
    }

    public void invalidateFilePath() {
        filePath = null;
    }

    public String getFilePath(Context context, String parentId) {
        if (filePath == null) {
            filePath = DataManagerFactory.getDataManager().getFilePath(context, AbInBevConstants.AbInBevObjects.ATTACHMENT, getId());
            if (filePath != null) {
                filePath = new File(filePath).getAbsolutePath();
            } else {
                filePath = new File(attachmentDirectoryForAccount(parentId).getPath(), getName()).getPath();
            }
        }
        return filePath;
    }

    public String getAssetFilePath(Context context, String parentId) {
        if (filePath == null) {
            filePath = DataManagerFactory.getDataManager().getFilePath(context, AbInBevConstants.AbInBevObjects.ATTACHMENT, getId());
            if (filePath != null) {
                filePath = new File(filePath).getAbsolutePath();
            } else {
                filePath = new File(attachmentDirectoryForAssets(parentId).getPath(), getName()).getPath();
            }
        }
        return filePath;
    }

    public String getCaseFilePath(Context context, String parentId) {
        if (filePath == null) {
            filePath = DataManagerFactory.getDataManager().getFilePath(context, AbInBevConstants.AbInBevObjects.ATTACHMENT, getId());
            if (filePath != null) {
                filePath = new File(filePath).getAbsolutePath();
            } else {
                filePath = new File(attachmentDirectoryForCases(parentId).getPath(), getName()).getPath();
            }
        }
        return filePath;
    }

    public boolean isFileDownloaded(Context context, String parentId) {
        invalidateFilePath();
        String path = getFilePath(context, parentId);
        if (!TextUtils.isEmpty(path)) {
            try {
                File file = new File(path);
                return file.exists();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        return false;
    }

    public boolean isCaseFileDownloaded(Context context, String parentId) {
        invalidateFilePath();
        String path = getCaseFilePath(context, parentId);
        if (!TextUtils.isEmpty(path)) {
            try {
                File file = new File(path);
                return file.exists();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        return false;
    }

    public static Attachment getById(String id) {
        if (TextUtils.isEmpty(id)) return null;

        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(
                AbInBevConstants.AbInBevObjects.ATTACHMENT, SyncEngineConstants.StdFields.ID, id);
        return new Attachment(jsonObject);
    }

    public static List<Attachment> getAttachmentsForCase(String caseId) {
        List<Attachment> attachmentList = new ArrayList<>();

        String filter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC",
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, caseId,
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);

        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartQuery);

        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Attachment attachment = new Attachment(jsonObject);
                attachmentList.add(attachment);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error retrieving Attachments for Case with id " + caseId, e);
        }

        attachmentList.addAll(0, getCaseAttachments(caseId));

        return attachmentList;
    }

    private static List<Attachment> getCaseAttachments(String caseId) {
        ArrayList<Attachment> attachments = new ArrayList<>();

        File caseFilesDir = attachmentDirectoryForCases(caseId);
        for (File file : caseFilesDir.listFiles()) {
            Attachment attachment = createAttachmentFromFile(file, caseId, false);
            if (attachment != null) {
                attachments.add(attachment);
            }
        }

        return attachments;
    }

    public static Attachment getAccountPhotoAttachment(String accountId) {
        return getAccountAttachment(accountId, getAccountPhotoFileName());
    }

    public static Attachment getAccountLicensePhotoAttachment(String accountId) {
        return getAccountAttachment(accountId, getAccountLicensePhotoFileName());
    }

    public static Attachment getAccountAttachment(String accountId, String photoName) {
        Attachment attachment = getNewestLocalAccountPhoto(accountId, photoName);

        if (attachment == null) {
            String filter = String.format("{%s:%s} = '%s' AND {%s:%s} like '%%%s%%' ORDER BY {%s:%s} DESC limit 1",
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, accountId,
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.NAME, photoName,
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);
            String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartQuery);
            try {
                if (recordsArray.length() > 0) {
                    JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
                    attachment = new Attachment(jsonObject);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving account photo for Account with id " + accountId, e);
            }
        }

        return attachment;
    }

    private static Attachment getNewestLocalAccountPhoto(String accountId, String photoName) {
        Attachment attachment = null;
        long lastModifiedDate = 0;

        File accountFilesDir = attachmentDirectoryForAccount(accountId);
        for (File file : accountFilesDir.listFiles()) {
            if (file.getName().contains(photoName) && file.lastModified() > lastModifiedDate) {
                lastModifiedDate = file.lastModified();
                attachment = createAttachmentFromFile(file, accountId, true);
            }
        }

        return attachment;
    }

    public static List<Attachment> getAttachmentsForAccount(String accountId) {
        List<Attachment> attachmentList = new ArrayList<>();

        String filter = String.format("{%s:%s} = '%s' and NOT {%s:%s} like '%%%s%%' and NOT {%s:%s} like '%%%s%%' ORDER BY {%s:%s} DESC",
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, accountId,
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.NAME, getAccountPhotoFileName(),
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.NAME, getAccountLicensePhotoFileName(),
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);
        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartQuery);

        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Attachment attachment = new Attachment(jsonObject);
                attachmentList.add(attachment);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error retrieving Attachments for Account with id " + accountId, e);
        }

        attachmentList.addAll(0, getLocalAccountAttachments(accountId));

        return attachmentList;
    }

    private static List<Attachment> getLocalAccountAttachments(String accountId) {
        ArrayList<Attachment> attachments = new ArrayList<>();

        File accountFilesDir = attachmentDirectoryForAccount(accountId);
        for (File file : accountFilesDir.listFiles()) {
            if (!file.getName().contains(getAccountPhotoFileName()) &&
                    !file.getName().contains(getAccountLicensePhotoFileName())) {
                Attachment attachment = createAttachmentFromFile(file, accountId, false);
                if (attachment != null) {
                    attachments.add(attachment);
                }
            }
        }

        return attachments;
    }

    public static Attachment getAssetPhoto(String assetId) {
        Attachment attachment = getNewestLocalAssetPhoto(assetId);

        if (attachment == null) {
            String filter = String.format("{%s:%s} = '%s' AND {%s:%s} like '%%%s%%' ORDER BY {%s:%s} DESC limit 1",
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, assetId,
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.NAME, getAssetPhotoFileName(),
                    AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);
            String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartQuery);
            try {
                if (recordsArray.length() > 0) {
                    JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
                    attachment = new Attachment(jsonObject);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving asset photo for Asset with id " + assetId, e);
            }

        }

        return attachment;
    }

    private static Attachment getNewestLocalAssetPhoto(String assetId) {
        Attachment attachment = null;
        long lastModifiedDate = 0;
        File accountFilesDir = attachmentDirectoryForAssets(assetId);
        for (File file : accountFilesDir.listFiles()) {
            if (file.getName().contains(getAssetPhotoFileName()) && file.lastModified() > lastModifiedDate) {
                lastModifiedDate = file.lastModified();
                attachment = createAttachmentFromFile(file, assetId, true);
            }
        }

        return attachment;
    }

    public static File attachmentDirectoryForAssets(String assetId) {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + "/temp/asset/" + assetId + "/";
        return getDirectory(directoryPath);
    }

    public static File attachmentDirectoryForSurveyQR(String surveyQRId) {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + "/temp/survey_qr/" + surveyQRId + "/";
        return getDirectory(directoryPath);
    }

    public static File attachmentDirectoryForSurveyQuestion() {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + "/temp/survey_question/";
        return getDirectory(directoryPath);
    }

    public static File attachmentDirectoryForAccounts(boolean isAsset) {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + (isAsset ? "/temp/asset/" : "/temp/account/");
        return getDirectory(directoryPath);
    }

    public static File getDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static File attachmentDirectoryForAccount(String accountId) {
        File accountsDir = attachmentDirectoryForAccounts(false);
        String accountPath = accountsDir.getPath() + "/" + accountId + "/";
        return getDirectory(accountPath);
    }

    public static File attachmentDirectoryForEvents(String accountId) {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + "/temp/event/" + accountId + "/";
        return getDirectory(directoryPath);
    }

    public static File attachmentDirectoryForCases(String caseId) {
        File filesDir = ABInBevApp.getAppContext().getFilesDir();
        String directoryPath = filesDir.getPath() + "/temp/case/" + caseId + "/";
        return getDirectory(directoryPath);
    }

    public static Attachment createAttachmentFromFile(File file, String parentId, boolean includeBody) {
        JSONObject jsonObject = new JSONObject();

        try {
            if (includeBody) {
                byte[] attachmentAsBytes = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(attachmentAsBytes);
                fileInputStream.close();
                String bodyBase64 = Base64.encodeToString(attachmentAsBytes, Base64.DEFAULT);
                jsonObject.put(AbInBevConstants.AttachmentFields.BODY, bodyBase64);
            }

            Uri fileUri = Uri.fromFile(file);
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

            jsonObject.put(AbInBevConstants.AttachmentFields.CONTENT_TYPE, mimeType);
            jsonObject.put(AbInBevConstants.AttachmentFields.NAME, file.getName());
            jsonObject.put(AbInBevConstants.AttachmentFields.PARENT_ID, parentId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating attachment from file: ", e);
            return null;
        }

        return new Attachment(jsonObject);
    }

    public static boolean uploadAttachment(Attachment attachment, ParentType parentType) {
        ABInBevApp app = (ABInBevApp) ABInBevApp.getAppContext();
        ClientManager clientManager = app.createClientManager();

        RestClient client = clientManager.peekRestClient();
        String apiVersion = app.getString(R.string.api_version);

        HashMap<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(AbInBevConstants.AttachmentFields.BODY, attachment.getBody());
        fieldMap.put(AbInBevConstants.AttachmentFields.CONTENT_TYPE, attachment.getContentType());
        fieldMap.put(AbInBevConstants.AttachmentFields.NAME, attachment.getName());
        fieldMap.put(AbInBevConstants.AttachmentFields.PARENT_ID, attachment.getParentId());

        RestResponse restResponse = null;
        File file = null;
        try {
            RestRequest restRequest = RestRequest.getRequestForCreate(apiVersion, AbInBevConstants.AbInBevObjects.ATTACHMENT, fieldMap);
            restResponse = client.sendSync(restRequest);
            //update pending map with result
            if (restResponse != null) {

                if (ParentType.PARENT_ACCOUNT == parentType) {
                    file = new File(attachmentDirectoryForAccount(attachment.getParentId()), attachment.getName());
                } else if (ParentType.PARENT_ASSET == parentType) {
                    file = new File(attachmentDirectoryForAssets(attachment.getParentId()), attachment.getName());
                } else if (ParentType.PARENT_CASE == parentType) {
                    file = new File(attachmentDirectoryForCases(attachment.getParentId()), attachment.getName());
                } else if (ParentType.PARENT_SURVEY_QR == parentType) {
                    file = new File(attachmentDirectoryForSurveyQR(attachment.getParentId()), attachment.getName());
                } else if (ParentType.PARENT_EVENT == parentType) {
                    file = new File(attachmentDirectoryForEvents(attachment.getParentId()), attachment.getName());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error posting request:", e);
        }

        boolean isSuccess = restResponse != null && restResponse.isSuccess();
        if (file != null) {
            Log.i(TAG, "File updated: " + file.getPath());

            if (isSuccess) {
                String attachmentId = null;

                try {
                    SmartStoreDataManagerImpl smartStore = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
                    attachmentId = restResponse.asJSONObject().getString("id");

                    String soqlFilter = String.format("%s='%s'", SyncEngineConstants.StdFields.ID, attachmentId);
                    smartStore.fetchFromServer(app, AbInBevConstants.AbInBevObjects.ATTACHMENT, soqlFilter);

                    // Move from temp file to target.
                    File newFile = new File(app.getFilesDir(), AttachmentUtils.getAttachmentFileName(app, attachmentId));
                    FileUtils.moveFile(file, newFile);

                } catch (JSONException | IOException | SyncException e) {
                    Log.e(TAG, "Error posting request: ", e);
                    if (!TextUtils.isEmpty(attachmentId)) {
                        PendingSyncAttachments.updateAttachment(file.getPath(), attachmentId);
                    }
                }
            }
        }

        Log.i(TAG, "Attachment Post result isSuccess: " + isSuccess);
        return isSuccess;
    }

    public static void deleteAttachment(String attachmentId) {
        DataManagerFactory.getDataManager().deleteRecord(AbInBevConstants.AbInBevObjects.ATTACHMENT, attachmentId);
    }

    public static void deleteLocalSurveyPhotos(String path) {
        //if we have attachmentId, still need to delete from server
        String attachmentId = PendingSyncAttachments.getAttachemntId(path);
        if (attachmentId != null) {
            Log.e("Babu", "attachment Id: " + attachmentId);
            deleteFromServer(attachmentId);
        }

        //update pending map
        PendingSyncAttachments.removeAttachment(path);
        File file = new File(path);
        if (file.exists()) file.delete();

    }

    public static void deleteUnsyncedAttachment(String accountId, String attachmentName) {
        File file = new File(attachmentDirectoryForAccount(accountId).getPath(), attachmentName);
        String path = file.getPath();

        //if we have attachmentId, still need to delete from server
        String attachmentId = PendingSyncAttachments.getAttachemntId(path);
        if (attachmentId != null) {
            deleteFromServer(attachmentId);
        }

        //update pending map
        PendingSyncAttachments.removeAttachment(path);
        file.delete();
    }

    public static void deleteUnsyncedCaseAttachment(String attachmentName, String casoId) {
        //delete file
        String pathOfDeletedFile = deleteCaseAttachmentTempFile(attachmentName, casoId);

        //if we have account id, still need to delete from server
        String attachmentId = PendingSyncAttachments.getAttachemntId(pathOfDeletedFile);
        if (attachmentId != null) {
            deleteFromServer(attachmentId);
        }

        //update pending map
        PendingSyncAttachments.removeAttachment(pathOfDeletedFile);
    }

    private static void deleteFromServer(String attachmentId) {
        if (DUMMY_ID.equals(attachmentId)) {
            //TODO: this is Azure Attachment we need to delete from Azure if we get here
            return;
        }
        ClientManager clientManager = new ClientManager(ABInBevApp.getAppContext(), SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

        RestClient client = clientManager.peekRestClient();
        String apiVersion = ABInBevApp.getAppContext().getString(R.string.api_version);

        try {
            RestRequest restRequest = RestRequest.getRequestForDelete(apiVersion, AbInBevConstants.AbInBevObjects.ATTACHMENT, attachmentId);
            RestResponse restResponse = client.sendSync(restRequest);
            Log.i(TAG, "Attachment Deleted from server isSuccess: " + (restResponse != null && restResponse.isSuccess()));
        } catch (IOException e) {
            Log.e(TAG, "Error deleting attachment from server, id: " + attachmentId, e);
        }
    }

    public static String deleteCaseAttachmentTempFile(String attachmentName, String casoId) {
        File directory = Attachment.attachmentDirectoryForCases(casoId);
        File attachmentFile = new File(directory, attachmentName);
        boolean isDeleted = attachmentFile.delete();
        Log.i(TAG, "Attachment File Deleted: " + attachmentName + " " + isDeleted);
        return attachmentFile.getPath();
    }

    public static String getAccountPhotoFileName() {
        return "AccountPhoto";
    }

    public static String getAccountLicensePhotoFileName() {
        return "AccountLicensePhoto";
    }

    public static String getAccountPhotoFileType() {
        return ".jpg";
    }

    public static String getCheckInPhotoFileName() {
        return "CheckInPhoto";
    }

    public static String getCheckOutPhotoFileName() {
        return "CheckOutPhoto";
    }

    public static String getAssetPhotoFileName() {
        return "AssetPhoto";
    }

    public static JSONObject getFilesForSurveyQuestionResponse(String SurveyQuestionResponseId) {

        JSONObject mainJsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Set<String> fileNames = new HashSet<>();

        List<JSONObject> files = AzureUtils.getAzureFilesForSurveyQuestionResponse(SurveyQuestionResponseId);
        for (JSONObject fileJson : files) {
            String name = fileJson.optString("name");
            if (!fileNames.contains(name)) {
                fileNames.add(name);
                jsonArray.put(fileJson);
            }
        }

        files = getAttachmentFilesForSurveyQuestionResponse(SurveyQuestionResponseId);
        for (JSONObject fileJson : files) {
            String name = fileJson.optString("name");
            if (!fileNames.contains(name)) {
                fileNames.add(name);
                jsonArray.put(fileJson);
            }
        }

        files = getQrFilesForSurveyQuestionResponse(SurveyQuestionResponseId);
        for (JSONObject fileJson : files) {
            String name = fileJson.optString("name");
            if (!fileNames.contains(name)) {
                fileNames.add(name);
                jsonArray.put(fileJson);
            }
        }

        try {
            mainJsonObject.put("objectArray", jsonArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error retrieving Attachments for SurveyQuestionResponse with id " + SurveyQuestionResponseId, e);
        }

        return mainJsonObject;
    }

    private static List<JSONObject> getAttachmentFilesForSurveyQuestionResponse(String SurveyQuestionResponseId) {
        String filter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s}",
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, SurveyQuestionResponseId,
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);

        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONArray recordsArray = dataManager.fetchAllSmartSQLQuery(smartQuery);

        List<JSONObject> result = new ArrayList<>();

        for (int i = 0; i < recordsArray.length(); i++) {

            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Attachment attachment = new Attachment(jsonObject);
                String filePath = dataManager.getFilePath(ABInBevApp.getAppContext(), AbInBevConstants.AbInBevObjects.ATTACHMENT, attachment.getId());
                if (filePath != null) {
                    JSONObject data = new JSONObject();
                    data.put("name", attachment.getName());
                    Log.e("Babu", "filePath: " + filePath);
                    filePath = new File(filePath).getAbsolutePath();
                    Log.e("Babu", "Absolute filePath: " + filePath);
                    data.put("path", filePath);
                    result.add(data);
                } else {
                    // filePath = new File(attachmentDirectoryForAssets(parentId).getPath(), getName()).getPath();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving Attachments for SurveyQuestionResponse with id " + SurveyQuestionResponseId, e);
            }
        }

        return result;
    }

    private static List<JSONObject> getQrFilesForSurveyQuestionResponse(String SurveyQuestionResponseId) {
        List<JSONObject> result = new ArrayList<>();
        File surveysDirectory = attachmentDirectoryForSurveyQR(SurveyQuestionResponseId);
        for (File file : surveysDirectory.listFiles()) {

            try {
                JSONObject data = new JSONObject();
                data.put("name", file.getName());
                String filePath = file.getAbsolutePath();
                Log.e("Babu", "Absolute filePath: " + filePath);
                data.put("path", filePath);
                result.add(data);
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving Attachments for SurveyQuestionResponse with id " + SurveyQuestionResponseId, e);
            }
        }

        return result;
    }

    private static List<JSONObject> getFilesForSurveyQuestion(String surveyQuestionId) {
        List<JSONObject> result = new ArrayList<>();
        File surveysDirectory = new File(attachmentDirectoryForSurveyQuestion(), surveyQuestionId);

        File[] files = surveysDirectory.listFiles();
        if (files != null) {
            for (File file : files) {

                try {
                    JSONObject data = new JSONObject();
                    data.put("name", file.getName());
                    String filePath = file.getAbsolutePath();
                    Log.e("Babu", "Absolute filePath: " + filePath);
                    data.put("path", filePath);
                    result.add(data);
                } catch (JSONException e) {
                    Log.e(TAG, "Error retrieving Attachments for SurveyQuestionResponse with id " + surveyQuestionId, e);
                }
            }
        }

        return result;
    }

//    private static List<JSONObject> getAzureFilesForSurveyQuestionResponse(String SurveyQuestionResponseId) {
//
//        List<JSONObject> result = new ArrayList<>();
//
//        String filter = String.format("{%s:%s} = '%s'",
//                AbInBevConstants.AbInBevObjects.AZURE_CONTENT, "ParentId__c", SurveyQuestionResponseId);
//
//        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.AZURE_CONTENT, filter);
//
//        DataManager dataManager = DataManagerFactory.getDataManager();
//        JSONArray recordsArray = dataManager.fetchAllSmartSQLQuery(smartQuery);
//
//        for (int i = 0; i < recordsArray.length(); i++) {
//            try {
//                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
//                AzureContent__c attachment = new AzureContent__c(jsonObject);
//                String filePath = dataManager.getFilePath(ABInBevApp.getAppContext(), AbInBevConstants.AbInBevObjects.AZURE_CONTENT, attachment.getId());
//                if (filePath != null) {
//                    JSONObject data = new JSONObject();
//                    data.put("name", attachment.getName());
//                    Log.e("Babu", "filePath: " + filePath);
//                    filePath = new File(filePath).getAbsolutePath();
//                    Log.e("Babu", "Absolute filePath: " + filePath);
//                    data.put("path", filePath);
//                    result.add(data);
//                }
//            } catch (JSONException e) {
//                Log.e(TAG, "Error retrieving Attachments for SurveyQuestionResponse with id " + SurveyQuestionResponseId, e);
//            }
//        }
//
//        return result;
//    }

    public static JSONObject getAttachmentsForSurveyQuestion(String SurveyQuestionId) {

        JSONObject mainJsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        String filter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC",
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.PARENT_ID, SurveyQuestionId,
                AbInBevConstants.AbInBevObjects.ATTACHMENT, AbInBevConstants.AttachmentFields.LAST_MODIFIED_DATE);

        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ATTACHMENT, filter);

        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONArray recordsArray = dataManager.fetchAllSmartSQLQuery(smartQuery);

        // this is used to avoid duplicates
        List<String> attachmentNames = new ArrayList<>();

        for (int i = 0; i < recordsArray.length(); i++) {

            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Attachment attachment = new Attachment(jsonObject);
                String filePath = dataManager.getFilePath(ABInBevApp.getAppContext(), AbInBevConstants.AbInBevObjects.ATTACHMENT, attachment.getId());
                if (filePath != null) {
                    if (!attachmentNames.contains(attachment.getName())) {
                        attachmentNames.add(attachment.getName());
                        JSONObject data = new JSONObject();
                        data.put("name", attachment.getName());
                        Log.e("Babu", "filePath: " + filePath);
                        filePath = new File(filePath).getAbsolutePath();
                        Log.e("Babu", "Absolute filePath: " + filePath);
                        data.put("path", filePath);
                        jsonArray.put(data);
                    }
                } else {
                    // filePath = new File(attachmentDirectoryForAssets(parentId).getPath(), getName()).getPath();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving Attachments for SurveyQuestion with id " + SurveyQuestionId, e);
            }
        }

        List<JSONObject> files = getFilesForSurveyQuestion(SurveyQuestionId);
        for (JSONObject fileJson : files) {
            String name = fileJson.optString("name");
            if (!attachmentNames.contains(name)) {
                attachmentNames.add(name);
                jsonArray.put(fileJson);
            }
        }


        try {
            mainJsonObject.put("objectArray", jsonArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error retrieving Attachments for SurveyQuestion with id " + SurveyQuestionId, e);
        }

        return mainJsonObject;
    }
}
