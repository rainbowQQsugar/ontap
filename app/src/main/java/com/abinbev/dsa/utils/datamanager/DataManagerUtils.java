package com.abinbev.dsa.utils.datamanager;

import android.text.TextUtils;
import android.util.Log;

import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants.Formats;

import org.apache.commons.text.StrSubstitutor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 04.09.2017.
 */

public final class DataManagerUtils {

    private static final String TAG = "DataManagerUtils";

    private static final String VARIABLE_PREFIX = "{";

    private static final String VARIABLE_SUFFIX = "}";

    private static final char ESCAPE_CHAR = '$';

    private DataManagerUtils() {}

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static <T extends SFBaseObject> List<T> fetchObjects(DataManager dm, String smartSql, Class<T> klass) {
        List<T> results = new ArrayList<>();
        try {
            Log.d(TAG, "Query: " + smartSql);
            JSONArray recordsArray = dm.fetchAllSmartSQLQuery(smartSql);
            Constructor<T> constructor = klass.getConstructor(JSONObject.class);

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(constructor.newInstance(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while calling query: " + smartSql + " for object: " + klass, e);
        }

        return results;
    }

    public static <T extends SFBaseObject> List<T> fetchObjects(DataManager dm, String smartSql, FormatValues formatValues, Class<T> klass) {
        return fetchObjects(dm, format(smartSql, formatValues), klass);
    }

    public static <T extends SFBaseObject> List<T> fetchAllObjects(DataManager dm, String objectType, Class<T> klass) {
        String smartSql = String.format(Formats.SMART_SQL_ALL_FORMAT, objectType);
        return fetchObjects(dm, smartSql, klass);
    }

    public static <T extends SFBaseObject> List<T> fetchAllObjects(DataManager dm, Class<T> klass) {
        return fetchAllObjects(dm, klass.getSimpleName(), klass);
    }

    public static <T extends SFBaseObject> T fetchObject(DataManager dm, String smartSql, Class<T> klass) {
        List<T> results = fetchObjects(dm, smartSql, klass);
        return results.isEmpty() ? null : results.get(0);
    }

    public static <T extends SFBaseObject> T fetchObject(DataManager dm, String smartSql, FormatValues formatValues, Class<T> klass) {
        return fetchObject(dm, format(smartSql, formatValues), klass);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<Integer> fetchIntegers(DataManager dm, String smartSql) {
        List<Integer> results = new ArrayList<>();
        try {
            Log.d(TAG, "Query: " + smartSql);
            JSONArray recordsArray = dm.fetchAllSmartSQLQuery(smartSql);

            for (int i = 0; i < recordsArray.length(); i++) {
                results.add(recordsArray.getJSONArray(i).getInt(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while calling query: " + smartSql + " for fetchIntegers", e);
        }

        return results;
    }

    public static List<Integer> fetchIntegers(DataManager dm, String smartSql, FormatValues formatValues) {
        return fetchIntegers(dm, format(smartSql, formatValues));
    }

    public static Integer fetchInteger(DataManager dm, String smartSql) {
        List<Integer> results = fetchIntegers(dm, smartSql);
        return results.isEmpty() ? null : results.get(0);
    }

    public static Integer fetchInteger(DataManager dm, String smartSql, FormatValues formatValues) {
        return fetchInteger(dm, format(smartSql, formatValues));
    }

    public static int fetchInt(DataManager dm, String smartSql) {
        List<Integer> results = fetchIntegers(dm, smartSql);
        return results.isEmpty() ? 0 : results.get(0);
    }

    public static int fetchInt(DataManager dm, String smartSql, FormatValues formatValues) {
        return fetchInt(dm, format(smartSql, formatValues));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> fetchStrings(DataManager dm, String smartSql) {
        List<String> results = new ArrayList<>();
        try {
            Log.d(TAG, "Query: " + smartSql);
            JSONArray recordsArray = dm.fetchAllSmartSQLQuery(smartSql);

            for (int i = 0; i < recordsArray.length(); i++) {
                results.add(recordsArray.getJSONArray(i).getString(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while calling query: " + smartSql + " for fetchStrings", e);
        }

        return results;
    }

    public static List<String> fetchStrings(DataManager dm, String smartSql, FormatValues formatValues) {
        return fetchStrings(dm, format(smartSql, formatValues));
    }

    public static String fetchString(DataManager dm, String smartSql) {
        List<String> results = fetchStrings(dm, smartSql);
        return results.isEmpty() ? null : results.get(0);
    }

    public static String fetchString(DataManager dm, String smartSql, FormatValues formatValues) {
        return fetchString(dm, format(smartSql, formatValues));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static <T extends SFBaseObject> T getById(DataManager dm, String objectType, String id, Class<T> klass) {
        return TextUtils.isEmpty(id) ? null : getByField(dm, objectType, StdFields.ID, id, klass);
    }

    public static <T extends SFBaseObject> T getById(DataManager dm, String id, Class<T> klass) {
        return getById(dm, klass.getSimpleName(), id, klass);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static <T extends SFBaseObject> T getByField(DataManager dm, String objectType, String fieldName, String fieldValue, Class<T> klass) {
        T result = null;
        JSONObject jsonObject = dm.exactQuery(objectType, fieldName, fieldValue);
        if (jsonObject != null) {
            try {
                Constructor<T> constructor = klass.getConstructor(JSONObject.class);
                result = constructor.newInstance(jsonObject);
            } catch (Exception e) {
                Log.e(TAG, "Exception while calling getById for object: " + objectType, e);
            }
        }

        return result;
    }

    public static <T extends SFBaseObject> T getByField(DataManager dm, String fieldName, String fieldValue, Class<T> klass) {
        return getByField(dm, klass.getSimpleName(), fieldName, fieldValue, klass);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String create(DataManager dm, String objectName, SFBaseObject object) {
        return dm.createRecord(objectName, object.toJson());
    }

    public static String create(DataManager dm, SFBaseObject object) {
        return create(dm, object.getClass().getSimpleName(), object);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean update(DataManager dm, String objectName, SFBaseObject object) {
        return dm.updateRecord(objectName, object.getId(), object.toJson());
    }

    public static boolean update(DataManager dm, SFBaseObject object) {
        return update(dm, object.getClass().getSimpleName(), object);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String format(String string, FormatValues values) {
        StrSubstitutor substitutor = new StrSubstitutor(values);
        substitutor.setEscapeChar(ESCAPE_CHAR);
        substitutor.setVariableSuffix(VARIABLE_SUFFIX);
        substitutor.setVariablePrefix(VARIABLE_PREFIX);
        substitutor.setValueDelimiterMatcher(null);
        return substitutor.replace(string);
    }

}
