package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.CN_TechniciansFields;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CN_Technicians__c extends SFBaseObject {
    private static final String TAG = CN_Technicians__c.class.getSimpleName();

    public CN_Technicians__c(JSONObject json) {
        super(AbInBevObjects.CN_TECHNICIANS, json);
    }

    public CN_Technicians__c(String objectName, JSONObject json) {
        super(objectName, json);
    }

    protected CN_Technicians__c(String objectName) {
        super(objectName);
    }

    public static int getTechniciansCountById(String id) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.CN_TECHNICIANS, SyncEngineConstants.StdFields.ID, id);

            String smartSql = String
                    .format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.CN_TECHNICIANS, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Technicians count by  ID: " + id, e);
        }
        return count;
    }

    public static CN_Technicians__c getD1ByTechnicianId(String id) {
        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.CN_TECHNICIANS, SyncEngineConstants.StdFields.ID, id);
        String smartSql = String
                .format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CN_TECHNICIANS, smartSqlFilter);
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObject(dm, smartSql, CN_Technicians__c.class);
    }

    public String getD1Name() {
        return getStringValueForKey(CN_TechniciansFields.D1_NAME);
    }

    public String getD1Id() {
        return getStringValueForKey(CN_TechniciansFields.D1_ID);
    }

    public String getD1Phone() {
        return getStringValueForKey(CN_TechniciansFields.D1_PHONE);
    }

}
