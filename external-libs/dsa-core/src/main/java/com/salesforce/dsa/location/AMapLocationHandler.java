/**
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce. All rights reserved.
 */

package com.salesforce.dsa.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.google.android.gms.maps.model.LatLng;
import com.salesforce.dsa.R;

import static com.amap.api.location.AMapLocation.LOCATION_SUCCESS;
import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

class AMapLocationHandler implements LocationHandler, AMapLocationListener {

    private static final String TAG = "AMapLocationHandler";
    private static final String PROVIDER_GPS = "gps";

    private static final long LAST_KNOWN_LOCATION_VALIDITY = 4L * 60L * 1000L; // 4 min
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;        // Update frequency in seconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private final Context applicationContext;

    private final SharedPreferences preferences;

    private LocationReceiver receiver;

    private AMapLocationClient aMapLocationClient;

    private boolean isConnected;

    public AMapLocationHandler(LocationReceiver receiver) {
        this.receiver = receiver;
        this.applicationContext = receiver.getReceivingActivity().getApplicationContext();
        this.preferences = applicationContext.getSharedPreferences("amapLocationHandler", Context.MODE_PRIVATE);
    }

    @Override
    public void connect() {
        if (!isConnected) {
            isConnected = true;
            aMapLocationClient = new AMapLocationClient(applicationContext);

            AMapLocationClientOption aMapLocationOption = new AMapLocationClientOption();
            aMapLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            aMapLocationOption.setInterval(UPDATE_INTERVAL);
            // aMapLocationOption.setGpsFirst(true);
            aMapLocationClient.setLocationOption(aMapLocationOption);

            if (receiver != null) receiver.onConnected();

            aMapLocationClient.setLocationListener(this);
            aMapLocationClient.startLocation();

            AMapLocation lastKnownLocation = getCachedLastKnownLocation();
            if (lastKnownLocation != null
                    && (System.currentTimeMillis() - lastKnownLocation.getTime()) < LAST_KNOWN_LOCATION_VALIDITY) {
                handleLocation(lastKnownLocation, false);
            }
        }
    }

    @Override
    public void disconnect() {
        if (isConnected) {
            isConnected = false;

            aMapLocationClient.stopLocation();
            aMapLocationClient.unRegisterLocationListener(this);
            aMapLocationClient.onDestroy();
            aMapLocationClient = null;
        }
    }

    @Override
    public void unregisterReceiver() {
        receiver = null;
        disconnect();
    }

    @Override
    public LatLng getCurrentLatLng() {
        if (aMapLocationClient != null) {
            return convertToLatLng(aMapLocationClient.getLastKnownLocation());
        } else {
            AMapLocationClient temporaryClient = new AMapLocationClient(applicationContext);
            LatLng result = convertToLatLng(temporaryClient.getLastKnownLocation());
            temporaryClient.onDestroy();
            return result;
        }
    }

    @Override
    public String analyzeErrorCode(int errorCode) {
        if (receiver == null) {
            return null;
        }

        Context context = receiver.getReceivingActivity().getApplicationContext();

        switch (errorCode) {
            case AMapLocation.ERROR_CODE_AIRPLANEMODE_WIFIOFF:
                return context.getString(R.string.error_msg_airplanemode_wifioff);

            case AMapLocation.ERROR_CODE_FAILURE_AUTH:
                return context.getString(R.string.error_msg_failure_auth);

            case AMapLocation.ERROR_CODE_FAILURE_CELL:
                return context.getString(R.string.error_msg_failure_cell);

            case AMapLocation.ERROR_CODE_FAILURE_CONNECTION:
                return context.getString(R.string.error_msg_failure_connection);

            case AMapLocation.ERROR_CODE_FAILURE_INIT:
                return context.getString(R.string.error_msg_failure_init);

            case AMapLocation.ERROR_CODE_FAILURE_LOCATION:
                return context.getString(R.string.error_msg_failure_location);

            case AMapLocation.ERROR_CODE_FAILURE_LOCATION_PARAMETER:
                return context.getString(R.string.error_msg_failure_location_parameter);

            case AMapLocation.ERROR_CODE_FAILURE_LOCATION_PERMISSION:
                return context.getString(R.string.error_msg_failure_location_permission);

            case AMapLocation.ERROR_CODE_FAILURE_NOENOUGHSATELLITES:
                return context.getString(R.string.error_msg_failure_no_enough_statellites);

            case AMapLocation.ERROR_CODE_FAILURE_NOWIFIANDAP:
                return context.getString(R.string.error_msg_failure_no_wifi_and_ap);

            case AMapLocation.ERROR_CODE_FAILURE_PARSER:
                return context.getString(R.string.error_msg_failure_parse);

            case AMapLocation.ERROR_CODE_FAILURE_SIMULATION_LOCATION:
                return context.getString(R.string.error_msg_failure_simulation_location);

            case AMapLocation.ERROR_CODE_NOCGI_WIFIOFF:
                return context.getString(R.string.error_msg_no_cgi_wifi_off);

            case AMapLocation.ERROR_CODE_SERVICE_FAIL:
                return context.getString(R.string.error_msg_service_fail);

            case AMapLocation.ERROR_CODE_INVALID_PARAMETER:
                return context.getString(R.string.error_msg_invalid_parameter);

            case AMapLocation.ERROR_CODE_UNKNOWN:
                return context.getString(R.string.error_msg_unknow);

            default:
                return context.getString(R.string.error_msg_default);
        }

    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        handleLocation(location, true);
    }

