/*
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.androidsyncengine.datamanager;

import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsdk.smartstore.store.DBOpenHelper;
import com.salesforce.androidsdk.smartstore.store.IndexSpec;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsdk.smartstore.store.SmartStore.Type;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.ErrorObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.model.QueueOperation;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.model.SyncStatus;
import com.salesforce.androidsyncengine.datamanager.queue.RequestQueueStorage;
import com.salesforce.androidsyncengine.datamanager.queue.SqliteRequestQueueStorage;
import com.salesforce.androidsyncengine.services.DownloadIntentService;
import com.salesforce.androidsyncengine.syncmanifest.ConfigObject;
import com.salesforce.androidsyncengine.syncmanifest.FieldToIndex;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.syncmanifest.SyncDirection;
import com.salesforce.androidsyncengine.syncmanifest.SyncManifest;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * {@link DataManager} implementation using smartstore
 * 
 * @author bduggirala
 */
public class SmartStoreDataManagerImpl implements DataManager {

	private static final String FIELD_RELATIONSHIP_SELECT_FORMAT = "SELECT {Queue:_soup} FROM {Queue} " +
			"WHERE {Queue:objectType} == '%s' AND " +
			"{Queue:operation} IN ('UPDATE','CREATE')";

	private final static String TAG = "SmartStoreDataManagerIm";

	private final static String SOUP_ENTRY_ID = "_soupEntryId";

	// SmartStore to save errors
	private static String ERROR_SOUP = "ErrorStore";
	private static IndexSpec[] ERROR_INDEX_SPEC = { new IndexSpec(CONSTANTS_ID, Type.string) };

	private static final String REQUEST_QUEUE_DB_NAME = "sqlite_queue_storage";
	private static final String AZURE_QUEUE_DB_NAME = "sqlite_azure_queue_storage";

	private SmartStoreSDKManager sdkManager;
	private RequestQueueStorage requestQueueStorage;
	private RequestQueueStorage azureQueueStorage;
	private AbstractTempIdContainer tempIdContainer;
	private SmartStore smartStore;
	private Random random;
	private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[A-Z\\-]{3}.*", Pattern.DOTALL);

	SmartStoreDataManagerImpl() {
		sdkManager = SmartStoreSDKManager.getInstance();
		smartStore = sdkManager.getSmartStore();
		random = new Random();

		UserAccount user = sdkManager.getUserAccountManager().getCurrentUser();
		requestQueueStorage = new SqliteRequestQueueStorage(sdkManager.getAppContext(), REQUEST_QUEUE_DB_NAME, user.getUserId(), user.getOrgId());
		azureQueueStorage = new SqliteRequestQueueStorage(sdkManager.getAppContext(), AZURE_QUEUE_DB_NAME, user.getUserId(), user.getOrgId());
		tempIdContainer = new SQLiteTempIdContainer(sdkManager.getAppContext());
	}

	@Override
	public synchronized void init(Context context) {

		// get the current client app version
		int clientPkgVersion = getClientVersion(context);

		// get the saved client version
		int savedClientVersion = PreferenceUtils.getClientAppVersion(context);

		// see if we have a different client version 
		if (savedClientVersion == 0 || savedClientVersion != clientPkgVersion) {

//			context.sendBroadcast(new Intent(DataManager.FETCH_METADATA_STARTED));

			ManifestUtils manifestUtils = ManifestUtils.getInstance(context);
			manifestUtils.invalidateManifest();

			SyncManifest syncManifest = manifestUtils.getManifest();
			// check if manifest version is changed
			// if yes, resave preferences 
			// else return
			float savedManifestVersion = PreferenceUtils.getManifestVersion(context);

			if (savedManifestVersion == 0 || savedManifestVersion != syncManifest.getConfiguration().getManifestVersion()) {

				// Moved Manifest loading logic into SyncEngine
				PreferenceUtils.putManifestVersion(0, context);

//				deleteOldObjectsSoups(context);
//
//				manifestUtils.fetchAndSaveMetadataPrefs();
//
//				// we are doing the reinit since the smartStore for the current user needs to be refetched
//				// if the user has logged out and logged back in.
//				smartStore = sdkManager.getSmartStore();
//
//				setUpNecessaryQueueAndErrorSoups();
//
//				createManifestObjectsSoups(context);
//
//				PreferenceUtils.putClientAppVersion(clientPkgVersion, context);

//				context.sendBroadcast(new Intent(DataManager.FETCH_METADATA_COMPLETED));

				// TODO: Removing this logic so that we can
				// SyncUtils.TriggerRefresh(context);
			} else {
				Log.d(TAG, "Manifest data is already saved and manifest version is not changed");
			}
		} else {
			Log.d(TAG, "Manifest data is already saved and client version is not changed");
		}

	}

	/**
	 * Creates a soup for object
	 *
	 * @param soupName
	 * @param soupIndexSpec
	 */
	protected void createObjectSoup(String soupName, IndexSpec[] soupIndexSpec) {
		smartStore.registerSoup(soupName, soupIndexSpec);
	}

	/**
	 * Creates a soup to queue updates.
	 */
	protected void createQueueSoup() {
		requestQueueStorage.createStorage();
		azureQueueStorage.createStorage();
	}

	/**
	 * Creates a soup to queue updates.
	 */
	protected void createErrorSoup() {
		smartStore.registerSoup(ERROR_SOUP, ERROR_INDEX_SPEC);
	}

	/**
	 * Deletes the existing soup for Object.
	 */
	protected void deleteObjectSoup(String soupName) {
		if (smartStore.hasSoup(soupName)) {
			smartStore.dropSoup(soupName);
		}
	}

	/**
	 * Deletes the existing soup for queue.
	 */
	protected void deleteQueueSoup() {
		requestQueueStorage.deleteStorage();
		azureQueueStorage.deleteStorage();
	}

	/**
	 * Deletes the existing soup for errors.
	 */
	protected void deleteErrorSoup() {
		if (smartStore.hasSoup(ERROR_SOUP)) {
			smartStore.dropSoup(ERROR_SOUP);
		}
	}

