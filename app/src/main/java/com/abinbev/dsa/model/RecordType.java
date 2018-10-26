package com.abinbev.dsa.model;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.RecordTypeFields;
import com.abinbev.dsa.utils.ContentUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.google.gson.Gson;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.data.layouts.ObjectLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecordType extends SFBaseObject {

    private static final String TAG = "RecordType";

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("RecordType", AbInBevObjects.RECORD_TYPE)
                .putColumn("Name", RecordTypeFields.NAME)
            .build();

    public RecordType(JSONObject json) {
        super(AbInBevObjects.RECORD_TYPE, json);
    }

    public RecordType() {
        super(AbInBevObjects.RECORD_TYPE);
    }

    public String getName() {
        return getStringValueForKey(RecordTypeFields.NAME);
    }

    public String getDeveloperName() {
        return getStringValueForKey(RecordTypeFields.DEVELOPER_NAME);
    }

    public static RecordType getById(String recordTypeId) {
        
        if (!ContentUtils.isStringValid(recordTypeId)) return null;

        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.RECORD_TYPE, SyncEngineConstants.StdFields.ID, recordTypeId);
        if (jsonObject != null) {
            return new RecordType(jsonObject);
        }
        return null;
    }

    public static RecordType getByName(String name) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME, String.valueOf(name));
        if (jsonObject != null) {
            return new RecordType(jsonObject);
        }
        return null;
    }

    public static RecordType getByNameAndObjectType(String name, String objectType) {
        String nameSpacedObjectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, ABInBevApp.getAppContext());
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' ORDER BY {%s:%s} ASC LIMIT 1",
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME, name,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.OBJECT_TYPE, nameSpacedObjectType,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME);

        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.RECORD_TYPE, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
        RecordType result = null;
        if (recordsArray.length() > 0){
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
                result = new RecordType(jsonObject);
            }catch (JSONException e) {
                Log.e(TAG, "Exception getting record type '" + name + "' with object type '"
                        + objectType + ".'", e);
            }
        } else {
            String recordTypeId = getRecordTypeIdFromMetaData(objectType, name);
            if (recordTypeId != null) {
                return getById(recordTypeId);
            }
        }
        return result;
    }


    public static String getRecordIdsByNameAndObjectType(String name, String objectType) {
        List<String> recordIds = getRecordIdsListByNameAndObjectType(name, objectType);
        String recordIdString = Joiner.on("','").join(recordIds);

        Log.e("Babu", "recordIdString: " + recordIdString);
        return recordIdString;
    }


    public static List<String> getRecordIdsListByNameAndObjectType(String name, String objectType) {

        String nameSpacedObjectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, ABInBevApp.getAppContext());
        String smartSqlFilter = String.format("{%s:%s} LIKE '%%%s%%' AND {%s:%s} = '%s' ORDER BY {%s:%s}",
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME, name,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.OBJECT_TYPE, nameSpacedObjectType,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME);

        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.RECORD_TYPE, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
        ArrayList<String> recordIds = new ArrayList<>();

        for (int i = 0; i < recordsArray.length(); i++) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                recordIds.add(new RecordType(jsonObject).getId());
            } catch (JSONException e) {
                Log.e(TAG, "Exception getting record type '" + name + "' with object type '"
                        + objectType + ".'", e);
            }
        }

        return recordIds;
    }

    public static List<RecordType> getByType(String objectType) {

        String nameSpacedObjectType = ManifestUtils.getNamespaceSupportedObjectName(objectType, ABInBevApp.getAppContext());
        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = 'true' ORDER BY {%s:%s} ASC",
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.OBJECT_TYPE, nameSpacedObjectType,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.IS_ACTIVE,
                AbInBevObjects.RECORD_TYPE, RecordTypeFields.NAME);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.RECORD_TYPE, smartSqlFilter);
        return RecordType.getBySQLFilter(smartSql);
    }

    public static String getDefaultRecordTypeId(Context context, String objectType){
        Gson gson = new Gson();
        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(context, gson, objectType);
        RecordTypeMapping recordTypeMapping = null;
        List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
        for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
            if (recordTypeMappingItem.getDefaultRecordTypeMapping() == true) {
                return recordTypeMappingItem.getRecordTypeId();
            }
        }
        return null;
    }

    private static List<RecordType> getBySQLFilter(String sqlFilter) {
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(sqlFilter);

        ArrayList<RecordType> recordTypes = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                recordTypes.add(new RecordType(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting RecordTypes: ", e);
        }
        return recordTypes;
    }

    interface RecordTypeNameCase {
        String REPORT_CASES = "Denuncias";
        String INFORMATION_CASES = "Información";
        String COMPLAINTS_AND_CLAIMS_CASES = "Quejas y reclamos";
        String SERVICE_REQUEST_CASES = "Solicitud de Mantenimiento";
        String APPLICATIONS_CASES = "Solicitudes";
        String INSTALLATION_APPLICATIONS_CASES = "Solicitud creación de cliente";
        String UNINSTALLATION_APPLICATIONS_CASES = "Solicitudes de Desinstalacion Casos";
        String ORDERED_ASSETS_CASES = "Pedidas de Activos Casos";
        String FINDINGS_ASSETS_CASES = "Hallazgos de Activos Casos";
    }

    public static List<PicklistValue>getAllRecordTypesForCases(){
        ArrayList<PicklistValue> pickListValues = new ArrayList<>();
        String nameSpacedObjectType = ManifestUtils.getNamespaceSupportedObjectName(AbInBevObjects.CASOS, ABInBevApp.getAppContext());
        String smartSqlFilter = String.format("{%s:%s} in ('%s', '%s', '%s', '%s', '%s', '%s') AND {%s:%s} = '%s'", AbInBevObjects.RECORD_TYPE,
                RecordTypeFields.NAME, RecordTypeNameCase.REPORT_CASES, RecordTypeNameCase.INFORMATION_CASES,
                RecordTypeNameCase.COMPLAINTS_AND_CLAIMS_CASES, RecordTypeNameCase.SERVICE_REQUEST_CASES , RecordTypeNameCase.INSTALLATION_APPLICATIONS_CASES,
                RecordTypeNameCase.APPLICATIONS_CASES, AbInBevObjects.RECORD_TYPE, RecordTypeFields.OBJECT_TYPE, nameSpacedObjectType);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.RECORD_TYPE, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                RecordType tmpRecordType = new RecordType(jsonObject);
                PicklistValue pickListValue = new PicklistValue();
                pickListValue.setLabel(tmpRecordType.getName());
                pickListValue.setValue(tmpRecordType.getId());
                pickListValues.add(pickListValue);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting RecordTypes for type: ", e);
        }

        return pickListValues;
    }

    public static void createRecordType(String name) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(RecordTypeFields.NAME, name);
            DataManagerFactory.getDataManager().createRecord(AbInBevObjects.RECORD_TYPE, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "createRecordType: error creating RecordType with name: " + name, e);
        }
    }

    public static List<String> getAvailableRecordTypeNames(String objectName){
        List<String> recordTypeName = new ArrayList<String>();
        Gson gson = new Gson();
        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(ABInBevApp.getAppContext(), gson, objectName);
        if (objectLayouts != null) {
            List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
            for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
                boolean isAvailable = recordTypeMappingItem.getAvailable();
                Log.i(TAG, "recordType: " + recordTypeMappingItem.getName() + recordTypeMappingItem.getAvailable() +
                        recordTypeMappingItem.getRecordTypeId());
                if (isAvailable) {
                    if (recordTypeMappingItem.getName() != null)
                        recordTypeName.add(recordTypeMappingItem.getName());
                }
            }
            // we want to remove this recordType since this does not seem to be available
            // when we do a lookup on the RecordType table
            if (recordTypeName.remove("Principal")) {
                Log.i(TAG, "removed Principal recordType");
            }
        }

        return recordTypeName;
    }

    public static String getRecordTypeIdFromMetaData(String objectName, String recordName){
        Gson gson = new Gson();
        ObjectLayouts objectLayouts = MetaDataProvider.getMetaDataForLayouts(ABInBevApp.getAppContext(), gson, objectName);
        List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
        for (RecordTypeMapping recordTypeMappingItem : recordTypeMappingList) {
            if (recordName.equalsIgnoreCase(recordTypeMappingItem.getName())) {
                return recordTypeMappingItem.getRecordTypeId();
            }
        }
        return null;
    }

    // this gets the recordTypeId


    public static List<String> getNewCaseRecordTypes() {
        String userId = UserAccountManager.getInstance().getStoredUserId();
        User user = User.getUserByUserId(userId);

        List<String> availableRecordTypes = getAvailableRecordTypeNames(AbInBevObjects.CASE);

        List<AssetActions__c> assetActions = AssetActions__c.getAssetCasesByCountryCode(user.getCountry());

        for (AssetActions__c assetAction : assetActions) {
            availableRecordTypes.remove(assetAction.getRecordType());
        }

        availableRecordTypes.remove(AbInBevConstants.CasosFields.CASO_DE_ACTIVOS);
        return availableRecordTypes;
    }
}
