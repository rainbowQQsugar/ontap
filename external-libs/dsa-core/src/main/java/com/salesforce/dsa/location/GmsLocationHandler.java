/**
 * @author <a href="mailto:usanaga@salesforce.com">Usha Sanaga</a>
 * Copyright (c) 2014 Salesforce. All rights reserved.
 */

package com.salesforce.dsa.location;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.salesforce.dsa.R;

class GmsLocationHandler implements LocationHandler, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "LocationHandler";
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;        // Update frequency in seconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;        // The fastest update frequency, in seconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private LocationReceiver receiver;
    private GoogleApiClient locationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private Builder dialog;

    private boolean isConnected;

    public GmsLocationHandler(LocationReceiver receiver) {
        this.receiver = receiver;

        this.locationClient = new GoogleApiClient.Builder(receiver.getReceivingActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.locationRequest = LocationRequest.create();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(UPDATE_INTERVAL);
        this.locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(receiver.getReceivingActivity(), receiver.getFailureResolutionRequestCode());

                // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // TODO: handle?
                Log.w(TAG, e);
            }
        } else {
            receiver.handleUnresolvedError(result.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Location services connected");

        // If already requested, start periodic updates
        if (servicesConnected() && locationClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
            if (receiver != null) receiver.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location services disconnected");
        //TODO: implement this method
    }

    public boolean servicesConnected() {

        if (receiver == null) return false;

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(receiver.getReceivingActivity());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Google Play services is available.");
            return true;
        } else {
            receiver.handleUnresolvedError(resultCode);
            return false;
        }
    }

    public void connect() {
        isConnected = true;

        if (servicesConnected() && !locationClient.isConnected()) {
            locationClient.connect();
        }
    }

    public void disconnect() {
        isConnected = false;

        if (locationClient.isConnected() && servicesConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, this);
            locationClient.disconnect();
        }
    }

    public void unregisterReceiver() {
        receiver = null;
    }

    public Location getCurrentLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            if (checkLocationEnabled()) {
                // Get the current location
                if (currentLocation == null) {
                    if (locationClient.isConnected()) {
                        currentLocation = LocationServices.FusedLocationApi.getLastLocation(locationClient);
                    } else if (!locationClient.isConnecting()) {
                        locationClient.connect();
                    }
                }
            }
        }
        if (currentLocation == null) {
            Activity activity = receiver.getReceivingActivity();
            Toast.makeText(activity, activity.getResources().getString(R.string.locating_current_location), Toast.LENGTH_SHORT).show();
        }
        return currentLocation;
    }

    public LatLng getCurrentLatLng() {
        return convertToLatLng(getCurrentLocation());
    }


    @Override
    public String analyzeErrorCode(int errorCode) {
        return GooglePlayServicesUtil.getErrorString(errorCode);
    }

    private LatLng convertToLatLng(Location loc) {
        return loc == null ? null : new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    private boolean checkLocationEnabled() {
        @SuppressWarnings("deprecation")
        String provider = Settings.Secure.getString(receiver.getReceivingActivity().getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.equals("")) {
            // location enabled
            return true;
        } else {
            dialog = new Builder(receiver.getReceivingActivity());
            dialog.setMessage("Access to location is disabled. Please enable location services in settings");
            dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    receiver.getReceivingActivity().startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Cancel", null);
            dialog.show();
            return false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " + location.getLatitude() + "," + location.getLongitude() + " accuracy: " + location.getAccuracy();
        Log.d(TAG, msg);

        if (!isConnected) {
            Log.w(TAG, "Receiving location while disconnected.");
            return;
        }

        float accuracy = location.getAccuracy();
        if (accuracy > 0) {
            currentLocation = location;
            if (receiver != null) {
                receiver.onNewLocationReceived(location);
            }
        }
    }
}
