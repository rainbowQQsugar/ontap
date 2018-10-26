package com.abinbev.dsa.model;

import android.content.Context;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.CaseStatusListFields;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.soql.QueryOp;
import com.salesforce.androidsyncengine.syncmanifest.FilterObject;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CaseStatusList__c extends SFBaseObject {
    public static final String TAG = CaseStatusList__c.class.getSimpleName();

    public CaseStatusList__c(JSONObject json) {
        super(AbInBevObjects.CASESTATUSLIST, json);
    }

    public CaseStatusList__c() {
        super(AbInBevObjects.CASESTATUSLIST);
    }

    public String getPerfiles__c() {
        return getStringValueForKey(CaseStatusListFields.PERFILES);
    }


    public static boolean isValidStatusChange (String oldValue, String newValue) {

        String smartSqlFilter = String.format("{%s:%s} = ('%s') AND {%s:%s} = '%s'",
                AbInBevObjects.CASESTATUSLIST, CaseStatusListFields.ESTADO1, oldValue,
                AbInBevObjects.CASESTATUSLIST, CaseStatusListFields.ESTADO2, newValue);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CASESTATUSLIST, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        if (recordsArray.length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<FilterObject> getAdditionalFilters(Context context) {

        ArrayList<FilterObject> filterObjects = new ArrayList<FilterObject>();


        String userProfile = AppPreferenceUtils.getUserProfile(context);
        String countryAlias = AppPreferenceUtils.getCountryAlias(context);

        FilterObject filterObject = new FilterObject();
        filterObject.setField(CaseStatusListFields.COUNTRY);
        filterObject.setValue("%" + countryAlias + "%");
        filterObject.setOp(QueryOp.like);
        filterObjects.add(filterObject);

        filterObject = new FilterObject();
        filterObject.setField(CaseStatusListFields.PERFILES);
        filterObject.setValue("%" + userProfile + "%");
        filterObject.setOp(QueryOp.like);
        filterObjects.add(filterObject);

        return filterObjects;
    }

}
