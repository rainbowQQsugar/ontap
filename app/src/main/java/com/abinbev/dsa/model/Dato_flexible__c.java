package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.ui.view.FlexibleData.Category;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.FlexibleDataFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 11/24/15.
 */
public class Dato_flexible__c extends SFBaseObject {

    public static final String TAG = Dato_flexible__c.class.getName();

    public Dato_flexible__c() {
        super(AbInBevObjects.FLEXIBLE_DATA);
    }

    public Dato_flexible__c(JSONObject jsonObject) {
        super(AbInBevObjects.FLEXIBLE_DATA, jsonObject);
    }

    public String getCategory() {
        String name = getName();
        int start = name.indexOf("-") + 1;
        return name.substring(start, name.length());
    }

    public boolean isValidBottle() {
        Category category = Category.from(getCategory());
        return category != null;
    }

    public String getValue() {
        return getStringValueForKey(FlexibleDataFields.VALUE);
    }

    public String getType() {
        return getStringValueForKey(FlexibleDataFields.TYPE);
    }

    public String getConceptoId(){
        return getStringValueForKey(FlexibleDataFields.CONCEPTO);
    }

    public static List<Dato_flexible__c> getDatoFlexibleForParams(String accountId, String type) {
        List<Dato_flexible__c> datoFlexibleList = new ArrayList<>();
        String filter = String.format("{%s:%s} = ('%s') ORDER BY {%s:%s} ASC",
                AbInBevObjects.FLEXIBLE_DATA, FlexibleDataFields.CLIENT, accountId,
                AbInBevObjects.FLEXIBLE_DATA, FlexibleDataFields.ORDENAMIENTO_F);
        String filterType = null;
        if (type != null){
            filterType = String.format("{%s:%s} = '%s' AND ", AbInBevObjects.FLEXIBLE_DATA, FlexibleDataFields.TYPE, type);
        }
        if (filterType != null){
            filter = filterType.concat(filter);
        }
        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.FLEXIBLE_DATA, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);

        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Dato_flexible__c datoFlexible = new Dato_flexible__c(jsonObject);
                datoFlexibleList.add(datoFlexible);
            } catch (JSONException e) {
                Log.e(TAG, "Error retrieving Dato Flexible for Account with ID " + accountId);
            }
        }
        return datoFlexibleList;
    }

    public static List<Dato_flexible__c> getDatoFlexibleForAccountId(String accountId) {
        return getDatoFlexibleForParams(accountId, null);
    }
}
