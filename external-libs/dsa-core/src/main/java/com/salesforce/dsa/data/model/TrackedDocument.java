package com.salesforce.dsa.data.model;

public class TrackedDocument {

    private ContentVersion contentVersion;
    private boolean markedToEmail;
    private float rating;
    private long startTimeMillis;
    private long endTimeInMillis;
    private boolean isTracking;

    public TrackedDocument(ContentVersion contentVersion) {
        this.contentVersion = contentVersion;
        this.startTimeMillis = System.currentTimeMillis();
        this.isTracking = true;
    }

    public ContentVersion getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(ContentVersion contentVersion) {
        this.contentVersion = contentVersion;
    }

    public boolean isMarkedToEmail() {
        return markedToEmail;
    }

    public void setMarkedToEmail(boolean markedToEmail) {
        this.markedToEmail = markedToEmail;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public long getStartTimeInSecs() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeInMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeInMillis = endTimeMillis;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void setTracking(boolean isTracking) {
        this.isTracking = isTracking;
    }

}
