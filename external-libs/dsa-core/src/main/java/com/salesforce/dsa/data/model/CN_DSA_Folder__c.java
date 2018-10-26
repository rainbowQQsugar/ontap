package com.salesforce.dsa.data.model;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

public class CN_DSA_Folder__c extends SFBaseObject {

    public CN_DSA_Folder__c(JSONObject json) {
        super(DSAConstants.DSAObjects.CN_DSA_Folder__c, json);
    }

    public String getCN_Parent_Folder__c() {
        return getStringValueForKey(DSAConstants.CNDSAFolderFields.CN_Parent_Folder__c);
    }

    public String getCN_DSA__c() {
        return getStringValueForKey(DSAConstants.CNDSAFolderFields.CN_DSA__c);
    }

    public String getCN_Is_Top_Level__c() {
        return getStringValueForKey(DSAConstants.CNDSAFolderFields.CN_Is_Top_Level__c);
    }

    public static CN_DSA_Folder__c getDsaFolderForId(String id) {

        String filter = String.format("{CN_DSA_Folder__c:%s} = '%s'",
                SyncEngineConstants.StdFields.ID,
                id);
        String subCatSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAConstants.DSAObjects.CN_DSA_Folder__c, filter);
        JSONArray records = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(subCatSql);
        try {
            if (records.length() > 0) {
                JSONObject jsonCat = records.getJSONArray(0).getJSONObject(0);
                CN_DSA_Folder__c category = new CN_DSA_Folder__c(jsonCat);
                return category;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

}