	/**
	 * Inserts records into the object soup.
	 *
	 * @param soupName
	 * @param records
	 *            Records
	 * @param firstSync
	 * @param localFields - used to make sure we retain local field data when we sync
	 */
	protected void insertRecords(String soupName, JSONArray records, boolean skipDuplicatesCheck, boolean firstSync, boolean speedSync, Set<String> localFields, Set<String> sensitiveFields) {

		synchronized(smartStore.getDatabase()) {
			try {
				if (records != null) {
					if (firstSync || speedSync) smartStore.beginTransaction();
					for (int i = 0; i < records.length(); i++) {
						//					Log.i(TAG, "inserting record: " + records.getJSONObject(i).toString());
						insertRecord(soupName, records.getJSONObject(i), skipDuplicatesCheck, firstSync, localFields, sensitiveFields);
					}
					if (firstSync || speedSync) {
						smartStore.setTransactionSuccessful();
						smartStore.endTransaction();
					}
				}
			} catch (JSONException e) {
				Log.e(TAG,
						"Error occurred while attempting to insert records. Please verify validity of JSON data set.");
			}
		}
	}

	/**
	 * Inserts a single record into the records soup.
	 *
	 * @param soupName
	 * @param record
	 *            Record
	 * @param firstSync
	 * @param localFields
	 */
	@SuppressWarnings("unchecked")
	private void insertRecord(String soupName, JSONObject record, boolean skipDuplicatesCheck, boolean firstSync, Set<String> localFields, Set<String> sensitiveFields) {
		if (record != null) {

			// Some of the local fields should be copied from local database to the new json object
			// because without that they will be overridden.
			Set<String> fieldsToCopy = new HashSet<>();
			if (localFields != null) {
				fieldsToCopy.addAll(localFields);
			}
			if (sensitiveFields != null) {
				fieldsToCopy.addAll(sensitiveFields);
			}

			try {
				// we don't want this overhead on firstSync
				if (!(skipDuplicatesCheck && firstSync)) {
					String id = record.getString(CONSTANTS_ID);
					if (id != null) {
//						if (existsRecord(soupName, id)) {
							JSONObject existingRecord = exactQuery(soupName, CONSTANTS_ID, id);
							if (existingRecord != null) {

								Long soupEntryId = existingRecord.getLong(SOUP_ENTRY_ID);
								record.put(SOUP_ENTRY_ID, soupEntryId);

								// make sure we copy over localFields
								if (!fieldsToCopy.isEmpty()) {
									for(String localField: fieldsToCopy) {
										Object value = existingRecord.opt(localField);
										if (value != null) {
											record.put(localField, value);
										}
									}
								}
							}
//						}
					}
				}

				// filter namespace prefix from field names if present
				JSONObject filteredRecord = new JSONObject();

				// iterate over record to see if the keys have prefix
				Iterator<String> recordIterator = record.keys();
				while (recordIterator.hasNext()) {
					String key = recordIterator.next();
					Object value = record.get(key);

					// if prefix is present in the key then use a stripped version
					key = ManifestUtils.removeNamespaceFromField(soupName, key, sdkManager.getAppContext());

					// store in new object
					filteredRecord.put(key, value);
				}

				smartStore.upsert(soupName, filteredRecord, SOUP_ENTRY_ID, !firstSync);
			} catch (JSONException exc) {
				Log.e(TAG,
						"Error occurred while attempting to insert record into "
								+ soupName
								+ ". Please verify validity of JSON data set.");
			}
		}
	}

//	private boolean existsRecord(String soupName, String id) {
//		return smartStore.countQuery(QuerySpec.buildExactQuerySpec(soupName, CONSTANTS_ID,
//				id, null, null, 1)) > 0;
//	}

	/*
	 *
	 * Inserting into Queue is different from inserting into other objects. Be
	 * aware of this before you try to do any optimizations
	 */
	protected void insertRecordIntoQueue(QueueObject queueObj) {
		// TODO: if the queueData is empty that is if there are no values that have been changed then we should not add it to the queue
		if (queueObj != null) {
			try {
				// if specified sync direction doesn't include push, then ignore
				SyncDirection syncDirection = PreferenceUtils.getSyncDirection(queueObj.getObjectType(), sdkManager.getAppContext());
				if (syncDirection == SyncDirection.DOWN || syncDirection == SyncDirection.NONE) {
					Log.e("Babu", "not adding to queue due to sync direction");
					return;
				}

				if (queueObj.getOperation() == QueueOperation.CREATE ||
						queueObj.getOperation() == QueueOperation.UPDATE) {
					Set<String> sensitiveFields = getSensitiveFields(queueObj.getObjectType());

					if (!sensitiveFields.isEmpty()) {
						JSONObject azureFields = new JSONObject();

						for (String sensitiveField : sensitiveFields) {
							if (queueObj.hasField(sensitiveField)) {
								azureFields.put(sensitiveField, queueObj.removeField(sensitiveField));
							}
						}

						JSONObject queueObjJson = queueObj.toJson();
						Log.d(TAG, "Inserting queue object: " + queueObjJson);
						queueObjJson = requestQueueStorage.upsert(queueObjJson);

						if (azureFields.length() > 0) {
							QueueObject azureQueueObj = new QueueObject(queueObj.getId(),
									queueObj.getObjectType(), queueObj.getOperation());
							azureQueueObj.setFieldsJson(azureFields);
							azureQueueObj.setSoupEntryId(queueObjJson.getLong(QueueObject.SOUP_ENTRY_ID_FIELD));

							// Check if it already exists.
							JSONArray jsonArray = azureQueueStorage.retrieve(azureQueueObj.getSoupEntryId());
							if (jsonArray == null || jsonArray.length() < 1) {
								JSONObject azureJsonObject = azureQueueObj.toJson();
								Log.d(TAG, "Inserting azure queue object: " + azureJsonObject);
								azureQueueStorage.create(azureJsonObject);
							}
							else {
								JSONObject azureJsonObject = azureQueueObj.toJson();
								Log.d(TAG, "Inserting azure queue object: " + azureJsonObject);
								azureQueueStorage.update(azureJsonObject, azureQueueObj.getSoupEntryId());
							}
						}
					}
					else {
						JSONObject jsonObject = queueObj.toJson();
						Log.d(TAG, "Inserting queue object: " + jsonObject);
						requestQueueStorage.upsert(jsonObject);
					}
				}
				else {
					JSONObject jsonObject = queueObj.toJson();
					Log.d(TAG, "Inserting queue object: " + jsonObject);
					requestQueueStorage.upsert(jsonObject);
				}

			} catch (JSONException exc) {
				Log.e(TAG, "Error occurred while attempting to insert into Queue. Please verify validity of JSON data set.");
			}
		}
	}

