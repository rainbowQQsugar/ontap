package com.abinbev.dsa.model;


import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.EventCatalogFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Event_Catalog__c extends SFBaseObject {
    public static final String TAG = Event_Catalog__c.class.getSimpleName();

    public static final String ACTIVE_STATUS = "Active";

    public Event_Catalog__c(JSONObject json) {
        super(AbInBevObjects.EVENT_CATALOG, json);
    }

    protected Event_Catalog__c() {
        super(AbInBevObjects.EVENT_CATALOG);
    }

    public String getStatus() {
        return getStringValueForKey(EventCatalogFields.STATUS);
    }

    public String getInitialDate() {
        return getStringValueForKey(EventCatalogFields.INITIAL_DATE);
    }

    public String getEndDate() {
        return getStringValueForKey(EventCatalogFields.END_DATE);
    }

    public Double getMaxDuration() {
        return getDoubleValueForKey(EventCatalogFields.MAX_DURATION);
    }

    public Boolean getUsageOfEquipment() {
        return getBooleanValueForKey(EventCatalogFields.USAGE_OF_EQUIPMENT);
    }

    //TODO: implement the Country filter
    public static List<Event_Catalog__c> getActiveEventCatalog(String selectedDate) {
        List<Event_Catalog__c> eventCatalogList = new ArrayList<>();
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} < '%s' AND {%s:%s} > '%s'",
                    AbInBevObjects.EVENT_CATALOG, EventCatalogFields.STATUS, ACTIVE_STATUS,
                    AbInBevObjects.EVENT_CATALOG, EventCatalogFields.INITIAL_DATE, selectedDate,
                    AbInBevObjects.EVENT_CATALOG, EventCatalogFields.END_DATE, selectedDate);

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.EVENT_CATALOG, smartSqlFilter);
            Log.e("Babu", "event catalog query is: " + smartSql);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                eventCatalogList.add(new Event_Catalog__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Active Event Catalog");
        }

        return eventCatalogList;
    }

    public static Event_Catalog__c getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.EVENT_CATALOG, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new Event_Catalog__c(jsonObject);
        }
        return null;
    }
}
