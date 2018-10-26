/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */

package com.salesforce.androidsyncengine.syncmanifest;

import android.text.TextUtils;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.MinSize;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.ValidateWithMethod;

import java.util.Arrays;
import java.util.List;

public class ConfigObject {

	@NotNull
	@NotEmpty
	private String objectName;
	private SyncDirection syncDirection = SyncDirection.BOTH;
	private String[] dependencies; 
	@ValidateWithMethod(
		methodName = "validateFieldProperties", 
		parameterType = Object.class,
		message = "FieldsToIgnore.violated"
	)
	private String[] fieldsToIgnore;
	private String[] fieldsToFetch;
	private String[] cleanupQueryFilters;

	private List<SFQueryFilter> filters;
	private List<SFQueryFilter> dynamicFetchFilters;
	private String checkLastModify;
	private boolean fetchAllFields;
	private int limit;
	@AssertValid
	@MinSize(value = 1)
	private FieldToIndex[] fieldsToIndex;
	@AssertValid
	private List<OrderByField> orderBy;
	private String[] localFields;
	@AssertValid
	private FileInfo fileInfo; //TODO: validate fileInfo fields if present are always fetched 
	@AssertValid
	private FieldRelationship fieldRelationship; //TODO: make it array and fix code 
	private boolean shouldCheckForDeleted = true;
	private boolean purgeEnabled = false;
	private String[] extraFieldsToFetch;
	private boolean shouldFetchLayoutMetadata = false;
	private boolean shouldFetchCompactLayoutMetadata = false;
	private boolean dateQuery = false;
	private boolean shouldUseNamespace = false;
	private List<DynamicFetchConfig> dynamicFetch;

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public SyncDirection getSyncDirection() {
		return syncDirection;
	}

	public void setSyncDirection(SyncDirection syncDirection) {
		this.syncDirection = syncDirection;
	}

	public String[] getFieldsToIgnore() {
		return fieldsToIgnore;
	}

	public void setFieldsToIgnore(String[] fieldsToIgnore) {
		this.fieldsToIgnore = fieldsToIgnore;
	}

	public String[] getFieldsToFetch() {
		return fieldsToFetch;
	}

	public void setFieldsToFetch(String[] fieldsToFetch) {
		this.fieldsToFetch = fieldsToFetch;
	}

	public String[] getCleanupQueryFilters() {
		return cleanupQueryFilters;
	}

	public void setCleanupQueryFilters(String[] cleanupQueryFilters) {
		this.cleanupQueryFilters = cleanupQueryFilters;
	}
	public String getCheckLastModify(){
		return this.checkLastModify;
	}

	public void setCheckLastModify(String checkLastModify){
		this.checkLastModify=checkLastModify;
	}
	public boolean isFetchAllFields() {
		return fetchAllFields;
	}

	public void setFetchAllFields(boolean fetchAllFields) {
		this.fetchAllFields = fetchAllFields;
	}