    private void handleLocation(AMapLocation location, boolean saveLocation) {
        if (location == null) {
            return;
        }

        String msg = "Updated Location: " + location.getLatitude() + "," + location.getLongitude()
                + " accuracy: " + location.getAccuracy()
                + " age: " + (System.currentTimeMillis() - location.getTime()) / 1000L + "sec";
        Log.d(TAG, msg);

        if (location.getErrorCode() != LOCATION_SUCCESS) {
            if (receiver == null) {
                return;
            }

            receiver.handleUnresolvedError(location.getErrorCode());

            return;
        }

        float accuracy = location.getAccuracy();
        if (accuracy < 0) {
            return;
        }

        if (saveLocation) {
            storeLocation(location);
        }

        if (receiver != null) {
            receiver.onNewLocationReceived(location);
        }
    }

    private void storeLocation(AMapLocation location) {
        preferences.edit()
                .putLong("time", location.getTime())
                .putString("provider", location.getProvider())
                .putLong("lat", doubleToLongBits(location.getLatitude()))
                .putLong("lon", doubleToLongBits(location.getLongitude()))
                .putFloat("accuracy", location.getAccuracy())
                .apply();
    }


    private AMapLocation getCachedLastKnownLocation() {
        AMapLocation locationSavedPositively = loadStoredLocation();

        if (aMapLocationClient == null) {
            return locationSavedPositively;
        }

        AMapLocation lastKnownLocation = aMapLocationClient.getLastKnownLocation();
        if (lastKnownLocation == null || lastKnownLocation.getErrorCode() != LOCATION_SUCCESS) {
            return locationSavedPositively;
        }

        if (locationSavedPositively == null) {
            return lastKnownLocation;
        }

        if (lastKnownLocation.getTime() > locationSavedPositively.getTime()) {
            return lastKnownLocation;
        } else if (lastKnownLocation.getTime() < locationSavedPositively.getTime()) {
            return locationSavedPositively;
        }

        if (locationSavedPositively.getProvider().equals(PROVIDER_GPS)) {
            return locationSavedPositively;
        }

        if (lastKnownLocation.getProvider().equals(PROVIDER_GPS)) {
            return lastKnownLocation;
        }

        return locationSavedPositively;


    }

    private AMapLocation loadStoredLocation() {
        if (preferences.contains("provider")) {
            AMapLocation location = new AMapLocation(preferences.getString("provider", null));
            location.setTime(preferences.getLong("time", 0));
            location.setLatitude(longBitsToDouble(preferences.getLong("lat", 0)));
            location.setLongitude(longBitsToDouble(preferences.getLong("lon", 0)));
            location.setAccuracy(preferences.getFloat("accuracy", 0));
            return location;
        }

        return null;
    }

    private LatLng convertToLatLng(AMapLocation loc) {
        return loc == null ? null : new LatLng(loc.getLatitude(), loc.getLongitude());
    }
}
