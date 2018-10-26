package com.salesforce.androidsyncengine.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.salesforce.androidsyncengine.utils.CollectionUtils.fill;

/**
 * Created by Jakub Stefanowski on 21.09.2017.
 */

public final class JSONUtils {

    private static final String TAG = "JSONUtils";

    private JSONUtils() {}

    public static JSONObject deepCopy(JSONObject jsonObject) {
        if (jsonObject == null) return null;

        JSONObject newJson = new JSONObject();

        for (Iterator<String> it = jsonObject.keys(); it.hasNext();) {
            String key = it.next();
            Object value = deepCopy(jsonObject.opt(key));
            silentPut(newJson, key, value);
        }

        return newJson;
    }

    public static JSONArray deepCopy(JSONArray jsonArray) {
        if (jsonArray == null) return null;

        JSONArray newJson = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = deepCopy(jsonArray.opt(i));
            silentPut(newJson, i, value);
        }

        return newJson;
    }

    public static Object deepCopy(Object o) {
        if (o instanceof JSONObject) {
            return deepCopy((JSONObject) o);
        }
        else if (o instanceof JSONArray) {
            return deepCopy((JSONArray) o);
        }
        else {
            return o;
        }
    }

    public static void silentPut(JSONObject jsonObject, String key, Object value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            // This shouldn't happen.
            Log.e(TAG, "Couldn't add value to JSONObject.", e);
        }
    }

    private static void silentPut(JSONArray jsonArray, int index, Object value) {
        try {
            jsonArray.put(index, value);
        } catch (JSONException e) {
            // This shouldn't happen.
            Log.e(TAG, "Couldn't add value to JSONArray.", e);
        }
    }

    public static boolean deepEquals(JSONObject j1, JSONObject j2) {
        if (j1 == j2) return true;
        if (j1 == null || j2 == null) return false;
        if (j1.length() != j2.length()) return false;

        Set<String> keys = new HashSet<>();
        fill(keys, j1.keys());
        fill(keys, j2.keys());

        for (String key : keys) {
            if (!deepEquals(j1.opt(key), j2.opt(key))) {
                return false;
            }
        }

        return true;
    }

    public static boolean deepEquals(JSONArray j1, JSONArray j2) {
        if (j1 == j2) return true;
        if (j1 == null || j2 == null) return false;
        if (j1.length() != j2.length()) return false;

        int length = j1.length();
        for (int i = 0; i < length; i++) {
            if (!deepEquals(j1.opt(i), j2.opt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean deepEquals(Object o1, Object o2) {
        if (o1 == o2) return true;

        if (o1 instanceof JSONObject) {
            if (o2 instanceof JSONObject) {
                return deepEquals((JSONObject) o1, (JSONObject) o2);
            }
            else {
                return false;
            }
        }

        if (o1 instanceof JSONArray) {
            if (o2 instanceof JSONArray) {
                return deepEquals((JSONArray) o1, (JSONArray) o2);
            }
            else {
                return false;
            }
        }

        if (o1 == JSONObject.NULL) {
            return o1.equals(o2);
        }

        if (o2 == JSONObject.NULL) {
            return o2.equals(o1);
        }

        return Objects.equals(o1, o2);
    }
}
