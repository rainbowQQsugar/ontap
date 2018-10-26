package com.salesforce.dsa.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.salesforce.dsa.app.sync.CheckAccessFilePermissionTask;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;
import com.salesforce.dsa.data.model.CN_DSA__c;
import com.abinbev.dsa.model.CN_DSA_Azure_File__c;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.app.ui.viewmodel.PlaylistViewModel;
import com.salesforce.dsa.async.ModelDataFetcherTask;
import com.salesforce.dsa.async.ModelDataFetcherTask.ModelDataFetcherCb;
import com.salesforce.dsa.data.model.Attachment;
import com.salesforce.dsa.data.model.Cat_Content_Junction__c;
import com.salesforce.dsa.data.model.CategoryMobileConfig__c;
import com.salesforce.dsa.data.model.Category__c;
import com.salesforce.dsa.data.model.Contact;
import com.salesforce.dsa.data.model.ContentDocument;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.DSA_Playlist__c;
import com.salesforce.dsa.data.model.MobileAppConfig__c;
import com.salesforce.dsa.data.model.Playlist_Content_Junction__c;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File__c;
import static com.salesforce.dsa.utils.DSAConstants.CNDSAFolderFields.CN_Is_Top_Level__c;

/**
 * Util class to fetch data from smartstore
 * some of the calls are synchronous
 *
 * @author usanaga
 */

public class DataUtils {

    public static String TAG = "DataUtils";

    // Data fetching
    // MobileAppConfigs/CategoryMobileConfigs/Categories

    public static void fetchActiveMobileAppConfigs(final ModelDataFetcherCb<CN_DSA__c> callback) {
//        String activeConfigFilter = "{MobileAppConfig__c:" + DSAConstants.MobileAppConfigFields.ACTIVE + "} = 'true'";
//        String activeConfigFilter = "{CN_DSA__c:" + CN_IsActive__c + "} = 'true'";
        String activeConfigFilter = "{CN_DSA__c:IsActive__c} = 'true'";
//        ModelDataFetcherTask<MobileAppConfig__c> configTask = new ModelDataFetcherTask<MobileAppConfig__c>(MobileAppConfig__c.class, activeConfigFilter, callback);

        ModelDataFetcherTask<CN_DSA__c> configTask = new ModelDataFetcherTask<CN_DSA__c>(CN_DSA__c.class, activeConfigFilter, callback);
        configTask.execute();
    }

    public static void fetchCategoryMobileConfig(MobileAppConfig__c currentConfig, ModelDataFetcherCb<CategoryMobileConfig__c> callback) {

        // invoke the CategoryMobileConfig with a IN clause filter
        String catMobileConfigFilter = String.format("{CategoryMobileConfig__c:%s} IN ('%s')",
                DSAConstants.CategoryMobileConfigFields.MOBILE_APP_CONFIGURATION_ID,
                currentConfig.getId());
        ModelDataFetcherTask<CategoryMobileConfig__c> catMobileConfigTask = new ModelDataFetcherTask<CategoryMobileConfig__c>(CategoryMobileConfig__c.class, catMobileConfigFilter, callback);
        catMobileConfigTask.execute();
    }

    public static void fetchCategoryMobileConfig(CN_DSA__c cn_dsa__c, ModelDataFetcherCb<CN_DSA_Folder__c> callback) {
        String catMobileConfigFilter = String.format("{CN_DSA_Folder__c:%s} = ('%s') AND {CN_DSA_Folder__c:%s} = 1.0",
                DSAConstants.CNDSAFolderFields.CN_DSA__c,
                cn_dsa__c.getId(), CN_Is_Top_Level__c);
        ModelDataFetcherTask<CN_DSA_Folder__c> catMobileConfigTask = new ModelDataFetcherTask<CN_DSA_Folder__c>(CN_DSA_Folder__c.class, catMobileConfigFilter, callback);
        catMobileConfigTask.execute();
    }

