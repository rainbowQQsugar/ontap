package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProductNegotiationFields;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Diana Błaszczyk on 16/10/17.
 */

public class CN_Product_Negotiation__c extends TranslatableSFBaseObject {

    public static final String TAG = CN_Product_Negotiation__c.class.getName();
    public static final String STATUS_IN_NEGOTIATION = "In Negotiation";
    public static final String STATUS_CONFIRMED = "Confirmed";

    public static final String FIELD_RECORD_NAME = AbInBevObjects.RECORD_TYPE +
            "." + RecordTypeFields.NAME;

    public CN_Product_Negotiation__c(JSONObject json) {
        super(AbInBevObjects.PRODUCT_NEGOTIATIONS, json);
    }

    public CN_Product_Negotiation__c() {
        super(AbInBevObjects.PRODUCT_NEGOTIATIONS);
    }

    public String getProductName() {
        Product product = Product.getById(getProductId());
        if (product != null)
            return product.getProductShortName().isEmpty() ? product.getProductName() : product.getProductShortName();
        return "";
    }

    public String getTranslatedType() {
        return getTranslatedStringValueForKey(ProductNegotiationFields.PROMOTION_TYPE);
    }

    public String getType() {
        return getStringValueForKey(ProductNegotiationFields.PROMOTION_TYPE);
    }

    public String getDescription() {
        return getStringValueForKey(ProductNegotiationFields.DESCRIPTION);
    }

    public String getAccount() {
        return getStringValueForKey(ProductNegotiationFields.ACCOUNT);
    }

    public void setAccount(String accountId) {
        setStringValueForKey(ProductNegotiationFields.ACCOUNT, accountId);
    }

    public String getStatus() {
        return getStringValueForKey(ProductNegotiationFields.STATUS);
    }

    public String getTranslatedStatus() {
        return getTranslatedStringValueForKey(ProductNegotiationFields.STATUS);
    }

    public void setStatus(String status) {
        setStringValueForKey(ProductNegotiationFields.STATUS, status);
    }

    public String getCategory() {
        return getStringValueForKey(ProductNegotiationFields.CATEGORY);
    }

    public String getBrand() {
        return getStringValueForKey(ProductNegotiationFields.BRAND);
    }

    public void setBrand(String brand) {
        setValueForKey(ProductNegotiationFields.BRAND, brand);
    }

    public String getUnit() {
        return getStringValueForKey(ProductNegotiationFields.UNIT);
    }

    public void setUnit(String unit) {
        setValueForKey(ProductNegotiationFields.UNIT, unit);
    }

    public String getPackage() {
        return getStringValueForKey(ProductNegotiationFields.PACKAGE);
    }

    public void setPackage(String strPackage) {
        setValueForKey(ProductNegotiationFields.PACKAGE, strPackage);
    }

    public String getPTR() {
        return getStringValueForKey(ProductNegotiationFields.PTR);
    }

    public String getProductId() {
        return getStringValueForKey(ProductNegotiationFields.PRODUCT);
    }

    public void setProductId(String productId) {
        setValueForKey(ProductNegotiationFields.PRODUCT, productId);
    }

    public String getNegotiationId() {
        return getStringValueForKey(ProductNegotiationFields.NAME);
    }

    public String getRecordTypeId() {
        return getStringValueForKey(ProductNegotiationFields.RECORD_TYPE_ID);
    }

    public String getStartTime() {
        return getStringValueForKey(ProductNegotiationFields.START_TIME);
    }

    public void setStartTime(String startTime) {
        setValueForKey(ProductNegotiationFields.START_TIME, startTime);
    }

    public String getEndTime() {
        return getStringValueForKey(ProductNegotiationFields.END_TIME);
    }

    public void setEndTime(String endTime) {
        setValueForKey(ProductNegotiationFields.END_TIME, endTime);
    }

    public boolean isCompleted() {
        return "Confirmed".equals(getStatus());
    }

