package com.salesforce.androidsyncengine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.LruCache;

import com.google.gson.Gson;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.SyncDirection;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple util class to make all key usage consistent
 * @author usanaga
 */
public class PreferenceUtils {

	private static final String OBJECT_KEY_FORMAT = "%s__%s"; // ex: Conatct__query OR Contact__local_fields
	private static final String QUERY_KEY_SUFFIX = "query";
	private static final String LOCAL_FIELDS_KEY_SUFFIX = "local_fields";
	private static final String LAST_REFRESH_TIME_KEY_SUFFIX = "last_refresh_time";
	private static final String PREVIOUS_REFRESH_TIME_KEY_SUFFIX = "previous_refresh_time_content";
	private static final String LAST_GET_DELETED_TIME_KEY_SUFFIX = "last_get_deleted_time";
	private static final String REPLICABLE_KEY_SUFFIX = "object_replicable";
	private static final String BINARY_FIELD_KEY_SUFFIX = "binary_field";
	private static final String FILE_NAME_FIELD_KEY_SUFFIX = "file_name";
	private static final String FILE_TYPE_FIELD_KEY_SUFFIX = "file_type";
	private static final String FILE_SIZE_FIELD_KEY_SUFFIX = "file_size";
	private static final String REQUIRED_FILES_FILTERS_SUFFIX = "required_files_filters";
	private static final String ADDITIONAL_FILES_FILTERS_SUFFIX = "additional_files_filters";
	private static final String FIELD_RELATIONSHIP_NAME_SUFFIX = "field_relationship_name";
	private static final String FIELD_RELATIONSHIP_VALUE_SUFFIX = "field_relationship_value";
	private static final String SHOULD_CHECK_FOR_DELETED_SUFFIX = "should_check_for_deleted";
	private static final String PURGE_ENABLED_KEY_SUFFIX = "purge_enabled";
	private static final String FETCH_LAYOUT_METADATA_KEY_SUFFIX = "fetch_layout_metadata";
	private static final String FETCH_COMPACT_LAYOUT_METADATA_KEY_SUFFIX = "fetch_compact_layout_metadata";
	private static final String DATE_QUERY_KEY_SUFFIX = "date_query";
	private static final String USE_NAMESPACE_KEY_SUFFIX = "use_namespace";
	private static final String DEPENDENCIES_SUFFIX = "dependencies";

	private static final String SYNC_DIRECTION_SUFFIX = "sync_direction";
	private static final String METADATA_JSON_OBJECT = "metadata_json_object";

	private static final String FULL_SYNC_COMPLETE_KEY = "full_sync";
	private static final String ORDERED_OBJECTS_SET_KEY = "ordered_objects";
	private static final String GROUPS_FOR_BATCH_SYNC_KEY = "groups_for_batch_sync";

	private static final String MANIFEST_VERSION_NUMBER = "manifest_version_number";
	private static final String MANIFEST_LOADING_COMPLETED = "manifest_loading_completed";
	private static final String SYNC_FREQUENCY = "syncFrequency";
	private static final String LAST_PURGE_TIME = "last_purge_time";
	private static final String PURGE_FREQUENCY = "purge_frequency";
	private static final String SYNC_ON_WIFI = "sync_on_wifi";
	private static final String SYNC_ON_4G = "sync_on_4g";
	private static final String SYNC_ON_3G = "sync_on_3g";
	private static final String SYNC_ON_LTE = "sync_on_lte";
	private static final String SYNC_ON_EDGE = "sync_on_edge";
	private static final String NAMESPACE_PREFIX = "namespace_prefix";
	
	private static final String CLIENT_APP_VERSION = "client_app_version";

	private static final String FIRST_LAUNCH_KEY = "first_launch";
	private static final String LOGGED_IN_KEY = "user_logged_in";

	private static final String TRIGGER_DELTA_SYNC_KEY = "trigger_delta_sync";

	private static final String ORG_ID = "org_id";

	private static final String CONFIGURATION = "configuration";

