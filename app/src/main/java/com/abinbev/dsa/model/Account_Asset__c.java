package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountAssetFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Account_Asset__c extends TranslatableSFBaseObject {

    public static final String TAG = Account_Asset__c.class.getName();
    public static final String ASSET_STATUS_IN_STOCK = "In Stock";
    public static final String ASSET_STATUS_INSTALLED = "Installed";
    public static final String CN_CLEAN_STATUS_CLEANED = "Cleaned";
    public static final String CN_CLEAN_STATUS_NOT_CLEANED = "Not Cleaned";

    public Account_Asset__c(JSONObject json) {
        super(AbInBevObjects.ACCOUNT_ASSET_C, json);
    }

    protected Account_Asset__c() {
        super(AbInBevObjects.ACCOUNT_ASSET_C);
    }

    public String getSerialNumber() {
        return getStringValueForKey(AccountAssetFields.SERIAL_NUMBER);
    }

    public String getBrand() {
        return getStringValueForKey(AccountAssetFields.BRAND);
    }

    public String getCode() {
        return getStringValueForKey(AccountAssetFields.CODE);
    }

    public String getType() {
        return getStringValueForKey(AccountAssetFields.TYPE);
    }

    public String getQuantity() {
        return getStringValueForKey(AccountAssetFields.QUANTITY);
    }

    public String getStatus() {
        return getStringValueForKey(AccountAssetFields.STATEMENT);
    }

    public String getReason() {
        return getStringValueForKey(AccountAssetFields.REASON);
    }

    public String getComment() {
        return getStringValueForKey(AccountAssetFields.COMMENT);
    }

    public String getQRcode() {
        return getStringValueForKey(AccountAssetFields.QR_CODE);
    }

    public String getWifiTag() {
        return getStringValueForKey(AccountAssetFields.WIFI_TAG);
    }

    public String getAssetName() {
        return getStringValueForKey(AccountAssetFields.ASSET_NAME);
    }

    public String getSupplementaryQrCode() {
        return getStringValueForKey(AccountAssetFields.CN_SUPPLEMENTARY_QR_CODE);
    }

    public String getCN_AssetCategory() {
        return getStringValueForKey(AccountAssetFields.CN_ASSET_CATEGORY);
    }

    public String getCN_CleanStatus() {
        return getStringValueForKey(AccountAssetFields.CN_CLEAN_STATUS);
    }

    public String getCN_LatestCleanTime() {
        return getStringValueForKey(AccountAssetFields.CN_LATEST_CLEAN_TIME);
    }

    public String getWarrantyDate() {
        return getStringValueForKey(AccountAssetFields.WARRANTY_DATE);
    }

    public String getInventaryDate() {
        return getStringValueForKey(AccountAssetFields.INVENTORY_DATE);
    }

    public String getRecordTypeId() {
        return getStringValueForKey(AccountAssetFields.RECORD_TYPE_ID);
    }

    public String getDescription() {
        return getStringValueForKey(AccountAssetFields.DESCRIPTION__C);
    }

    public String getAccountId() {
        return getStringValueForKey(AccountAssetFields.CLIENT);
    }

    public void setStatus(String status) {
        setStringValueForKey(AccountAssetFields.STATEMENT, status);
    }

    public void setReason(String reason) {
        setStringValueForKey(AccountAssetFields.REASON, reason);
    }

    public void setComment(String comment) {
        setStringValueForKey(AccountAssetFields.COMMENT, comment);
    }

    public void setAccountId(String account__c) {
        setStringValueForKey(AccountAssetFields.CLIENT, account__c);
    }

    public void setQrCode(String qrCode) {
        setStringValueForKey(AccountAssetFields.QR_CODE, qrCode);
    }

    public void setCN_QR_Code(String qr_code) {
        setStringValueForKey(AccountAssetFields.CN_QR_CODE, qr_code);
    }

    public void setSupplementaryQrCode(String supplementaryQrCode) {
        setStringValueForKey(AccountAssetFields.CN_SUPPLEMENTARY_QR_CODE, supplementaryQrCode);
    }

    public void setAssetName(String assetName) {
        setStringValueForKey(AccountAssetFields.ASSET_NAME, assetName);
    }

    public void setAssetType(String assetType) {
        setStringValueForKey(AccountAssetFields.TYPE, assetType);
    }

    public static int getCustomerAssetsCountByAccountId(String accountId) {
        int count = 0;
        count += getCustomerAssetsCountByAccountIdAndRecordType(accountId,
                RecordType.getByName(AssetsListAdapter.SERIALIZED_RECORD_NAME));
        count += getCustomerAssetsCountByAccountIdAndRecordType(accountId,
                RecordType.getByName(AssetsListAdapter.NO_SERIALIZED_RECORD_NAME));
        return count;
    }

    public static int getCustomerAssetsCountByOwnerId(String ownerId) {
        int count = 0;
        count += getCustomerAssetsCountByUserIdAndRecordType(ownerId,
                RecordType.getByName(AssetsListAdapter.SERIALIZED_RECORD_NAME));
        count += getCustomerAssetsCountByUserIdAndRecordType(ownerId,
                RecordType.getByName(AssetsListAdapter.NO_SERIALIZED_RECORD_NAME));
        return count;
    }

    private static int getCustomerAssetsCountByAccountIdAndRecordType(String accountId, RecordType recordType) {
        int count = 0;

        if (recordType == null) {
            Log.e(TAG, "NULL recorddType passed in");
            return count;
        }
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CLIENT, accountId,
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.RECORD_TYPE_ID, recordType.getId());

            String smartSql = String
                    .format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting customer assets by Account ID: " + accountId, e);
        }
        return count;
    }

    private static int getCustomerAssetsCountByUserIdAndRecordType(String userId, RecordType recordType) {
        int count = 0;

        if (recordType == null) {
            Log.e(TAG, "NULL recorddType passed in");
            return count;
        }
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.OWNER, userId,
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.RECORD_TYPE_ID, recordType.getId());

            String smartSql = String
                    .format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting customer assets by User ID: " + userId, e);
        }
        return count;
    }

    public static List<Account_Asset__c> getCustomerAssets(String accountId, String recordName) {
        RecordType recordType = RecordType.getByName(recordName);
        if (TextUtils.isEmpty(recordType.getId())) {
            Log.e(TAG, "Unable to find RecordType with name: " + recordName);
            return Collections.emptyList();
        } else {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' ORDER BY {%s:%s} ASC",
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CLIENT, accountId,
                    AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.RECORD_TYPE_ID, recordType.getId(),
                    AbInBevObjects.ACCOUNT_ASSET_C, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE);

            String smartSql = String
                    .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);

            DataManager dm = DataManagerFactory.getDataManager();
            return DataManagerUtils.fetchObjects(dm, smartSql, Account_Asset__c.class);
        }
    }

    public static Account_Asset__c getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, AbInBevObjects.ACCOUNT_ASSET_C, id, Account_Asset__c.class);
    }

    public static boolean auditAsset(String assetId, LatLng latLng) {
        try {
            JSONObject jsonObject = new JSONObject();
            Calendar cal = Calendar.getInstance();
            String now = DateUtils.SERVER_DATE_TIME_FORMAT.format(cal.getTime());
            jsonObject.put(AccountAssetFields.INVENTORY_DATE, now);
            if (latLng != null) {
                jsonObject.put(AccountAssetFields.LATITUD__C, latLng.latitude);
                jsonObject.put(AccountAssetFields.LONGITUD__C, latLng.longitude);
            }
            return DataManagerFactory.getDataManager()
                    .updateRecord(AbInBevObjects.ACCOUNT_ASSET_C, assetId, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "audit assets failed", e);
        }
        return false;
    }

    public static String createAccountAsset(String recordName, String assetStatus, String ONTAP__Account__c, String ONTAP__QR_Code__c, String ONTAP__Asset_Name__c, String ONTAP__Asset_Type__c) {
        RecordType recordType = RecordType.getByName(recordName);
        if (recordType == null || TextUtils.isEmpty(recordType.getId())) {
            Log.e(TAG, "Unable to find RecordType with name: " + recordName);
            throw new RuntimeException("unknown recordType:" + recordName);
        }
        Account_Asset__c asset__c = new Account_Asset__c();
        asset__c.setAccountId(ONTAP__Account__c);
        asset__c.setCN_QR_Code(null);
        asset__c.setQrCode(null);
        asset__c.setSupplementaryQrCode(ONTAP__QR_Code__c);
        asset__c.setAssetName(ONTAP__Asset_Name__c);
        asset__c.setAssetType(ONTAP__Asset_Type__c);
        asset__c.setRecordTypeId(recordType.getId());
        asset__c.setStatus(assetStatus);
        return asset__c.createRecord();
    }

    public static void updateAccountAsset(String recordName,String assetId, String ONTAP__Asset_Name__c, String ONTAP__Asset_Type__c) {
//        RecordType recordType = RecordType.getByName(recordName);
//        if (recordType == null || TextUtils.isEmpty(recordType.getId())) {
//            Log.e(TAG, "Unable to find RecordType with name: " + recordName);
//            throw new RuntimeException("unknown recordType:" + recordName);
//        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(AccountAssetFields.ASSET_NAME, ONTAP__Asset_Name__c);
            jsonObject.put(AccountAssetFields.CN_ASSET_CATEGORY, ONTAP__Asset_Type__c);
            DataManagerFactory.getDataManager()
                    .updateRecord(AbInBevObjects.ACCOUNT_ASSET_C, assetId, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "updateAccountAsset failed", e);
        }
    }

    public static Account_Asset__c getAccount_AssetByQrCode(String accountId, String qrCode) {
        Account_Asset__c asset = null;
        String smartSqlFilter = String.format("({%s:%s} = '%s' OR {%s:%s} = '%s') AND {%s:%s} = '%s' ORDER BY {%s:%s} ASC",
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.QR_CODE, qrCode,
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CN_SUPPLEMENTARY_QR_CODE, qrCode,
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CLIENT, accountId,
                AbInBevObjects.ACCOUNT_ASSET_C, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE);

        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        List<Account_Asset__c> list = DataManagerUtils.fetchObjects(dm, smartSql, Account_Asset__c.class);
        if (list != null && !list.isEmpty()) {
            asset = list.get(0);
        }
        return asset;
    }

    public static String getAccount_AssetBySqrCode(String accountId, String qrCode) {
        Account_Asset__c asset;
        String assetId = null;
        String smartSqlFilter = String.format("({%s:%s} = '%s') AND {%s:%s} = '%s' ORDER BY {%s:%s} ASC",
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CN_SUPPLEMENTARY_QR_CODE, qrCode,
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CLIENT, accountId,
                AbInBevObjects.ACCOUNT_ASSET_C, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE);

        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        List<Account_Asset__c> list = DataManagerUtils.fetchObjects(dm, smartSql, Account_Asset__c.class);
        if (list != null && !list.isEmpty()) {
            asset = list.get(0);
            assetId = asset.getId();
        }
        return assetId;
    }

    public static List<Account_Asset__c> getAssetByAccountId(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} ASC",
                AbInBevObjects.ACCOUNT_ASSET_C, AccountAssetFields.CLIENT, accountId,
                AbInBevObjects.ACCOUNT_ASSET_C, SyncEngineConstants.StdFields.LAST_MODIFIED_DATE);

        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ACCOUNT_ASSET_C, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, Account_Asset__c.class);
    }
}
