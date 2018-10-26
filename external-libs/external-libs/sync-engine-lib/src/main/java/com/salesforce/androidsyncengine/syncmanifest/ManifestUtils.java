package com.salesforce.androidsyncengine.syncmanifest;

import static com.salesforce.androidsyncengine.datamanager.SyncHelper.MAX_SUBREQUESTS;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.data.layouts.LayoutUtils;
import com.salesforce.androidsyncengine.data.model.DescribeSObjectResult;
import com.salesforce.androidsyncengine.data.model.Field;
import com.salesforce.androidsyncengine.data.model.PicklistDependencyHolder;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.model.SFObjectMetadata;
import com.salesforce.androidsyncengine.datamanager.synchelper.BatchResponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;
import com.salesforce.androidsyncengine.syncmanifest.constraints.ValidationUtil;
import com.salesforce.androidsyncengine.syncmanifest.syncorder.SyncOrderCalculator;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.androidsyncengine.utils.MathUtils;
import com.salesforce.androidsyncengine.utils.ObjectMapperFactory;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Utility class to read and save manifest data
 * (sorted list of objects and relative select queries) in shared preferences
 * @author usanaga
 *
 */
public class ManifestUtils {

	private static final String TAG = "ManifestUtils";
	private static final String CUSTOM_SUFFIX = "__c";
	private static final String COMPOUND_FIELD_SUFFIX = "__s";
	private static final String RELATION_FIELD_SUFFIX = "__r";
	private static final String DOUBLE_UNDERSCORE = "__";
	private static final Pattern NAMESPACED_REGEX = Pattern.compile("^[a-zA-Z0-9_]+__[a-zA-Z0-9_]+__c$");

	public static final String DOWNLOADED_SYNC_FILE = "downloaded_sync_file";

	private static ManifestUtils instance;

	private final Object manifestLoadLock = new Object();

	private final Context context;

	private final ClientManager clientManager;

	private final String apiVersion;

	private SyncManifest syncManifest;

	public static ManifestUtils getInstance(Context context) {
		if (instance == null) {
			synchronized (ManifestUtils.class) {
				if (instance == null) {
					instance = new ManifestUtils(context.getApplicationContext());
				}
			}
		}

		return instance;
	}

