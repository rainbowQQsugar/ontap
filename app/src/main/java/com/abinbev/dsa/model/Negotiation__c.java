package com.abinbev.dsa.model;

import android.util.Log;
import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.NegotiationFields;
import com.abinbev.dsa.utils.PicklistUtils;
import com.google.gson.Gson;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.EditLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Negotiation__c extends TranslatableSFBaseObject {

    public static final String TAG = Negotiation__c.class.getName();

    public static final String STATUS_PENDING_APPROVAL = "Pendiente de aprobación";
    public static final String STATUS_SUBMITTED = "Sugerida";


    public Negotiation__c(JSONObject json) {
        super(AbInBevObjects.NEGOTIATIONS, json);
    }

    public Negotiation__c() {
        super(AbInBevObjects.NEGOTIATIONS);
    }

    public String getObservations() {
        return getStringValueForKey(NegotiationFields.OBSERVATIONS);
    }

    public String getStatus() {
        return getStringValueForKey(NegotiationFields.STATUS);
    }

    public String getTranslatedStatus() {
        return getTranslatedStringValueForKey(NegotiationFields.STATUS);
    }

    public String getStartDate() {
        return getStringValueForKey(NegotiationFields.START_DATE);
    }

    public String getDeliveryDate() {
        return getStringValueForKey(NegotiationFields.DELIVER_DATE);
    }

    public String getPesos() {
        return getStringValueForKey(NegotiationFields.PESOS_PER_CASE);
    }

    public String getNegotiationId() {
        return getStringValueForKey(NegotiationFields.NEGOTIATION_ID);
    }

    public String getType() {
        return getStringValueForKey(NegotiationFields.TYPE);
    }

    public String getClassification() {
        return getStringValueForKey(NegotiationFields.CLASIFICATION);
    }

    public String getTranslatedClassification() {
        return getTranslatedStringValueForKey(NegotiationFields.CLASIFICATION);
    }

    public String getExpirationDate() {
        return getStringValueForKey(NegotiationFields.EXPIRATION_DATE);
    }

    public String getApprover() {
        return getStringValueForKey(NegotiationFields.APPROVER);
    }

    public String getRecordTypeId() {
        return getStringValueForKey(NegotiationFields.RECORD_TYPE_ID);
    }

    public boolean isCompleted() {
        return "Finalizada".equals(getStatus());
    }

    public static boolean updateObservations(String id, String observation) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NegotiationFields.OBSERVATIONS, observation);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATIONS, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "updateObservations: ", e);
        }
        return false;
    }

    public static boolean submit(String id) {
        return updateStatus(id, "Pendiente de aprobación");
    }

    public static boolean delete(String id) {
        return DataManagerFactory.getDataManager().deleteRecord(AbInBevObjects.NEGOTIATIONS, id);
    }

    public static boolean updateStatus(String negotiationId, String status) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NegotiationFields.STATUS, status);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.NEGOTIATIONS, negotiationId, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "Error submitting negotiation with id: " + negotiationId, e);
        }
        return false;
    }

    public static String lastCalculatedPickListRecordTypeId = "";
    public static List<PicklistValue> lastCalculatedPickListValues;

    public static List<PicklistValue> getClassificationValues(String recordTypeId) {
        if (lastCalculatedPickListRecordTypeId != null && lastCalculatedPickListRecordTypeId.equals(recordTypeId)) {
            return lastCalculatedPickListValues;
        } else {
            Log.v(TAG, "getPicklistMetadata: calculating");
            lastCalculatedPickListValues = getPicklistValues(NegotiationFields.CLASIFICATION, recordTypeId);
            lastCalculatedPickListRecordTypeId = recordTypeId;
            return lastCalculatedPickListValues;
        }
    }

    private static List<PicklistValue> getPicklistValues(String fieldName, String recordTypeId) {

        List<PicklistValue> validPickListValues = new ArrayList<PicklistValue>();

        Map detailsMap;

        String objectType = AbInBevObjects.NEGOTIATIONS;
        Gson gson = new Gson();

        RecordTypeMapping recordTypeMapping = PicklistUtils.getRecordTypeMapping(ABInBevApp.getAppContext(), gson, objectType, recordTypeId);

        if (recordTypeMapping == null) {
            Log.v(TAG, "no recordTypeMapping available for RecordType");
            return validPickListValues;
        }

        // Let us ignore the available value for now
        Log.v(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

        IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(ABInBevApp.getAppContext(), gson, objectType, recordTypeMapping.getRecordTypeId());

        if (individualLayouts == null) {
            Log.v(TAG, "no individualLayouts available for RecordType");
            return validPickListValues;
        }
        List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();

        if (editLayoutSections == null) {
            Log.v(TAG, "no editLayoutSections available for RecordType");
            return validPickListValues;
        }

        detailsMap = PicklistUtils.buildDetailsMap(editLayoutSections);

        Details field = (Details) detailsMap.get(fieldName);

        if (field != null) {
            List<PicklistValue> picklistValueList = field.getPicklistValues();

            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    validPickListValues.add(picklistValue);
            }
        }

        return validPickListValues;

    }
}
