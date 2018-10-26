package com.salesforce.dsa.data.model;

import static com.salesforce.androidsyncengine.utils.CharUtils.SPACE;
import static com.salesforce.androidsyncengine.utils.StringUtils.appendLine;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class Address implements Parcelable {

    private static final String TAG = Address.class.getSimpleName();

    private static final String ADDRESS_SUFFIX = "Address";

    public static final String[] ADDRESS_FIELD_SUFFIXES = { "City", "Country", "GeocodeAccuracy",
            "Latitude", "Longitude", "PostalCode", "State", "Street" };

    private final JSONObject jsonObject;

    public Address(JSONObject json) {
        this.jsonObject = json;
    }

    public Address() {
        this(new JSONObject());
    }

    public static String getAddressFieldPrefix(String fieldName) {
        if (TextUtils.isEmpty(fieldName)) return fieldName;

        if (fieldName.endsWith(ADDRESS_SUFFIX)) {
            return fieldName.substring(0, fieldName.length() - ADDRESS_SUFFIX.length());
        }
        else {
            return fieldName;
        }
    }

    public void setValue(String key, Object value) {
        put(key, value);
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public String getCity() {
        return jsonObject.optString(AddressFields.CITY);
    }

    public void setCity(String city) {
        put(AddressFields.CITY, city);
    }

    public String getCountry() {
        return jsonObject.optString(AddressFields.COUNTRY);
    }

    public void setCountry(String country) {
        put(AddressFields.COUNTRY, country);
    }

    public String getGeocodeAccuracy() {
        return jsonObject.optString(AddressFields.GEOCODE_ACCURACY);
    }

    public void setGeocodeAccuracy(String geocodeAccuracy) {
        put(AddressFields.GEOCODE_ACCURACY, geocodeAccuracy);
    }

    public Double getLatitude() {
        return !jsonObject.isNull(AddressFields.LATITUDE) ? jsonObject.optDouble(AddressFields.LATITUDE) : null;
    }

    public void setLatitude(Double latitude) {
        put(AddressFields.LATITUDE, latitude);
    }

    public Double getLongitude() {
        return !jsonObject.isNull(AddressFields.LONGITUDE) ? jsonObject.optDouble(AddressFields.LONGITUDE) : null;
    }

    public void setLongitude(Double longitude) {
        put(AddressFields.LONGITUDE, longitude);
    }

    public String getPostalCode() {
        return jsonObject.optString(AddressFields.POSTAL_CODE);
    }

    public void setPostalCode(String postalCode) {
        put(AddressFields.POSTAL_CODE, postalCode);
    }

    public String getState() {
        return jsonObject.optString(AddressFields.STATE);
    }

    public void setState(String state) {
        put(AddressFields.STATE, state);
    }

    public String getStreet() {
        return jsonObject.optString(AddressFields.STREET);
    }

    public void setStreet(String street) {
        put(AddressFields.STREET, street);
    }

    public Object getValue(String fieldName) {
        return jsonObject.opt(fieldName);
    }

    public String getPrintableAddress() {
        StringBuilder sb = new StringBuilder();

        // First line of address.
        appendLine(sb, getCountry());

        // Second line of address.
        appendLine(sb, SPACE, getPostalCode(), getState(), getCity());

        // Third line of address.
        appendLine(sb, getStreet());

        // Fourth line of address.
        Double lat = getLatitude();
        Double lon = getLongitude();
        if (lat != null && lon != null) {
            appendLine(sb, String.format(Locale.US, "%.5f, %.5f", lat, lon));
        }

        return sb.toString();
    }

    private void put(String key, Object val) {
        try {
            jsonObject.put(key, val);
        } catch (JSONException e) {
            Log.w(TAG, "Error while setting val: " + val + " to field: " + key, e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.jsonObject.toString());
    }

    protected Address(Parcel in) {
        try {
            this.jsonObject = new JSONObject(in.readString());
        } catch (JSONException e) {
            throw new IllegalStateException("Couldn't read saved json");
        }
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
