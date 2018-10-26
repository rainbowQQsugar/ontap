package com.abinbev.dsa.model;


import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.EventEquipmentFields;
import com.abinbev.dsa.utils.AbInBevConstants.EventFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Event_Equipment__c extends SFBaseObject {
    public static final String TAG = Event_Equipment__c.class.getSimpleName();

    public static final String ACTIVE_STATUS = "Active";

    public Event_Equipment__c(JSONObject json) {
        super(AbInBevObjects.EVENT_EQUIPMENT, json);
    }

    protected Event_Equipment__c() {
        super(AbInBevObjects.EVENT_EQUIPMENT);
    }

    public String getStatus() {
        return getStringValueForKey(EventEquipmentFields.STATUS);
    }

    public static List<Event_Equipment__c> getActiveEquipment() {
        List<Event_Equipment__c> eventEquipmentList = new ArrayList<>();
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.STATUS, ACTIVE_STATUS);

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.EVENT_EQUIPMENT, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                eventEquipmentList.add(new Event_Equipment__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Active Event Catalog");
        }

        return eventEquipmentList;
    }

    public static List<Event_Equipment__c> getActiveEquipmentForAccount(Account account) {
        List<Event_Equipment__c> eventEquipmentList = new ArrayList<>();
        try {

            String smartSqlFilter;

            if(account.getSalesOffice().isEmpty()) {
                smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                        AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.STATUS, ACTIVE_STATUS,
                        AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.BUSINESS_UNIT, account.getBusinessUnit());
            } else {
                smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' AND {%s:%s} = '%s'",
                        AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.STATUS, ACTIVE_STATUS,
                        AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.BUSINESS_UNIT, account.getBusinessUnit(),
                        AbInBevObjects.EVENT_EQUIPMENT, EventEquipmentFields.Sales_Office__c, account.getSalesOffice());
            }

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.EVENT_EQUIPMENT, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                eventEquipmentList.add(new Event_Equipment__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Active Event Catalog", e);
        }

        return eventEquipmentList;
    }

    public static boolean isEquipmentAvailableInDate(String equipmentId, String startDate, String endDate) {
        try {
            String smartSqlFilter;

            smartSqlFilter = String.format("{%s:%s} = '%s' AND (({%s:%s} <= '%s' AND {%s:%s} >= '%s') OR ({%s:%s} <= '%s' AND {%s:%s} >= '%s'))",
                    AbInBevObjects.EVENT, EventFields.EQUIPMENT, equipmentId,
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, startDate,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, startDate,
                    AbInBevObjects.EVENT, EventFields.START_DATE_TIME, endDate,
                    AbInBevObjects.EVENT, EventFields.END_DATE_TIME, endDate);

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.EVENT, smartSqlFilter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

            return recordsArray.length() > 0 ? false : true;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Active Event Catalog");
        }

        return false;
    }

    public static Event_Equipment__c getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.EVENT_EQUIPMENT, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new Event_Equipment__c(jsonObject);
        }
        return null;
    }

    // SELECT Name FROM Event_Equipment__c WHERE Business_Unit__c = [ACCOUNT.Business_Unit__c] AND Sales_Office__c = [ACCOUNT.Sales_Office__c]
    // If the field Sales_Office is EMPTY
    // SELECT Name FROM Event_Equipment__c WHERE Business_Unit__c = [ACCOUNT.Business_Unit__c]

    // The selected Equipment needs to be validated against the Event object to check if it's not booked for the selected Date (Start and End Date)
    // If the Equipment is not available, an error message should be displayed: "Equipment not available for the selected Date/Time"
    // If the Equipment is available, the Event is saved using the option selected
}
