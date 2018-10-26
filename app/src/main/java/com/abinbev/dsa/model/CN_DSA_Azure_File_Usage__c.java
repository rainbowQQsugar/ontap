package com.abinbev.dsa.model;

import android.content.ContentValues;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsdk.smartstore.app.SmartStoreSDKManager;
import com.salesforce.androidsdk.smartstore.store.DBHelper;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.salesforce.androidsdk.smartstore.store.SmartStore.SOUP_ENTRY_ID;
import static com.salesforce.androidsdk.smartstore.store.SmartStore.SOUP_LAST_MODIFIED_DATE;

public class CN_DSA_Azure_File_Usage__c extends SFBaseObject {

    String TAG = getClass().getSimpleName();

    public CN_DSA_Azure_File_Usage__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c, json);
    }

    protected CN_DSA_Azure_File_Usage__c() {
        super(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c);
    }


    public String getID() {
        return getStringValueForKey(SyncEngineConstants.StdFields.ID);
    }

    public long getSoupEntryID(JSONObject jsonObject) throws JSONException {
        return jsonObject.getLong(SOUP_ENTRY_ID);
    }

    public void setID(String Id) {
        setStringValueForKey(SyncEngineConstants.StdFields.ID, Id);
    }

    public void setCN_User__c(String Id) {
        setStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_User__c, Id);
    }


    //if need setFields ,pls add it self
    public String getUser__c() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_User__c);
    }

    public String getUsedTimeDate() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_Datetime__c);
    }

    public String getCN_Access_Type__c() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_Access_Type__c);
    }

    public String getCN_DSA_Azure_File__c() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_DSA_Azure_File__c);
    }

    public static boolean updateAzureFileUsage(String id, JSONObject updatedObject) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c, id, updatedObject);
    }


    public String createRecord(DataManager dm) {
        String recordId = dm.createRecord(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c, toJson());
        Log.e("CN_DSA_Azure_", "createRecord: " + recordId);
        setStringValueForKey(AbInBevConstants.PictureAuditStatusFields.ID, recordId);
        return recordId;
    }

    public void updateRecord(long soupEntryId, JSONObject jsonObject) throws JSONException {
        SmartStore smartStore = SmartStoreSDKManager.getInstance().getSmartStore();
        SQLiteDatabase db = smartStore.getDatabase();
        synchronized (db) {
            ContentValues contentValues = new ContentValues();
            jsonObject.put(SOUP_LAST_MODIFIED_DATE, System.currentTimeMillis());
            contentValues.put("soup", jsonObject.toString());
            String soupTableName = DBHelper.getInstance(db).getSoupTableName(db, AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c);
            db.update(soupTableName, contentValues, "id = ?", new String[]{soupEntryId + ""});
            db.close();
        }
    }

    public void deleteRecords(Long... ids) {
        SmartStore smartStore = SmartStoreSDKManager.getInstance().getSmartStore();
        smartStore.delete(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File_Usage__c, ids);
    }


}
