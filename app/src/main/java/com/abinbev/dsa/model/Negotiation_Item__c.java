package com.abinbev.dsa.model;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import static com.abinbev.dsa.utils.AbInBevConstants.NegotiationItemFields;
import static com.salesforce.dsa.utils.DSAConstants.Formats;

import android.util.Log;
import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.utils.DateUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wandersonblough on 12/16/15.
 */
public class Negotiation_Item__c extends SFBaseObject {

    private static final String TAG = "Negotiation_Item__c";
    public static final String RECORD_TYPE_GET = "Item Negotiation (Get)";
    public static final String RECORD_TYPE_GIVE = "Item Negotiation (Give)";


    public int amount;
    public Material__c material__c;
    private String comment;

    public Negotiation_Item__c() {
        super(AbInBevObjects.NEGOTIATION_ITEM);
    }

    public Negotiation_Item__c(JSONObject jsonObject) {
        super(AbInBevObjects.NEGOTIATION_ITEM, jsonObject);
    }

    public Negotiation_Item__c(Material__c material__c, int amount) {
        super(AbInBevObjects.NEGOTIATION_ITEM);
        this.material__c = material__c;
        this.amount = amount;
    }

    public Negotiation_Item__c(Material__c material__c, int amount, String comment) {
        super(AbInBevObjects.NEGOTIATION_ITEM);
        this.material__c = material__c;
        this.amount = amount;
        this.comment = comment;
    }

    public String getQuantity() {
        if (amount == -1) {
            return "0";
        } else {
            return String.valueOf(amount);
        }
    }

    public String getAmount() {
        return getStringValueForKey(NegotiationItemFields.AMOUNT);
    }

    public String fetchGiveId() {
        return getStringValueForKey(NegotiationItemFields.MATERIAL_GIVE);
    }

    public String getRecordId() {
        return getStringValueForKey(NegotiationItemFields.RECORD_TYPE_ID);
    }

    public String getComment() {
        return getStringValueForKey(NegotiationItemFields.COMMENT);
    }

    public String getSavedComment() {
        return comment;
    }

    public String fetchGetId() {
        return getStringValueForKey(NegotiationItemFields.MATERIAL_GET);
    }

    public Material__c getMaterial() {
        return material__c;
    }

    public Negotiation_Item__c copy() {
        JSONObject oldJson = toJson();
        JSONObject newJson = null;
        try {
            newJson = oldJson == null ? null : new JSONObject(oldJson.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error while cloning Negotiation item.");
        }
        Negotiation_Item__c newItem = new Negotiation_Item__c(newJson);
        newItem.amount = amount;
        newItem.comment = comment;
        newItem.material__c = material__c.copy();
        return newItem;
    }

    public static List<Negotiation_Item__c> fetchGiveNegotiationItems(String negotiationId) {
        List<Negotiation_Item__c> negotiationItems = new ArrayList<>();
        try {
            String recordName = RECORD_TYPE_GIVE;
            RecordType recordType = RecordType.getByName(recordName);

            String filter = String.format("{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s'",
                    AbInBevObjects.NEGOTIATION_ITEM, NegotiationItemFields.NEGOTIATION,
                    negotiationId, NegotiationItemFields.RECORD_TYPE_ID, recordType.getId());
            String smartSQL = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.NEGOTIATION_ITEM, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Negotiation_Item__c negotiationItem = new Negotiation_Item__c(jsonObject);
                negotiationItems.add(negotiationItem);
            }

        } catch (JSONException e) {
            Log.e(TAG, "fetchGiveNegotiationItems: Error fetching negotiation item for id " + negotiationId, e);
        }
        return negotiationItems;
    }

    public static String getIdForNegotiationItem(Material__c material_c, String negotiationId) {
        try {
            String negotiationField = material_c instanceof Material_Get__c ?  NegotiationItemFields.NEGOTIATION_GET : NegotiationItemFields.NEGOTIATION_GIVE;
            String materialField = material_c instanceof Material_Get__c ? NegotiationItemFields.MATERIAL_GET : NegotiationItemFields.MATERIAL_GIVE;
            String filter = String.format("{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s'", AbInBevObjects.NEGOTIATION_ITEM, materialField, material_c.getId(), negotiationField, negotiationId);
            String smartSQL = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.NEGOTIATION_ITEM, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);
            JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
            return jsonObject.getString(SyncEngineConstants.StdFields.ID);
        } catch (JSONException e) {
            Log.e(TAG, "getIdForNegotiationItem: ", e);
        }
        return null;
    }

    public static List<Negotiation_Item__c> fetchGetsNegotiationItems(String negotiationId) {
        List<Negotiation_Item__c> negotiationItems = new ArrayList<>();
        try {
            String recordName = RECORD_TYPE_GET;
            RecordType recordType = RecordType.getByName(recordName);

            String filter = String.format("{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s'",
                    AbInBevObjects.NEGOTIATION_ITEM, NegotiationItemFields.NEGOTIATION,
                    negotiationId, NegotiationItemFields.RECORD_TYPE_ID, recordType.getId());
            String smartSQL = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.NEGOTIATION_ITEM, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Negotiation_Item__c negotiationItem = new Negotiation_Item__c(jsonObject);
                negotiationItems.add(negotiationItem);
            }
        } catch (JSONException e) {
            Log.e(TAG, "fetchGetsNegotiationItems: Error fetching negotiation item for id: " + negotiationId, e);
        }
        return negotiationItems;
    }

    public static boolean removeNegotiationItem(String id) {
        if (id != null) {
            return DataManagerFactory.getDataManager().deleteRecord(AbInBevObjects.NEGOTIATION_ITEM, id);
        }
        return false;
    }

    public static boolean updateAmount(String id, int amount) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NegotiationItemFields.AMOUNT, amount);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATION_ITEM, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "updateAmount: error updating ammount", e);
        }
        return false;
    }

    public static boolean updateStartDate(String id, String date) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NegotiationItemFields.START_DATE, DateUtils.getNegociacionStartDate(DateUtils.dateFromString(date)));
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATION_ITEM, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "updateStartDate: ", e);
        }
        return false;
    }

    public static boolean updateEndDate(String id, String date) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NegotiationItemFields.END_DATE, date);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATION_ITEM, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "updateEndDate: ", e);
        }
        return false;
    }

    public static boolean submit(String id) {
        try {
            JSONObject jsonObject =  new JSONObject();
            jsonObject.put(NegotiationItemFields.STATUS, "Comprometido");
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATION_ITEM, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "submit: ", e);
        }
        return false;
    }
}
