package com.abinbev.dsa.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.bus.event.AttachmentEvent;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.PendingSyncAttachments;
import com.abinbev.dsa.model.Picture_Audit_Status__c;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AzureUtils;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.crashreport.CrashReportManagerProvider;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.dsa.BuildConfig;
import com.squareup.otto.Bus;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.abinbev.dsa.utils.AttachmentUtils.compressImage;


public class AttachmentUploadService extends IntentService {

    public static final String TAG = AttachmentUploadService.class.getSimpleName();

    public static final String EXTRA_PARENT_ID = "Parent_Id";

    public static final String ACTION_CASE_ATTACHMENT_UPLOAD = "Case_attachment_action";
    public static final String ACTION_ACCOUNT_ATTACHMENT_UPLOAD = "Account_attachment_action";
    public static final String ACTION_ACCOUNT_PHOTO_UPLOAD = "Account_photo_action";
    public static final String ACTION_ACCOUNT_LICENSE_PHOTO_UPLOAD = "Account_license_photo_action";
    public static final String ACTION_CHECK_IN_PHOTO_UPLOAD = "Check_in_photo_action";
    public static final String ACTION_CHECK_OUT_PHOTO_UPLOAD = "Check_out_photo_action";
    public static final String ACTION_ASSET_PHOTO_UPLOAD = "Asset_photo_action";
    public static final String ACTION_SURVEY_PHOTO_UPLOAD = "Asset_survey_action";

    public static final String ACTION_ACCOUNT_FLUSH = "Account_flush";
    public static final String ACTION_ASSET_FLUSH = "Asset_flush";
    public static final String ACTION_CASE_FLUSH = "Case_flush";
    public static final String ACTION_SURVEY_FLUSH = "Survey_flush";
    public static final String ACTION_EVENT_FLUSH = "Event_flush";

    @Inject
    Bus eventBus;

    private Handler mHandler;
    private SmartStoreDataManagerImpl mDataManager;
    private CloudBlobContainer mAzureContainer;

    public AttachmentUploadService() {
        super("AttachmentUploadService");
        ((ABInBevApp) ABInBevApp.getAppContext()).getAppComponent().inject(this);
        mDataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();
        mAzureContainer = AzureUtils.getAzureBlobContainer();
        eventBus.register(this);
    }