	private Set<String> getSensitiveFields(String objectName) {
		Set<String> fields = new HashSet<>();

		JSONObject jsonObject = exactQuery("SensitiveData__c", "ObjectName__c", objectName);
		if (jsonObject != null) {
			String fieldsString = jsonObject.optString("FieldNames__c");

			if (!TextUtils.isEmpty(fieldsString)) {
				String[] fieldsArray = fieldsString.split(";");

				for (String field : fieldsArray) {
					if (field != null) {
						fields.add(field.trim());
					}
				}
			}
		}

		return fields;
	}

	/**
	 * Returns saved updates from queue.
	 *
	 * @return Saved records from queue.
	 */
	protected JSONArray getQueueData() {
		return requestQueueStorage.fetchAllRecords();
	}

	protected JSONArray getAzureQueueData() {
		return azureQueueStorage.fetchAllRecords();
	}

	protected JSONObject getQueueRecordAt(int position) {
		return requestQueueStorage.get(position);
	}

	protected JSONObject getAzureQueueRecordAt(int position) {
		return azureQueueStorage.get(position);
	}

	public QueueObject getQueueRecordFromClient(Long soupEntryId) {

		QueueObject queueObject = null;
		try {
			JSONArray jsonArray = requestQueueStorage.retrieve(soupEntryId);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			queueObject = new QueueObject(jsonObject);
		} catch (JSONException e) {
			Log.e(TAG, "got exception in getQueueRecordFromClient", e);
		}
		return queueObject;
	}

	public QueueObject getAzureQueueRecordFromClient(Long soupEntryId) {

		QueueObject queueObject = null;
		try {
			JSONArray jsonArray = azureQueueStorage.retrieve(soupEntryId);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			queueObject = new QueueObject(jsonObject);
		} catch (JSONException e) {
			Log.e(TAG, "got exception in getQueueRecordFromClient", e);
		}
		return queueObject;
	}

	public List<QueueObject> getQueueRecordsFromClient(List<Long> soupEntryIds) {
		List<QueueObject> result = new ArrayList<>();

		for (Long id : soupEntryIds) {
			if (id != null) {
				QueueObject obj = getQueueRecordFromClient(id);
				if (obj != null) {
					result.add(obj);
				}
			}
		}

		return result;
	}

	public JSONObject getQueueJsonFromClient(Long soupEntryId) {
		try {
			JSONArray jsonArray = requestQueueStorage.retrieve(soupEntryId);
			return jsonArray.getJSONObject(0);
		} catch (JSONException e) {
			Log.e(TAG, "got exception in getQueueRecordFromClient", e);
		}
		return null;
	}

	public List<JSONObject> getQueueJsonsFromClient(List<Long> soupEntryIds) {
		List<JSONObject> result = new ArrayList<>();

		for (Long id : soupEntryIds) {
			if (id != null) {
				JSONObject obj = getQueueJsonFromClient(id);
				if (obj != null) {
					result.add(obj);
				}
			}
		}

		return result;
	}

	/**
	 * Insert error into the smartstore queue
	 *
	 * @return true or false depending on whether the operation is successful
	 */

	public boolean insertError(ErrorObject errorObject) {
		if (errorObject == null) {
			return false;
		}

		try {
			JSONObject errorData = errorObject.toJson();
			smartStore.upsert(ERROR_SOUP, errorData);

		} catch (JSONException exception) {
			Log.e(TAG,
					"Error occurred while attempting to insert error. Please verify validity of JSON data set.");
			return false;
		}

		return true;
	}

	/**
	 * Returns errors from Errors SmartStore.
	 *
	 * @return Saved errors from Errors smartstore
	 */
	public List<ErrorObject> getErrors() {
		ArrayList<ErrorObject> errorList = new ArrayList<ErrorObject>();
		JSONArray records = fetchAllRecords(ERROR_SOUP, SOUP_ENTRY_ID);

		if (records != null) {
			for (int i = 0; i < records.length(); i++) {
				try {
					errorList.add(new ErrorObject(records.getJSONObject(i)));
				} catch (JSONException e) {
					Log.e(TAG,
							"Error occurred while attempting to get error. Please verify validity of JSON data set.");
				}
			}
		}
		return errorList;
	}

	public void clearErrors() {
		smartStore.dropSoup(ERROR_SOUP);
		createErrorSoup();
	}


	public List<QueueObject> getQueueRecords() {
		ArrayList<QueueObject> queueList = new ArrayList<QueueObject>();
		JSONArray records = requestQueueStorage.fetchAllRecords();

		if (records != null) {
			for (int i = 0; i < records.length(); i++) {
				try {
					queueList.add(new QueueObject(records.getJSONObject(i)));
				} catch (JSONException e) {
					Log.e(TAG,
							"Error occurred while attempting to get error. Please verify validity of JSON data set.");
				}
			}
		}
		return queueList;
	}

	public void deleteQueueRecordFromClient(Long soupEntryId, boolean removeAllIfCreate) {

		// if this is a create failure then we need to delete all subsequent
		// associated with this local id
		if (removeAllIfCreate) {
			try {
				JSONArray jsonArray = requestQueueStorage.retrieve(soupEntryId);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				QueueObject queueObject = new QueueObject(jsonObject);
				// we got the value so let us now delete the record
				requestQueueStorage.delete(soupEntryId);

				String id = queueObject.getId();
				// delete any records that share the same id as the deleted
				// record
				if (queueObject.getOperation().equals(QueueOperation.CREATE)) {
					requestQueueStorage.deleteAll(CONSTANTS_ID, id);
				}

			} catch (JSONException e) {
				Log.e(TAG, "exception in deleteQueueRecordFromClient");
				e.printStackTrace();
			}
		} else {
			// perform simple delete
			requestQueueStorage.delete(soupEntryId);
		}
	}

	protected void deleteQueueRecords(Long... soupEntryIds) {
		requestQueueStorage.delete(soupEntryIds);
	}

