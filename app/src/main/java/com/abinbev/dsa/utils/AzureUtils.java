package com.abinbev.dsa.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.BuildConfig;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.Attachment;
import com.abinbev.dsa.model.Auth_Keys__c;
import com.abinbev.dsa.model.SensitiveData__c;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EdmType;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TablePayloadFormat;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableRequestOptions;
import com.microsoft.azure.storage.table.TableResult;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.DownloadHelper;
import com.salesforce.androidsyncengine.datamanager.SmartStoreDataManagerImpl;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.dsa.utils.DSAConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AzureUtils {

    private static final String TAG = "AzureUtils";
    private static final String CONNECTION_VALUE = "AzureCN";
    private static final String REFERENCE_VALUE = "salesforce";
    private static final String AZURE_NAME_SEPARATOR = "___";


    private static final int DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH = 100;


    @Nullable
    public static CloudBlobContainer getAzureBlobContainer() {
        try {
            String storageConnectionString = Auth_Keys__c.getStorageConnectionString(CONNECTION_VALUE);
            // Setup the cloud storage account.
            CloudStorageAccount account = CloudStorageAccount
                    .parse(storageConnectionString);

            // Create a blob service client
            CloudBlobClient blobClient = account.createCloudBlobClient();

            // Get a reference to a container
            CloudBlobContainer container = blobClient.getContainerReference(REFERENCE_VALUE);
            return container;

        } catch (Exception e) {
            Log.e(TAG, "got exception while initializing Azure container ", e);
            return null;
        }
    }

    @Nullable
    public static CloudTable getAzureCloudTable() {
        try {
            String storageConnectionString = Auth_Keys__c.getStorageConnectionString(CONNECTION_VALUE);
            // Setup the cloud storage account.
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);

            // Create a table service client.
            CloudTableClient tableClient = account.createCloudTableClient();
            CloudTable table = tableClient.getTableReference(REFERENCE_VALUE);
            return table;

        } catch (Exception e) {
            Log.e(TAG, "got exception while initializing Azure table service ", e);
            return null;
        }
    }

    public static void fetchSensitiveData(Collection<SensitiveData__c> sensitiveDataCollection) {
        //Get sensitive data associated with Accounts in visit list
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);

        CloudTable table = getAzureCloudTable();
        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();

        for (SensitiveData__c sensitiveData : sensitiveDataCollection) {
            String objectNameWithPrefix = sensitiveData.getObjectName();

            if (TextUtils.isEmpty(objectNameWithPrefix)) {
                continue;
            }

            String objectNameWithoutPrefix = ManifestUtils.removeNamespaceFromObject(objectNameWithPrefix, ABInBevApp.getAppContext());
            Log.i(TAG, "Fetching sensitive data for object: " + objectNameWithPrefix);

            JSONArray array = dataManager.fetchAllSmartSQLQuery(String.format("SELECT {%1$s:Id} FROM {%1$s}", objectNameWithoutPrefix));
            Log.i(TAG, "Fetching sensitive data for object: " + objectNameWithPrefix + " items: " + array.length());

            Set<String> requiredFields = sensitiveData.getFields();

            List<String> recordIds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {

                String recordId = null;
                try {
                    recordId = array.getJSONArray(i).getString(0);
                    recordIds.add(recordId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            fetchObjectSensitiveData(sensitiveData, recordIds);
        }
    }

    public static void fetchObjectSensitiveData(SensitiveData__c sensitiveData, List<String> recordIds) {
        //Get sensitive data associated with Accounts in visit list
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);

        CloudTable table = getAzureCloudTable();
        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();

        String objectNameWithPrefix = sensitiveData.getObjectName();

        if (TextUtils.isEmpty(objectNameWithPrefix)) {
            return;
        }

        String objectNameWithoutPrefix = ManifestUtils.removeNamespaceFromObject(objectNameWithPrefix, ABInBevApp.getAppContext());

        Log.i(TAG, "Fetching sensitive data for object: " + objectNameWithPrefix + " items: " + recordIds.size());
        TableBatchOperation batchOperation = new TableBatchOperation();

        Set<String> requiredFields = sensitiveData.getFields();
        TableQuery<DynamicTableEntity> query = TableQuery.from(DynamicTableEntity.class);
        String mainFilter = TableQuery.generateFilterCondition("PartitionKey", TableQuery.QueryComparisons.EQUAL, objectNameWithPrefix);


        if (recordIds.size() <= DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH) {
            query_table_under_limits(recordIds, options, table, dataManager, objectNameWithPrefix, objectNameWithoutPrefix, requiredFields, query, mainFilter);
            return;
        }

        int fromIndex = 0;
        int toIndex = 0;
        int max = recordIds.size() / DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH;
        if (recordIds.size() % DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH != 0) {
            max += 1;
        }
        int i = 0;
        int remain = recordIds.size();
        while (i < max) {

            if (remain > DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH) {
                toIndex = fromIndex + DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH;
                remain -= DEFAULT_MAX_ENTITY_NUMBER_IN_BATCH;
            } else {
                toIndex = fromIndex + remain;
            }

            List<String> sliceList = recordIds.subList(fromIndex, toIndex);
            query_table_under_limits(sliceList, options, table, dataManager, objectNameWithPrefix, objectNameWithoutPrefix, requiredFields, query, mainFilter);

            fromIndex = toIndex;
            i++;

        }


    }

    private static void query_table_under_limits(List<String> recordIds, TableRequestOptions options, CloudTable table, SmartStoreDataManagerImpl dataManager, String objectNameWithPrefix, String objectNameWithoutPrefix, Set<String> requiredFields, TableQuery<DynamicTableEntity> query, String mainFilter) {
        String secondFilter = null;
        for (String id : recordIds) {

            String temp = TableQuery.generateFilterCondition("RowKey", TableQuery.QueryComparisons.EQUAL, id);
            if (secondFilter == null) {
                secondFilter = temp;
                continue;
            }

            secondFilter = TableQuery.combineFilters(secondFilter, TableQuery.Operators.OR, temp);
        }

        query.setFilterString(TableQuery.combineFilters(mainFilter, TableQuery.Operators.AND, secondFilter));

        Iterable<DynamicTableEntity> results = table.execute(query, options, null);
        Iterator<DynamicTableEntity> iterator = results.iterator();

        while (iterator.hasNext()) {
            DynamicTableEntity dynamicTableEntity = iterator.next();
            try {
                dealWithTableEntity(objectNameWithPrefix, objectNameWithoutPrefix, requiredFields, dataManager, dynamicTableEntity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void fetchAccountRelatedContent(Collection<String> accountIds) {
        //Get sensitive data associated with Accounts in visit list
        TableRequestOptions options = new TableRequestOptions();
        options.setTablePayloadFormat(TablePayloadFormat.JsonNoMetadata);
//        CloudTable table = getAzureCloudTable();
//        SmartStoreDataManagerImpl dataManager = (SmartStoreDataManagerImpl) DataManagerFactory.getDataManager();

        for (String accountId : accountIds) {
//            fetchSingleRecord(AbInBevConstants.AbInBevObjects.ACCOUNT, AbInBevConstants.AbInBevObjects.ACCOUNT, accountId, /*requiredFields:*/ null, table, options, dataManager);
            if (BuildConfig.CHINA_BUILD) {
                fetchAccountAttachmentsFromAzure(accountId, false);
                List<Account_Asset__c> assets = getAccountAssets(accountId);
                for (Account_Asset__c a : assets)
                    fetchAccountAttachmentsFromAzure(a.getId(), true);
            }
        }
    }

    public static void fetchSurveyQuestionRelatedContent(Collection<String> surveyQuestionIds) {
        for (String surveyQuestionId : surveyQuestionIds) {
            if (BuildConfig.CHINA_BUILD) {
                fetchSurveyQuestionAttachmentsFromAzure(surveyQuestionId);
            }
        }
    }

    private static List<Account_Asset__c> getAccountAssets(String accountId) {

        List<Account_Asset__c> assets = new ArrayList<>();
        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevConstants.AbInBevObjects.ACCOUNT_ASSET_C, AbInBevConstants.AccountAssetFields.CLIENT, accountId);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            int recordsCount = recordsArray.length();
            for (int i = 0; i < recordsCount; i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                assets.add(new Account_Asset__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting  assets by Account ID: " + accountId, e);
        }
        return assets;
    }

    private static void fetchAccountAttachmentsFromAzure(final String accountId, boolean isAsset) {
        try {
            CloudBlobContainer blobContainer = AzureUtils.getAzureBlobContainer();
            if (blobContainer == null) {
                return;
            }

            final String azureDirectoryForAccount = accountId + "/";

            CloudBlobDirectory retrievedDirectory = blobContainer.getDirectoryReference(azureDirectoryForAccount);
            blobContainer.getServiceClient().getCredentials();
            final Iterable<ListBlobItem> listBlobItems = retrievedDirectory.listBlobs();

            File accountsDir = Attachment.attachmentDirectoryForAccounts(isAsset);
            for (ListBlobItem listBlobItem : listBlobItems) {
                Log.i("Adam", "fetchAccountAttachmentsFromAzure, listBlobItem.getStorageUri()=" + listBlobItem.getStorageUri());

                if (listBlobItem instanceof CloudBlockBlob) {
                    CloudBlockBlob fileBlob = (CloudBlockBlob) listBlobItem;
                    File localFile = new File(accountsDir, fileBlob.getName());
                    File parentDir = localFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    if (localFile.exists()) {
                        Log.i(TAG, "attachment already exists in path: " + localFile.getPath());
                        return;
                    }

                    Log.i(TAG, "will download attachment for accountId: " + accountId);
                    Log.i(TAG, "save attachment in path: " + localFile.getPath());
                    Log.i("BlobDownload1", "downloading: " + fileBlob.getUri() + " to: " + localFile.getPath());
                    fileBlob.downloadToFile(localFile.getPath());
                }
            }
        } catch (StorageException | FileNotFoundException | URISyntaxException | NoSuchElementException e) {
            Log.i(TAG, "fetchAccountAttachmentsFromAzure for accountId, exception: " + accountId, e);
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    private static void fetchSurveyQuestionAttachmentsFromAzure(final String surveyQuestionId) {
        try {
            CloudBlobContainer blobContainer = AzureUtils.getAzureBlobContainer();
            if (blobContainer == null) {
                return;
            }

            final String azureDirectoryForAccount = surveyQuestionId + "/";

            CloudBlobDirectory retrievedDirectory = blobContainer.getDirectoryReference(azureDirectoryForAccount);
            blobContainer.getServiceClient().getCredentials();
            final Iterable<ListBlobItem> listBlobItems = retrievedDirectory.listBlobs();

            File accountsDir = Attachment.attachmentDirectoryForSurveyQuestion();
            File currentSurveyQuestionDir = new File(accountsDir, surveyQuestionId);
            HashSet<String> allAzureFileNames = new HashSet<>();

            for (ListBlobItem listBlobItem : listBlobItems) {
                Log.i(TAG, "fetchAccountAttachmentsFromAzure, listBlobItem.getStorageUri()=" + listBlobItem.getStorageUri());

                if (listBlobItem instanceof CloudBlockBlob) {
                    CloudBlockBlob fileBlob = (CloudBlockBlob) listBlobItem;
                    Log.i(TAG, "survey blob: " + fileBlob.getName());
                    File localFile = new File(accountsDir, fileBlob.getName());
                    File parentDir = localFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    allAzureFileNames.add(localFile.getName());

                    if (localFile.exists()) {
                        Log.i(TAG, "attachment already exists in path: " + localFile.getPath());
                    } else {
                        Log.i(TAG, "will download attachment for accountId: " + surveyQuestionId);
                        Log.i(TAG, "save attachment in path: " + localFile.getPath());
                        Log.i("BlobDownload2", "downloading: " + fileBlob.getUri() + " to: " + localFile.getPath());
                        fileBlob.downloadToFile(localFile.getPath());
                    }
                }
            }

            String[] localFileNames = currentSurveyQuestionDir.list();
            if (localFileNames != null) {
                for (String localFileName : localFileNames) {
                    if (!allAzureFileNames.contains(localFileName)) {
                        File fileToDelete = new File(currentSurveyQuestionDir, localFileName);
                        Log.i(TAG, "This file does not exist in azure and will be deleted: " + fileToDelete);
                        fileToDelete.delete();
                    }
                }
            }

        } catch (StorageException | FileNotFoundException | URISyntaxException | NoSuchElementException e) {
            Log.i(TAG, "fetchAccountAttachmentsFromAzure for accountId, exception: " + surveyQuestionId, e);
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    public static void fetchAccountRelatedContent(String accountId) {
        fetchAccountRelatedContent(Collections.singleton(accountId));
        SensitiveData__c sensitiveData = SensitiveData__c.getByObjectName(AbInBevConstants.AbInBevObjects.ACCOUNT);
        if (sensitiveData != null) {
            fetchObjectSensitiveData(sensitiveData, Collections.singletonList(accountId));
        }
    }

    private static void fetchSingleRecord(String objectNameWithPrefix, String objectNameWithoutPrefix, String recordId, Set<String> requiredFields, CloudTable table, TableRequestOptions options, SmartStoreDataManagerImpl dataManager) {
        try {
            TableResult tableResult = table.execute(TableOperation.retrieve(objectNameWithPrefix,
                    recordId, DynamicTableEntity.class), options, null);
            DynamicTableEntity dynamicTableEntity = (DynamicTableEntity) tableResult.getResult();

            if (dynamicTableEntity == null) {
                Log.e(TAG, "no azure data for recordId: " + objectNameWithPrefix + " " + recordId);
                return;
            }

            dealWithTableEntity(objectNameWithPrefix, objectNameWithoutPrefix, requiredFields, dataManager, dynamicTableEntity);

        } catch (Exception e) {
            Log.e(TAG, "Azure exception: ", e);
        }
    }

    private static void dealWithTableEntity(String objectNameWithPrefix, String objectNameWithoutPrefix, Set<String> requiredFields, SmartStoreDataManagerImpl dataManager, DynamicTableEntity dynamicTableEntity) throws JSONException {
        HashMap<String, EntityProperty> properties = dynamicTableEntity.getProperties();
        JSONObject sensitiveData = new JSONObject();

        String recordId = dynamicTableEntity.getRowKey();

        for (Map.Entry<String, EntityProperty> entry : properties.entrySet()) {
            String fieldNameWithPrefix = entry.getKey();

            Log.i(TAG, objectNameWithPrefix + " -- " + fieldNameWithPrefix);

            if (requiredFields == null || requiredFields.contains(fieldNameWithPrefix)) {
                EdmType type = entry.getValue().getEdmType();
                String fieldNameWithoutPrefix = ManifestUtils.removeNamespaceFromField(
                        objectNameWithPrefix, fieldNameWithPrefix, ABInBevApp.getAppContext());

                switch (type) {
                    case BOOLEAN:
                        sensitiveData.put(fieldNameWithoutPrefix, entry.getValue().getValueAsBoolean());
                        break;
                    case STRING:
                        sensitiveData.put(fieldNameWithoutPrefix, entry.getValue().getValueAsString());
                        break;
                    case DOUBLE:
                        sensitiveData.put(fieldNameWithoutPrefix, entry.getValue().getValueAsDouble());
                        break;
                    case INT64:
                    case INT32:
                        sensitiveData.put(fieldNameWithoutPrefix, entry.getValue().getValueAsLong());
                        break;
                    default:
                        sensitiveData.put(fieldNameWithoutPrefix, entry.getValue().getValueAsString());
                }
            } else {
                Log.i(TAG, objectNameWithPrefix + " " + fieldNameWithPrefix + " is not in required fields.");
            }
        }
        Log.i(TAG, "updating data for recordId: " + recordId + ": " + sensitiveData.toString());
        dataManager.updateRecordWithoutAddingToQueue(objectNameWithoutPrefix, recordId, sensitiveData);
    }

    public static List<JSONObject> getAzureFilesForSurveyQuestionResponse(String SurveyQuestionResponseId) {

        List<JSONObject> result = new ArrayList<>();

        final String filePrefix = DownloadHelper.getFileName(ABInBevApp.getAppContext(), AbInBevConstants.AbInBevObjects.PICTURE_AUDIT_STATUS, SurveyQuestionResponseId);
        Log.e("Babu", "file prefix: " + filePrefix);

        if (filePrefix == null) return result;
        File f = ABInBevApp.getAppContext().getFilesDir();

        Log.e("Babu", "file dir: " + f.toString());
        File[] responseFiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.startsWith(filePrefix));
            }
        });

        for (File responseFile : responseFiles) {
            Log.e("Babu", "file: " + responseFile.getName());
            String fileName = responseFile.getName();
            int searchIndex = fileName.indexOf(AZURE_NAME_SEPARATOR);
            String actualFileName;
            if (searchIndex == -1) {
                actualFileName = "Unknown";
            } else {
                actualFileName = fileName.substring(searchIndex + 3, fileName.length());
            }
            Log.e("Babu", "actualFileName: " + actualFileName);
            try {
                JSONObject data = new JSONObject();
                data.put("name", actualFileName);
                data.put("path", responseFile.getAbsolutePath());
                result.add(data);
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving data for SurveyQuestionResponse with id " + SurveyQuestionResponseId, e);
            }
        }


//        String filter = String.format("{%s:%s} = '%s'",
//                AbInBevConstants.AbInBevObjects.AZURE_CONTENT, "ParentId__c", SurveyQuestionResponseId);
//
//        String smartQuery = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevConstants.AbInBevObjects.AZURE_CONTENT, filter);
//
//
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

        return result;
    }

    public static File getAzurePhotoFileForAccount(final String accountId) {
        final File attachmentDirectoryForAccounts = Attachment.attachmentDirectoryForAccount(accountId);
        final String pathToPhotoForAccount = attachmentDirectoryForAccounts.getPath() + "/" +
                Attachment.getAccountPhotoFileName() +
                Attachment.getAccountPhotoFileType();

        return new File(pathToPhotoForAccount);
    }

    public static boolean deleteAzureTableData(String objectName, String id) {

        try {
            TableOperation retrieveEventEntity = TableOperation.retrieve(objectName,
                    id, DynamicTableEntity.class);

            CloudTable table = getAzureCloudTable();

            DynamicTableEntity dynamicTableEntity;
            // Retrieve the entity
            dynamicTableEntity = table.execute(retrieveEventEntity)
                    .getResultAsType();
            if (dynamicTableEntity != null) {
                // Create an operation to delete the entity.
                TableOperation deleteOperation = TableOperation.delete(dynamicTableEntity);
                // Submit the delete operation to the table service.
                table.execute(deleteOperation);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean deleteBlobFromBackend(String parentId, String fileName) {
        try {

            CloudBlobContainer container = getAzureBlobContainer();
            CloudBlockBlob blob = container
                    .getBlockBlobReference(parentId + "/" + fileName);
            blob.deleteIfExists();

        } catch (Exception e) {
            //TODO: no network then add the delete operation to queue
            return false;
        }
        return true;
    }

    public static boolean deleteBlobFromBackend(String path) {

        String azureContentName = AbInBevConstants.AbInBevObjects.PICTURE_AUDIT_STATUS;

        //00DN0000000B1q0MAC_a0lN0000001mE1fIAE_AzureContent__c_a0lN0000001mE1fIAE___JPEG_20170607_053947_-711964191.jpg
        int parentIdStartIndex = path.indexOf(azureContentName) + azureContentName.length() + 1;
        int parentIdEndIndex = path.indexOf(AZURE_NAME_SEPARATOR);
        String parentId = path.substring(parentIdStartIndex, parentIdEndIndex);
        int fileNameStartIndex = parentIdEndIndex + AZURE_NAME_SEPARATOR.length();
        String fileName = path.substring(fileNameStartIndex, path.length());

        Log.v(TAG, "in deleteBlob parentId: " + parentId + " fileName: " + fileName);
        if (parentId != null && fileName != null) {
            File file = new File(path);
            if (file.exists()) file.delete();
            return deleteBlobFromBackend(parentId, fileName);
        } else {
            return false;
        }
    }

    public static String getPathToAccountPhotoInAzure(String accountId) {
        CloudBlobContainer blobContainer = AzureUtils.getAzureBlobContainer();
        if (blobContainer != null) {
            final String azureFileName = accountId + "/" + Attachment.getAccountPhotoFileName() + Attachment.getAccountPhotoFileType();

            try {
                CloudBlockBlob fileBlob = blobContainer.getBlockBlobReference(azureFileName);

                SharedAccessBlobPolicy sasConstraints = new SharedAccessBlobPolicy();

                Calendar now = GregorianCalendar.getInstance();
                now.add(Calendar.HOUR, 2);
                sasConstraints.setSharedAccessExpiryTime(now.getTime());
                sasConstraints.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));

                try {
                    URI blobItemUri = PathUtility.addToQuery(fileBlob.getUri(), fileBlob.generateSharedAccessSignature(sasConstraints, null));

                    return blobItemUri.toURL().toExternalForm();
                } catch (InvalidKeyException | MalformedURLException e) {
                    Log.w(TAG, e);
                }
            } catch (URISyntaxException | StorageException e) {
                Log.w(TAG, e);
            }
        }

        return "";
    }
}
