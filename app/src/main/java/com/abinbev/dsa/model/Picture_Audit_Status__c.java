package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PictureAuditStatusFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.List;

public class Picture_Audit_Status__c extends SFBaseObject {

    public static final String TAG = Picture_Audit_Status__c.class.getSimpleName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("PictureAuditStatus", AbInBevObjects.PICTURE_AUDIT_STATUS)
                .putColumn("Status", PictureAuditStatusFields.STATUS)
                .putColumn("CreatedDate", PictureAuditStatusFields.CREATED_DATE)
                .putColumn("ParentId", PictureAuditStatusFields.PARENT_ID)
                .putColumn("PictureReference", PictureAuditStatusFields.STORAGE_PICTURE_REFERENCE)
            .build();

    public static final class StoredIn {
        public static final String AZURE = "Azure";
        public static final String SALESFORCE = "Salesforce";

        private StoredIn() {}
    }

    public static final class Status {
        public static final String REJECTED = "Rejected";
        public static final String PENDING = "Pending";
        public static final String AWAITING_UPLOAD = "Awaiting upload";

        private Status() {}
    }

    protected Picture_Audit_Status__c() {
        super(AbInBevObjects.PICTURE_AUDIT_STATUS);
    }

    public Picture_Audit_Status__c(JSONObject json) {
        super(AbInBevObjects.PICTURE_AUDIT_STATUS, json);
    }

    public String getStatus() {
        return getStringValueForKey(PictureAuditStatusFields.STATUS);
    }

    public void setStatus(String status) {
        setStringValueForKey(PictureAuditStatusFields.STATUS, status);
    }

    public String getStoredIn() {
        return getStringValueForKey(PictureAuditStatusFields.STORED_IN);
    }

    public void setStoredIn(String storedIn) {
        setStringValueForKey(PictureAuditStatusFields.STORED_IN, storedIn);
    }

    public String getPictureReference() {
        return getStringValueForKey(PictureAuditStatusFields.STORAGE_PICTURE_REFERENCE);
    }

    public void setPictureReference(String storedIn) {
        setStringValueForKey(PictureAuditStatusFields.STORAGE_PICTURE_REFERENCE, storedIn);
    }

    public String getParentEventId() {
        return getStringValueForKey(PictureAuditStatusFields.PARENT_EVENT);
    }

    public void setParentEventId(String eventId) {
        setStringValueForKey(PictureAuditStatusFields.PARENT_EVENT, eventId);
    }

    public String getParentId() {
        return getStringValueForKey(PictureAuditStatusFields.PARENT_ID);
    }

    public void setParentId(String accountId) {
        setStringValueForKey(PictureAuditStatusFields.PARENT_ID, accountId);
    }

    public String getParentObjectName() {
        return getStringValueForKey(PictureAuditStatusFields.PARENT_OBJECT_NAME);
    }

    public void setParentObjectName(String objectName) {
        setStringValueForKey(PictureAuditStatusFields.PARENT_OBJECT_NAME, objectName);
    }

    public String getReadableCreatedDate() {
        if (isNullOrEmpty(PictureAuditStatusFields.CREATED_DATE)) return null;
        return DateUtils.fromServerDateTimeToDateTime(getCreatedDate());
    }

    public String createRecord(DataManager dm) {
        String recordId = dm.createRecord(AbInBevObjects.PICTURE_AUDIT_STATUS, toJson());
        setStringValueForKey(PictureAuditStatusFields.ID, recordId);
        return recordId;
    }

    public boolean updateRecord(DataManager dm) {
        return dm.updateRecord(AbInBevObjects.PICTURE_AUDIT_STATUS, getId(), toJson());
    }

    public static List<Picture_Audit_Status__c> getRejected() {
        FormatValues formatValues = new FormatValues();
        formatValues.addAll(OBJECT_FORMAT_VALUES);
        formatValues.putValue("rejected", Status.REJECTED);

        String sql = "SELECT {PictureAuditStatus:_soup} FROM {PictureAuditStatus} " +
                "WHERE {PictureAuditStatus:Status} = '{rejected}' " +
                "ORDER BY {PictureAuditStatus:CreatedDate} DESC";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, sql, formatValues, Picture_Audit_Status__c.class);
    }

    public static int getRejectedCount() {
        FormatValues formatValues = new FormatValues();
        formatValues.addAll(OBJECT_FORMAT_VALUES);
        formatValues.putValue("rejected", Status.REJECTED);

        String sql = "SELECT count() FROM {PictureAuditStatus} WHERE {PictureAuditStatus:Status} = '{rejected}'";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, sql, formatValues);
    }

    public static List<Picture_Audit_Status__c> getBy(String parentId, String fileName) {
        FormatValues formatValues = new FormatValues();
        formatValues.addAll(OBJECT_FORMAT_VALUES);
        formatValues.putValue("parentId", parentId);
        formatValues.putValue("fileName", fileName);

        String sql = "SELECT {PictureAuditStatus:_soup} FROM {PictureAuditStatus} " +
                "WHERE {PictureAuditStatus:ParentId} = '{parentId}' " +
                "AND {PictureAuditStatus:PictureReference} = '{fileName}'";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, sql, formatValues, Picture_Audit_Status__c.class);
    }
}