	public List<SFQueryFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<SFQueryFilter> filters) {
		this.filters = filters;
	}

	public List<SFQueryFilter> getDynamicFetchFilters() {
		return dynamicFetchFilters;
	}

	public void setDynamicFetchFilters(List<SFQueryFilter> dynamicFetchFilters) {
		this.dynamicFetchFilters = dynamicFetchFilters;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public FieldToIndex[] getFieldsToIndex() {
		return fieldsToIndex;
	}

	public void setFieldsToIndex(FieldToIndex[] fieldsToIndex) {
		this.fieldsToIndex = fieldsToIndex;
	}

	public List<OrderByField> getOrderByArray() {
		return orderBy;
	}

	public void setOrderBy(List<OrderByField> orderBy) {
		this.orderBy = orderBy;
	}
	
	public List<OrderByField> getOrderBy() {
		return orderBy;
	}

	public String[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(String[] dependencies) {
		this.dependencies = dependencies;
	}

	public String[] getLocalFields() {
		return localFields;
	}

	public void setLocalFields(String[] localFields) {
		this.localFields = localFields;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	public FieldRelationship getFieldRelationship() {
		return fieldRelationship;
	}

	public void setFieldRelationship(FieldRelationship fieldRelationship) {
		this.fieldRelationship = fieldRelationship;
	}

	public boolean isShouldCheckForDeleted() {
		return shouldCheckForDeleted;
	}

	public void setShouldCheckForDeleted(boolean shouldCheckForDeleted) {
		this.shouldCheckForDeleted = shouldCheckForDeleted;
	}

	public boolean isPurgeEnabled() {
		return purgeEnabled;
	}

	public void setPurgeEnabled(boolean purgeEnabled) {
		this.purgeEnabled = purgeEnabled;
	}

	public String[] getExtraFieldsToFetch() {
		return extraFieldsToFetch;
	}

	public void setExtraFieldsToFetch(String[] extraFieldsToFetch) {
		this.extraFieldsToFetch = extraFieldsToFetch;
	}

	public boolean isShouldFetchLayoutMetadata() {
		return shouldFetchLayoutMetadata;
	}

	public void setShouldFetchLayoutMetadata(boolean shouldFetchLayoutMetadata) {
		this.shouldFetchLayoutMetadata = shouldFetchLayoutMetadata;
	}

	public boolean isDateQuery() {
		return dateQuery;
	}

	public void setDateQuery(boolean dateQuery) {
		this.dateQuery = dateQuery;
	}

	public boolean isShouldUseNamespace() {
		return shouldUseNamespace;
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

	public boolean isShouldFetchCompactLayoutMetadata() {
		return shouldFetchCompactLayoutMetadata;
	}

	public void setShouldFetchCompactLayoutMetadata(boolean shouldFetchCompactLayoutMetadata) {
		this.shouldFetchCompactLayoutMetadata = shouldFetchCompactLayoutMetadata;
	}

	@Override
	public String toString() {
		return "ConfigObject{" +
				"objectName='" + objectName + '\'' +
				", syncDirection=" + syncDirection +
				", dependencies=" + Arrays.toString(dependencies) +
				", fieldsToIgnore=" + Arrays.toString(fieldsToIgnore) +
				", fieldsToFetch=" + Arrays.toString(fieldsToFetch) +
				", cleanupQueryFilters=" + Arrays.toString(cleanupQueryFilters) +
				", filters=" + filters +
				", fetchAllFields=" + fetchAllFields +
				", limit=" + limit +
				", fieldsToIndex=" + Arrays.toString(fieldsToIndex) +
				", orderBy=" + orderBy +
				", localFields=" + Arrays.toString(localFields) +
				", fileInfo=" + fileInfo +
				", fieldRelationship=" + fieldRelationship +
				", shouldCheckForDeleted=" + shouldCheckForDeleted +
				", purgeEnabled=" + purgeEnabled +
				", extraFieldsToFetch=" + Arrays.toString(extraFieldsToFetch) +
				", shouldFetchLayoutMetadata=" + shouldFetchLayoutMetadata +
				", shouldFetchCompactLayoutMetadata=" + shouldFetchCompactLayoutMetadata +
				", dateQuery=" + dateQuery +
				", shouldUseNamespace=" + shouldUseNamespace +
				", dynamicFetch=" + dynamicFetch +
				'}';
	}

	/**
	 * We need one of fieldsToFetch, fieldsToignore, fetchAllFields
	 * @param value
	 * @return boolean
	 */
	@SuppressWarnings("unused")
	private boolean validateFieldProperties(Object value) {
		// we need one and only one of these to be present
		int present = 0;
		if (!isNullOrEmpty(getFieldsToFetch())) {
			present++;
		}
		if (!isNullOrEmpty(getFieldsToIgnore())) {
			present++;
		}
		if (isFetchAllFields()) {
			present++;
		}

		if (present != 1) {
			return false;
		}

		return true;
	}
	
	private boolean isNullOrEmpty(String[] obj) {
		return obj == null || obj.length == 0;
	}
}
