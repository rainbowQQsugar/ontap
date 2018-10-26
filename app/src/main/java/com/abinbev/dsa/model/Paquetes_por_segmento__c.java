package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PackageFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wandersonblough on 12/10/15.
 */
public class Paquetes_por_segmento__c extends SFBaseObject {

    private static final String TAG = "Paquetes_por_segmento__";

    public Paquetes_por_segmento__c() {
        super(AbInBevObjects.PACKAGE);
    }

    public Paquetes_por_segmento__c(JSONObject jsonObject) {
        super(AbInBevObjects.PACKAGE, jsonObject);
    }

    public String getPackageId() {
        return getStringValueForKey(PackageFields.PAQUETE__C);
    }

    public String getPaqueteName() {
        JSONObject jsonObject = getJsonObject(PackageFields.PAQUETE__R);
        return jsonObject == null ? null : jsonObject.optString(StdFields.NAME, null);
    }

    public static List<Paquetes_por_segmento__c> fetchPackagesForSegment(String segment) {
        List<Paquetes_por_segmento__c> packages = new ArrayList<>();

        try {
            String filter = String.format("{%s:%s} = '%s'", AbInBevObjects.PACKAGE, PackageFields.SEGMENTO__C, segment);
            String smartSQL = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.PACKAGE, filter);

            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Paquetes_por_segmento__c paquete = new Paquetes_por_segmento__c(jsonObject);
                packages.add(paquete);
            }
        } catch (JSONException e) {
            Log.e(TAG, "fetchPackagesForSegment: Error getting Packages with segment " + segment, e);
        }
        return packages;
    }
}
