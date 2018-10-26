package com.salesforce.dsa.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Util class to handle Cordova PlugIn related items
 *
 */

public class PlugInUtils {

	public static String TAG = "PlugInUtils";

	public static JSONObject getTBDObjectArray() {

		JSONObject mainObj = new JSONObject();

		try {
			JSONObject jo = new JSONObject();
			jo.put("Name", "TBD");

			JSONArray ja = new JSONArray();
			ja.put(jo);

			mainObj.put("TBD", ja);
		} catch (JSONException je) {

		}
		return mainObj;

	}

	public static JSONArray getTBDArray() {

		JSONArray ja = new JSONArray();
		try {
			JSONObject jo = new JSONObject();
			jo.put("Name", "TBD");

			ja.put(jo);

			return ja;
		} catch (JSONException je) {

		}
		return ja;

	}

	public static Intent getNavigationIntent(Context context, String url, boolean sso, boolean externalBrowser, Class<?> activityClass) {
		if (externalBrowser) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			return i;
		} else {
			final Intent webViewIntent = new Intent(context, activityClass);
			webViewIntent.putExtra("Url", url);
			webViewIntent.putExtra("FileExtension", "LINK");
			webViewIntent.putExtra("SSO", sso);
			return webViewIntent;
		}
	}
}