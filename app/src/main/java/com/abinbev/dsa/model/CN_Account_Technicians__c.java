package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CN_Account_Technicians__c extends SFBaseObject {
    private static final String TAG = CN_Account_Technicians__c.class.getSimpleName();

    public CN_Account_Technicians__c(JSONObject json) {
        super(AbInBevObjects.CN_TECHNICIANS, json);
    }

    public CN_Account_Technicians__c(String objectName, JSONObject json) {
        super(objectName, json);
    }

    protected CN_Account_Technicians__c(String objectName) {
        super(objectName);
    }

    public static int getTechniciansCountByAccountId(String accountId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.CN_ACCOUNT_TECHNICIANS, AbInBevConstants.CN_Account_TechniciansFields.CN_POC, accountId);

            String smartSql = String
                    .format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.CN_ACCOUNT_TECHNICIANS, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting TechniciansId count by  ID: " + accountId, e);
        }
        return count;
    }

    public static List<CN_Account_Technicians__c> getD1ListByAccountId(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.CN_ACCOUNT_TECHNICIANS, AbInBevConstants.CN_Account_TechniciansFields.CN_POC, accountId);
        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CN_ACCOUNT_TECHNICIANS, smartSqlFilter);
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, CN_Account_Technicians__c.class);
    }

    public String getD1Id() {
        return getStringValueForKey(AbInBevConstants.CN_Account_TechniciansFields.CN_TECHNICIAN);
    }

}
