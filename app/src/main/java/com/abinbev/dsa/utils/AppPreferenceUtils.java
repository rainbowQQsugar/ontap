package com.abinbev.dsa.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.salesforce.androidsyncengine.utils.Guava.Joiner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by bduggirala on 1/2/16.
 */
public class AppPreferenceUtils {

    private static final String USER_PROFILE_FIELD_KEY= "user_profile";
    private static final String COUNTRY_ALIAS_FIELD_KEY= "country_alias";
    private static final String LANGUAGE_LOCALE_KEY = "language_locale";

    private static final String PEDIDO_SYNC_REQUIRED = "pedido_sync_required";

    private static AppPreferenceUtils instance;

    private final SharedPreferences prefs;

    public static AppPreferenceUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (AppPreferenceUtils.class) {
                if (instance == null) {
                    instance = new AppPreferenceUtils(context.getApplicationContext());
                }
            }
        }

        return instance;
    }

    private AppPreferenceUtils(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void removeValue(String key) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.commit();
    }

    private void putValue(String key, Object value) {
        SharedPreferences.Editor editor = prefs.edit();
        if (value != null) {
            if (value instanceof String) {
                editor.putString(key, (String)value);
            } else if (value instanceof Set) {
                editor.putStringSet(key, (Set<String>)value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float)value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean)value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            }  else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof List) {
                editor.putString(key, Joiner.on(',').join((List<String>) value));
            }
        }
        editor.commit();
    }

    private <T> T getValueFromPrefs(String key, Class<T> type, T defValue) {
        if (type == String.class) {
            return (T) prefs.getString(key, (String) defValue);
        } else if (type == Set.class) {
            return (T) prefs.getStringSet(key, (Set<String>) defValue);
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(prefs.getBoolean(key, (Boolean) defValue));
        } else if (type == Float.class) {
            return (T) Float.valueOf(prefs.getFloat(key, (Float) defValue));
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(prefs.getInt(key, (Integer) defValue));
        } else if (type == Long.class) {
            return (T) Long.valueOf(prefs.getLong(key, (Long) defValue));
        } else if (type == List.class) {
            return (T) Arrays.asList(prefs.getString(key, "").split(","));
        }

        return defValue;
    }

    public String getUserProfile() {
        return getValueFromPrefs(USER_PROFILE_FIELD_KEY, String.class, null);
    }

    public String getCountryAlias() {
        return getValueFromPrefs(COUNTRY_ALIAS_FIELD_KEY, String.class, null);
    }

    public void putUserProfile(Object value) {
        putValue(USER_PROFILE_FIELD_KEY, value);
    }

    public void putCountryAlias(Object value) {
        putValue(COUNTRY_ALIAS_FIELD_KEY, value);
    }

    public void putLanguageLocale(Object value) {
        putValue(LANGUAGE_LOCALE_KEY, value);
    }

    public String getLanguageLocale() {
        //default to spanish for this project,
        //default value prevents crash on existing installs without requiring a full sync
        return getValueFromPrefs(LANGUAGE_LOCALE_KEY, String.class, "es");
    }

    public boolean getPedidoSyncRequired() {
        return getValueFromPrefs(PEDIDO_SYNC_REQUIRED, Boolean.class, false);
    }

    public void putPedidoSyncRequired(Object value) {
        putValue(PEDIDO_SYNC_REQUIRED, value);
    }

    public static String getUserProfile(Context context) {
        return AppPreferenceUtils.getInstance(context).getUserProfile();
    }

    public static String getCountryAlias(Context context) {
        return AppPreferenceUtils.getInstance(context).getCountryAlias();
    }

    public static void putUserProfile(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putUserProfile(value);
    }

    public static void putCountryAlias(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putCountryAlias(value);
    }

    public static void putLanguageLocale(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putLanguageLocale(value);
    }

    public static String getLanguageLocale(Context context) {
        return AppPreferenceUtils.getInstance(context).getLanguageLocale();
    }

    public static boolean getPedidoSyncRequired(Context context) {
        return AppPreferenceUtils.getInstance(context).getPedidoSyncRequired();
    }

    public static void putPedidoSyncRequired(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putPedidoSyncRequired(value);
    }

    // only to assist with testing surveys

    private static final String SURVEY_TAKER_ID_KEY= "survey_taker_id";
    private static final String ACCOUNT_ID_KEY= "account_id";

    private static final String BUNDLE_LOC_KEY= "bundle_location";

    public String getSurveyTakerId() {
        return getValueFromPrefs(SURVEY_TAKER_ID_KEY, String.class, null);
    }

    public void putSurveyTakerId(Object value) {
        putValue(SURVEY_TAKER_ID_KEY, value);
    }

    public String getAccountId() {
        return getValueFromPrefs(ACCOUNT_ID_KEY, String.class, null);
    }

    public void putAccountId(Object value) {
        putValue(ACCOUNT_ID_KEY, value);
    }

    public String getBundleLocation() {
        return getValueFromPrefs(BUNDLE_LOC_KEY, String.class, null);
    }

    public void putBundleLocation(Object value) {
        putValue(BUNDLE_LOC_KEY, value);
    }

    public static String getSurveyTakerId(Context context) {
        return AppPreferenceUtils.getInstance(context).getSurveyTakerId();
    }

    public static void putSurveyTakerId(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putSurveyTakerId(value);
    }

    public static String getAccountId(Context context) {
        return AppPreferenceUtils.getInstance(context).getAccountId();
    }

    public static void putAccountId(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putAccountId(value);
    }

    public static String getBundleLocation(Context context) {
        return AppPreferenceUtils.getInstance(context).getBundleLocation();
    }

    public static void putBundleLocation(Context context, Object value) {
        AppPreferenceUtils.getInstance(context).putBundleLocation(value);
    }

    private final static String CURRENT_ACCOUNT_ID_IN_VISIT = "CURRENT_ACCOUNT_ID_IN_VISIT";

    public static void putAccountIdOfCuriosity(Context context, String value){
        AppPreferenceUtils.getInstance(context).putValue(CURRENT_ACCOUNT_ID_IN_VISIT, value);
    }

    public static String getAccountIdOfCuriosity(Context context){
        return AppPreferenceUtils.getInstance(context).getValueFromPrefs(CURRENT_ACCOUNT_ID_IN_VISIT, String.class, null);
    }

    public static void removeAccountIdOfCuriosity(Context context){
        AppPreferenceUtils.getInstance(context).removeValue(CURRENT_ACCOUNT_ID_IN_VISIT);
    }


}
