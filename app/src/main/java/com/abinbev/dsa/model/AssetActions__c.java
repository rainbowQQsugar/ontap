package com.abinbev.dsa.model;

import android.util.Log;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AssetActionFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AssetActions__c extends SFBaseObject {
    public static final String TAG = AssetActions__c.class.getSimpleName();

    public AssetActions__c(JSONObject json) {
        super(AbInBevObjects.ASSET_ACTIONS, json);
    }

    protected AssetActions__c() {
        super(AbInBevObjects.ASSET_ACTIONS);
    }

    public String getAction() {
        return getStringValueForKey(AssetActionFields.ACTION);
    }

    public String getActionLabel() {
        return getStringValueForKey(AssetActionFields.ACTION_LABEL);
    }


    public String getRecordType() {
        return getStringValueForKey(AssetActionFields.RECORD_TYPE);
    }

    public static List<AssetActions__c> getByCountryCode(String countryCode) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s')",
                AbInBevObjects.ASSET_ACTIONS,
                AssetActionFields.COUNTRY_CODE, countryCode);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ASSET_ACTIONS, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<AssetActions__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new AssetActions__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All AssetActions__c for Country Code: " + countryCode, e);
        }

        return results;
    }

    public static List<AssetActions__c> getAssetCasesByCountryCode(String countryCode) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s') AND {%s:%s} = '%s'",
                AbInBevObjects.ASSET_ACTIONS, AssetActionFields.COUNTRY_CODE, countryCode,
                AbInBevObjects.ASSET_ACTIONS, AssetActionFields.ACTION, AssetActionFields.ACTION_ASSET_CASE);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ASSET_ACTIONS, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<AssetActions__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new AssetActions__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting asset cases AssetActions__c for Country Code: " + countryCode, e);
        }

        return results;
    }

}
