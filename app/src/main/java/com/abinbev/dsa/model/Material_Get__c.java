package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MaterialFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 12/11/15.
 */
public class Material_Get__c extends SFBaseObject implements Material__c {

    private static final String TAG = "Material_Get__c";

    protected Material_Get__c() {
        super(AbInBevObjects.MATERIAL_GET);
    }

    public Material_Get__c(JSONObject jsonObject) {
        super(AbInBevObjects.MATERIAL_GET, jsonObject);
    }

    public String getCode() {
        return getStringValueForKey(MaterialFields.CODE);
    }

    @Override
    public String getDescription() {
        return getStringValueForKey(MaterialFields.DESCRIPTION);
    }

    @Override
    public String getScore() {
        return getStringValueForKey(MaterialFields.SCORE);
    }

    @Override
    public String getCalculation() {
        return getStringValueForKey(MaterialFields.CALCULATION);
    }

    public String getPoints() {
        return getStringValueForKey(MaterialFields.POINTS);
    }

    @Override
    public String getExclusive() {
        return getStringValueForKey(MaterialFields.EXCLUSIVE);
    }

    @Override
    public String getGroup() {
        return getStringValueForKey(MaterialFields.GROUP);
    }

    @Override
    public String getComment() {
        return getStringValueForKey(MaterialFields.COMMENT);
    }

    @Override
    public Material_Get__c copy() {
        JSONObject oldJson = toJson();
        JSONObject newJson = null;
        try {
            newJson = oldJson == null ? null : new JSONObject(oldJson.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error while cloning Material Give", e);
        }
        return new Material_Get__c(newJson);
    }

    public static List<Material_Get__c> fetchGetsForPackage(String packageId) {
        List<Material_Get__c> getList = new ArrayList<>();
        try {
            String recordName = "Item por Paquete (Get)";
            RecordType recordType = RecordType.getByName(recordName);
            if (TextUtils.isEmpty(recordType.getId())) {
                Log.e(TAG, "fetchGivesByPackageId: Unable to find RecordType with name " + recordName);
            } else {
                String filter = String.format("{%1$s:%2$s} = {%3$s:%4$s} AND {%3$s:%5$s} = ('%6$s') AND {%3$s:%7$s} = ('%8$s')",
                        AbInBevObjects.MATERIAL_GET, SyncEngineConstants.StdFields.ID,
                        AbInBevObjects.PACKAGE_ITEM, AbInBevConstants.PackageItemFields.MATERIAL_GET,
                        AbInBevConstants.PackageItemFields.RECORD_TYPE_ID, recordType.getId(),
                        AbInBevConstants.PackageItemFields.PACKAGE, packageId);
                String smartSQL = String.format("SELECT {%1$s:_soup} FROM {%1$s}, {%2$s} WHERE %3$s",
                        AbInBevObjects.MATERIAL_GET, AbInBevObjects.PACKAGE_ITEM, filter);

                JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                    Material_Get__c give = new Material_Get__c(jsonObject);
                    getList.add(give);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "fetchGivesByPackageId: Error getting MaterialGet by Package Id", e);
        }
        return getList;
    }

    public static Material_Get__c fetchById(String id) {
        try {
            String filter = String .format("{%1$s:%2$s} = '%3$s'", AbInBevObjects.MATERIAL_GET, SyncEngineConstants.StdFields.ID, id);
            String smartSQL = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.MATERIAL_GET, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);

            JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
            return new Material_Get__c(jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "fetchById: Error getting Material Get with id: " + id, e);
        }
        return null;
    }

    public static List<Material_Get__c> fetchAll() {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchAllObjects(dm, Material_Get__c.class);
    }

}
