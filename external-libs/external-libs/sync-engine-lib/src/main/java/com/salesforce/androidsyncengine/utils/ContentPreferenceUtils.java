package com.salesforce.androidsyncengine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Simple util class to make all key usage consistent
 * @author bduggirala
 */
public class ContentPreferenceUtils {

	// Keys are the file names and the values are the download long values that
	// are generated when we add the file to the download queue
	private static final String PREF_NAME = "content_dm_link";
	private static SharedPreferences prefs;
	private static Editor editor;
	
	private ContentPreferenceUtils() {}
	
	private static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}
	
	@SuppressWarnings("unchecked")
	private static void putValueInPrefs(String key, Object value, Context context) {
		if (prefs == null) prefs = getPreferences(context);
		if (editor == null) editor = prefs.edit();
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
			} else if (value instanceof Long) {
				editor.putLong(key, (Long) value);
			} else if (value instanceof List) {
				editor.putString(key, Joiner.on(',').join((List<String>) value));
			}
		}
		editor.commit();
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getValueFromPrefs(String key, Class<T> type, T defValue, Context context) {
		if (prefs == null) prefs = getPreferences(context);
		
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
	
	public static void removeValue(String key, Context context) {
		if (prefs == null) prefs = getPreferences(context);
		if (editor == null) editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}
	
	public static void putValue(String key, Long value, Context context) {
		putValueInPrefs(key, value, context);
	}
	
	public static Long getValue(String key, Context context) {
		return getValueFromPrefs(key, Long.class, Long.valueOf(0), context);
	}
}
