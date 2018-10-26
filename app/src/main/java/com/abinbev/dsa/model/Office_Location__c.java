package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import static com.abinbev.dsa.utils.AbInBevConstants.OfficeLocationFields.LATITUDE;
import static com.abinbev.dsa.utils.AbInBevConstants.OfficeLocationFields.LONGITUDE;

/**
 * Created by Jakub Stefanowski on 03.06.2017.
 */

public class Office_Location__c extends SFBaseObject {

    private static final String TAG = Office_Location__c.class.getSimpleName();


    public Office_Location__c(JSONObject json) {
        super(AbInBevObjects.OFFICE_LOCATION, json);
    }

    public Office_Location__c() {
        super(AbInBevObjects.OFFICE_LOCATION);
    }

    public double getLatitude() {
        return getDoubleValueForKey(LATITUDE);
    }

    public void setLatitude(double latitude) {
        setDoubleValueForKey(LATITUDE, latitude);
    }

    public boolean hasLatitude() {
        return !isNullValue(LATITUDE);
    }

    public double getLongitude() {
        return getDoubleValueForKey(LONGITUDE);
    }

    public void setLongitude(double longitude) {
        setDoubleValueForKey(LONGITUDE, longitude);
    }

    public boolean hasLongitude() {
        return !isNullValue(LONGITUDE);
    }

    public LatLng getLocation() {
        if (!hasLatitude() || !hasLongitude()) {
            return null;
        }
        else {
            double lat = getLatitude();
            double lon = getLongitude();
            return new LatLng(lat, lon);
        }
    }

    public static Office_Location__c getById(String id) {
        try {
            DataManager dataManager = DataManagerFactory.getDataManager();
            JSONObject jsonObject = dataManager.exactQuery(AbInBevObjects.OFFICE_LOCATION, StdFields.ID, id);
            return jsonObject == null ? null : new Office_Location__c(jsonObject);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Office_Location__c", e);
        }
        return null;
    }
}
