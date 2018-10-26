package com.salesforce.androidsyncengine.datamanager.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.soql.Query;
import com.salesforce.androidsyncengine.datamanager.soql.QueryBuilder;
import com.salesforce.androidsyncengine.syncmanifest.Configuration;
import com.salesforce.androidsyncengine.syncmanifest.DynamicFetchConfig;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.androidsyncengine.syncmanifest.ManifestProcessor;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.syncmanifest.OrderByField;
import com.salesforce.androidsyncengine.syncmanifest.SFQueryFilter;
import com.salesforce.androidsyncengine.syncmanifest.processors.ManifestProcessingException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SFObjectMetadata {

	private static final String TAG = SFObjectMetadata.class.getSimpleName();

	private String name;
	private String nameWitoutNameSpace;
	private Set<String> allFields;
	private List<String> fieldsToFetch;
	// extra fields that need to be added to SELECT 
	// should be excluded from validation after fetching all fields from describe call
	// should be excluded from namespace handling 
	private List<String> extraFieldsToFetch;

	private List<FilterObject> filterObjects;

	private List<SFQueryFilter> filters;

	private List<SFQueryFilter> dynamicFetchFilters;

	private List<DynamicFetchConfig> dynamicFetch;

	private int limit;
	private List<OrderByField> orderBy;
	private String[] cleanupQueryFilters;
	private boolean replicable;
	private String checkLastModify;
	public SFObjectMetadata() { }

	public SFObjectMetadata(SFObjectMetadata other) {
		this.name = other.name;
		this.nameWitoutNameSpace = other.nameWitoutNameSpace;
		this.allFields = other.allFields == null ? null : new HashSet<>(other.allFields);
		this.fieldsToFetch = other.fieldsToFetch == null ? null : new ArrayList<>(other.fieldsToFetch);
		this.extraFieldsToFetch = other.extraFieldsToFetch == null ? null : new ArrayList<>(other.extraFieldsToFetch);
		this.filterObjects = other.filterObjects == null ? null : new ArrayList<>(other.filterObjects);
		this.filters = other.filters == null ? null : new ArrayList<>(other.filters);
		this.dynamicFetchFilters = other.dynamicFetchFilters == null ? null : new ArrayList<>(other.dynamicFetchFilters);
		this.dynamicFetch = other.dynamicFetch == null ? null : new ArrayList<>(other.dynamicFetch);
		this.limit = other.limit;
		this.orderBy = other.orderBy == null ? null : new ArrayList<>(other.orderBy);
		this.cleanupQueryFilters = other.cleanupQueryFilters == null ? null : Arrays.copyOf(other.cleanupQueryFilters, other.cleanupQueryFilters.length);
		this.replicable = other.replicable;
		this.checkLastModify=other.checkLastModify;
	}

	public String getName() {
		return name;
	}
	public String getNameWitoutNameSpace() {
		return nameWitoutNameSpace;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNameWitoutNameSpace(String nameWithoutNameSpace) {
		this.nameWitoutNameSpace = nameWithoutNameSpace;
	}

	public boolean containsFieldToFetch(String field) {
		if (fieldsToFetch == null || fieldsToFetch.isEmpty()) return false;

		return fieldsToFetch.contains(field);
	}
	public void setCheckLastModify(String lastModify){
		this.checkLastModify=lastModify;
	}
	public String getCheckLastModify(){
		return checkLastModify;
	}
	public boolean containsField(String field) {
		if (allFields == null || allFields.isEmpty()) return false;

		return allFields.contains(field);
	}

	public void setFieldsToFetch(List<String> fieldsToFetch) {
		this.fieldsToFetch = fieldsToFetch;
	}
	
	public void setExtraFieldsToFetch(List<String> extraFieldsToFetch) {
		this.extraFieldsToFetch = extraFieldsToFetch;
	}

	public int getFieldsCount() {
		return (fieldsToFetch == null ? 0 : fieldsToFetch.size())
				+ (extraFieldsToFetch == null ? 0 : extraFieldsToFetch.size());
	}

	public void addFilterObjects(List<FilterObject> filters) {
		if (this.filterObjects != null)
			this.filterObjects.addAll(filters);
		else
			this.filterObjects = new ArrayList<FilterObject>(filters);
	}

	public void setFilterObjects(List<FilterObject> filterObjects) {
		this.filterObjects = new ArrayList<FilterObject>(filterObjects);
	}

	public List<FilterObject> getFilterObjects() {
		return this.filterObjects;
	}

	public void setFilters(List<SFQueryFilter> filters) {
		this.filters = filters;
	}

	public List<SFQueryFilter> getFilters() {
		return filters;
	}

	public void setDynamicFetchFilters(List<SFQueryFilter> dynamicFetchFilters) {
		this.dynamicFetchFilters = dynamicFetchFilters;
	}

	public List<SFQueryFilter> getDynamicFetchFilters() {
		return dynamicFetchFilters;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOrderBy(List<OrderByField> orderBy) {
		this.orderBy = orderBy;
	}
	
	public void setReplicable(boolean replicable) {
		this.replicable = replicable;
	}
	
	public boolean getReplicable() {
		return this.replicable;
	}

	public String[] getCleanupQueryFilters() {
		return cleanupQueryFilters;
	}

	public void setCleanupQueryFilters(String[] cleanupQueryFilters) {
		this.cleanupQueryFilters = cleanupQueryFilters;
	}

	public void setDynamicFetch(List<DynamicFetchConfig> dynamicFetch) {
		this.dynamicFetch = dynamicFetch;
	}

	public List<DynamicFetchConfig> getDynamicFetch() {
		return dynamicFetch;
	}

	public DynamicFetchConfig getDynamicFetchByName(String name) {
		if (TextUtils.isEmpty(name)) return null;
		if (dynamicFetch == null || dynamicFetch.isEmpty()) return null;

		for (DynamicFetchConfig config : dynamicFetch) {
			if (name.equals(config.getName())) return config;
		}

		return null;
	}

	public void setAllFields(Set<String> allFields) {
		this.allFields = allFields;
	}

	public Set<String> getAllFields() {
		return allFields;
	}
	
	/**
	 * Builds select query from soup name and fieldsToFetch. The query can be splitted to multiple
	 * smaller queries, i.e. when "IN (...)" statement contains too many items.
	 * 
	 * @return SOQL query string
	 */
	public List<String> getSyncQuery(Context context, ManifestProcessor manifestProcessor, Configuration configuration) throws ManifestProcessingException {

		if (filters == null || filters.isEmpty()) {
			// If dynamic fetch filters are not provided fetch all items.
			if (dynamicFetchFilters == null || dynamicFetchFilters.isEmpty()) {
				String query = getQuery(context, null, -1, 0);
				return Collections.singletonList(query);
			}
			// If dynamic fetch filters are provided we are not fetching anything.
			else {
				return Collections.emptyList();
			}
		}
		else {
			List<String> list = new ArrayList<String>();
			for (SFQueryFilter filter : filters) {
				List<String> subfilters = manifestProcessor.processFilter(configuration, this, filter);
				for (String subfilter : subfilters) {
					String query = getQuery(context, subfilter, -1, 0);
					list.add(query);
				}
			}

			return list;
		}
	}

	/**
	 *this work in delta_sync
	 *get query filter string
	 *
	 * @return SOQL query filter string
	 */
	public List<String> getCheckLastModifyQueryFilter(Context context, ManifestProcessor manifestProcessor, Configuration configuration) throws ManifestProcessingException {
		if(checkLastModify==null){
			return Collections.emptyList();
		}
		return manifestProcessor.processCheckLastModifyFilter(configuration, this, checkLastModify);
	}

	/** Get queries related with dynamic fetch that will be executed during delta and full sync. */
	public List<String> getDynamicFetchSyncQuery(Context context, ManifestProcessor manifestProcessor, Configuration configuration,
												 Map<String, Object> additionalVariables) throws ManifestProcessingException {

		if (dynamicFetchFilters == null || dynamicFetchFilters.isEmpty()) {
			return Collections.emptyList();
		}
		else {
			List<String> list = new ArrayList<String>();
			for (SFQueryFilter filter : dynamicFetchFilters) {
				List<String> subfilters = manifestProcessor.processFilter(configuration, this, filter, additionalVariables);
				for (String subfilter : subfilters) {
					String query = getQuery(context, subfilter, -1, 0);
					list.add(query);
				}
			}

			return list;
		}
	}

	/** Get queries that will be executed during specific dynamic fetch. */
	public List<String> getDynamicFetchQuery(Context context, ManifestProcessor manifestProcessor, Configuration configuration, String dynamicFetchName, Map<String, String> params) throws ManifestProcessingException {
		DynamicFetchConfig fetchConfig = getDynamicFetchByName(dynamicFetchName);
		if (fetchConfig == null) {
			Log.w(TAG, "There is no dynamic fetch config with name: " + dynamicFetchName);
		}

		List<SFQueryFilter> filters = fetchConfig == null ? null : fetchConfig.getFilters();

		if (filters == null || filters.isEmpty()) {
			String query = getQuery(context, null, -1, 0);
			return Collections.singletonList(query);
		}
		else {
			List<String> list = new ArrayList<String>();
			for (SFQueryFilter filter : filters) {
				List<String> subfilters = manifestProcessor.processDynamicFetchFilter(configuration, this, filter, params);
				for (String subfilter : subfilters) {
					String query = getQuery(context, subfilter, -1, 0);
					list.add(query);
				}
			}

			return list;
		}
	}

	/** You can use this method instead of getSyncQuery if you know that your filter doesn't need additional processing. */
	public String getSimpleQuery(Context context, String filter) {
		return getQuery(context, filter, -1, 0);
	}

	/** You can use this method instead of getSyncQuery if you know that your filter doesn't need additional processing. */
	public String getSimpleQuery(Context context) {
		return getSimpleQuery(context, null);
	}

	private String getQuery(Context context, String filter, int page, int pageSize) {
		ArrayList<String> allFieldsToFetch = new ArrayList<>();
		allFieldsToFetch.addAll(fieldsToFetch);
		if (extraFieldsToFetch != null) {
			allFieldsToFetch.addAll(extraFieldsToFetch);
		}

		removeSensitiveFields(context, name, allFieldsToFetch);

		Query query = QueryBuilder.query(page, pageSize).select(allFieldsToFetch).from(name);

		if (filterObjects != null && !filterObjects.isEmpty()) {
			query.filter(filterObjects);
		}
		if (filter != null) {
			query.filter(filter);
		}
		if (limit != 0) {
			query.limit(limit);
		}
		if (orderBy != null && !orderBy.isEmpty()) {
			query.orderBy(orderBy);
		}

		return query.toString();
	}

	private void removeSensitiveFields(Context context, String objectName, ArrayList<String> fields) {
		Set<String> sensitiveFields = getSensitiveFieldsWithNamespace(context, objectName);
		fields.removeAll(sensitiveFields);
	}

	public static Set<String> getSensitiveFieldsWithNamespace(Context context, String objectName) {
		Set<String> fields = new HashSet<>();

		try {
			for (String field : getRawSensitiveFields(context, objectName)) {
				if (field != null) {
					String fieldWithNamespace = ManifestUtils.getNamespaceSupportedFieldName(objectName, field.trim(), context);
					fields.add(fieldWithNamespace);
				}
			}
		}
		catch (Exception e) {
			Log.w(TAG, "Couldn't load sensitive data for: " + objectName, e);
		}

		return fields;
	}

	public static Set<String> getSensitiveFieldsWithoutNamespace(Context context, String objectName) {
		Set<String> fields = new HashSet<>();

		try {
			for (String field : getRawSensitiveFields(context, objectName)) {
				if (field != null) {
					String fieldWithNamespace = ManifestUtils.removeNamespaceFromField(objectName, field.trim(), context);
					fields.add(fieldWithNamespace);
				}
			}
		}
		catch (Exception e) {
			Log.w(TAG, "Couldn't load sensitive data for: " + objectName, e);
		}

		return fields;
	}

	private static Set<String> getRawSensitiveFields(Context context, String objectName) {
		Set<String> fields = new HashSet<>();

		try {
			DataManager dm = DataManagerFactory.getDataManager();
			JSONObject jsonObject = dm.exactQuery("SensitiveData__c", "ObjectName__c", objectName);
			if (jsonObject != null) {
				String fieldsString = jsonObject.optString("FieldNames__c");

				if (!TextUtils.isEmpty(fieldsString)) {
					String[] fieldsArray = fieldsString.split(";");

					for (String field : fieldsArray) {
						if (field != null) {
							fields.add(field);
						}
					}
				}
			}
		}
		catch (Exception e) {
			Log.w(TAG, "Couldn't load sensitive data for: " + objectName, e);
		}

		return fields;
	}
}
