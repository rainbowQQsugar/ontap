package com.salesforce.dsa.data.model;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.CATEGORY;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.CONTENT_DOCUMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.CONTENT_MODIFIED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.CONTENT_SIZE;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.CONTENT_URL;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.DESCRIPTION;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.DOCUMENT_TYPE;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.FEATURED_CONTENT_BOOST;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.FILE_TYPE;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.INTERNAL_DOCUMENT;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.PATH_ON_CLIENT;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.PUBLISH_STATUS;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.TAGS_CSV;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.TITLE;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.VERSION_DATA;
import static com.salesforce.dsa.utils.DSAConstants.ContentVersionFields.VERSION_NUMBER;

public class ContentVersion extends SFBaseObject {

    public static final String TAG = ContentVersion.class.getSimpleName();

    private static final long serialVersionUID = 7694266007100649241L;

    private String filePath;
    private String completeFileName;

    public ContentVersion() {
        super(DSAObjects.CONTENT_VERSION);
    }

    public ContentVersion(JSONObject json) {
        super(DSAObjects.CONTENT_VERSION, json);
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getDescription() {
        return getStringValueForKey(DESCRIPTION);
    }

    public String getTitle() {
        return getStringValueForKey(TITLE);
    }

    public String getPathOnClient() {
        return getStringValueForKey(PATH_ON_CLIENT);
    }

    public String getContentUrl() {
        return getStringValueForKey(CONTENT_URL);
    }

    public String getFileType() {
        return getStringValueForKey(FILE_TYPE);
    }

    public String getFeaturedContentBoost() {
        return getStringValueForKey(FEATURED_CONTENT_BOOST);
    }

    public String getTagsCsv() {
        return getStringValueForKey(TAGS_CSV);
    }

    public String getContentModifiedDate() {
        return getStringValueForKey(CONTENT_MODIFIED_DATE);
    }

    public String getContentSize() {
        return getStringValueForKey(CONTENT_SIZE);
    }

    public String getVersionNumber() {
        return getStringValueForKey(VERSION_NUMBER);
    }

    public String getDocument_Type__c() {
        return getStringValueForKey(DOCUMENT_TYPE);
    }

    public String getCategory__c() {
        return getStringValueForKey(CATEGORY);
    }

    public String getVersionData() {
        return getStringValueForKey(VERSION_DATA);
    }

    public String getInternal_Document__c() {
        return getStringValueForKey(INTERNAL_DOCUMENT);
    }

    public String getContentDocumentId() {
        return getStringValueForKey(CONTENT_DOCUMENT_ID);
    }

    public String getPublishStatus() {
        return getStringValueForKey(PUBLISH_STATUS);
    }

    public boolean isPrivateItem() {
        return DSAConstants.Constants.PRIVATE_FILE_PUBLISH_STATUS.equals(getPublishStatus());
    }

    public static ContentVersion getById(String Id) {
        String smartSqlFilter = String.format("{%s:%s} = '%s'", DSAObjects.CONTENT_VERSION,
                "Id", Id);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CONTENT_VERSION, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        JSONArray jsonArray = recordsArray.optJSONArray(0);
        JSONObject jsonObject = jsonArray == null ? null : jsonArray.optJSONObject(0);

        return jsonObject == null ? null : new ContentVersion(jsonObject);
    }

    public String getFilePath(Context context) {
        if (filePath == null) {
            filePath = DataManagerFactory.getDataManager().getFilePath(context, "ContentVersion", getId());
            if (filePath != null) {
                filePath = (new File(filePath)).getName();
            }
        }
        return filePath;
    }

    public String getCompleteFileName(Context context) {
        if (completeFileName == null) {
            completeFileName = DataManagerFactory.getDataManager().getFilePath(context, "ContentVersion", getId());
        }
        return completeFileName;
    }

    public String getCategoryLocation() {
        String location = "";
        Cat_Content_Junction__c catContentJunction = Cat_Content_Junction__c.getCatContentJunctionForContentDocumentId(this.getContentDocumentId());
        if (catContentJunction != null) {
            String categoryId = catContentJunction.getCategory();
            Category__c category = Category__c.getCategoryForId(categoryId);
            if (category != null) {
                location = category.getName();

                List<Category__c> parentCategories = category.getParentCategories();
                for (Category__c cat : parentCategories) {
                    location = String.format("%s/%s", cat.getName(), location);
                }
            }
        }

        return location;
    }

    public static List<ContentVersion> fetchFeaturedContent() {

        List<ContentVersion> featuredItems = new LinkedList<>();
        String smartSqlFilter = String.format("{ContentVersion:%s} >= %s", DSAConstants.ContentVersionFields.FEATURED_CONTENT_BOOST, 1);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CONTENT_VERSION, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                if (!"null".equals(contentVersion.getFeaturedContentBoost()) && !"0".equals(contentVersion.getFeaturedContentBoost())) { //TODO: make query do the filtering
                    featuredItems.add(contentVersion);
                }
            }
            Log.d(TAG, "Featured items length:" + featuredItems.size());
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Featured items", e);
        }
        return featuredItems;
    }

    public static List<ContentVersion> fetchRecentContent(Context context) {

        String lastRefreshTime = PreferenceUtils.getPreviousRefreshTime("ContentVersion", context.getApplicationContext());

        List<ContentVersion> featuredItems = new LinkedList<>();
        String smartSqlFilter = String.format("{ContentVersion:%s} >= '%s'", SyncEngineConstants.StdFields.LAST_MODIFIED_DATE, lastRefreshTime);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CONTENT_VERSION, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                featuredItems.add(contentVersion);
            }
            Log.d(TAG, "Recent items length:" + featuredItems.size());
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Recent items", e);
        }
        return featuredItems;
    }

    public static ContentVersion getContentVersionForContentId(String contentId) {
        String smartSqlFilter = String.format("{ContentVersion:%s} = '%s' AND {ContentVersion:IsLatest} = 'true'", DSAConstants.ContentVersionFields.CONTENT_DOCUMENT_ID, contentId);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CONTENT_VERSION, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            // temporary fix for having multiple zip file.
            // FIX this either here or in SyncEngine to get the latest file and remove the old files so we only get one file
            int length = recordsArray.length();
            if (length > 0) {
                JSONObject jsonObject = recordsArray.getJSONArray(length - 1).getJSONObject(0);
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                if (length > 1) {
                    Log.e(TAG, "In getContentVersionForContentId. Got a value greater than 1 which should not happen. length: " + length);
                    Log.e(TAG, "passing contentVersion: " + contentVersion.toJson());
                }

                return contentVersion;
            }
        } catch (Exception e) {
            Log.e("ContentVersion", "Exception in getting  ContentVersion for ContentId", e);
        }
        return null;
    }
}