	private static final String LAST_LOCAL_CLEANUP = "last_local_cleanup";
	private static final String LAST_FETCH_DELETED_RECORDS = "last_fetch_deleted_records";

	private static final String UNASSIGNED_STRING = new String();

	private PreferenceUtils() {}

	private static String getKey(String objectName, String suffix) {
		return String.format(OBJECT_KEY_FORMAT, objectName, suffix);
	}

	private static String getQueryKey(String objectName) {
		return getKey(objectName, QUERY_KEY_SUFFIX);
	}

	private static String getDependenciesKey(String objectName) {
		return getKey(objectName, DEPENDENCIES_SUFFIX);
	}

	private static String getRequiredFilesFiltersKey(String objectName) {
		return getKey(objectName, REQUIRED_FILES_FILTERS_SUFFIX);
	}

	private static String getAdditionalFilesFiltersKey(String objectName) {
		return getKey(objectName, ADDITIONAL_FILES_FILTERS_SUFFIX);
	}

	private static String getMetadataJsonObjectKey(String objectName) {
		return getKey(objectName, METADATA_JSON_OBJECT);
	}

	private static String getLocalFieldsKey(String objectName) {
		return getKey(objectName, LOCAL_FIELDS_KEY_SUFFIX);
	}
	
	private static String getBinaryFieldsKey(String objectName) {
		return getKey(objectName, BINARY_FIELD_KEY_SUFFIX);
	}
	
	private static String getFileNameFieldKey(String objectName) {
		return getKey(objectName, FILE_NAME_FIELD_KEY_SUFFIX);
	}
	
	private static String getFileTypeFieldKey(String objectName) {
		return getKey(objectName, FILE_TYPE_FIELD_KEY_SUFFIX);
	}
	
	private static String getFileSizeFieldKey(String objectName) {
		return getKey(objectName, FILE_SIZE_FIELD_KEY_SUFFIX);
	}
	
	private static String getFieldRelationshipNameKey(String objectName) {
		return getKey(objectName, FIELD_RELATIONSHIP_NAME_SUFFIX);
	}
	
	private static String getFieldrelationshipValueKey(String objectName) {
		return getKey(objectName, FIELD_RELATIONSHIP_VALUE_SUFFIX);
	}

	public static String getLastRefreshTimeKey(String objectName) {
		return getKey(objectName, LAST_REFRESH_TIME_KEY_SUFFIX);
	}

	public static String getPreviousRefreshTimeKey(String objectName) {
		return getKey(objectName, PREVIOUS_REFRESH_TIME_KEY_SUFFIX);
	}

	private static String getLastGetDeletedTimeKey(String objectName) {
		return getKey(objectName, LAST_GET_DELETED_TIME_KEY_SUFFIX);
	}
	
	private static String getReplicableKey(String objectName) {
		return getKey(objectName, REPLICABLE_KEY_SUFFIX);
	}
	
	private static String getCheckDeletedKey(String objectName) {
		return getKey(objectName, SHOULD_CHECK_FOR_DELETED_SUFFIX);
	}
	
	private static String getPurgeEnabledKey(String objectName) {
		return getKey(objectName, PURGE_ENABLED_KEY_SUFFIX);
	}
	
	private static String getSyncDirectionKey(String objectName) {
		return getKey(objectName, SYNC_DIRECTION_SUFFIX);
	}

	private static String getFetchLayoutMetadataKey(String objectName) {
		return getKey(objectName, FETCH_LAYOUT_METADATA_KEY_SUFFIX);
	}

	private static String getFetchCompactLayoutMetadataKey(String objectName) {
		return getKey(objectName, FETCH_COMPACT_LAYOUT_METADATA_KEY_SUFFIX);
	}

	private static String getDateQueryKey(String objectName) {
		return getKey(objectName, DATE_QUERY_KEY_SUFFIX);
	}

	private static String getUseNamespaceKey(String objectName) {
		return getKey(objectName, USE_NAMESPACE_KEY_SUFFIX);
	}