	protected void deleteAzureQueueRecords(Long... soupEntryIds) {
		azureQueueStorage.delete(soupEntryIds);
	}

	protected void deleteRecords(String soupName, Long... soupEntryIds) {
		smartStore.delete(soupName, soupEntryIds);
	}

	protected void incrementQueueRecordRetryCount(Long soupEntryId) {
		try {
			JSONArray jsonArray = requestQueueStorage.retrieve(soupEntryId);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			QueueObject queueObject = new QueueObject(jsonObject);
			queueObject.incrementRetryCount();
			insertRecordIntoQueue(queueObject);
		} catch (JSONException e) {
			Log.e(TAG, "exception in incrementQueueRecordRetryCount");
			e.printStackTrace();
		}
	}

	protected void updateQueueRecordFields(Long soupEntryId, JSONObject newFields) {
		try {
			JSONArray jsonArray = requestQueueStorage.retrieve(soupEntryId);
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			QueueObject queueObject = new QueueObject(jsonObject);
			queueObject.setFieldsJson(newFields);
			insertRecordIntoQueue(queueObject);
		} catch (JSONException e) {
			Log.e(TAG, "exception in updating queue record fields", e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.salesforce.androidsyncengine.datamanager.DataManager#updateRecord
	 * (java.lang.String, java.lang.String, org.json.JSONObject)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean updateRecord(String soupName, String id, JSONObject updatedFieldsJSON) {

		// TODO: Check whether this is an insert or an update

		// If this is a client generated id and if there is no insert operation in the queue with this client id then it should
		// be converted to a create/insert if there is no matching SalesforceId

		if (isClientId(id)) {
			if (!checkIfInQueueWithInsert(id)) {
				// if there is none let us check if the id is the updated hashmap
				String serverId = getSalesforceIdFromTemporaryId(id);
				if (serverId != null) {
					Log.i(TAG, "*** in update with server id ***");
					return updateRecord(soupName, serverId, updatedFieldsJSON);
				} else {
					Log.i(TAG, "in create from update");
					createRecordFromUpdate(soupName, id, updatedFieldsJSON);
					return true;
				}
			}
		}

		JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, id);
		if (jsonObject == null) {
			return false;
		}
		Log.i(TAG, "before modify jsonObject: " + jsonObject.toString());

		try {

			String jsonString = updatedFieldsJSON.toString();
			// update current record with the updatedFields
			jsonString = tempIdContainer.updateWithServerId(jsonString);

			JSONObject changedElementField = new JSONObject(jsonString);
			Iterator<String> iter = updatedFieldsJSON.keys();

			String currentValue;
			String updatedValue;
			while (iter.hasNext()) {
				String currentKey = iter.next();
				if (currentKey.startsWith("_soup") || currentKey.equals(CONSTANTS_ID)) {
					// remove any local soup related fields
					changedElementField.remove(currentKey);
				} else {
					currentValue = jsonObject.optString(currentKey, null);
					updatedValue = updatedFieldsJSON.getString(currentKey);
					Log.e("Babu", "currentValue: " + currentValue + " updatedValue: " + updatedValue);
					if (updatedValue.equals(currentValue) || (!isStringValid(currentValue) && updatedValue.equals(""))) {
						changedElementField.remove(currentKey);
					} else {
						jsonObject.put(currentKey, updatedFieldsJSON.get(currentKey));
					}
				}
			}

			insertRecord(soupName, jsonObject, false, false, null, null);

			if (changedElementField.length() == 0) {
				Log.i(TAG, "Nothing changed! Skipping insertion into queue!");
			} else {
				// now create the Queue record and add it to the queue
				if (isClientId(id)) {
					String salesforceId = tempIdContainer.getSalesforceId(id);
					if (salesforceId != null) {
						id = salesforceId;
					}
				}
				QueueObject queueObj = new QueueObject(id, soupName, QueueOperation.UPDATE);
				queueObj.setFieldsJson(changedElementField);
				insertRecordIntoQueue(queueObj);
			}

		} catch (JSONException e) {
			Log.e(TAG, "Error updating record. soupName: " + soupName + " Id: " + id + " updatedFieldsJSON: " + updatedFieldsJSON, e);
			// TODO: Should we throw this back or return a boolean to tell the client the update failed??
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.salesforce.androidsyncengine.datamanager.DataManager#deleteRecord
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteRecord(String soupName, String Id) {
		JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, Id);
		if (jsonObject == null) {
			return false;
		}
		try {

			Long soupEntryId = jsonObject.getLong(SOUP_ENTRY_ID);
			Log.i(TAG, "Deleting soupEntryId: " + soupEntryId);

			deleteRecords(soupName, soupEntryId);

			// now create the Queue record and add it to the queue
			QueueObject queueObj = new QueueObject(Id, soupName, QueueOperation.DELETE);
			insertRecordIntoQueue(queueObj);

		} catch (JSONException e) {
			Log.e(TAG, "Error updating record. soupName: " + soupName + " Id: "
					+ Id);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
         * (non-Javadoc)
         *
         * @see
         * com.salesforce.androidsyncengine.datamanager.DataManager#createRecord
         * (java.lang.String, org.json.JSONObject)
         */
	@Override
	public String createRecord(String soupName, JSONObject objectJSON) {

		try {

			String jsonString = objectJSON.toString();
			jsonString = tempIdContainer.updateWithServerId(jsonString);

			objectJSON = new JSONObject(jsonString);
			JSONObject newJSONObject = new JSONObject(jsonString);

			// calculate a new Unique Id for the record
			String newId = calculateNewUniqueID(soupName, objectJSON);
			newJSONObject.put(CONSTANTS_ID, newId);

			// if the below entry exists then we update so we should remove it
			newJSONObject.remove(SOUP_ENTRY_ID);

			Log.i(TAG, "In create record, upsert data is: " + newJSONObject.toString());
			smartStore.upsert(soupName, newJSONObject);

			// now create the Queue record and add it to the queue
			QueueObject queueObj = new QueueObject(newId, soupName, QueueOperation.CREATE);
			queueObj.setFieldsJson(objectJSON);
			insertRecordIntoQueue(queueObj);

			return newId;

		} catch (JSONException e) {
			Log.e(TAG, "Error updating record. soupName: " + soupName
					+ " objectJSON: " + objectJSON.toString());
			e.printStackTrace();
			return null;
		}
	}

	private String calculateNewUniqueID(String soupName, JSONObject objectJSON) {

		String newId;

		Log.i(TAG, "in calcualteNewUniqueID with soupName: " + soupName + "jsonData: " + objectJSON);
		// Get first three letters of soupName
		if (soupName.length() < 3) soupName = soupName + "ABC"; // add three dashes

		while (true) {
			// We use the hashCode of the string rather than the object below since this is predictable and reproducable
			newId = soupName.substring(0, 3).toUpperCase(Locale.US) + random.nextInt(10) + random.nextInt(10) + random.nextInt(10) +
					objectJSON.toString().hashCode();
			Log.e(TAG, "calculated unique id: " + newId);
			JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, newId);
			if (jsonObject == null) {
				Log.e(TAG, "unique id: " + newId);
				// the record could have been created and deleted while offline so the record exists in Queue but
				// not in the soup. So, let us search for this id in Queue data

                if (isClientId(newId)) {
					String salesforceId = tempIdContainer.getSalesforceId(newId);
					if (salesforceId != null) {
						newId = salesforceId;
					}
                }

				JSONObject queueObject = requestQueueStorage.exactQuery(CONSTANTS_ID, newId);
				if (queueObject == null) {
					break; // this is a unique id so let us break
				} else {
					Log.i(TAG, "calculated id is in queue: " + newId);
				}
			} else {
				Log.i(TAG, "calculated id is already in soup: " + newId);
			}
		}

		return newId;
	}

	@SuppressWarnings("unchecked")
	public boolean createRecordFromUpdate(String soupName, String id, JSONObject updatedFieldsJSON) {

		try {

			JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, id);
			if (jsonObject == null) {
				return false;
			}

			// update current record with the updatedFields
			JSONObject changedElementField = new JSONObject(updatedFieldsJSON.toString());
			Iterator<String> iter = updatedFieldsJSON.keys();

			while (iter.hasNext()) {
				String currentKey = iter.next();
				if (currentKey.startsWith("_soup")) {
					changedElementField.remove(currentKey);
				} else {
					if (updatedFieldsJSON.getString(currentKey).equals(jsonObject.optString(currentKey, null))) {
						changedElementField.remove(currentKey);
					} else {
						jsonObject.put(currentKey, updatedFieldsJSON.get(currentKey));
					}
				}
			}

			insertRecord(soupName, jsonObject, false, false, null, null);

			// now create the Queue record and add it to the queue
			QueueObject queueObj = new QueueObject(id, soupName, QueueOperation.CREATE);
			changedElementField.remove(CONSTANTS_ID);

			if (changedElementField.length() == 0) {
				Log.i(TAG, "Empty field data!");
			} else {
				queueObj.setFieldsJson(changedElementField);
				insertRecordIntoQueue(queueObj);
			}

		} catch (JSONException e) {
			Log.e(TAG, "Error updating record. soupName: " + soupName
					+ " objectJSON: " + updatedFieldsJSON.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Runs a smart SQL query against the smartstore and returns results.
	 *
	 * @param smartSql
	 *            Smart SQL query string.
	 * @return Results of the query.
	 */
	public JSONArray fetchSmartSQLQuery(String smartSql, int pageIndex, int pageSize) {
		JSONArray result = null;
		QuerySpec querySpec = QuerySpec.buildSmartQuerySpec(smartSql, pageSize);
		int count = smartStore.countQuery(querySpec);
		Log.i(TAG, "In fetchSmartSQLQuery query count: " + count);
		try {
			result = smartStore.query(querySpec, pageIndex);
		} catch (JSONException e) {
			Log.e(TAG,
					"Error occurred while attempting to run query. Please verify validity of the query.");
		}
		return result;
	}

	/**
	 * Runs a smart SQL query against the smartstore and returns results.
	 *
	 * @param smartSql
	 *            Smart SQL query string.
	 * @return Results of the query.
	 */
	public JSONArray fetchAllSmartSQLQuery(String smartSql) {
		JSONArray result = null;
		QuerySpec querySpec = QuerySpec.buildSmartQuerySpec(smartSql, 10);
		int count = smartStore.countQuery(querySpec);
		querySpec = QuerySpec.buildSmartQuerySpec(smartSql, count);
//		Log.i(TAG, "In fetchAllSmartSQLQuery query count: " + count);
		try {
			result = smartStore.query(querySpec, 0);
		} catch (JSONException e) {
			Log.e(TAG,
					"Error occurred while attempting to run query. Please verify validity of the query.");
		}
		return result;
	}

	protected JSONObject retrieve(String soupName, Long... soupEntryId) {
		try {
			JSONArray jsonArray = smartStore.retrieve(soupName, soupEntryId);
			if (jsonArray.length() == 0) {
				return null;
			} else {
				return jsonArray.getJSONObject(0);
			}
		} catch (JSONException e) {
			Log.e(TAG, "error in retrieve for: " + soupName + "soupEntryId: "
					+ Arrays.toString(soupEntryId));
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.salesforce.androidsyncengine.datamanager.DataManager#fetchAllRangeQuery
	 * (java.lang.String, java.lang.String)
	 */
	// TODO: Implement paging to handle large data sets
	@Override
	public JSONArray fetchAllRecords(String soupName, String path) {
		Log.i(TAG, "In fetchAllRecords for: " + soupName);
		JSONArray result = null;
		QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(soupName, path, null, null, null, QuerySpec.Order.ascending, 10);
		int count = smartStore.countQuery(querySpec);
		Log.i(TAG, "In fetchAllRecords for: " + soupName + " count: " + count);
		querySpec = QuerySpec.buildRangeQuerySpec(soupName, path,null, null, null, QuerySpec.Order.ascending, count);
		try {
			result = smartStore.query(querySpec, 0);
		} catch (JSONException e) {
			Log.e(TAG,
					"Error occurred while attempting to run query. Please verify validity of the query.");
		}
		return result;
	}

	public JSONArray fetchRecords(String soupName, String path, int pageIndex, int pageSize) {
		JSONArray result = null;
		QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(soupName, path,null, null, null, QuerySpec.Order.ascending, pageSize);
		int count = smartStore.countQuery(querySpec);
		Log.i(TAG, "In fetchPagedRangeQuery for: " + soupName + " count: " + count);
		querySpec = QuerySpec.buildRangeQuerySpec(soupName, path,null, null, null, QuerySpec.Order.ascending, pageSize);
		try {
			result = smartStore.query(querySpec, pageIndex);
		} catch (JSONException e) {
			Log.e(TAG,
					"Error occurred while attempting to run query. Please verify validity of the query.");
		}
		return result;
	}

	public JSONArray fetchMatchingRecords(String soupName, String path, String matchKey, int pageSize) {
		QuerySpec querySpec = QuerySpec.buildMatchQuerySpec(soupName, path, matchKey, path, QuerySpec.Order.ascending, pageSize);
		JSONArray result = null;
		try {
			result = smartStore.query(querySpec, 0);
		} catch (JSONException e) {
			Log.e(TAG,
					"Error occurred while attempting to run query. Please verify validity of the query.");
		}
		return result;
	}

	public int getRecordCount(String soupName, String path) {
		if ("Queue".equals(soupName)) {
			return requestQueueStorage.getRecordsCount();
		}
		else {
			QuerySpec querySpec = QuerySpec.buildRangeQuerySpec(soupName, path, null, null, null, QuerySpec.Order.ascending, 10);
			int count = smartStore.countQuery(querySpec);
			Log.i(TAG, "In getCountForQuery for: " + soupName + " count: " + count);
			return count;
		}
	}

	public JSONObject exactQuery(String soupName, String path,
								 String exactMatchKey) {
		try {
			if (exactMatchKey == null) return null;

			// if we are searching for Id and
			// if it is a temporary id and if there is an exact match
			// then get the SalesforceId from the hashmap
			if (path.equals(CONSTANTS_ID)) {
				if (isClientId(exactMatchKey)) {
					String salesforceId = tempIdContainer.getSalesforceId(exactMatchKey);
					if (salesforceId != null) {
						exactMatchKey = salesforceId;
					}
				}
			}
			QuerySpec querySpec = QuerySpec.buildExactQuerySpec(soupName, path,
					exactMatchKey, null, QuerySpec.Order.ascending, 10);
			JSONArray result = smartStore.query(querySpec, 0);
			if (result.length() == 0) {
				return null;
			} else {
				return result.getJSONObject(0);
			}
		} catch (JSONException e) {
			Log.e(TAG, "error in exactQuery for: " + soupName + " path: "
					+ path + " exactMatchKey:" + exactMatchKey);
			e.printStackTrace();
		}
		return null;
	}

	private void resetLastRefreshTimeString(String objectName) {
		PreferenceUtils.removeValue(PreferenceUtils.getLastRefreshTimeKey(objectName), sdkManager.getAppContext());
	}

	@Override
	public boolean getEncryptionStatus(Context context) {
		final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if (dpm != null) {
			int status = dpm.getStorageEncryptionStatus();
			if (status == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOnline() {
		return sdkManager.hasNetwork();
	}

	public void updateWithServerId(QueueObject queueObj, String serverId, Context context) {

		Log.i(TAG, "in updateWithServerId");

		try {
			String localId = queueObj.getId();

			// this actually updates the record that we pushed
			updateWithServerIDinSmartStore(queueObj.getObjectType(), localId, localId, serverId);

			insertIntoTempIdHashMap(localId, serverId);

			JSONArray queueArray = getQueueData();
			int count = queueArray.length();
			for (int i = 0; i < count; i++) {
				if (queueArray.getJSONObject(i) == null)  {
					Log.e(TAG, "Null value for queueArray.getJSONObject. Investigate!");
					Log.i(TAG, "Null value for: " + serverId);
				} else {
					QueueObject queueObject = new QueueObject(queueArray.getJSONObject(i));
					String objectType = queueObject.getObjectType();
					String queueObjectId = queueObject.getId();

					boolean shouldUpdateQueueObject = false;
					if (queueObjectId.equals(localId)) {
						queueObject.setId(serverId);
						shouldUpdateQueueObject = true;
						Log.i(TAG, "Id Match!");
					}

					JSONObject fieldsJson = queueObject.getFieldsJson();
					if (fieldsJson != null) {
						String fieldsString = fieldsJson.toString();

						if (fieldsString.contains(localId)) {
							String updatedFieldsString = fieldsString.replace(localId, serverId);
							JSONObject updatedFieldsJSON = new JSONObject(updatedFieldsString);
							queueObject.setFieldsJson(updatedFieldsJSON);
							shouldUpdateQueueObject = true;
							Log.i(TAG, "Field Match!");
						}
					}

					if (shouldUpdateQueueObject) {


//						Set<String> sensitiveFields = getSensitiveFields(queueObj.getObjectType());
//						JSONObject azureFields = new JSONObject();
//
//						for (String sensitiveField : sensitiveFields) {
//							if (queueObj.hasField(sensitiveField)) {
//								azureFields.put(sensitiveField, queueObj.removeField(sensitiveField));
//							}
//						}

						Log.i(TAG, "updating Queue Object");
						requestQueueStorage.update(queueObject.toJson(), queueObject.getSoupEntryId());
						Log.i(TAG, "data: " + objectType + ":" + queueObjectId + ":" + localId + ":" + serverId);
						updateWithServerIDinSmartStore(objectType, queueObjectId, localId, serverId);
						Log.i(TAG, "updated SmartStore");

//						if (azureFields.length() > 0) {
//							QueueObject azureQueueObj = new QueueObject(queueObj.getId(),
//									queueObj.getObjectType(), queueObj.getOperation());
//							azureQueueObj.setSoupEntryId(queueObj.getSoupEntryId());
//							azureQueueStorage.update(azureQueueObj.toJson(), azureQueueObj.getSoupEntryId());
//						}
					}
				}
			}

			//update AzureQueue
			queueArray = getAzureQueueData();
			count = queueArray.length();
			for (int i = 0; i < count; i++) {
				if (queueArray.getJSONObject(i) == null)  {
					Log.e(TAG, "Null value for queueArray.getJSONObject. Investigate!");
					Log.i(TAG, "Null value for: " + serverId);
				} else {
					QueueObject queueObject = new QueueObject(queueArray.getJSONObject(i));
					String objectType = queueObject.getObjectType();
					String queueObjectId = queueObject.getId();

					boolean shouldUpdateQueueObject = false;
					if (queueObjectId.equals(localId)) {
						queueObject.setId(serverId);
						shouldUpdateQueueObject = true;
						Log.i(TAG, "Id Match!");
					}

					if (shouldUpdateQueueObject) {
						Log.i(TAG, "updated Azure Queue Object");
						azureQueueStorage.update(queueObject.toJson(), queueObject.getSoupEntryId());
						Log.i(TAG, "data: " + objectType + ":" + queueObjectId + ":" + localId + ":" + serverId);
					}
				}
			}

		} catch (JSONException e) {
			Log.e(TAG, "In updateWithServerId. Problem updating records with new Id: " + serverId);
		}

	}

	private void updateWithServerIDinSmartStore(String soupName, String objectId, String localId, String serverId) throws JSONException {

		JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, objectId);
		boolean success;
		if (jsonObject != null) {
			String jsonString = jsonObject.toString();
			if (jsonString.contains(localId)) {
				jsonString = jsonString.replace(localId, serverId);
				jsonObject = new JSONObject(jsonString);
				smartStore.upsert(soupName, jsonObject);
				success = true;
			} else {
				success = false;
			}
		} else {
			success = false;
		}

		if (!success){
			Log.i(TAG, "This record might have been deleted or already modified!");
			Log.i(TAG, "May be a problem updating soup: " + soupName
					+ " id : " + objectId
					+ " local id with server id!" + " localId: " + localId
					+ " serverId: " + serverId);
		} else {
			Log.i(TAG, "Successfully updated soup: " + soupName
					+ " id : " + objectId
					+ " local id with server id!" + " localId: " + localId
					+ " serverId: " + serverId);
		}
	}


	private int getClientVersion(Context ctx) {
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			if (packageInfo != null) {
				return packageInfo.versionCode;
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error getting package info!", e);
		}

		return -1;
	}

	@Override
	public void logout(Context context, RestClient client) {

		PreferenceUtils.putLoggedIn(false, context);

		// TODO: Fix the below code. It is a hack right now.
		//this deletes the soups and recreates them
		PreferenceUtils.putClientAppVersion(0, context);
		PreferenceUtils.putManifestVersion(0, context);
		PreferenceUtils.putFullSyncComplete(false, context);

		// turn off auto-sync
		SyncUtils.updateSyncProperties(context, false, false, 60);

		// Remove all the queued content downloads
		removeAllQueuedContentFileDownloads(context, client);

		clearLocalData(context, true);

		DataManagerFactory.clearDataManager();
		DynamicFetchPreferences.getInstance(context).clear();
	}

	@Override
	public String getFilePath(Context context, String objectName, String id) {

		String fileName = DownloadHelper.getFileName(context, objectName, id);
		File file = new File(context.getFilesDir(), fileName);
		if (file.exists()) {
			return file.getPath();
		} else {
			// Don't do anything, we should not be triggering downloads from
			// here
		}

		return null;
	}

	private void removeAllQueuedContentFileDownloads(Context context, RestClient client) {

		String orgId = client.getClientInfo().orgId;
		DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor c = mgr.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED |
				DownloadManager.STATUS_PENDING |
				DownloadManager.STATUS_RUNNING));
		if (c == null) {
			Log.i(TAG, "Download Content Queue is empty");
		} else {
			if (c.moveToFirst()) {
				do {
					// do what you need with the cursor here
					String title = c.getString(c
							.getColumnIndex(DownloadManager.COLUMN_TITLE));
					if (title != null && title.contains(orgId)) {
						Log.i(TAG, "removing from queue content file :" + title);
						long id = c.getLong(c
								.getColumnIndex(DownloadManager.COLUMN_ID));
						mgr.remove(id);
					}
				} while (c.moveToNext());
			}
		}

		DownloadIntentService.clearRetryCounts(context);
	}

	public void reloadManifestOverridePrefs(Context context) throws Exception {
		ManifestUtils manifestUtils = ManifestUtils.getInstance(context);
		manifestUtils.invalidateManifest();
		manifestUtils.fetchAndSaveMetadataPrefs();
		Log.d(TAG, "Manifest is reloaded and preferences are over written");
	}

	@Override
	public void clearLocalData(Context context, boolean shouldClearContent) {
		// delete the smartstore database and try to reinitalize it
		Log.i(TAG, "trying to delete database");
		DBOpenHelper.deleteAllUserDatabases(context);
		// there is hope
		Log.i(TAG, "deleted database");
		DataManagerFactory.clearDataManager();
		// deleteOldObjectsSoups(context);
		//Moving manifest fetching logic to SyncEngine
		PreferenceUtils.putManifestVersion(0, context);
		//reloadManifestOverridePrefs(context);
		//createManifestObjectsSoups(context);
		if (shouldClearContent) {
			clearLocalContent(context);
		}
	}

	public void clearLocalContent(Context context) {
		// delete content files
		File dir = context.getFilesDir();
		for (File file : dir.listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}

		File tempDir = new File(dir, "temp");
		deleteDirectory(tempDir);
	}


	private static void deleteDirectory(File dir) {
		if (dir.exists()) {
			if (dir.isDirectory()) {
				for (File child : dir.listFiles()) {
					deleteDirectory(child);
				}
			}
			dir.delete();
		}
	}

	/**
	 * Deletes existing Queue and Error soups and creates new ones
	 * should be done on first launch and when offline data is cleared
	 */
	private void setUpNecessaryQueueAndErrorSoups() {

		// delete any current queue but only if contains data of single user
		if (requestQueueStorage.isSingleUserStorage()) {
			deleteQueueSoup();
		}

		// Remove all temp ids
		tempIdContainer.deleteAll();

		deleteErrorSoup();

		// create a new queue
		createQueueSoup();

		// create the error soup
		createErrorSoup();
	}

	private void deleteOldObjectsSoups(Context context) {
		// get set of objects from preferences if any saved
		List<String> oldManifestObjects = PreferenceUtils.getSortedObjects(context);

		// delete old manifest objects soups
		if (oldManifestObjects != null && !oldManifestObjects.isEmpty()) {
			for (String string : oldManifestObjects) {
				deleteObjectSoup(string);
			}
		}
	}

	/**
	 * Creates soups for all the objects specified in the manifest
	 * NOTE: this should be always called after loadManifest, deleteOldObjectsSoups, savePrefs
	 * @param context
	 */

	public void createManifestObjectsSoups(Context context) {

		// get new set of objects to sync from manifest
		// and create object soup
		ManifestUtils manifestUtils = ManifestUtils.getInstance(context);
		for (ConfigObject configObject : manifestUtils.getManifest().getObjects()) {
			FieldToIndex[] fieldsToIndex = configObject.getFieldsToIndex();
			IndexSpec[] indexSpec = null;
			if (fieldsToIndex != null) {
				indexSpec = new IndexSpec[fieldsToIndex.length];
				for (int i = 0; i < fieldsToIndex.length; i++) {
					FieldToIndex fieldToIndex = fieldsToIndex[i];
					indexSpec[i] = new IndexSpec(fieldToIndex.getName(), Type.valueOf(fieldToIndex.getType()));
				}
			}
			createObjectSoup(configObject.getObjectName(), indexSpec);
			resetLastRefreshTimeString(configObject.getObjectName());
		}

		// trigger a sync
		PreferenceUtils.putFirstLaunchComplete(false, context);

		PreferenceUtils.putFullSyncComplete(false, context);

		setUpNecessaryQueueAndErrorSoups();
	}

	public void deleteRecordWithoutAddingToQueue(String soupName, String Id) {
		JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, Id);
		if (jsonObject == null) {
			// A record could be created and deleted on the server end.
			// Our client database wont have a copy of this record
			Log.i(TAG, "Record with id not found to delete. id: " + Id);
		} else {
			Long soupEntryId;
			try {
				soupEntryId = jsonObject.getLong(SOUP_ENTRY_ID);
				deleteRecords(soupName, soupEntryId);
				Log.i(TAG, "Deleted Id:" + Id + " soupEntryId: " + soupEntryId);
			} catch (JSONException e) {
				Log.e(TAG, "Unable to get soupEntryId for Id: " + Id + ". This should not happen!");
			}
		}
	}

	@Override
	public SyncStatus getSyncStatus() {
		return SyncEngine.getSyncStatus();
	}

	public boolean isClientId(String id) {
		// Log.i(TAG, "isClientId is: " + (id.startsWith("CON") || id.length() != 18));
		boolean isClientId = CLIENT_ID_PATTERN.matcher(id).matches();
		return isClientId ;
	}

	private boolean checkIfInQueueWithInsert(String id) {

		try {
			JSONArray result = requestQueueStorage.queryAll(CONSTANTS_ID, id);
			Log.i(TAG, "result count is: " + result.length());
			if (result.length() != 0) {
				int recordCount = result.length();
				for (int j = 0; j < recordCount; j++) {
					JSONObject recordObject = result.getJSONObject(j);
					Log.i(TAG, "in checkIfInsertInQueue. queue record:"
							+ recordObject.toString());
					QueueObject temp = new QueueObject(recordObject);
					if (temp.getOperation().equals(QueueOperation.CREATE)) {
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			Log.w(TAG, e);
		}
		return false;
	}

	public boolean isFirstSyncComplete(Context context) {
		return PreferenceUtils.getFullSyncComplete(context);
	}

	@Override
	public SmartStore getSmartStore() {
		return smartStore;
	}

	private static final String emptyString = "";
	private static final String nullString = "null";

	public Boolean isStringValid(String stringValue){
		return !nullString.equals(stringValue) && !emptyString.equals(stringValue) && stringValue != null;
	}

	public int deleteOldestTempIds() {
		long date = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7); // Current time minus 7 days.
		return tempIdContainer.deleteOlderThan(date);
	}

	protected void insertIntoTempIdHashMap(String temporaryId, String salesforceId) {
		tempIdContainer.insertSalesforceId(temporaryId, salesforceId);
	}

	@Override
	public String getSalesforceIdFromTemporaryId(String temporaryId) {
		return tempIdContainer.getSalesforceId(temporaryId);
	}

	protected String updatedWithServerIdJsonString(String jsonString) {
		return tempIdContainer.updateWithServerId(jsonString);
	}

	public void fetchFromServer(Context context, String objectName, String queryFilter) throws SyncException {
		if (TextUtils.isEmpty(queryFilter)) return;

		try {
			SFObjectMetadata objectMetadata = PreferenceUtils.getMetadataObject(objectName, context);
			SFSyncHelper customSyncHelper = SFSyncHelper.getSFSyncHelperInstance(context);
			String query = objectMetadata.getSimpleQuery(context, queryFilter);
			SyncHelper syncHelper = SyncHelper.createNew(context);
			SyncEngine.fetchRecordsForObject(context, syncHelper, this, customSyncHelper, query, objectName, objectMetadata.getFieldsCount(), null);
		} catch (SyncException e) {
			Log.w(TAG, e);
			throw e;
		} catch (Exception e) {
			Log.w(TAG, e);
			throw new SimpleSyncException(e);
		}
	}


	public boolean updateRecordWithoutAddingToQueue(String soupName, String id, JSONObject updatedFieldsJSON) {

		JSONObject jsonObject = exactQuery(soupName, CONSTANTS_ID, id);
		if (jsonObject == null) {
			return false;
		}
		try {
			Iterator<String> iter = updatedFieldsJSON.keys();
			String currentValue;
			String updatedValue;
			while (iter.hasNext()) {
				String currentKey = iter.next();
					currentValue = jsonObject.optString(currentKey, null);
					updatedValue = updatedFieldsJSON.getString(currentKey);
					Log.e("Babu", "currentValue: " + currentValue + " updatedValue: " + updatedValue);
					jsonObject.put(currentKey, updatedFieldsJSON.get(currentKey));
			}

			insertRecord(soupName, jsonObject, false, false, null, null);

		} catch (JSONException e) {
			Log.e(TAG, "Error updating record. soupName: " + soupName + " Id: " + id + " updatedFieldsJSON: " + updatedFieldsJSON, e);
			// TODO: Should we throw this back or return a boolean to tell the client the update failed??
			return false;
		}

		return true;
	}
}
