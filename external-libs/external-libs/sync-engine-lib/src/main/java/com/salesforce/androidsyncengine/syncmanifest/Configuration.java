/**
 * 
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce.com, inc. 
 *
 */

package com.salesforce.androidsyncengine.syncmanifest;

import net.sf.oval.constraint.NotNull;

import java.util.Map;

public class Configuration {

	private boolean enableLogging = false;
	private boolean allowBackup = false;
	@NotNull
	private BackgroundSync backgroundSync;
	private int syncFrequency; // 0 indicates manual sync
	private int purgeFrequency;
	private float manifestVersion;
	private boolean shouldCheckForDeleted = true;
	private String nameSpacePrefix;
	private String shouldRunLocalCleanup;
	private String shouldFetchDeletedRecords;

	private Map<String, LocalQuery> localQueries;

	public float getManifestVersion() {
		return manifestVersion;
	}

	public void setManifestVersion(float manifestVersion) {
		this.manifestVersion = manifestVersion;
	}

	public boolean isEnableLogging() {
		return enableLogging;
	}

	public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}

	public boolean isAllowBackup() {
		return allowBackup;
	}

	public void setAllowBackup(boolean allowBackup) {
		this.allowBackup = allowBackup;
	}

	public BackgroundSync getBackgroundSync() {
		return backgroundSync;
	}

	public void setBackgroundSync(BackgroundSync backgroundSync) {
		this.backgroundSync = backgroundSync;
	}

	public int getSyncFrequency() {
		return syncFrequency;
	}

	public void setSyncFrequency(int syncFrequency) {
		this.syncFrequency = syncFrequency;
	}

	public boolean shouldCheckForDeleted() {
		return shouldCheckForDeleted;
	}

	public void setShouldCheckForDeleted(boolean shouldCheckForDeleted) {
		this.shouldCheckForDeleted = shouldCheckForDeleted;
	}

	public int getPurgeFrequency() {
		return purgeFrequency;
	}

	public void setPurgeFrequency(int purgeFrequency) {
		this.purgeFrequency = purgeFrequency;
	}

	public String getNameSpacePrefix() {
		return nameSpacePrefix;
	}

	public void setNameSpacePrefix(String nameSpacePrefix) {
		this.nameSpacePrefix = nameSpacePrefix;
	}

	public Map<String, LocalQuery> getLocalQueries() {
		return localQueries;
	}

	public LocalQuery getLocalQuery(String queryName) {
		return localQueries == null ? null : localQueries.get(queryName);
	}

	public void setLocalQueries(Map<String, LocalQuery> localQueries) {
		this.localQueries = localQueries;
	}

	public String getShouldRunLocalCleanup() {
		return shouldRunLocalCleanup;
	}

	public void setShouldRunLocalCleanup(String shouldRunLocalCleanup) {
		this.shouldRunLocalCleanup = shouldRunLocalCleanup;
	}

	public String getShouldFetchDeletedRecords() {
		return shouldFetchDeletedRecords;
	}

	public void setShouldFetchDeletedRecords(String shouldFetchDeletedRecords) {
		this.shouldFetchDeletedRecords = shouldFetchDeletedRecords;
	}

	@Override
	public String toString() {
		return "Configuration [enableLogging=" + enableLogging
				+ ", allowBackUp=" + allowBackup + ", backgroundSync="
				+ backgroundSync + ", syncFrequency=" + syncFrequency
				+ ", manifestVersion=" + manifestVersion + "]";
	}
}
