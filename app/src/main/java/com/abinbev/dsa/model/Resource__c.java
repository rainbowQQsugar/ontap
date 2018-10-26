package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ResourceFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

public class Resource__c extends SFBaseObject {

    public static final String TAG = Resource__c.class.getName();

    public Resource__c() {
        super(AbInBevObjects.RESOURCE);
    }

    public Resource__c(JSONObject jsonObject) {
        super(AbInBevObjects.RESOURCE, jsonObject);
    }

    public String getFieldText() {
        return getStringValueForKey(ResourceFields.FIELD_TEXT);
    }

    public static String getFieldText(String profileName, String fieldName) {
            Resource__c resource = getResourceByProfileName(profileName, fieldName);
            if (resource != null) {
                return resource.getFieldText();
            } else {
                resource = getResourceByFieldName(fieldName);
                if (resource != null) {
                    return resource.getFieldText();
                } else {
                    return null;
                }
            }
    }

    public static Resource__c getResourceByFieldName(String fieldName) {
        JSONObject jsonObject = DataManagerFactory
                .getDataManager().exactQuery(AbInBevObjects.RESOURCE, ResourceFields.FIELD_NAME, fieldName);
        return jsonObject == null ? null : new Resource__c(jsonObject);
    }

    public static Resource__c getResourceByProfileName(String profileName, String fieldName) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                AbInBevObjects.RESOURCE, ResourceFields.PROFILE, profileName,
                AbInBevObjects.RESOURCE, ResourceFields.FIELD_NAME, fieldName);


        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.RESOURCE, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Resource__c resource = new Resource__c(jsonObject);
                return resource;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getting QuestionResponseId");
        }
        return null;
    }
}