    public static void fetchTopLevelCategoriesForConfigs(List<String> configIds, ModelDataFetcherCb<Category__c> callback) {

        String categoryFilter = String.format("{Category__c:%s} IN ('%s') AND {Category__c:%s} = 1.0",
                SyncEngineConstants.StdFields.ID, Joiner.on("','")
                        .join(configIds),
                DSAConstants.CategoryFields.IS_TOP_LEVEL);
        ModelDataFetcherTask<Category__c> categoryTask = new ModelDataFetcherTask<Category__c>(Category__c.class, categoryFilter, callback);
        categoryTask.execute();
    }

    public static CategoryMobileConfig__c fetchCategoryConfigForCategoryAndAppConfigId(Category__c category, String id) {
        String smartSqlFilter = String.format("{CategoryMobileConfig__c:%s} = ('%s') AND {CategoryMobileConfig__c:%s} = ('%s')",
                DSAConstants.CategoryMobileConfigFields.CATEGORY_ID, category.getId(),
                DSAConstants.CategoryMobileConfigFields.MOBILE_APP_CONFIGURATION_ID, id);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CATEGORY_MOBILE_CONFIG, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        if (recordsArray.length() != 1) {
            Log.e(TAG, "got a length other than 1. length is: " + recordsArray.length());
        }

        CategoryMobileConfig__c configCategory = null;
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                configCategory = new CategoryMobileConfig__c(jsonObject);
                Log.i(TAG, "category config is: " + configCategory);
                if (configCategory.isIsDefault__c()) {
                    return configCategory;
                } else {
                    Log.i(TAG, "config is not default. " + configCategory.getName());
                }
            }

        } catch (Exception e) {
            return null;
        }
        Log.i(TAG, "using category config: " + configCategory.getName());
        return configCategory;

    }


    public static CN_DSA__c fetchMobileAppConfigForId(String configId) {
        String smartSqlFilter = String.format("{CN_DSA__c:%s} = '%s'", SyncEngineConstants.StdFields.ID, configId);
//        String smartSqlFilter = String.format("{MobileAppConfig__c:%s} = '%s'", SyncEngineConstants.StdFields.ID, configId);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.CNDSAFolderFields.CN_DSA__c, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        if (recordsArray.length() != 1) {
            Log.e(TAG, "got a length other than 1 in fetching MobileAppConfigs. length is: " + recordsArray.length());
        }
        try {
            JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
//            MobileAppConfig__c appConfig = new MobileAppConfig__c(jsonObject);
            CN_DSA__c appConfig = new CN_DSA__c(jsonObject);
            return appConfig;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Cat_Content_Junction__c> fetchJunctionObjectsForCategory(Category__c category) {

        // get junction using category Id (1:n)
        String filter = String.format("{Cat_Content_Junction__c:%s} = '%s'",
                DSAConstants.CatContentJunctionFields.CATEGORY__C,
                category.getId());
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CAT_CONTENT_JUNCTION, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<Cat_Content_Junction__c> catContentJunctions = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Cat_Content_Junction__c catJunction = new Cat_Content_Junction__c(jsonObject);
                catContentJunctions.add(catJunction);
            }
            Log.d(TAG, "CatContentJunctions length:" + catContentJunctions.size());
            return catContentJunctions;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting cat content junctions", e);
            return null;
        }
        // for each content documnet get content version
    }

    private static List<ContentDocument> fetchContentDocuments(Category__c category) {

        List<Cat_Content_Junction__c> junctions = fetchJunctionObjectsForCategory(category);
        List<String> contentIds = GuavaUtils.transform(junctions, new GuavaUtils.Function<Cat_Content_Junction__c, String>() {
            public String apply(Cat_Content_Junction__c junction) {
                return junction.getContentId();
            }
        });

        String filter = String.format("{ContentDocument:%s} IN ('%s')",
                SyncEngineConstants.StdFields.ID, Joiner.on("','")
                        .join(contentIds));
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_DOCUMENT, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<ContentDocument> contentDocuments = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                ContentDocument contentDocument = new ContentDocument(jsonObject);
                contentDocuments.add(contentDocument);
            }
            Log.d(TAG, "ContentDocuments length:" + contentDocuments.size());
            return contentDocuments;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting content documents", e);
            return null;
        }
    }

    private static List<String> fetchContentIds(Category__c category) {
        List<Cat_Content_Junction__c> junctions = fetchJunctionObjectsForCategory(category);
        List<String> contentIds = GuavaUtils.transform(junctions, new GuavaUtils.Function<Cat_Content_Junction__c, String>() {
            public String apply(Cat_Content_Junction__c junction) {
                return junction.getContentId();
            }
        });

        return contentIds;
    }

    public static int getContentCountForPlayList(DSA_Playlist__c playlist) {
        String filter = String.format("{Playlist_Content_Junction__c:%s} = '%s'", DSAConstants.PlaylistContentJunctionFields.PLAYLIST_ID, playlist.getId());
        String smartSQL = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.PLAYLIST_CONTENT_JUNCTION, filter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);
        return recordsArray.length();
    }

    public static List<ContentVersion> fetchContentVersionsForCategory(Category__c category, Context context) {
//        List<ContentDocument> documents = fetchContentDocuments(category);
//        List<String> contentDocumentIds = GuavaUtils.transform(documents, new GuavaUtils.Function<ContentDocument, String>() {
//            public String apply(ContentDocument document) {
//                return document.getId();
//            }
//        });

        List<String> contentDocumentIds = fetchContentIds(category);

        String filter;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean internalMode = prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false);
        if (internalMode) {
            filter = String.format("{ContentVersion:%s} IN ('%s')",
                    DSAConstants.ContentVersionFields.CONTENT_DOCUMENT_ID, Joiner.on("','").join(contentDocumentIds));
        } else {
            filter = String.format("{ContentVersion:%s} IN ('%s') AND {ContentVersion:%s} = 'false'",
                    DSAConstants.ContentVersionFields.CONTENT_DOCUMENT_ID, Joiner.on("','").join(contentDocumentIds), DSAConstants.ContentVersionFields.INTERNAL_DOCUMENT);
        }

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_VERSION, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<ContentVersion> contentVersions = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                contentVersions.add(contentVersion);
            }
            Log.d(TAG, "ContentVersions length:" + contentVersions.size());
            return contentVersions;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting content versions", e);
            return null;
        }
    }

    public static List<Attachment> fetchAttachmentsForCategory(Category__c category) {
        String filter = String.format("{Attachment:%s} = '%s'",
                DSAConstants.AttachmentFields.PARENT_ID, category.getId());
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.ATTACHMENT, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<Attachment> attachments = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Attachment attachment = new Attachment(jsonObject);
                attachments.add(attachment);
            }
            Log.d(TAG, "Attachments length:" + attachments.size());
            return attachments;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting content versions", e);
            return null;
        }
    }

    public static List<Contact> fetchContacts() {
        String smartSql = String.format("SELECT {%1$s:_soup} FROM {%1$s}", DSAConstants.DSAObjects.CONTACT);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<Contact> contacts = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Contact contact = new Contact(jsonObject);
                contacts.add(contact);
            }
            Log.d(TAG, "Contacts length:" + contacts.size());
            return contacts;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting content versions", e);
            return null;
        }
    }

    public static List<Contact> fetchContactsForIds(List<String> contactIds) {
        List<Contact> contacts = new LinkedList<>();
        for (String contactId : contactIds) {
            String smartSqlFilter = String.format("{Contact:%s} = '%s'", SyncEngineConstants.StdFields.ID, contactId);
            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTACT, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            try {
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                    Contact contact = new Contact(jsonObject);
                    contacts.add(contact);
                }
                Log.d(TAG, "Contacts length:" + contacts.size());
            } catch (Exception e) {
                Log.e(TAG, "Exception in getting recent contacts", e);
            }
        }
        return contacts;
    }

    public static List<ContentVersion> fetchContentForIds(List<String> historyIds) {
        List<ContentVersion> historyItems = new LinkedList<>();
        for (String contentId : historyIds) {
            String smartSqlFilter = String.format("{ContentVersion:%s} = '%s'", SyncEngineConstants.StdFields.ID, contentId);
            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_VERSION, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            try {
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                    ContentVersion contentVersion = new ContentVersion(jsonObject);
                    historyItems.add(contentVersion);
                }
                Log.d(TAG, "History items length:" + historyItems.size());
            } catch (Exception e) {
                Log.e(TAG, "Exception in getting history items", e);
            }
        }
        return historyItems;
    }

    public static List<ContentVersion> searchContentForText(String queryString, Context context) {
        List<ContentVersion> searchResults = new LinkedList<>();
        String smartSqlFilter;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean internalMode = prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false);
        if (internalMode) {
            smartSqlFilter = String.format("{ContentVersion:%s} LIKE '%%%s%%' OR {ContentVersion:%s} LIKe '%%%s%%'",
                    DSAConstants.ContentVersionFields.TITLE, queryString,
                    DSAConstants.ContentVersionFields.TAGS_CSV, queryString);
        } else {
            smartSqlFilter = String.format("({ContentVersion:%s} LIKE '%%%s%%' OR {ContentVersion:%s} LIKe '%%%s%%') AND {ContentVersion:%s} = 'false'",
                    DSAConstants.ContentVersionFields.TITLE, queryString,
                    DSAConstants.ContentVersionFields.TAGS_CSV, queryString,
                    DSAConstants.ContentVersionFields.INTERNAL_DOCUMENT);

        }

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_VERSION, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                searchResults.add(contentVersion);
            }
            Log.d(TAG, "Search results length:" + searchResults.size());
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting search results", e);
        }
        return searchResults;
    }

    public static List<DSA_Playlist__c> fetchAllPlayLists() {
        String smartSql = String.format("SELECT {%1$s:_soup} FROM {%1$s}", DSAConstants.DSAObjects.DSA_PLAYLIST);
        smartSql = smartSql.concat(String.format(" ORDER BY {%1$s:%2$s}, {%3$s:%4$s}",
                DSAConstants.DSAObjects.DSA_PLAYLIST, DSAConstants.PlaylistFields.IS_FEATURED,
                DSAConstants.DSAObjects.DSA_PLAYLIST, DSAConstants.PlaylistFields.NAME));
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<DSA_Playlist__c> playlists = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                DSA_Playlist__c playlist = new DSA_Playlist__c(jsonObject);
                playlists.add(playlist);
            }
            Log.d(TAG, "Playlists length:" + playlists.size());
            return playlists;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting playlists", e);
            return null;
        }
    }

    public static List<SFBaseObject> fetchAllPlayListsJunctionRecords() {
        String smartSql = String.format("SELECT {%1$s:_soup} FROM {%1$s}", DSAConstants.DSAObjects.PLAYLIST_CONTENT_JUNCTION);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<SFBaseObject> playlistsJunctionRecords = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                SFBaseObject playlistJunctionRecord = new SFBaseObject(DSAConstants.DSAObjects.PLAYLIST_CONTENT_JUNCTION, jsonObject) {
                };
                playlistsJunctionRecords.add(playlistJunctionRecord);
            }
            Log.d(TAG, "Playlist junctions length:" + playlistsJunctionRecords.size());
            return playlistsJunctionRecords;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting playlist junctions", e);
            return null;
        }
    }

    public static List<String> fetchPlaylistForContentVersion(ContentVersion contentVersion) {
        String filter = String.format("{Playlist_Content_Junction__c:%s} = '%s'", DSAConstants.PlaylistContentJunctionFields.CONTENT_ID, contentVersion.getContentDocumentId());
        String smartSQL = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.PLAYLIST_CONTENT_JUNCTION, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);
        List<String> playlistsIds = new ArrayList<>();
        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                JSONObject record = recordsArray.getJSONArray(i).getJSONObject(0);
                playlistsIds.add(record.getString(DSAConstants.PlaylistContentJunctionFields.PLAYLIST_ID));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return playlistsIds;
    }

    public static List<PlaylistViewModel> fetchPlayListModelsForAllPlaylists(Context context) {

        // fetch all playlists
        List<DSA_Playlist__c> playlists = fetchAllPlayLists();

        List<PlaylistViewModel> playlistViewModels = new ArrayList<>();

        Comparator<ContentVersion> versionComparator = new Comparator<ContentVersion>() {
            @Override
            public int compare(ContentVersion lhs, ContentVersion rhs) {
                if (lhs.getTitle() != null && lhs.getTitle() != null) {
                    return lhs.getTitle()
                            .compareToIgnoreCase(rhs.getTitle());
                }

                return 0;
            }
        };

        for (DSA_Playlist__c playlist : playlists) {

            // create playlist model for each playlist
            // and set content version ids
            PlaylistViewModel playlistViewModel = new PlaylistViewModel(playlist);
            playlistViewModels.add(playlistViewModel);

            // get all playlist junction records where junction.playlist__c = playlistId
            String smartSqlFilter = String.format("{Playlist_Content_Junction__c:%s} = '%s'", "Playlist__c", playlist.getId());
            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.PLAYLIST_CONTENT_JUNCTION, smartSqlFilter);
            JSONArray junctionRecordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            List<Playlist_Content_Junction__c> playlistJunctionRecords = new ArrayList<>();

            try {
                for (int i = 0; i < junctionRecordsArray.length(); i++) {
                    JSONObject jsonObject = junctionRecordsArray.getJSONArray(i).getJSONObject(0);
                    Playlist_Content_Junction__c playlistJunctionRecord = new Playlist_Content_Junction__c(jsonObject);
                    playlistJunctionRecords.add(playlistJunctionRecord);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in getting playlist junctions", e);
                return null;
            }

            // iterate through playlist junctions and get contentDocument ids
            // get ContentVersion for each contentdocumnetid
            for (Playlist_Content_Junction__c playlistJunction : playlistJunctionRecords) {
                try {
                    String contentDocumentId = playlistJunction.toJson().getString("ContentId__c");
                    if (contentDocumentId != null) {
                        String contentVersionFilter = String.format("{ContentVersion:%s} = '%s'", "ContentDocumentId", contentDocumentId);
                        String contentVersionSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_VERSION, contentVersionFilter);
                        JSONArray contentVersionRecords = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(contentVersionSql);
                        try {
                            if (contentVersionRecords != null
                                    && contentVersionRecords.optJSONArray(0) != null
                                    && contentVersionRecords.getJSONArray(0).getJSONObject(0) != null) {
                                JSONObject jsonObject = contentVersionRecords.getJSONArray(0).getJSONObject(0);
                                ContentVersion contentVersion = new ContentVersion(jsonObject);

                                playlistJunction.setContentVersion(contentVersion);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Exception in getting playlist content versions", e);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception in getting playlist content versions", e);
                }
            }

            Collections.sort(playlistJunctionRecords, new Comparator<Playlist_Content_Junction__c>() {

                @Override
                public int compare(Playlist_Content_Junction__c lhs, Playlist_Content_Junction__c rhs) {
                    int lhsOrder = lhs.getOrder();
                    int rhsOrder = rhs.getOrder();
                    if (lhsOrder == rhsOrder) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        try {
                            Date leftDate = sdf.parse(lhs.getContentVersion()
                                    .getContentModifiedDate());
                            Date rightDate = sdf.parse(rhs.getContentVersion()
                                    .getContentModifiedDate());
                            return leftDate.compareTo(rightDate);
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                    return lhsOrder - rhsOrder;
                }
            });

            // check if the user is in internal mode and add them to the model accordingly
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean internalMode = prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false);

            for (Playlist_Content_Junction__c playlistJunction : playlistJunctionRecords) {
                boolean internalDocument = Boolean.valueOf(playlistJunction.getContentVersion().getInternal_Document__c());
                if (internalMode || !internalDocument) {
                    playlistViewModel.getContent().add(playlistJunction);
                }
            }

        }
        return playlistViewModels;
    }

    public static List<Playlist_Content_Junction__c> fetchPrivateContentVersions(Context context) {

        String filter;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean internalMode = prefs.getBoolean(DSAConstants.Constants.INTERNAL_MODE, false);
        if (internalMode) {
            filter = String.format("{ContentVersion:%s} = '%s'",
                    DSAConstants.ContentVersionFields.PUBLISH_STATUS,
                    DSAConstants.Constants.PRIVATE_FILE_PUBLISH_STATUS);
        } else {
            filter = String.format("{ContentVersion:%s} IN ('%s') AND {ContentVersion:%s} = 'false'",
                    DSAConstants.ContentVersionFields.PUBLISH_STATUS,
                    DSAConstants.Constants.PRIVATE_FILE_PUBLISH_STATUS,
                    DSAConstants.ContentVersionFields.INTERNAL_DOCUMENT);
        }

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CONTENT_VERSION, filter);
        smartSql = smartSql.concat(String.format(" ORDER BY {ContentVersion:%s}", DSAConstants.ContentVersionFields.TITLE));
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<Playlist_Content_Junction__c> content = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Playlist_Content_Junction__c junction = new Playlist_Content_Junction__c(new JSONObject());
                ContentVersion contentVersion = new ContentVersion(jsonObject);
                junction.setContentVersion(contentVersion);
                content.add(junction);
            }
            Log.d(TAG, "PrivateContentVersions length:" + content.size());
            return content;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting content versions", e);
            return null;
        }
    }

    public static List<com.abinbev.dsa.model.CN_DSA_Azure_File__c> fetchAllDsaFileForCatetories(String parentCategoryId) {
        String filter = String.format("select {%1$s:_soup} from {%1$s} where {%1$s:CN_DSA_Folder__c}= '%2$s'",
                CN_DSA_Azure_File__c, parentCategoryId);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(filter);
        ArrayList<com.abinbev.dsa.model.CN_DSA_Azure_File__c> dsaFiles = new ArrayList<>();
        DateUtils.ComparatorDate comparatorDate = new DateUtils.ComparatorDate();
        String date = DateUtils.getTimeZoneDate("+8:00", DateUtils.SERVER_DATE_FORMAT);
        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                com.abinbev.dsa.model.CN_DSA_Azure_File__c cn_DSA_azure_filec = new CN_DSA_Azure_File__c(jsonObject);
                int result = comparatorDate.compare(date, cn_DSA_azure_filec.getFileExpireDate());
                if (result != -1) dsaFiles.add(cn_DSA_azure_filec);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dsaFiles;
    }

    public static void checkAccessPermisson(List dsaFile, CheckAccessFilePermissionTask.OnCheckedCallBack onCheckedCallBack, Class type) {
        List<String> dsaFileIds = new ArrayList<>();
        for (Object object : dsaFile) {
            if (object instanceof CN_DSA__c) {
                dsaFileIds.add(((CN_DSA__c) object).getId());
            } else if (object instanceof CN_DSA_Folder__c) {
                dsaFileIds.add(((CN_DSA_Folder__c) object).getId());
            } else if (object instanceof com.abinbev.dsa.model.CN_DSA_Azure_File__c) {
                dsaFileIds.add(((com.abinbev.dsa.model.CN_DSA_Azure_File__c) object).getId());
            }

        }
        String filter = String.format("select {CN_Share__c:%1$s} from {CN_Share__c} where {CN_Share__c:%1$s} IN ('%2$s')",
                DSAConstants.CNShareFields.CN_Parent_ID__c, Joiner.on("','").join(dsaFileIds));
        CheckAccessFilePermissionTask<SFBaseObject> checkAccessFilePermissionTask = new CheckAccessFilePermissionTask(onCheckedCallBack, type);
        checkAccessFilePermissionTask.execute(filter);
    }
}
