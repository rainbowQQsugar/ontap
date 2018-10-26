package com.abinbev.dsa.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.abinbev.dsa.BuildConfig;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lukaszwalukiewicz on 22.01.2016.
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public static LatLng getCurrentLocation(Context context){

        if (BuildConfig.CHINA_BUILD) {
            AMapLocationClient client = new AMapLocationClient(context);
            AMapLocation location = client.getLastKnownLocation();
            return location == null ? null : new LatLng(location.getLatitude(), location.getLongitude());
        }
        else {
            LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            try {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                return new LatLng(latitude, longitude);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static boolean isGooglePlayServicesDevice(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        Log.i(TAG, "resultCode: " + resultCode);
        switch (resultCode) {
            case ConnectionResult.SUCCESS: return true;
            case ConnectionResult.SERVICE_MISSING: return false;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: return true;
            case ConnectionResult.SERVICE_DISABLED: return true;
            case ConnectionResult.SERVICE_INVALID: return false;
            default: return false;
        }
    }

    public static float calculateDistance(LatLng pointA, LatLng pointB) {
        float[] result = { -1f };

        if (pointA != null && pointB != null) {
            if (BuildConfig.CHINA_BUILD) {
                AMapLocation.distanceBetween(pointA.latitude, pointA.longitude, pointB.latitude, pointB.longitude, result);
            }
            else {
                Location.distanceBetween(pointA.latitude, pointA.longitude, pointB.latitude, pointB.longitude, result);
            }
        }
        Log.v(TAG, "Point A: " + pointA);
        Log.v(TAG, "Point B: " + pointB);
        
        Log.v(TAG, "result: " + result[0]);
        return result[0];
    }
}
