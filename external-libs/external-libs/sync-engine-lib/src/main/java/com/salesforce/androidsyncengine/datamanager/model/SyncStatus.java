package com.salesforce.androidsyncengine.datamanager.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class SyncStatus implements Parcelable {
	
	public SyncStatus() {
		super();
		status = SyncStatusState.NOT_SYNCING;
		stage = SyncStatusStage.NOT_APPLICABLE;
		currentItem = 0;
		totalCount = 0;
		description = "Not Syncing";
	}

	private SyncStatusState status;
	private SyncStatusStage stage;
	private String description;
	private int currentItem;
	private int totalCount;
	
	/**
	 * @return the status
	 */
	public SyncStatusState getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(SyncStatusState status) {
		this.status = status;
		if (status.equals(SyncStatusState.COMPLETED)) {
			currentItem = 0;
			totalCount = 0;
			stage = SyncStatusStage.NOT_APPLICABLE;
		}
	}
	/**
	 * @return the stage
	 */
	public SyncStatusStage getStage() {
		return stage;
	}
	/**
	 * @param stage the stage to set
	 */
	public void setStage(SyncStatusStage stage) {
		this.stage = stage;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
		Log.i("SyncStatus", this.toString());
	}
	/**
	 * @return the currentItem
	 */
	public int getCurrentItem() {
		return currentItem;
	}
	/**
	 * @param currentItem the currentItem to set
	 */
	public void setCurrentItem(int currentItem) {
		this.currentItem = currentItem;
	}
	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}
	/**
	 * @param totalCount the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public String toString() {
		return String.format("status: %s stage: %s \ncurrentItem: %d totalCount: %d\ndescription: %s", status, stage, currentItem, totalCount, description);
		
	}
	
	public enum SyncStatusStage {
		UPLOAD_QUEUE,
		GET_DELETED,
		RUN_LOCAL_CLEANUP,
		FETCH_RECORDS,
		FETCH_UPDATED_RECORDS,
		FETCH_CONTENT,
		NOT_APPLICABLE
	}
	
	public enum SyncStatusState {
		NOT_SYNCING,
		INPROGRESS,
		COMPLETED,
		PENDING
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.status == null ? -1 : this.status.ordinal());
		dest.writeInt(this.stage == null ? -1 : this.stage.ordinal());
		dest.writeString(this.description);
		dest.writeInt(this.currentItem);
		dest.writeInt(this.totalCount);
	}

	protected SyncStatus(Parcel in) {
		int tmpStatus = in.readInt();
		this.status = tmpStatus == -1 ? null : SyncStatusState.values()[tmpStatus];
		int tmpStage = in.readInt();
		this.stage = tmpStage == -1 ? null : SyncStatusStage.values()[tmpStage];
		this.description = in.readString();
		this.currentItem = in.readInt();
		this.totalCount = in.readInt();
	}

	public static final Parcelable.Creator<SyncStatus> CREATOR = new Parcelable.Creator<SyncStatus>() {
		@Override
		public SyncStatus createFromParcel(Parcel source) {
			return new SyncStatus(source);
		}

		@Override
		public SyncStatus[] newArray(int size) {
			return new SyncStatus[size];
		}
	};
}