	private ManifestUtils(Context context) {
		this.context = context;
		
		// set REST client
		this.clientManager = new ClientManager(context,
				SalesforceSDKManager.getInstance().getAccountType(),
				SalesforceSDKManager.getInstance().getLoginOptions(),
				SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

		// set api_version
		this.apiVersion = context.getString(R.string.api_version);

//		// TODO: FIX THIS! This filter field is never used. We are including the namespace in the filter object
//		this.filterFieldTransformer = new GuavaUtils.Function<FilterObject, FilterObject>() {
//			public FilterObject apply(FilterObject obj) {
//				obj.setField(ManifestUtils.this.simpleFieldTransformer.apply(obj.getField()));
//				return obj;
//			}
//		};
	}

	private SyncManifest loadManifest() {
		ObjectMapper objectMapper = ObjectMapperFactory.createMapper();

		try {
			InputStream manifestInputStream;
			//use external sync manifest if it exists, otherwise, use included sync manifest
			File downloadedSyncFile = new File(context.getFilesDir(), ManifestUtils.DOWNLOADED_SYNC_FILE);
			if (downloadedSyncFile.exists()) {
				manifestInputStream = new FileInputStream(downloadedSyncFile);
				Log.i(TAG, "using downloaded sync manifest");
			} else {
				Log.i(TAG, "using bundled sync manifest");
				manifestInputStream = context.getResources().openRawResource(R.raw.sync_manifest);
			}
			SyncManifest syncManifest = objectMapper.readValue(manifestInputStream, SyncManifest.class);
			String errorMessage = ValidationUtil.validate(syncManifest);
			if (errorMessage != null) {
				throw new Exception(errorMessage);
			}

			return syncManifest;
		} catch (Exception e) {
			Log.e(TAG, "Invalid Manifest file provided", e);
			throw new IllegalArgumentException("Invalid Manifest file provided. Error: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Getter for sync manifest with null check to make sure
	 * clients call the loadManifest method prior to any other calls
	 * @return SyncManifest
	 */
	public SyncManifest getManifest() {
		if (syncManifest == null) {
			synchronized (manifestLoadLock) {
				if (syncManifest == null) {
					syncManifest = loadManifest();
					return syncManifest;				// Return new manifest now to be sure it won't be invalidated when outside synchronized block.
				}
			}
		}

		return syncManifest;
	}

	/**
	 * Make sure that next getManifest() call will load sync manifest once again.
	 */
	public void invalidateManifest() {
		if (syncManifest != null) {
			synchronized (manifestLoadLock) {
				if (syncManifest != null) {
					syncManifest = null;
				}
			}
		}
	}

	// use this only for objectNames - not fields
	public static String getNamespaceSupportedObjectName(String objectName, Context context){
		if (objectName != null && PreferenceUtils.getShouldUseNamespace(objectName, context)) {
			String namespacePrefix = PreferenceUtils.getNamespacePrefix(context);
			if (namespacePrefix != null // if we have a prefix
					&& (objectName.endsWith(CUSTOM_SUFFIX) || objectName.endsWith(COMPOUND_FIELD_SUFFIX)) // this is a custom obj or field
					&& !NAMESPACED_REGEX.matcher(objectName).matches()) { // and it is not already namespaced
				objectName = namespacePrefix + DOUBLE_UNDERSCORE + objectName;
			}
		}
		return objectName;
	}

	public static String getNamespaceSupportedFieldName(String objectName, String fieldName, Context context){
		if (objectName != null && fieldName != null && PreferenceUtils.getShouldUseNamespace(objectName, context)) {
			String namespacePrefix = PreferenceUtils.getNamespacePrefix(context);
			if (namespacePrefix != null // if we have a prefix
					&& (fieldName.endsWith(CUSTOM_SUFFIX) || fieldName.endsWith(COMPOUND_FIELD_SUFFIX)) // this is a custom obj or field
					&& !NAMESPACED_REGEX.matcher(fieldName).matches()) { // and it is not already namespaced
				SFObjectMetadata metadata = PreferenceUtils.getMetadataObject(objectName, context);
				Set<String> allFields = metadata.getAllFields();
				if (allFields == null || !allFields.contains(fieldName)) {	// If this fields exists without prefix don't add it.
					String fieldNameWithPrefix = namespacePrefix + DOUBLE_UNDERSCORE + fieldName;
					if (allFields.contains(fieldNameWithPrefix)) {
						fieldName = fieldNameWithPrefix;
					}
				}
			}
		}

		return fieldName;
	}

	private static String getNamespaceSupportedFieldName(String objectName, String fieldName, List<String> allFields, Context context){
		if (PreferenceUtils.getShouldUseNamespace(objectName, context)) {
			String namespacePrefix = PreferenceUtils.getNamespacePrefix(context);
			if (namespacePrefix != null // if we have a prefix
					&& (fieldName.endsWith(CUSTOM_SUFFIX) || fieldName.endsWith(COMPOUND_FIELD_SUFFIX)) // this is a custom obj or field
					&& !NAMESPACED_REGEX.matcher(fieldName).matches()) { // and it is not already namespaced

				if (allFields == null || !allFields.contains(fieldName)) {	// If this fields exists without prefix don't add it.
					fieldName = namespacePrefix + DOUBLE_UNDERSCORE + fieldName;
				}
			}
		}

		return fieldName;
	}

	public static String removeNamespaceFromField(String objectName, String fieldName, Context context) {
		if (TextUtils.isEmpty(objectName) || TextUtils.isEmpty(fieldName)) {
			return fieldName;
		}

		if (PreferenceUtils.getShouldUseNamespace(objectName, context)) {
			String namespacePrefix = PreferenceUtils.getNamespacePrefix(context);
			if (namespacePrefix != null
					&& (fieldName.endsWith(CUSTOM_SUFFIX) || fieldName.endsWith(COMPOUND_FIELD_SUFFIX) || fieldName.endsWith(RELATION_FIELD_SUFFIX))) { // This is a custom obj or field.

				if (fieldName.startsWith(namespacePrefix) && fieldName.startsWith(DOUBLE_UNDERSCORE, namespacePrefix.length())) {	// Check if the field starts with this prefix.
					SFObjectMetadata metadata = PreferenceUtils.getMetadataObject(objectName, context);
					Set<String> allFields = metadata.getAllFields();
					String simpleFieldName = fieldName.substring(namespacePrefix.length() + DOUBLE_UNDERSCORE.length());

					if (allFields == null || !allFields.contains(simpleFieldName)) {	// Don't remove prefix if there is separate field with such name.
						fieldName = simpleFieldName;
					}
				}
			}
		}

		return fieldName;
	}

	public static String removeNamespaceFromObject(String objectName, Context context) {
		if (TextUtils.isEmpty(objectName)) {
			return objectName;
		}

		if (objectName.endsWith(CUSTOM_SUFFIX) || objectName.endsWith(COMPOUND_FIELD_SUFFIX)) { // This is a custom obj or field.
			String namespacePrefix = PreferenceUtils.getNamespacePrefix(context);

			if (!TextUtils.isEmpty(namespacePrefix)) {
				namespacePrefix += DOUBLE_UNDERSCORE;

				if (objectName.startsWith(namespacePrefix)) {    // Check if the field starts with this prefix.
					String resultName = objectName.substring(namespacePrefix.length());
					if (PreferenceUtils.getShouldUseNamespace(resultName, context)) {
						return resultName;
					}
				}
			}
		}

		return objectName;
	}

	// use this before making calls on fields
	public static boolean useNamespaceSupportedObjectName(String objectName, Context context){
		if (PreferenceUtils.getShouldUseNamespace(objectName, context) && PreferenceUtils.getNamespacePrefix(context) != null) {
				return true;
		}
		return false;
	}

	private Map<String, SFObjectMetadata> sendMetadataRequest(List<ConfigObject> configObjects, RestClient client) throws IOException, SyncException {
		if (configObjects == null || configObjects.isEmpty()) return Collections.emptyMap();
		// before making the server request check if the object is a custom object
		// if so, append name space to objectType
		// else use just objectType


		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		SyncHelper syncHelper = new SyncHelper(client, apiVersion , context);
		Map<String, SFObjectMetadata> result = new HashMap<String, SFObjectMetadata>();
		int pagesCount = MathUtils.ceilInt(configObjects.size() / (double) MAX_SUBREQUESTS);
		int pageSize = MathUtils.ceilInt(configObjects.size() / (double) pagesCount);

		for (int i = 0; i < pagesCount; i++) {
			int startIndex = i * pageSize;
			int endIndex = Math.min(startIndex + pageSize, configObjects.size());
			List<ConfigObject> sublistOfConfigs = configObjects.subList(startIndex, endIndex);
			List<Subrequest> subrequests = new ArrayList<Subrequest>();
			for (ConfigObject configObject : sublistOfConfigs) {
				String serverObjectName = getNamespaceSupportedObjectName(configObject.getObjectName(), context);
				Subrequest subrequest = syncHelper.getDescribeSubrequest(apiVersion, serverObjectName);
				subrequests.add(subrequest);
			}

			RestResponse response = syncHelper.sendSubrequests(subrequests, true /* haltOnError */, null);

			if (!response.isSuccess()) {
				throw ServerErrorException.createFrom(response, context);
			}

			String objectType = null;

			try {

				BatchResponse batchResponse = BatchResponse.createFrom(response);
				List<Subresponse> responseList = batchResponse.getElements();

				for (int j = 0; j < responseList.size(); j++) {
					objectType = sublistOfConfigs.get(j).getObjectName();

					Subresponse subresponse = responseList.get(j);
					if (!subresponse.isSuccess()) {
						throw ServerErrorException.createFrom(subresponse, context);
					}

					// calculate picklist dependencies
					DescribeSObjectResult describeSObjectResult = gson.fromJson(subresponse.asString(), DescribeSObjectResult.class);
					savePickListData(objectType, describeSObjectResult, subresponse.asString());
					List<String> allFields = new ArrayList<String>();
					for (Field field : describeSObjectResult.getFields()) {
						allFields.add(field.getName());
					}

					SFObjectMetadata sfObjectMetadata = createSfObjectMetadata(objectType, allFields, describeSObjectResult.getReplicateable());
					result.put(objectType, sfObjectMetadata);
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception in onSuccess of metadata ", e);
				throw new SimpleSyncException(e, e.getMessage() + " (on object: " + objectType + ")");
			}
		}

		return result;
	}

	/**
	 * creates model metadata object with object name and fields to fetch
	 * @param name
	 * @param allFields
	 */
	private SFObjectMetadata createSfObjectMetadata(String name, final List<String> allFields, boolean replicable) {

		// get the config object from manifest
		SyncManifest syncManifest = getManifest();
		ConfigObject configObject = syncManifest.getConfigObject(name);
		SimpleFieldTransformer simpleFieldTransformer = new SimpleFieldTransformer(context, name, allFields);

		// create the metadata object
		SFObjectMetadata sfObjectMetadata = new SFObjectMetadata();
		sfObjectMetadata.setName(getNamespaceSupportedObjectName(name, context));
		sfObjectMetadata.setNameWitoutNameSpace(name);
		sfObjectMetadata.setReplicable(replicable);
		sfObjectMetadata.setCheckLastModify(configObject.getCheckLastModify());
		sfObjectMetadata.setDynamicFetchFilters(configObject.getDynamicFetchFilters());
		sfObjectMetadata.setAllFields(new HashSet<String>(allFields));
		if (configObject.getExtraFieldsToFetch() != null) {
			sfObjectMetadata.setExtraFieldsToFetch(Arrays.asList(configObject.getExtraFieldsToFetch()));
		}

		boolean useNameSpace = ManifestUtils.useNamespaceSupportedObjectName(name, context);
		sfObjectMetadata.setFilters(configObject.getFilters());
		sfObjectMetadata.setDynamicFetch(configObject.getDynamicFetch());

		// validate orderBy fields
		List<OrderByField> orderBy = configObject.getOrderByArray();
		if (orderBy != null && !orderBy.isEmpty()) {
			Iterator<OrderByField> obIterator = orderBy.iterator();
			while(obIterator.hasNext()) {
				if (!allFields.contains(obIterator.next().getName())) {
					obIterator.remove();
				}
			}
			if (useNameSpace) {
				orderBy = GuavaUtils.transform(orderBy, new OrderByFieldTransformer(simpleFieldTransformer));
			}
			sfObjectMetadata.setOrderBy(orderBy);
		}

		// set limit value on SFObjectMetadata to compute appropriate query
		sfObjectMetadata.setLimit(configObject.getLimit());

		// decide which fields need to be fetched
		if (configObject.isFetchAllFields()) {
			// fetch all fields - nothing to do here
		} else if (configObject.getFieldsToFetch() != null) {
			// fetch only the fields mentioned in manifest
			List<String> includeList = Arrays.asList(configObject.getFieldsToFetch());

			// if namespaced then make sure we compare apples to apples
			if (useNameSpace) {
				includeList = GuavaUtils.transform(includeList, simpleFieldTransformer);
			}

			allFields.retainAll(includeList);
		} else {
			// lets filter all fields and only collect fields that we need
			List<String> ignoreList = Arrays.asList(configObject.getFieldsToIgnore());
			// if namespaced then make sure we compare apples to apples
			if (useNameSpace) {
				ignoreList = GuavaUtils.transform(ignoreList, simpleFieldTransformer);
			}

			allFields.removeAll(ignoreList);
		}

		sfObjectMetadata.setFieldsToFetch(allFields);
		sfObjectMetadata.setCleanupQueryFilters(configObject.getCleanupQueryFilters());

		return sfObjectMetadata;
	}


	public void fetchAndSaveMetadataPrefs() throws Exception {

		context.sendBroadcast(new Intent(DataManager.FETCH_METADATA_STARTED));
		Log.i(TAG, "Fetch metadata started");
		
		final AtomicInteger metadataLoadCounter = new AtomicInteger(0);
		
		saveConfigValuesInPrefs();

		final ConfigObject[] configObjects = getManifest().getObjects();

		// get sync order calculator
		SyncOrderCalculator calculator = new SyncOrderCalculator();

		// add the objects to the sync calculator
		for (ConfigObject configObject : configObjects) {
			final String objectName = configObject.getObjectName();

			String lastRefreshedTimeKey = PreferenceUtils.getLastRefreshTimeKey(objectName);
			PreferenceUtils.removeValue(lastRefreshedTimeKey, context);

			// save localFields
			String[] localFields = configObject.getLocalFields();
			if (localFields != null && localFields.length > 0) {
				PreferenceUtils.putLocalFields(objectName, new HashSet<String>(Arrays.asList(localFields)), context);
			}

			// save fileInfo
			FileInfo fileInfo = configObject.getFileInfo();
			if (fileInfo != null) {
				PreferenceUtils.putBinaryField(objectName, fileInfo.getBinaryField(), context);
				PreferenceUtils.putNameFiled(objectName, fileInfo.getNameField(), context);
				PreferenceUtils.putTypeField(objectName, fileInfo.getTypeField(), context);
				PreferenceUtils.putAdditionalFilesFilters(objectName, fileInfo.getAdditionalFilesFilters(), context);
				PreferenceUtils.putRequiredFilesFilters(objectName, fileInfo.getRequiredFilesFilters(), context);
			}
			
			// save field relationships
			FieldRelationship fieldRelationship = configObject.getFieldRelationship();
			if (fieldRelationship != null) {
				PreferenceUtils.putFieldRelationFieldName(objectName, fieldRelationship.getFieldName(), context);
				PreferenceUtils.putFieldRelationFieldValue(objectName, fieldRelationship.getValue(), context);
			}
			
			// save check for deleted property 
			PreferenceUtils.putCheckForDeleted(objectName, configObject.isShouldCheckForDeleted(), context);
			
			// save purge enabled property
			PreferenceUtils.putPurgeEnabled(objectName, configObject.isPurgeEnabled(), context);
			
			// save sync direction
			PreferenceUtils.putSyncDirection(objectName, configObject.getSyncDirection(), context);

			// save fetchLayoutMetadata
			PreferenceUtils.putShouldFetchLayoutMetadata(objectName, configObject.isShouldFetchLayoutMetadata(), context);

			//save fetchCompactLayoutMetadata
			PreferenceUtils.putShouldFetchCompactLayoutMetadata(objectName, configObject.isShouldFetchCompactLayoutMetadata(), context);

			// save dateQuery
			PreferenceUtils.putIsDateQuery(objectName, configObject.isDateQuery(), context);

			// save useNamespace
			PreferenceUtils.putShouldUseNamespace(objectName, configObject.isShouldUseNamespace(), context);
			
			// add to sync order calculator
			String[] dependenciesArray = configObject.getDependencies();

			// Save dependencies
			if (dependenciesArray != null) {
				Set<String> dependenciesSet = new HashSet<>();
				Collections.addAll(dependenciesSet, dependenciesArray);
				PreferenceUtils.putDependencies(objectName, dependenciesSet, context);
			}

			@SuppressWarnings("unchecked")
			List<String> dependencies = dependenciesArray != null ? Arrays.asList(dependenciesArray) : Collections.EMPTY_LIST; 
			calculator.addSyncObject(configObject.getObjectName(), dependencies);
		}

		RestClient client = clientManager.peekRestClient();
		Map<String, SFObjectMetadata> objectMetadata;

		try {
			objectMetadata = sendMetadataRequest(Arrays.asList(configObjects), client);
		} catch (Exception e) {
			Log.e(TAG, "Error on send metadata requests", e);
			// if we fail on meta data then we need to refetch all the metadata again ...
			PreferenceUtils.putManifestVersion(0, context);
			throw new Exception("Error on send metadata requests: " + e.getMessage(), e);
		}

		List<String> layoutObjectNames = new ArrayList<String>();
		List<String> compactLayoutObjectNames = new ArrayList<String>();

		String objectName = null;

		try {

			for (ConfigObject configObject : configObjects) {
				objectName = configObject.getObjectName();

				Log.i(TAG, "fetch metadata completed for object: " + objectName);
				SFObjectMetadata metadata = objectMetadata.get(objectName);

				PreferenceUtils.putMetadataObject(objectName, metadata, context);
				PreferenceUtils.putReplicable(objectName, metadata.getReplicable(), context);


				int count = metadataLoadCounter.incrementAndGet();
				if (count >= configObjects.length) {
					// manifest loading is completed
					// save a flag in preferences
					PreferenceUtils.putManifestLoaded(true, context);
					context.sendBroadcast(new Intent(DataManager.FETCH_METADATA_COMPLETED));
					Log.i(TAG, "Fetch metadata completed");
				}

				if (PreferenceUtils.getShouldFetchLayoutMetadata(objectName, context)) {
					// this should not get called for all objects and if we get exceptions we should just proceed
					String serverObjectName = getNamespaceSupportedObjectName(objectName, context);
					layoutObjectNames.add(serverObjectName);
				}
				// save it in prefs for later use

				if (PreferenceUtils.getShouldFetchCompactLayoutMetadata(objectName, context)) {
					String serverObjectName = getNamespaceSupportedObjectName(objectName, context);
					compactLayoutObjectNames.add(serverObjectName);
				}
			}

		} catch (Exception e) {
			Log.e(TAG, "Error on object " + objectName + " on send metadata requests", e);
			// if we fail on meta data then we need to refetch all the metadata again ...
			PreferenceUtils.putManifestVersion(0, context);
			throw new Exception("Error on object " + objectName + " on send metadata requests: " + e.getMessage(), e);
		}

		try {
			LayoutUtils.sendMetadataRequestForLayouts(context, client, apiVersion, layoutObjectNames);
			LayoutUtils.sendMetadataRequestForCompactLayouts(context, client, apiVersion, compactLayoutObjectNames);
		} catch (Exception e) {
			Log.e(TAG, "Error on send layout requests", e);
			// if we fail on meta data then we need to refetch all the metadata again ...
			PreferenceUtils.putManifestVersion(0, context);
			throw new Exception("Error on send layout requests: " + e.getMessage(), e);
		}

		// calculate all objects sync order and store it in prefs
		PreferenceUtils.putSortedObjects(calculator.getSortedSetForSync(), context);
		PreferenceUtils.putGroupsForBatchSync(calculator.getGroupsForBatchSync(), context);
	}

	private void saveConfigValuesInPrefs() {
		Configuration configuration = getManifest().getConfiguration();
		// save manifest version number
		PreferenceUtils.putManifestVersion(configuration.getManifestVersion(), context);

		// save sync frequency 
		// sync frequency is 0 = disable autoSync/ only manual sync
		PreferenceUtils.putSyncFrequency(configuration.getSyncFrequency(), context);
		PreferenceUtils.putPurgeFrequency(configuration.getPurgeFrequency(), context);

		// save background sync behavior on 3G/4G/wifi/LTE
		BackgroundSync backgroundSync = configuration.getBackgroundSync();
		PreferenceUtils.put3gSync(backgroundSync.is_3G(), context);
		PreferenceUtils.put4gSync(backgroundSync.is_4G(), context);
		PreferenceUtils.putWifiSync(backgroundSync.isWifi(), context);
		PreferenceUtils.putLteSync(backgroundSync.isLte(), context);
		
		// save namespace prefix if exists 
		PreferenceUtils.putNamespacePrefix(configuration.getNameSpacePrefix(), context);

		PreferenceUtils.putConfiguration(configuration, context);
	}

	public void savePickListData(String objectType, DescribeSObjectResult describeSObjectResult, String stringResult) {
		// inner class to decode a "validFor" bitset
		// code copied from Salesforce docs
		class Bitset {
			byte[] data;

			public Bitset(byte[] data) {
				this.data = data == null ? new byte[0] : data;
			}

			public boolean testBit(int n) {
				return (data[n >> 3] & (0x80 >> n % 8)) != 0;
			}

			public int size() {
				return data.length * 8;
			}
		}

		try {

			MetaDataProvider.saveMetaData(context, objectType, stringResult);

			List<Field> fields = describeSObjectResult.getFields();
			// create a map of all fields for later lookup
			Map fieldMap = new HashMap();
			for (int i = 0; i < fields.size(); i++) {
				fieldMap.put(fields.get(i).getName(), fields.get(i));
			}

			PicklistDependencyHolder fieldValidityHashMap = new PicklistDependencyHolder() ;
			for (int i = 0; i < fields.size(); i++) {
				// check whether this is a dependent picklist
				if (fields.get(i).getDependentPicklist()) {
					// get the controller by name
					Field controller = (Field)fieldMap.get(fields.get(i).getControllerName());

					String controllerType;
					if (controller == null) {
						Log.e(TAG,  objectType + " got null for controller: " + fields.get(i).getControllerName());
						controllerType = null;
					} else {
						controllerType = controller.getType();
					}
//					System.out.println("Field '" + fields.get(i).getLabel() + "' depends on '" +
//							controller.getLabel() + "'");
					List<PicklistValue> picklistValues = fields.get(i).getPicklistValues();
					for (int j = 0; j < picklistValues.size(); j++) {
						// for each PicklistEntry: list all controlling values for which it is valid
//						System.out.println("Item: '" + picklistValues.get(j).getLabel() +
//								"' is valid for: ");

						String validForStringValue = (String) picklistValues.get(j).getValidFor();

						if (validForStringValue == null || "null".equalsIgnoreCase(validForStringValue)) {
							//Ignore the null values
						} else {
							Bitset validFor = new Bitset(Base64.decode(validForStringValue, Base64.DEFAULT));
							if ("picklist".equalsIgnoreCase(controllerType)) {
								// if the controller is a picklist, list all
								// controlling values for which this entry is valid

								List<PicklistValue> controllerPickListValues = controller.getPicklistValues();
								int controllerPickListSize = controllerPickListValues.size();
								for (int k = 0; k < validFor.size(); k++) {
									if (validFor.testBit(k)) {
										// if bit k is set, this entry is valid for the
										// for the controlling entry at index k

										if (k >= controllerPickListSize) {
											Log.e(TAG, "problem k > controllerPickListSize : " + k + " > " + controllerPickListSize);
										} else {
											// System.out.println(controllerPickListValues.get(k).getLabel());
											FieldValueObject fieldValueObject = new FieldValueObject(controller.getName(), controllerPickListValues.get(k).getValue());
											ArrayList<FieldValueObject> availableValues = (ArrayList<FieldValueObject>) fieldValidityHashMap.get(fieldValueObject);
											if (availableValues == null) availableValues = new ArrayList<FieldValueObject>();
											FieldValueObject valueElement = new FieldValueObject(fields.get(i).getName(), picklistValues.get(j).getValue());
											availableValues.add(valueElement);
											fieldValidityHashMap.put(fieldValueObject, availableValues);
										}
									}
								}
							} else if ("Boolean".equalsIgnoreCase(controllerType)) {
								// the controller is a checkbox
								// if bit 1 is set this entry is valid if the controller is checked
								if (validFor.testBit(1)) {
									System.out.println(" checked");
								}
								// if bit 0 is set this entry is valid if the controller is not checked
								if (validFor.testBit(0)) {
									System.out.println(" unchecked");
								}
							}
						}
					}
				}
			}

//			Iterator<FieldValueObject> keySetIterator = fieldValidityHashMap.keySet().iterator();
//
//			while(keySetIterator.hasNext()){
//				FieldValueObject key = keySetIterator.next();
//				Log.e("PickListValues", "key: " + key.field + ":" + key.fieldValue + "\nvalues: " + fieldValidityHashMap.get(key));
//			}

			MetaDataProvider.savePicklistDependency(context, objectType, fieldValidityHashMap);

		} catch (Exception ce) {
			Log.e(TAG, "objectType: " + objectType + " " + ce.getMessage());
			ce.printStackTrace();
		}
	}

	private static class SimpleFieldTransformer implements GuavaUtils.Function<String, String> {

		private final Context context;

		private final String objectName;

		private final List<String> allFields;

		private SimpleFieldTransformer(Context context, String objectName, List<String> allFields) {
			this.context = context;
			this.objectName = objectName;
			this.allFields = allFields;
		}

		@Override
		public String apply(String field) {
			return getNamespaceSupportedFieldName(objectName, field, allFields, context);
		}
	}

	private static class OrderByFieldTransformer implements GuavaUtils.Function<OrderByField, OrderByField> {

		private final SimpleFieldTransformer fieldTransformer;

		private OrderByFieldTransformer(SimpleFieldTransformer fieldTransformer) {
			this.fieldTransformer = fieldTransformer;
		}

		public OrderByField apply(OrderByField obj) {
			obj.setName(fieldTransformer.apply(obj.getName()));
			return obj;
		}
	};
}

