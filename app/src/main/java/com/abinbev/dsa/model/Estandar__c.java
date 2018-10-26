package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nchangnon on 11/24/15.
 */
public class Estandar__c extends SFBaseObject {
    public static final String TAG = Estandar__c.class.getSimpleName();
    public static final String VARIABLE__C_VALUE_AVAILABLILITY = "Disponibilidad";

    public Estandar__c(JSONObject json) {
        super(AbInBevObjects.ESTANDAR, json);
    }

    protected Estandar__c() {
        super(AbInBevObjects.ESTANDAR);
    }

    public String getIdentificadorDocumento() {
        return getStringValueForKey(AbInBevConstants.Estandar__c.IDENTIFICADOR_DOCUMENTO);
    }

    public String getVariable() {
        return getStringValueForKey(AbInBevConstants.Estandar__c.VARIABLE);
    }

    public String getIdealValues() {
        return getStringValueForKey(AbInBevConstants.Estandar__c.IDEAL);
    }

    public String getRealValues() {
        return getStringValueForKey(AbInBevConstants.Estandar__c.REAL);
    }

    public List<String> getRealisedValuesAsList() {
        String stringValue = getStringValueForKey(AbInBevConstants.Estandar__c.REAL);
        return Arrays.asList(TextUtils.split(stringValue, ","));
    }

    public String getOpportunityValues() {
        return getStringValueForKey(AbInBevConstants.Estandar__c.OPORTUNIDAD);
    }

    public List<String> getOpportunityValuesAsList() {
        String stringValue = getStringValueForKey(AbInBevConstants.Estandar__c.OPORTUNIDAD);
        return Arrays.asList(TextUtils.split(stringValue, ","));
    }

    public static List<Estandar__c> getByAccountIdAndVariable(String accountId, String variable) {
        String smartSqlFilter = String.format("{Estandar__c:%s} = ('%s') AND {Estandar__c:%s} = ('%s')",
                AbInBevConstants.Estandar__c.CLIENTE, accountId,
                AbInBevConstants.Estandar__c.VARIABLE, variable);
        return getBySQLFilter(smartSqlFilter);
    }

    public static List<Estandar__c> getByAccountId(String accountId) {
        String smartSqlFilter = String.format("{Estandar__c:%s} = ('%s')",
                AbInBevConstants.Estandar__c.CLIENTE, accountId);
        return getBySQLFilter(smartSqlFilter);
    }

    private static List<Estandar__c> getBySQLFilter(String sqlFilter){
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ESTANDAR, sqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Estandar__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Estandar__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Estandars: ", e);
        }
        return results;
    }

}