	@SuppressWarnings("unchecked")
	private static void putValueInPrefs(String key, Object value, Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		Editor editor = prefs.edit();
		if (value != null) {
			if (value instanceof String) {
				editor.putString(key, (String)value);
			} else if (value instanceof Set) {
				editor.putStringSet(key, (Set<String>)value);
			} else if (value instanceof Float) {
				editor.putFloat(key, (Float)value);
			} else if (value instanceof Boolean) {
				editor.putBoolean(key, (Boolean)value);
			} else if (value instanceof Integer) {
				editor.putInt(key, (Integer) value);
			}  else if (value instanceof Long) {
				editor.putLong(key, (Long) value);
			} else if (value instanceof List) {
				editor.putString(key, Joiner.on(',').join((List<String>) value));
			}
		}
		editor.commit();
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getValueFromPrefs(String key, Class<T> type, T defValue, Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		
		if (type == String.class) {
			return (T) prefs.getString(key, (String) defValue);
		} else if (type == Set.class) {
			return (T) prefs.getStringSet(key, (Set<String>) defValue);
		} else if (type == Boolean.class) {
			return (T) Boolean.valueOf(prefs.getBoolean(key, (Boolean) defValue));
		} else if (type == Float.class) {
			return (T) Float.valueOf(prefs.getFloat(key, (Float) defValue));
		} else if (type == Integer.class) {
			return (T) Integer.valueOf(prefs.getInt(key, (Integer) defValue));
		} else if (type == Long.class) {
			return (T) Long.valueOf(prefs.getLong(key, (Long) defValue));
		} else if (type == List.class) {
			return (T) Arrays.asList(prefs.getString(key, "").split(","));
		}
		
		return defValue;
	}
	
	public static void removeValue(String key, Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}

	private static SharedPreferences sharedPreferences;

	public static SharedPreferences getSharedPreferences(Context context) {
		if (sharedPreferences == null) {
			synchronized (PreferenceUtils.class) {
				if (sharedPreferences == null) {
					sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				}
			}
		}
		return sharedPreferences;
	}

	public static void putSortedObjects(List<String> value, Context context) {
		putValueInPrefs(ORDERED_OBJECTS_SET_KEY, value, context);
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> getSortedObjects(Context context) {
		return getValueFromPrefs(ORDERED_OBJECTS_SET_KEY, List.class, null, context);
	}

	public static void putLocalFields(String objectName, Set<String> value, Context context) {
		putValueInPrefs(getLocalFieldsKey(objectName), value, context);
	}

	@SuppressWarnings("unchecked")
	public static Set<String> getLocalFields(String objectName, Context context) {
		return getValueFromPrefs(getLocalFieldsKey(objectName), Set.class, null, context);
	}

	public static void putDependencies(String objectName, Set<String> value, Context context) {
		putValueInPrefs(getDependenciesKey(objectName), value, context);
	}

	@SuppressWarnings("unchecked")
	public static Set<String> getDependencies(String objectName, Context context) {
		return getValueFromPrefs(getDependenciesKey(objectName), Set.class, null, context);
	}
	
	public static void putManifestVersion(float value, Context context) {
		putValueInPrefs(MANIFEST_VERSION_NUMBER, value, context);
	}
	
	public static float getManifestVersion(Context context) {
		return getValueFromPrefs(MANIFEST_VERSION_NUMBER, Float.class, Float.valueOf(0), context);
	}
	
	public static void putSyncFrequency(int value, Context context) {
		putValueInPrefs(SYNC_FREQUENCY, value, context);
	}
	
	public static int getSyncFrequency(Context context) {
		return getValueFromPrefs(SYNC_FREQUENCY, Integer.class, Integer.valueOf(0), context);
	}
	
	public static void put3gSync(boolean value, Context context) {
		putValueInPrefs(SYNC_ON_3G, value, context);
	}
	
	public static boolean get3gSync(Context context) {
		return getValueFromPrefs(SYNC_ON_3G, Boolean.class, true, context);
	}
	
	public static void put4gSync(boolean value, Context context) {
		putValueInPrefs(SYNC_ON_4G, value, context);
	}
	
	public static boolean get4gSync(Context context) {
		return getValueFromPrefs(SYNC_ON_4G, Boolean.class, true, context);
	}
	
	public static void putWifiSync(boolean value, Context context) {
		putValueInPrefs(SYNC_ON_WIFI, value, context);
	}
	
	public static boolean getWifiSync(Context context) {
		return getValueFromPrefs(SYNC_ON_WIFI, Boolean.class, true, context);
	}
	
	public static void putLteSync(boolean value, Context context) {
		putValueInPrefs(SYNC_ON_LTE, value, context);
	}
	
	public static boolean getLteSync(Context context) {
		return getValueFromPrefs(SYNC_ON_LTE, Boolean.class, true, context);
	}
	
	public static void putEdgeSync(boolean value, Context context) {
		putValueInPrefs(SYNC_ON_EDGE, value, context);
	}
	
	public static boolean getEdgeSync(Context context) {
		return getValueFromPrefs(SYNC_ON_EDGE, Boolean.class, true, context);
	}

	private static String namespacePrefixCache = UNASSIGNED_STRING;
	
	public static String getNamespacePrefix(Context context) {
		if (namespacePrefixCache == UNASSIGNED_STRING) {
			namespacePrefixCache = getValueFromPrefs(NAMESPACE_PREFIX, String.class, null, context);
		}
		return namespacePrefixCache;
	}
	
	public static void putNamespacePrefix(String value, Context context) {
		namespacePrefixCache = value;
		putValueInPrefs(NAMESPACE_PREFIX, value, context);
	}
	
	public static void putFullSyncComplete(boolean value, Context context) {
		putValueInPrefs(FULL_SYNC_COMPLETE_KEY, value, context);
	}
	
	public static boolean getFullSyncComplete(Context context) {
		return getValueFromPrefs(FULL_SYNC_COMPLETE_KEY, Boolean.class, false, context);
	}
	
	public static void putFirstLaunchComplete(boolean value, Context context) {
		putValueInPrefs(FIRST_LAUNCH_KEY, value, context);
	}
	
	public static boolean getFirstLaunchComplete(Context context) {
		return getValueFromPrefs(FIRST_LAUNCH_KEY, Boolean.class, false, context);
	}

	public static void putTriggerDeltaSync(boolean value, Context context) {
		putValueInPrefs(TRIGGER_DELTA_SYNC_KEY, value, context);
	}

	public static boolean getTriggerDeltaSync(Context context) {
		return getValueFromPrefs(TRIGGER_DELTA_SYNC_KEY, Boolean.class, true, context);
	}
	
	public static void putLoggedIn(boolean value, Context context) {
		putValueInPrefs(LOGGED_IN_KEY, value, context);
	}
	
	public static boolean getLoggedIn(Context context) {
		return getValueFromPrefs(LOGGED_IN_KEY, Boolean.class, true, context);
	}
	
	public static void putOrgId(String value, Context context) {
		putValueInPrefs(ORG_ID, value, context);
	}
	
	public static String getOrgId(Context context) {
		return getValueFromPrefs(ORG_ID, String.class, null, context);
	}
	
	public static void putLastRefreshTime(String objectName, String value, Context context) {
		putValueInPrefs(getLastRefreshTimeKey(objectName), value, context);
	}
	
	public static String getLastRefreshTime(String objectName, Context context) {
		return getValueFromPrefs(getLastRefreshTimeKey(objectName), String.class, null, context);
	}

	public static void putPreviousRefreshTime(String objectName, String value, Context context) {
		putValueInPrefs(getPreviousRefreshTimeKey(objectName), value, context);
	}

	public static String getPreviousRefreshTime(String objectName, Context context) {
		return getValueFromPrefs(getPreviousRefreshTimeKey(objectName), String.class, null, context);
	}
	
	public static void putLastDeletedTime(String objectName, String value, Context context) {
		putValueInPrefs(getLastGetDeletedTimeKey(objectName), value, context);
	}
	
	public static String getLastDeletedTime(String objectName, Context context) {
		return getValueFromPrefs(getLastGetDeletedTimeKey(objectName), String.class, null, context);
	}
	
	public static void putLastPurgeTime(long value, Context context) {
		putValueInPrefs(LAST_PURGE_TIME, value, context);
	}
	
	public static long getLastPurgeTime(Context context) {
		return getValueFromPrefs(LAST_PURGE_TIME, Long.class, Long.valueOf(0), context);
	}
	
	public static void putPurgeFrequency(long value, Context context) {
		putValueInPrefs(PURGE_FREQUENCY, value, context);
	}
	
	public static long getPurgeFrequency(Context context) {
		return getValueFromPrefs(PURGE_FREQUENCY, Long.class, Long.valueOf(0), context);
	}
	
	public static void putClientAppVersion(int value, Context context) {
		putValueInPrefs(CLIENT_APP_VERSION, value, context);
	}
	
	public static int getClientAppVersion(Context context) {
		return getValueFromPrefs(CLIENT_APP_VERSION, Integer.class, 0, context);
	}
	
	public static void putReplicable(String objectName, boolean value, Context context) {
		putValueInPrefs(getReplicableKey(objectName), value, context);
	}
	
	public static boolean getReplicable(String objectName, Context context) {
		return getValueFromPrefs(getReplicableKey(objectName), Boolean.class, false, context);
	}
	
	public static void putBinaryField(String objectName, String value, Context context) {
		putValueInPrefs(getBinaryFieldsKey(objectName), value, context);
	}
	
	public static String getBinaryField(String objectName, Context context) {
		return getValueFromPrefs(getBinaryFieldsKey(objectName), String.class, null, context);
	}
	
	public static void putNameFiled(String objectName, String value, Context context) {
		putValueInPrefs(getFileNameFieldKey(objectName), value, context);
	}
	
	public static String getNameField(String objectName, Context context) {
		return getValueFromPrefs(getFileNameFieldKey(objectName), String.class, null, context);
	}

	public static void putTypeField(String objectName, String value, Context context) {
		putValueInPrefs(getFileTypeFieldKey(objectName), value, context);
	}

	public static String getTypeField(String objectName, Context context) {
		return getValueFromPrefs(getFileTypeFieldKey(objectName), String.class, null, context);
	}

	public static void putRequiredFilesFilters(String objectName, String[] filters, Context context) {
		HashSet<String> set = null;
		if (filters != null) {
			set = new HashSet<String>();
			Collections.addAll(set, filters);
		}
		putValueInPrefs(getRequiredFilesFiltersKey(objectName), set, context);
	}

	public static Set<String> getRequiredFilesFilters(String objectName, Context context) {
		return (Set<String>) getValueFromPrefs(getRequiredFilesFiltersKey(objectName), Set.class, null, context);
	}

	public static void putAdditionalFilesFilters(String objectName, String[] filters, Context context) {
		HashSet<String> set = null;
		if (filters != null) {
			set = new HashSet<String>();
			Collections.addAll(set, filters);
		}
		putValueInPrefs(getAdditionalFilesFiltersKey(objectName), set, context);
	}

	public static Set<String> getAdditionalFilesFilters(String objectName, Context context) {
		return (Set<String>) getValueFromPrefs(getAdditionalFilesFiltersKey(objectName), Set.class, null, context);
	}
	
	public static void putSizeField(String objectName, String value, Context context) {
		putValueInPrefs(getFileSizeFieldKey(objectName), value, context);
	}
	
	public static String geSizeField(String objectName, Context context) {
		return getValueFromPrefs(getFileSizeFieldKey(objectName), String.class, null, context);
	}
	
	public static void putFieldRelationFieldName(String objectName, String value, Context context) {
		putValueInPrefs(getFieldRelationshipNameKey(objectName), value, context);
	}
	
	public static String getFieldRelationFieldName(String objectName, Context context) {
		return getValueFromPrefs(getFieldRelationshipNameKey(objectName), String.class, null, context);
	}
	
	public static void putFieldRelationFieldValue(String objectName, String value, Context context) {
		putValueInPrefs(getFieldrelationshipValueKey(objectName), value, context);
	}
	
	public static String getFieldRelationFieldValue(String objectName, Context context) {
		return getValueFromPrefs(getFieldrelationshipValueKey(objectName), String.class, null, context);
	}

	public static void putManifestLoaded(boolean value, Context context) {
		putValueInPrefs(MANIFEST_LOADING_COMPLETED, value, context);
	}

	public static boolean getManifestLoaded(Context context) {
		return getValueFromPrefs(MANIFEST_LOADING_COMPLETED, Boolean.class, false, context);
	}

	public static void putLastLocalCleanup(long value, Context context) {
		putValueInPrefs(LAST_LOCAL_CLEANUP, value, context);
	}

	public static long getLastLocalCleanup(Context context) {
		return getValueFromPrefs(LAST_LOCAL_CLEANUP, Long.class, 0L, context);
	}

	public static void putLastFetchDeletedRecords(long value, Context context) {
		putValueInPrefs(LAST_FETCH_DELETED_RECORDS, value, context);
	}

	public static long getLastFetchDeletedRecords(Context context) {
		return getValueFromPrefs(LAST_FETCH_DELETED_RECORDS, Long.class, 0L, context);
	}
	
	public static boolean containsBinaryField(String objectName, Context context) {
		String binaryField = getBinaryField(objectName, context);
		if (binaryField != null && !binaryField.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean getCheckForDeleted(String objectName, Context context) {
		return getValueFromPrefs(getCheckDeletedKey(objectName), Boolean.class, true, context);
	}
	
	public static void putCheckForDeleted(String objectName, boolean value, Context context) {
		putValueInPrefs(getCheckDeletedKey(objectName), value, context);
	}
	
	public static SyncDirection getSyncDirection(String objectName, Context context) {
		String valueFromPrefs = getValueFromPrefs(getSyncDirectionKey(objectName), String.class, null, context);
		
		return SyncDirection.valueOf(valueFromPrefs);
	}
	
	public static void putSyncDirection(String objectName, SyncDirection value, Context context) {
		putValueInPrefs(getSyncDirectionKey(objectName), value.name(), context);
	}
	
	public static boolean getPurgeEnabled(String objectName, Context context) {
		return getValueFromPrefs(getPurgeEnabledKey(objectName), Boolean.class, true, context);
	}
	
	public static void putPurgeEnabled(String objectName, boolean value, Context context) {
		putValueInPrefs(getPurgeEnabledKey(objectName), value, context);
	}

	public static boolean getShouldFetchLayoutMetadata(String objectName, Context context) {
		return getValueFromPrefs(getFetchLayoutMetadataKey(objectName), Boolean.class, false, context);
	}

	public static void putShouldFetchLayoutMetadata(String objectName, boolean value, Context context) {
		putValueInPrefs(getFetchLayoutMetadataKey(objectName), value, context);
	}

	public static boolean getShouldFetchCompactLayoutMetadata(String objectName, Context context) {
		return getValueFromPrefs(getFetchCompactLayoutMetadataKey(objectName), Boolean.class, false, context);
	}

	public static void putShouldFetchCompactLayoutMetadata(String objectName, boolean value, Context context) {
		putValueInPrefs(getFetchCompactLayoutMetadataKey(objectName), value, context);
	}

	public static boolean getIsDateQuery(String objectName, Context context) {
		return getValueFromPrefs(getDateQueryKey(objectName), Boolean.class, false, context);
	}

	public static void putIsDateQuery(String objectName, boolean value, Context context) {
		putValueInPrefs(getDateQueryKey(objectName), value, context);
	}

	public static boolean getShouldUseNamespace(String objectName, Context context) {
		return getUseNamespaceCache(context).get(objectName);
	}

	public static void putShouldUseNamespace(String objectName, boolean value, Context context) {
		putValueInPrefs(getUseNamespaceKey(objectName), value, context);
		getUseNamespaceCache(context).remove(objectName);
	}

	private static ObjectMetadataCache METADATA_CACHE;

	private static final Object METADATA_CACHE_LOCK = new Object();

	private static ObjectMetadataCache getMetadataCache(Context context) {
		if (METADATA_CACHE == null) {
			synchronized (METADATA_CACHE_LOCK) {
				if (METADATA_CACHE == null) {
					METADATA_CACHE = new ObjectMetadataCache(context.getApplicationContext());
				}
			}
		}

		return METADATA_CACHE;
	}

	private static ShouldUseNamespaceCache USE_NAMESPACE_CACHE;

	private static final Object USE_NAMESPACE_CACHE_LOCK = new Object();

	private static ShouldUseNamespaceCache getUseNamespaceCache(Context context) {
		if (USE_NAMESPACE_CACHE == null) {
			synchronized (USE_NAMESPACE_CACHE_LOCK) {
				if (USE_NAMESPACE_CACHE == null) {
					USE_NAMESPACE_CACHE = new ShouldUseNamespaceCache(context.getApplicationContext());
				}
			}
		}

		return USE_NAMESPACE_CACHE;

	}

	public static void putMetadataObject(String objectName, SFObjectMetadata metadataObj, Context context) {
		Gson gson = new Gson();
		String metadataObjectString = gson.toJson(metadataObj);
		putValueInPrefs(getMetadataJsonObjectKey(objectName), metadataObjectString, context);
		getMetadataCache(context).remove(objectName);	// Remove object from cache.
	}

	public static SFObjectMetadata getMetadataObject(String objectName, Context context) {
		SFObjectMetadata result = getMetadataCache(context).get(objectName);
		return result == null ? null : new SFObjectMetadata(result); //Make shallow copy to make sure that it won't be changed.
	}

	public static void putGroupsForBatchSync(List<List<String>> groups, Context context) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(groups);
		putValueInPrefs(GROUPS_FOR_BATCH_SYNC_KEY, jsonString, context);
	}

	public static List<List<String>> getGroupsForBatchSync(Context context) {
		String objectString = getValueFromPrefs(GROUPS_FOR_BATCH_SYNC_KEY, String.class, null, context);
		Gson gson = new Gson();
		return gson.fromJson(objectString, List.class);
	}

	public static void putConfiguration(Configuration conf, Context context) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(conf);
		putValueInPrefs(CONFIGURATION, jsonString, context);
	}

	public static Configuration getConfiguration(Context context) {
		String objectString = getValueFromPrefs(CONFIGURATION, String.class, null, context);
		Gson gson = new Gson();
		return gson.fromJson(objectString, Configuration.class);
	}

	static class ObjectMetadataCache extends LruCache<String, SFObjectMetadata> {

		static final int METADATA_CACHE_SIZE = 3;	// Keep max 3 SFObjectMetadata objects.

		final Context context;

		public ObjectMetadataCache(Context context) {
			super(METADATA_CACHE_SIZE);
			this.context = context;
		}

		@Override
		protected int sizeOf(String key, SFObjectMetadata value) {
			return 1; // Size of each element is 1.
		}

		@Override
		protected SFObjectMetadata create(String key) {
			String objectString = getValueFromPrefs(getMetadataJsonObjectKey(key), String.class, null, context);
			Gson gson = new Gson();
			SFObjectMetadata metadataObj = gson.fromJson(objectString, SFObjectMetadata.class);

			return metadataObj;
		}
	}

	static class ShouldUseNamespaceCache extends LruCache<String, Boolean> {

		static final int CACHE_SIZE = 3;

		final Context context;

		public ShouldUseNamespaceCache(Context context) {
			super(CACHE_SIZE);
			this.context = context;
		}

		@Override
		protected int sizeOf(String key, Boolean value) {
			return 1; // Size of each element is 1.
		}

		@Override
		protected Boolean create(String key) {
			return getValueFromPrefs(getUseNamespaceKey(key), Boolean.class, false, context);
		}
	}
}