    public static String createSellInNegotiationForAccount(String accountId, String status, String productId, String pckg,
                                                           String ptr, String brand, String unit) {

        RecordType rt = RecordType.getByNameAndObjectType("Product Sell-In", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ProductNegotiationFields.ACCOUNT, accountId);
            jsonObject.put(ProductNegotiationFields.STATUS, status);
            jsonObject.put(ProductNegotiationFields.BRAND, brand);
            jsonObject.put(ProductNegotiationFields.UNIT, unit);
            jsonObject.put(ProductNegotiationFields.PTR, ptr);
            jsonObject.put(ProductNegotiationFields.PACKAGE, pckg);
            jsonObject.put(ProductNegotiationFields.PRODUCT, productId);
            jsonObject.put(ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());

            DataManager dm = DataManagerFactory.getDataManager();
            String newId = dm.createRecord(AbInBevObjects.PRODUCT_NEGOTIATIONS, jsonObject);

            Account account = DataManagerUtils.getById(dm, accountId, Account.class);
//            if (account != null && account.isProspect()) {
//                account.changeProspectStatusNegotiationUpdated();
//                DataManagerUtils.update(dm, account);
//            }

            return newId;

        } catch (JSONException e) {
            Log.e(TAG, "createSellInNegotiationForAccount: Error creating Negotiation", e);
        }
        return null;
    }

    public static String createPromotionNegotiationForAccount(String accountId, String status, String type, String category, String description,
                                                              String productId, String brand, String unit) {

        RecordType rt = RecordType.getByNameAndObjectType("Promotion", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ProductNegotiationFields.ACCOUNT, accountId);
            jsonObject.put(ProductNegotiationFields.STATUS, status);
            jsonObject.put(ProductNegotiationFields.PROMOTION_TYPE, type);
            jsonObject.put(ProductNegotiationFields.CATEGORY, category);
            jsonObject.put(ProductNegotiationFields.DESCRIPTION, description);
            jsonObject.put(ProductNegotiationFields.PRODUCT, productId);
            jsonObject.put(ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());
            jsonObject.put(ProductNegotiationFields.BRAND, brand);
            jsonObject.put(ProductNegotiationFields.UNIT, unit);

            DataManager dm = DataManagerFactory.getDataManager();
            String newId = dm.createRecord(AbInBevObjects.PRODUCT_NEGOTIATIONS, jsonObject);

            Account account = DataManagerUtils.getById(dm, accountId, Account.class);
//            if (account != null && account.isProspect()) {
//                account.changeProspectStatusNegotiationUpdated();
//                DataManagerUtils.update(dm, account);
//            }

            return newId;

        } catch (JSONException e) {
            Log.e(TAG, "createPromotionNegotiationForAccount: Error creating Negotiation", e);
        }
        return null;
    }


    public static boolean updatePromotionNegotiation(String accountId, String negotiationId, String status, String type, String category, String description,
                                                     String productId, String brand, String unit) {

        RecordType rt = RecordType.getByNameAndObjectType("Promotion", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ProductNegotiationFields.ACCOUNT, accountId);
            jsonObject.put(ProductNegotiationFields.STATUS, status);
            jsonObject.put(ProductNegotiationFields.PROMOTION_TYPE, type);
            jsonObject.put(ProductNegotiationFields.CATEGORY, category);
            jsonObject.put(ProductNegotiationFields.DESCRIPTION, description);
            jsonObject.put(ProductNegotiationFields.PRODUCT, productId);
            jsonObject.put(ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());
            jsonObject.put(ProductNegotiationFields.BRAND, brand);
            jsonObject.put(ProductNegotiationFields.UNIT, unit);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.PRODUCT_NEGOTIATIONS, negotiationId, jsonObject);

        } catch (JSONException e) {
            Log.e(TAG, "createPromotionNegotiationForAccount: Error updating product negotiation", e);
        }
        return false;
    }

    public static boolean updateSellInNegotiation(String negotiationId, String accountId, String status, String productId, String pckg,
                                                  String ptr, String brand, String unit) {

        RecordType rt = RecordType.getByNameAndObjectType("Product Sell-In", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ProductNegotiationFields.ACCOUNT, accountId);
            jsonObject.put(ProductNegotiationFields.STATUS, status);
            jsonObject.put(ProductNegotiationFields.BRAND, brand);
            jsonObject.put(ProductNegotiationFields.UNIT, unit);
            jsonObject.put(ProductNegotiationFields.PTR, ptr);
            jsonObject.put(ProductNegotiationFields.PACKAGE, pckg);
            jsonObject.put(ProductNegotiationFields.PRODUCT, productId);
            jsonObject.put(ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.PRODUCT_NEGOTIATIONS, negotiationId, jsonObject);

        } catch (JSONException e) {
            Log.e(TAG, "createSellInNegotiationForAccount: Error updating product negotiation", e);
        }
        return false;
    }

    public static int getActivePromotionsCountByAccountId(String accountId) {
        int count = 0;
        RecordType rt = RecordType.getByNameAndObjectType("Promotion", AbInBevObjects.PRODUCT_NEGOTIATIONS);
        try {
            String smartSqlFilter = String.format("{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s' AND {%1$s:%6$s} = '%7$s'",
                    AbInBevObjects.PRODUCT_NEGOTIATIONS, ProductNegotiationFields.ACCOUNT, accountId,
                    ProductNegotiationFields.STATUS, STATUS_CONFIRMED,
                    ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());

            String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.PRODUCT_NEGOTIATIONS, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Promotions by Account ID: " + accountId, e);
        }

        return count;
    }


    public static List<CN_Product_Negotiation__c> getActivePromotionsByAccountId(String accountId) {

        RecordType rt = RecordType.getByNameAndObjectType("Promotion", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s}  WHERE " +
                        "{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s'"
                , AbInBevObjects.PRODUCT_NEGOTIATIONS,
                ProductNegotiationFields.ACCOUNT, accountId,
                ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSqlFilter, CN_Product_Negotiation__c.class);

    }

    public static List<CN_Product_Negotiation__c> getActivePromotionsByAccountIdAndType(String accountId, String type) {

        RecordType rt = RecordType.getByNameAndObjectType("Promotion", AbInBevObjects.PRODUCT_NEGOTIATIONS);

        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s}  WHERE " +
                        "{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s' AND {%1$s:%6$s} = '%7$s' AND {%1$s:%8$s} = '%9$s'"
                , AbInBevObjects.PRODUCT_NEGOTIATIONS,
                ProductNegotiationFields.ACCOUNT, accountId,
                ProductNegotiationFields.PROMOTION_TYPE, type,
                ProductNegotiationFields.STATUS, STATUS_CONFIRMED,
                ProductNegotiationFields.RECORD_TYPE_ID, rt.getId());

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSqlFilter, CN_Product_Negotiation__c.class);

    }

    public static List<CN_Product_Negotiation__c> getNegotiationsByAccountId(String accountId) {

        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s}  WHERE " +
                        "{%1$s:%2$s} = '%3$s'"
                , AbInBevObjects.PRODUCT_NEGOTIATIONS,
                ProductNegotiationFields.ACCOUNT, accountId
        );
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSqlFilter, CN_Product_Negotiation__c.class);

    }

    public static CN_Product_Negotiation__c getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.PRODUCT_NEGOTIATIONS, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new CN_Product_Negotiation__c(jsonObject);
        }
        return null;
    }

    public static int getActiveNegotiationCountByAccountId(String accountId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.PRODUCT_NEGOTIATIONS, ProductNegotiationFields.ACCOUNT, accountId);

            String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.PRODUCT_NEGOTIATIONS, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Negotiation count by Account ID: " + accountId, e);
        }
        return count;
    }
}
