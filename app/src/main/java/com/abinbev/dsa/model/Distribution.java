package com.abinbev.dsa.model;


import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.DistributionFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants.Formats;

import org.json.JSONObject;

import java.util.List;

public class Distribution extends TranslatableSFBaseObject {

    public static final String TAG = Distribution.class.getSimpleName();

    protected Distribution() {
        super(AbInBevObjects.CN_DISTRIBUTION);
    }

    public Distribution(JSONObject json) {
        super(AbInBevObjects.CN_DISTRIBUTION, json);
    }

    public static int getActiveDistributionCount(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                AbInBevObjects.CN_DISTRIBUTION, DistributionFields.POC_ID, accountId,
                AbInBevObjects.CN_DISTRIBUTION, DistributionFields.IS_ACTIVE, "true");

        String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.CN_DISTRIBUTION, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, smartSql);
    }

    public static List<Distribution> getAllByAccountId(final String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                AbInBevObjects.CN_DISTRIBUTION, DistributionFields.POC_ID, accountId,
                AbInBevObjects.CN_DISTRIBUTION, DistributionFields.IS_ACTIVE, "true");

        String smartSql = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.CN_DISTRIBUTION, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, Distribution.class);
    }

    public static Distribution getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, AbInBevObjects.CN_DISTRIBUTION, id, Distribution.class);
    }

    public String getCategory() {
        return getStringValueForKey(DistributionFields.CN_CATEGORY);
    }

    public void setCategory(String category) {
        setStringValueForKey(DistributionFields.CN_CATEGORY, category);
    }

    public String getBrand() {
        return getStringValueForKey(DistributionFields.CN_BRAND);
    }

    public void setBrand(String brand) {
        setStringValueForKey(DistributionFields.CN_BRAND, brand);
    }

    public String getSKUName() {
        return getStringValueForKey(DistributionFields.CN_SKU_NAME);
    }

    public void setSKUName(String skuName) {
        setStringValueForKey(DistributionFields.CN_SKU_NAME, skuName);
    }

    public String getPackage() {
        return getStringValueForKey(DistributionFields.CN_PACKAGE);
    }

    public String getTranslatedPackage() {
        return getTranslatedStringValueForKey(DistributionFields.CN_PACKAGE);
    }

    public void setPackage(String packageName) {
        setStringValueForKey(DistributionFields.CN_PACKAGE, packageName);
    }

    public String getUnit() {
        return getStringValueForKey(DistributionFields.CN_UNIT);
    }

    public String getTranslatedUnit() {
        return getTranslatedStringValueForKey(DistributionFields.CN_UNIT);
    }

    public void setUnit(String unit) {
        setStringValueForKey(DistributionFields.CN_UNIT, unit);
    }

    public String getLastCollectedPtr() {
        return getStringValueForKey(DistributionFields.LAST_COLLECTED_PTR);
    }

    public void setLastCollectedPtr(String ptr) {
        setStringValueForKey(DistributionFields.LAST_COLLECTED_PTR, ptr);
    }

    public String getLastCollectedPtc() {
        return getStringValueForKey(DistributionFields.LAST_COLLECTED_PTC);
    }

    public void setLastCollectedPtc(String ptc) {
        setStringValueForKey(DistributionFields.LAST_COLLECTED_PTC, ptc);
    }

    public String getAccountId() {
        return getStringValueForKey(DistributionFields.POC_ID);
    }

    public void setAccountId(String id) {
        setStringValueForKey(DistributionFields.POC_ID, id);
    }

    public void setProductId(String id) {
        setStringValueForKey(DistributionFields.CN_PRODUCT, id);
    }

    public String getProductId() {
        return getStringValueForKey(DistributionFields.CN_PRODUCT);
    }

    public void setIsActive(boolean isActive) {
        setBooleanValueForKey(DistributionFields.IS_ACTIVE, isActive);
    }
}
