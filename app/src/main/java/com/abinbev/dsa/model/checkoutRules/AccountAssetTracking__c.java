package com.abinbev.dsa.model.checkoutRules;

import android.util.Log;

import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountAssetTrackingFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Diana Błaszczyk on 21/11/17.
 */

public class AccountAssetTracking__c extends TranslatableSFBaseObject {

    public static final String TAG = AccountAssetTracking__c.class.getName();

    public AccountAssetTracking__c(JSONObject json) {
        super(AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, json);
    }

    public AccountAssetTracking__c() {
        super(AbInBevObjects.ACCOUNT_ASSET_TRACKING_C);
    }

    public String getStatus() {
        return getStringValueForKey(AccountAssetTrackingFields.STATUS);
    }


    public String getVisitId() {
        return getStringValueForKey(AccountAssetTrackingFields.VISIT_ID);
    }

    public String getReason() {
        return getStringValueForKey(AccountAssetTrackingFields.REASON);
    }

    public String getComment() {
        return getStringValueForKey(AccountAssetTrackingFields.COMMENT);
    }

    public String getTrackingTime() {
        return getStringValueForKey(AccountAssetTrackingFields.TRACKING_TIME);
    }

    public void setStatus(String status) {
        setStringValueForKey(AccountAssetTrackingFields.STATUS, status);
    }

    public void setReason(String reason) {
        setStringValueForKey(AccountAssetTrackingFields.REASON, reason);
    }

    public void setComment(String comment) {
        setStringValueForKey(AccountAssetTrackingFields.COMMENT, comment);
    }

    public void setVisitId(String id) {
        setStringValueForKey(AccountAssetTrackingFields.VISIT_ID, id);
    }

    public void setParentId(String id) {
        setStringValueForKey(AccountAssetTrackingFields.PARENT_ID, id);
    }

    public void setTrackingTime(String time) {
        setStringValueForKey(AccountAssetTrackingFields.TRACKING_TIME, time);
    }

    public static String createAssetTrackingRecord(String parentId, String visitId, String status, String reason,
                                                   String comment,String trackingTime) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AccountAssetTrackingFields.STATUS, status);
            jsonObject.put(AccountAssetTrackingFields.REASON, reason);
            jsonObject.put(AccountAssetTrackingFields.COMMENT, comment);
            jsonObject.put(AccountAssetTrackingFields.VISIT_ID, visitId);
            jsonObject.put(AccountAssetTrackingFields.PARENT_ID, parentId);
            jsonObject.put(AccountAssetTrackingFields.TRACKING_TIME, trackingTime);
            DataManager dm = DataManagerFactory.getDataManager();
            String id = dm.createRecord(AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, jsonObject);
            return id;

        } catch (JSONException e) {
            Log.e(TAG, "createNegotiationForAccount: Error creating AssetTrackingRecord", e);
        }
        return null;
    }

    public static String updateAssetTrackingRecord(String recordId, String trackingTime) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AccountAssetTrackingFields.TRACKING_TIME, trackingTime);
            DataManager dm = DataManagerFactory.getDataManager();
            dm.updateRecord(AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, recordId, jsonObject);
            return  recordId;
        } catch (JSONException e) {
            Log.e(TAG, "createNegotiationForAccount: Error update AssetTrackingRecord", e);
        }
        return null;
    }

    public static List<AccountAssetTracking__c> getTrackingByAssetId(String accountAssetId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC Limit 1",
                AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, AbInBevConstants.AccountAssetTrackingFields.PARENT_ID, accountAssetId,
                AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, AccountAssetTrackingFields.TRACKING_TIME);
        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, smartSqlFilter);
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, AccountAssetTracking__c.class);
    }

    public static List<AccountAssetTracking__c> getTrackingByAssetIdAndVisitId(String accountAssetId, String visitId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' ORDER BY {%s:%s} DESC Limit 1",
                AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, AbInBevConstants.AccountAssetTrackingFields.PARENT_ID, accountAssetId,
                AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, AbInBevConstants.AccountAssetTrackingFields.VISIT_ID, visitId,
                AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, AccountAssetTrackingFields.TRACKING_TIME);
        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_TRACKING_C, smartSqlFilter);
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, AccountAssetTracking__c.class);
    }

    public static boolean upsertAssetTrackingRecord(AccountAssetTracking__c assetTracking) {
        return assetTracking.upsertRecord();
    }
}