    public static Intent uploadCaseAttachment(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_CASE_ATTACHMENT_UPLOAD);
    }

    public static Intent uploadAccountAttachment(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ACCOUNT_ATTACHMENT_UPLOAD);
    }

    public static Intent uploadAccountPhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ACCOUNT_PHOTO_UPLOAD);
    }

    public static Intent uploadLicensePhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ACCOUNT_LICENSE_PHOTO_UPLOAD);
    }

    public static Intent uploadCheckInPhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_CHECK_IN_PHOTO_UPLOAD);
    }

    public static Intent uploadCheckOutPhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_CHECK_OUT_PHOTO_UPLOAD);
    }

    public static Intent uploadAssetPhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ASSET_PHOTO_UPLOAD);
    }

    public static Intent uploadSurveyPhoto(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_SURVEY_PHOTO_UPLOAD);
    }

    public static Intent flushAccount(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ACCOUNT_FLUSH);
    }

    public static Intent flushAsset(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_ASSET_FLUSH);
    }

    public static Intent flushCase(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_CASE_FLUSH);
    }

    public static Intent flushSurvey(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_SURVEY_FLUSH);
    }

    public static Intent flushEvent(Context context, Uri uri, String parentId) {
        return createIntent(context, uri, parentId, ACTION_EVENT_FLUSH);
    }

    private static Intent createIntent(Context context, Uri uri, String parentId, String action) {
        Intent intent = new Intent(context, AttachmentUploadService.class);
        intent.setAction(action);
        intent.setData(uri);
        intent.putExtra(EXTRA_PARENT_ID, parentId);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //saving attachment and uploading
        Uri uri = intent.getData();
        String parentId = intent.getStringExtra(EXTRA_PARENT_ID);
        File file;
        String type;
        String extension;
        String eventId;

        // this should not happen but seems to happen in some cases ...
        // possible due to the way a third party app might be return the photo
        if (uri == null)  return;

        switch (intent.getAction()) {
            case ACTION_CASE_ATTACHMENT_UPLOAD:
                file = saveFile(uri, Attachment.attachmentDirectoryForCases(parentId));
                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_CASE);
                    // only start upload if we have an actual id for this parent
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_CASE);
                    }
                }
                break;
            case ACTION_ACCOUNT_ATTACHMENT_UPLOAD:
                file = saveFile(uri, Attachment.attachmentDirectoryForAccount(parentId));
                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_ACCOUNT);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_ACCOUNT);
                    }
                }
                break;
            case ACTION_ASSET_PHOTO_UPLOAD:
                type =  getContentResolver().getType(uri);
                extension = type != null ? type.substring(type.lastIndexOf("/") + 1, type.length()) : "jpg";
                file = saveFile(uri, Attachment.attachmentDirectoryForAssets(parentId), Attachment.getAssetPhotoFileName() + "." + extension);

                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_ASSET);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_ASSET);
                    }
                }
                break;
            case ACTION_ACCOUNT_PHOTO_UPLOAD:
                extension = Attachment.getAccountPhotoFileType();
                file = saveFile(uri, Attachment.attachmentDirectoryForAccount(parentId), Attachment.getAccountPhotoFileName() + extension);

                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_ACCOUNT);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_ACCOUNT);
                    }
                }
                break;
            case ACTION_ACCOUNT_LICENSE_PHOTO_UPLOAD:
                extension = Attachment.getAccountPhotoFileType();
                file = saveFile(uri, Attachment.attachmentDirectoryForAccount(parentId), Attachment.getAccountLicensePhotoFileName() + extension);

                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_ACCOUNT);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_ACCOUNT);
                    }
                }
                break;
            case ACTION_CHECK_IN_PHOTO_UPLOAD:
                type =  getContentResolver().getType(uri);
                extension = type != null ? type.substring(type.lastIndexOf("/") + 1, type.length()) : "jpg";
                file = saveFile(uri, Attachment.attachmentDirectoryForEvents(parentId), Attachment.getCheckInPhotoFileName() + "." + extension);

                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_EVENT);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_EVENT);
                    }
                }
                break;
            case ACTION_CHECK_OUT_PHOTO_UPLOAD:
                type =  getContentResolver().getType(uri);
                extension = type != null ? type.substring(type.lastIndexOf("/") + 1, type.length()) : "jpg";
                file = saveFile(uri, Attachment.attachmentDirectoryForEvents(parentId), Attachment.getCheckOutPhotoFileName() + "." + extension);

                if (file != null) {
                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_EVENT);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_EVENT);
                    }
                }
                break;
            case ACTION_SURVEY_PHOTO_UPLOAD:
                String fileName = uri.getLastPathSegment();
                Log.e(TAG, "fileName in surveyPhotoUpload: " + fileName);
                file = saveFile(uri, Attachment.attachmentDirectoryForSurveyQR(parentId), fileName);

                if (file != null) {
                    Log.e(TAG, "deleting file: " + uri.getPath());
                    ContentUtils.deleteContent(this, uri);

                    Uri thumbnailUri = getThumbnailUri(uri);
                    Log.e(TAG, "deleting thumbnail: " + thumbnailUri.getPath());
                    ContentUtils.deleteContent(this, thumbnailUri);

                    eventId = getCurrentEventId();
                    createPictureAuditStatus(file, parentId, eventId, Attachment.ParentType.PARENT_SURVEY_QR);
                    if (!mDataManager.isClientId(parentId)) {
                        uploadFile(file, parentId, Attachment.ParentType.PARENT_SURVEY_QR);
                    }
                }
                break;
            case ACTION_ACCOUNT_FLUSH:
                if (!mDataManager.isClientId(parentId)) {
                    uploadFile(new File(uri.getPath()), parentId, Attachment.ParentType.PARENT_ACCOUNT);
                }
                break;
            case ACTION_ASSET_FLUSH:
                if (!mDataManager.isClientId(parentId)) {
                    uploadFile(new File(uri.getPath()), parentId, Attachment.ParentType.PARENT_ASSET);
                }
                break;
            case ACTION_CASE_FLUSH:
                if (!mDataManager.isClientId(parentId)) {
                    uploadFile(new File(uri.getPath()), parentId, Attachment.ParentType.PARENT_CASE);
                }
                break;
            case ACTION_EVENT_FLUSH:
                if (!mDataManager.isClientId(parentId)) {
                    uploadFile(new File(uri.getPath()), parentId, Attachment.ParentType.PARENT_EVENT);
                }
                break;
            case ACTION_SURVEY_FLUSH:
                if (!mDataManager.isClientId(parentId)) {
                    uploadFile(new File(uri.getPath()), parentId, Attachment.ParentType.PARENT_SURVEY_QR);
                }
                break;
        }
    }

    private Uri getThumbnailUri(Uri fileUri) {
        if (fileUri == null) return null;

        List<String> pathSegments = new ArrayList<>(fileUri.getPathSegments());
        if (pathSegments.size() > 0) {
            int fileNameSegmentIndex = pathSegments.size() - 1;
            String fileName = pathSegments.get(fileNameSegmentIndex); // Get last path segment.
            String thumbnailName = "thumb_" + fileName;
            pathSegments.set(fileNameSegmentIndex, thumbnailName);
        }

        Uri.Builder uriBuilder = fileUri.buildUpon().path(null);
        for (String pathSegment: pathSegments) {
            uriBuilder.appendPath(pathSegment);
        }

        return uriBuilder.build();
    }

    private File saveFile(Uri uri, File rootDirectory) {

        try {
            String fileName = getFileNameFrom(uri);
            return saveFile(uri, rootDirectory, fileName);
        } catch (Exception e) {
            // Fix this more cleanly using one of the approaches provided at
            // http://stackoverflow.com/questions/19837358/android-kitkat-securityexception-when-trying-to-read-from-mediastore
            eventBus.post(new AttachmentEvent.AttachmentSavedEvent(false));
            Log.e(TAG, "in saveFile exception:", e);
            final String errorMessage = e.getMessage();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AttachmentUploadService.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });


        }
        return null;
    }

    private String getFileNameFrom(Uri uri) {
        if (uri == null) throw new IllegalArgumentException("File uri cannot be null.");

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getLastPathSegment();
        }
        else {
            Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            cursor.moveToFirst();
            String fileName = cursor.getString(0);
            cursor.close();

            return fileName;
        }
    }

    private String getCurrentEventId() {
        List<Event> events = Event.getAllCheckedInVisits();
        return events.isEmpty() ? null : events.get(0).getId();
    }

    private File saveFile(Uri uri, File rootDirectory, String fileName) {
        File outputFile = new File(rootDirectory, fileName);
        boolean copySuccessful = false;

        try {
            copySuccessful = ContentUtils.copyContent(this, uri, outputFile) >= 0;
        } catch(Exception e){
            CrashReportManagerProvider.getInstance().logException(e);
            Log.e(TAG, "Error converting attachment to byte array " , e);
            eventBus.post(new AttachmentEvent.AttachmentSavedEvent(false));
            final String errorMessage = e.getMessage();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AttachmentUploadService.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }

        if (!copySuccessful) {
            Log.e(TAG, "*** Investigate ***");
            Log.e(TAG, "Got outputFile null for fileName: " + fileName);
            return null;
        }

        Log.i(TAG, "before compressed length is: " + outputFile.length());

        try {
            if (isImage(uri)) {
                compressImage(getApplicationContext(), outputFile);
            }
        } catch (Exception e) {
            CrashReportManagerProvider.getInstance().logException(e);
            Log.e(TAG, "got exception: " + e.getMessage());
        }

        if (outputFile != null) {
            Log.i(TAG, "after compressed length is: " + outputFile.length());

            //add to pending map with null id signifying that it needs to be uploaded
            PendingSyncAttachments.updateAttachment(outputFile.getPath(), null);

            //fire event file saved
            eventBus.post(new AttachmentEvent.AttachmentSavedEvent(true));
            return outputFile;
        }
        else  {
            Log.w(TAG, "Compressed image is null!");
            return null;
        }
    }

    private boolean isImage(Uri fileUri) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String fileType = mime.getExtensionFromMimeType(getContentResolver().getType(fileUri));
        Log.i(TAG, "fileType: " + fileType);

        if (fileType == null) {
            String lowerCaseUri = fileUri.toString().toLowerCase();
            return lowerCaseUri.endsWith("jpg") || lowerCaseUri.endsWith("jpeg") || lowerCaseUri.endsWith("png");
        } else {
            String lowerCaseType = fileType.toLowerCase();
            return lowerCaseType.equals("jpg") || lowerCaseType.equals("jpeg") || lowerCaseType.equals("png");
        }
    }

    private void uploadFile(File file, String parentId, Attachment.ParentType parentType) {
        if (file == null) return;
        if (!file.exists()) {
            Log.w(TAG, "File: " + file + " does not exist.");
            return;
        }

        boolean isSuccess = false;

        if (BuildConfig.CHINA_BUILD) {
            isSuccess = uploadToAzure(file, parentId);
        } else {
            Attachment attachment = Attachment.createAttachmentFromFile(file, parentId, true);
            if (attachment != null) {
                isSuccess = Attachment.uploadAttachment(attachment, parentType);
            } // if we dont get a valid attachment then there is no point in retrying again by triggering a sync ...
        }

        if (isSuccess) {
            DataManager dm = DataManagerFactory.getDataManager();
            for (Picture_Audit_Status__c auditStatus : Picture_Audit_Status__c.getBy(parentId, file.getName())) {
                if (Picture_Audit_Status__c.Status.AWAITING_UPLOAD.equals(auditStatus.getStatus())) {
                    auditStatus.setStatus(Picture_Audit_Status__c.Status.PENDING);
                    auditStatus.updateRecord(dm);
                }
            }
        }

        SyncUtils.TriggerRefresh(this);

        //fire event file uploaded
        eventBus.post(new AttachmentEvent.AttachmentUploadEvent(isSuccess));
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    private boolean uploadToAzure(File file, String parentId) {

        String azureFileName = null;

        try {
            azureFileName = parentId + "/" + file.getName();
            //TODO: validate that the file name is valid before you upload

            CloudBlockBlob fileBlob = mAzureContainer.getBlockBlobReference(azureFileName);
            fileBlob.uploadFromFile(file.getPath());
            Log.i(TAG, "uploaded to Azure. file: " + azureFileName + " blob name: " + fileBlob.getName());

            // this is used to delete the old file in the temp folder after delta sync
            // the id we pass is irrelevant for Azure so a dummy value is ok
            PendingSyncAttachments.updateAttachment(file.getPath(), Attachment.DUMMY_ID);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "got exception while uploading to Azure " + azureFileName + e.getMessage(), e);
            return false;
        }
    }

    private void createPictureAuditStatus(File file, String parentId, String eventId, Attachment.ParentType parentType) {
        if (file == null) return;

        DataManager dm = DataManagerFactory.getDataManager();
        Picture_Audit_Status__c auditStatus = new Picture_Audit_Status__c(new JSONObject());
        auditStatus.setPictureReference(file.getName());
        auditStatus.setStoredIn(Picture_Audit_Status__c.StoredIn.AZURE);
        auditStatus.setParentId(parentId);
        auditStatus.setParentEventId(eventId);
        auditStatus.setStatus(Picture_Audit_Status__c.Status.AWAITING_UPLOAD);

        String parentObjectName;
        switch (parentType) {
            case PARENT_ACCOUNT:
                parentObjectName = AbInBevObjects.ACCOUNT;
                break;
            case PARENT_EVENT:
                parentObjectName = AbInBevObjects.EVENT;
                break;
            case PARENT_ASSET:
                parentObjectName = AbInBevObjects.ACCOUNT_ASSET_C;
                break;
            case PARENT_SURVEY_QR:
                parentObjectName = AbInBevObjects.SURVEY_Question_Response;
                break;
            case PARENT_CASE:
                parentObjectName = AbInBevObjects.CASE;
                break;
            default:
                throw new IllegalStateException("Unknown Attachment parent type: " + parentType);
        }

        auditStatus.setParentObjectName(ManifestUtils.getNamespaceSupportedObjectName(parentObjectName, this));
        auditStatus.createRecord(dm);
    }
}

