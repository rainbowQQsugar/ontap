package com.salesforce.androidsyncengine.datamanager;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.core.PathUtility;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsyncengine.BuildConfig;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.services.DownloadIntentService;
import com.salesforce.androidsyncengine.utils.ContentPreferenceUtils;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import okio.BufferedSink;
import okio.Okio;
import org.json.JSONObject;

/**
 * Created by Jakub Stefanowski on 12.05.2017.
 */

public class DownloadHelper {

    private static final String TAG = "DownloadHelper";

    private final Context context;

    private final DownloadManager downloadManager;

    private final RestClient restClient;

    private final OkHttpClient okHttpClient;

    private final String appName;

    private CloudBlobContainer blobContainer;

    public DownloadHelper(Context context, RestClient client) {
        this.context = context;
        this.restClient = client;
        this.appName = getAppName(context);
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.okHttpClient = new OkHttpClient();
    }

    public void setAzureContainer(CloudBlobContainer blobContainer) {
        this.blobContainer = blobContainer;
    }

    public void download(String objectName, String id, String stringUrl) throws IOException {
        Log.i(TAG, "download(objectName=" + objectName + ", id=" + id + ", stringUrl=" + stringUrl + ")");

        File file = getTargetFile(objectName, id);
        if (file.exists()) {
            Log.v(TAG, file + " file is already downloaded");
        }

        else {

            if (BuildConfig.CHINA_BUILD) {
                // let us try Azure download
                // if download successful return else continue with the below flow to download from Salesforce
                if (downloadFromAzure(id, file)) return;
            }

            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + restClient.getAuthToken())
                    .url(getFullUrl(stringUrl))
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (response.code() / 100 != 2) {
                String stringResponse = toStringSilently(response);

                if (TextUtils.isEmpty(stringResponse)) {
                    throw new IOException("File download error code: " + response.code());
                }
                else {
                    throw new IOException("File download error: " + stringResponse);
                }
            }

            boolean isSaved = false;

            try {
                BufferedSink sink = Okio.buffer(Okio.sink(file));
                sink.writeAll(response.body().source());
                sink.close();
                isSaved = true;
            } finally {
                if (!isSaved) {
                    file.delete();
                }
            }
        }
    }

    private String toStringSilently(Response response) {
        try {
            return response.body().string();
        } catch (IOException e) {
            // ignore
            return null;
        }
    }

    private URL getFullUrl(String url) throws MalformedURLException {
        return restClient.getClientInfo().resolveUrl(url).toURL();
    }

    public void addToDownloadQueue(String objectName, String id, String binaryUrl) {
        Log.i(TAG, "addToDownloadQueue(objectName=" + objectName + ", id=" + id + ", binaryUrl=" + binaryUrl + ")");

        if (objectName.equals("Picture_Audit_Status__c")) {
            addAzureDownloadToDownloadQueue(objectName, id);
            return;
        }

        Log.v(TAG, "in addToDownloadQueue");
        try {

            URI completeURI = restClient.getClientInfo().resolveUrl(binaryUrl);
            String completeUrl = completeURI.toURL().toExternalForm();

            String fileName = getFileName(context, objectName, id);
            File file = getTargetFile(objectName, id);

            if (file.exists()) {
                Log.v(TAG, fileName + " file is already downloaded");
                ContentPreferenceUtils.removeValue(fileName, context);

            } else if (isInDownloadQueue(fileName)) {

                Log.v(TAG, fileName + " file is already in download queue. No need to add it.");

            } else {

                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(completeUrl));
                request.setTitle(fileName);
                request.setDescription("Sync Engine file download");
                request.setDestinationInExternalPublicDir(appName, fileName);

                // TODO: Enable these two once you know that things are working correctly
                // request.setVisibleInDownloadsUi(false);
                // request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);

                // set auth header
                request.addRequestHeader("Authorization", "Bearer " + restClient.getAuthToken());

                long downloadId = downloadManager.enqueue(request);

                ContentPreferenceUtils.putValue(fileName, downloadId, context);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error while starting content download.", e);
        }
    }

    private File getTargetFile(String objectName, String id) {
        return new File(context.getFilesDir(), getFileName(context, objectName, id));
    }

    private File getAzureTargetFile(String objectName, String parentId, String name) {
        String objectDirName = getDirNameForObject(objectName);
        if (objectDirName == null) return null;

        File directory = new File(context.getFilesDir(), String.format("/temp/%s/%s/", objectDirName, parentId));
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return new File(context.getFilesDir(), String.format("/temp/%s/%s", objectDirName, name));
    }

    private static String getDirNameForObject(String objectName) {
        if ("Account".equals(objectName)) {
            return "account";
        }
        else if ("ONTAP__Account_Asset__c".equals(objectName)) {
            return "asset";
        }
        else if ("ONTAP__Case_Force__c".equals(objectName)) {
            return "case";
        }
        else if ("ONTAP__SurveyQuestionResponse__c".equals(objectName)) {
            return "survey_qr";
        }
        else if ("Event".equals(objectName)) {
            return "event";
        }
        else {
            return null;
        }
    }

    private boolean isInDownloadQueue(String fileName) {
        // get the downloadId from preference using the filename, if there is no value then return false
        long downloadId = ContentPreferenceUtils.getValue(fileName, context);
        if (downloadId == 0) {
            return false;
        }

        // query the download queue to see if this value exists. if does not exist return false
        Cursor c = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
        if (c == null) {
            return false;
        } else {

            if (c.moveToFirst()) {
                int status = c.getInt(c
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                // check if this file is already in download queue with pause,
                // pending or running then return true
                // if it is successful or failure then return false;

                c.close();

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                    case DownloadManager.STATUS_SUCCESSFUL:
                        // this is to address an edge case
                        downloadManager.remove(downloadId);
                        // Ensure that retry count was not set for this file.
                        DownloadIntentService.setRetryCount(context, fileName, 0);
                        return false;

                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        return true;

                    default:
                        return false;
                }
            } else {
                c.close();
                return false;
            }
        }
    }

    private static String getAppName(Context c) {
        return (c.getResources().getString(R.string.app_name)).replace(" ", "_");
    }

    public static String getFileName(Context context, String objectName, String id) {
        String orgId = PreferenceUtils.getOrgId(context);
        return orgId + "_" + id + "_" + objectName;
    }

    public static String getAzureFileName(Context context, String objectName, String parentId, String name) {
        String objectDirName = getDirNameForObject(objectName);
        if (objectDirName == null) return null;

        return String.format("/temp/%s/%s", objectDirName, name);
    }

    public void removeContentFiles(String objectName, String id) {
        // check preference file and see if we have a downloadId

        String fileName = getFileName(context, objectName, id);
        long downloadId = ContentPreferenceUtils.getValue(fileName, context);

        // remove this id from the preference file
        ContentPreferenceUtils.removeValue(fileName, context);

        // remove file from download Queue if we have a non-zero downloadId
        if (downloadId != 0) {
            downloadManager.remove(downloadId);
        }

        // Ensure that retry count was not set for this file.
        DownloadIntentService.setRetryCount(context, fileName, 0);

        // remove file from file system if it exists
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) file.delete();

        if ("Picture_Audit_Status__c".equals(objectName)) {
            DataManager dm = DataManagerFactory.getDataManager();
            JSONObject jsonObject = dm.exactQuery("Picture_Audit_Status__c", "Id", id);

            if (jsonObject != null) {
                String parentId = jsonObject.optString("Parent_ID__c");
                String parentObjectName = jsonObject.optString("Parent_Object_Name__c");
                String auditFileName = jsonObject.optString("Storage_Picture_Reference__c");
                String objectDirectoryName = getDirNameForObject(parentObjectName);

                if (!TextUtils.isEmpty(parentId)
                        && !TextUtils.isEmpty(auditFileName)
                        && !TextUtils.isEmpty(objectDirectoryName)) {
                    File tempFile = new File(context.getFilesDir(), String.format("/temp/%s/%s/%s", objectDirectoryName, parentId, auditFileName));
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }
        }
    }

    public static void downloadContentFile(DownloadManager dm, RestClient client, Context context, Uri uri, String fileName) {
        Log.v(TAG, "in downloadContentFile");
        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            request.setDescription("Sync Engine file download");
            request.setDestinationInExternalPublicDir(getAppName(context), fileName);

            // TODO: Enable these two once you know that things are working correctly
            // request.setVisibleInDownloadsUi(false);
            // request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);

            // set auth header
            request.addRequestHeader("Authorization", "Bearer " + client.getAuthToken());

            long downloadId = dm.enqueue(request);

            ContentPreferenceUtils.putValue(fileName, downloadId, context);
        }
        catch (Exception e) {
            Log.w(TAG, "Error while starting content download.", e);
        }
    }

    private boolean downloadFromAzure(String SalesforceId, File file) {
        try {

            CloudBlockBlob fileBlob = blobContainer
                    .getBlockBlobReference(SalesforceId);

            Log.i("BlobDownload3", "downloading: " + fileBlob.getUri() + " to: " + file.getPath());
            fileBlob.downloadToFile(file.getPath());

            Log.v(TAG, "downloaded from Azure. file: " + SalesforceId);

        } catch (Exception e) {
            Log.v(TAG, "got exception while downloading from Azure " + SalesforceId + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    public void addAzureDownloadToDownloadQueue(String objectName, String id) {

        JSONObject pictureAuditStatus = DataManagerFactory.getDataManager().exactQuery(objectName, "Id", id);
        if (pictureAuditStatus == null) {
            Log.w(TAG, "There is no Picture_Audit_Status with id: " + id);
            return;
        }

        Log.v(TAG, "in addAzureDownloadToDownloadQueue");
        String parentId = pictureAuditStatus.optString("Parent_ID__c");
        String parentObjectName = pictureAuditStatus.optString("Parent_Object_Name__c");
        String blobFileName = pictureAuditStatus.optString("Storage_Picture_Reference__c");

        if (TextUtils.isEmpty(parentId) || TextUtils.isEmpty(parentObjectName) || TextUtils.isEmpty(blobFileName)) {
            Log.w(TAG, "Picture_Audit_Status doesn't contain Parent_ID__c");
            return;
        }

        try {
            SharedAccessBlobPolicy sasConstraints = new SharedAccessBlobPolicy();

            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.HOUR, 2);
            sasConstraints.setSharedAccessExpiryTime(now.getTime());
            sasConstraints.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));

            Log.v(TAG, "virtual directory: " + parentId);
            CloudBlobDirectory retrievedDirectory = blobContainer.getDirectoryReference(parentId);
            blobContainer.getServiceClient().getCredentials();

            CloudBlockBlob retrievedBlob = retrievedDirectory.getBlockBlobReference(blobFileName);

            if (retrievedBlob.exists()) {
                Log.v(TAG, "blob Name: " + retrievedBlob.getName());
                String fileName = getAzureFileName(context, parentObjectName, parentId, retrievedBlob.getName());
                File file = getAzureTargetFile(parentObjectName, parentId, retrievedBlob.getName());
                Log.v(TAG, "fileName: " + fileName);

                if (file == null) {
                    Log.v(TAG, "there is no directory where current object could be stored");
                }
                else if (file.exists()) {
                    Log.v(TAG, fileName + " file is already downloaded");
                    ContentPreferenceUtils.removeValue(fileName, context);
                }
                else if (isInDownloadQueue(fileName)) {
                    Log.v(TAG, fileName + " file is already in download queue. No need to add it.");
                }
                else {
                    Log.i("BlobDownload4", "downloading: " + retrievedBlob.getUri() + " to: " + file.getPath());
                    URI blobItemUri = PathUtility.addToQuery(retrievedBlob.getUri(),
                            retrievedBlob.generateSharedAccessSignature(sasConstraints, null));
                    String completeUrl = blobItemUri.toURL().toExternalForm();
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(completeUrl));
                    request.setTitle(fileName);
                    request.setDescription("Sync Engine file download");
                    request.setDestinationInExternalPublicDir(appName, fileName);

                    long downloadId = downloadManager.enqueue(request);

                    ContentPreferenceUtils.putValue(fileName, downloadId, context);
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "Error while starting content download.", e);
        }
    }
}
