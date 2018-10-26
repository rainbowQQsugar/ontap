package com.abinbev.dsa.utils.amap;

import android.util.Log;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Created by Jakub Stefanowski on 11.08.2017.
 */

public class LatLngBoundsBuilder {

    private static final String TAG = "LatLngBoundsBuilder";

    private double southLat = 1.0D / 0.0;
    private double northLat = -1.0D / 0.0;
    private double westLong = 0.0D / 0.0;
    private double eastLong = 0.0D / 0.0;

    private double border = 300; // meters

    public LatLngBoundsBuilder() {
    }

    public LatLngBoundsBuilder include(LatLng latLng) {
        if (latLng == null) {
            return this;
        } else {
            southLat = Math.min(southLat, latLng.latitude);
            northLat = Math.max(northLat, latLng.latitude);
            double longitude = latLng.longitude;

            if (Double.isNaN(westLong)) {
                westLong = longitude;
                eastLong = longitude;
            }
            else if (isInLongitudeRange(longitude)) {
                if (degreesToLeft(westLong, longitude) < degreesToRight(eastLong, longitude)) {
                    westLong = longitude;
                } else {
                    eastLong = longitude;
                }
            }

            return this;
        }
    }

    private boolean isInLongitudeRange(double longitude) {
        return westLong <= eastLong ? westLong <= longitude && longitude <= eastLong : westLong <= longitude || longitude <= eastLong;
    }

    public LatLngBounds build() {
        try {
            if (Double.isNaN(westLong)) {
                Log.w(TAG, "no included points");
                return null;
            } else {
                LatLng southWest = new LatLng(southLat, westLong);
                southWest = computeOffset(southWest, border, 180);
                southWest = computeOffset(southWest, border, 270);
                LatLng northEast = new LatLng(northLat, eastLong);
                northEast = computeOffset(northEast, border, 0);
                northEast = computeOffset(northEast, border, 90);
                return new LatLngBounds(southWest, northEast);
            }
        } catch (Throwable e) {
            Log.w(TAG, e);
            return null;
        }
    }

    /** How many degrees from start to target going west. */
    private static double degreesToLeft(double startLong, double targetLong) {
        return (startLong - targetLong + 360.0D) % 360.0D;
    }

    /** How many degrees from start to target going east. */
    private static double degreesToRight(double startLong, double targetLong) {
        return (targetLong - startLong + 360.0D) % 360.0D;
    }

    static final double EARTH_RADIUS = 6371009;

    private static LatLng computeOffset(LatLng from, double distance, double heading) {
        distance /= EARTH_RADIUS;
        heading = toRadians(heading);
        // http://williams.best.vwh.net/avform.htm#LL
        double fromLat = toRadians(from.latitude);
        double fromLng = toRadians(from.longitude);
        double cosDistance = cos(distance);
        double sinDistance = sin(distance);
        double sinFromLat = sin(fromLat);
        double cosFromLat = cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading);
        double dLng = atan2(
                sinDistance * cosFromLat * sin(heading),
                cosDistance - sinFromLat * sinLat);
        return new LatLng(toDegrees(asin(sinLat)), toDegrees(fromLng + dLng));
    }
}
