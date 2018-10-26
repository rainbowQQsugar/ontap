package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.CasosFields;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Caso extends SFBaseObject {

//    public interface CasosStates {
//        String OPEN = "Abierto";
//        String IN_PROGRESS_LEVEL_1 = "En proceso Nivel 1";
//        String IN_PROGRESS_LEVEL_2 = "En proceso Nivel 2";
//        String ASSIGNED = "Asignado";
//        String IN_PROGRESS = "En proceso";
//        String MISSALLOCATED = "Mal asignado";
//        String REASIGNED = "Reasignado";
//        String RESOLVED = "Resuelto";
//    }

    public static final String TAG = Caso.class.getName();
    public static final String ASSET_CASE_TYPE_NAME = "Caso de Activos";

    public Caso(JSONObject json) {
        super(AbInBevObjects.CASOS, json);
    }

    public Caso(String accountId) {
        this(accountId, null, null);
    }

    public Caso(String accountId, String assetId) {
        this(accountId, assetId, null);
    }

    public Caso(String accountId, String assetId, String parentId) {
        super(AbInBevObjects.CASOS, new JSONObject());
        if(TextUtils.isEmpty(accountId)) {
            throw new IllegalArgumentException("Missing required info to create new case -  "
                    + "ensure you are providing a recordType, contact, and account");
        }

        setStringValueForKey(CasosFields.NOMBRE_DE_LA_CUENTA, accountId);
        setStringValueForKey(CasosFields.ESTADO__C, CasosFields.STATUS_OPEN);
        setStringValueForKey(CasosFields.PRIORIDAD__C, "2");
        setStringValueForKey(CasosFields.ORIGEN_DEL_CASO__C, "Mobile");
        if (!TextUtils.isEmpty(assetId)) {
            setStringValueForKey(CasosFields.ACTIVO_POR_CLIENTE__C, assetId);
        }
        if(!TextUtils.isEmpty(parentId)) {
            setStringValueForKey(CasosFields.PARENT_ID, parentId);
        }
    }

    public String getRecordName() {
        RecordType recordType = RecordType.getById(getRecordTypeId());
        if (recordType != null) {
            return recordType.getName();
        } else {
            return null;
        }
    }

    public void setRecordName(String recordName) {
        JSONObject jsonObject = getJsonObject(CasosFields.RECORD_TYPE);
        if (jsonObject == null) {
            jsonObject = new JSONObject();
            setJsonObject(CasosFields.RECORD_TYPE, jsonObject);
        }

        try {
            jsonObject.put(StdFields.NAME, recordName);
        } catch (JSONException e) {
            Log.e(TAG, "Error setting json value", e);
        }
    }

    // TODO: Babu: Not sure what this actually means ... Need to understand this ...
    public boolean isAssetCase() {
        return ASSET_CASE_TYPE_NAME.equals(getRecordName());
    }

    public static boolean isAssetCaseRecordType(String recordTypeName) {
        if (CasosFields.CASO_DE_ACTIVOS.equals(recordTypeName)) {
            return true;
        } else {
            String userId = UserAccountManager.getInstance().getStoredUserId();
            User user = User.getUserByUserId(userId);
            List<AssetActions__c> assetActions = AssetActions__c.getByCountryCode(user.getCountry());

            for (AssetActions__c assetAction : assetActions) {
                if (assetAction.getRecordType().equals(recordTypeName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getRecordTypeId() {
        return getStringValueForKey(CasosFields.RECORD_TYPE_ID);
    }

    public void setRecordTypeId(String recordId) {
        setStringValueForKey(CasosFields.RECORD_TYPE_ID, recordId);
    }

    public String getNombreDeLaCuenta() {
        return getStringValueForKey(CasosFields.NOMBRE_DE_LA_CUENTA);
    }

    public void setNombreDeLaCuenta(String nombre) {
        setStringValueForKey(CasosFields.NOMBRE_DE_LA_CUENTA, nombre);
    }

    public String getNombreDelContacto() {
        return getStringValueForKey(CasosFields.NOMBRE_DEL_CONTACTO__C);
    }

    public void setNombreDelContacto(String nombre) {
        setStringValueForKey(CasosFields.NOMBRE_DEL_CONTACTO__C, nombre);
    }

    public String getActivoPorClient(){
        return getStringValueForKey(CasosFields.ACTIVO_POR_CLIENTE__C);
    }

    public void setActivoPorClient(String id){
        setStringValueForKey(CasosFields.ACTIVO_POR_CLIENTE__C, id);
    }

    public String getQuantityRequired() {
        return getStringValueForKey(CasosFields.QUANTITY2__C);
    }

    public String getParentId() {
        return getStringValueForKey(CasosFields.PARENT_ID);
    }

    public void setParentId(String parentId) {
        setStringValueForKey(CasosFields.PARENT_ID, parentId);
    }

    public void setParentIdDelC(String value) {
        setStringValueForKey(CasosFields.PARENT_ID_DEL__C, value);
    }

    public String getPrioridad(){
        return getStringValueForKey(CasosFields.PRIORIDAD__C);
    }

    public void setPrioridad(String prioridad){
        setStringValueForKey(CasosFields.PRIORIDAD__C, prioridad);
    }

    public String getOwnerId(){
        return getStringValueForKey(CasosFields.OWNER_ID);
    }

    public void setOwnerId(String ownerId){
        setStringValueForKey(CasosFields.OWNER_ID, ownerId);
    }

    public String getScheduledDate() {
        return getStringValueForKey(CasosFields.SCHEDULED_DATE);
    }

    public void setScheduledDate(String scheduledDate) {
        setStringValueForKey(CasosFields.SCHEDULED_DATE, scheduledDate);
    }

    public String getOrigenDelCaso() {
        return getStringValueForKey(CasosFields.ORIGEN_DEL_CASO__C);
    }

    public void setOrigenDelCaso(String origin) {
        setStringValueForKey(CasosFields.ORIGEN_DEL_CASO__C, origin);
    }

    public String getState() {
        return getStringValueForKey(CasosFields.STATE);
    }

    public void setState(String state) {
        setStringValueForKey(CasosFields.STATE, state);
    }

    public static Caso getCasoById(String casoId) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.CASOS, SyncEngineConstants.StdFields.ID, casoId);
        return new Caso(jsonObject);
    }

    public static List<Caso> getRelatedCases(String id) {
        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.CASOS, CasosFields.PARENT_ID, id);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CASOS, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Caso> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Caso(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting related cases: " + id, e);
        }
        return results;
    }

    public static boolean updateQuantityRequired(String id, String quantity) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(CasosFields.QUANTITY2__C, quantity);
            return DataManagerFactory.getDataManager().updateRecord(AbInBevObjects.CASOS, id, jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "update case quantity failed", e);
        }
        return false;
    }

    public void setQuanity(String quantity) {
        setStringValueForKey(CasosFields.QUANTITY2__C, quantity);
    }

    public void setId(String Id) {
        setStringValueForKey(StdFields.ID, Id);
    }
}
