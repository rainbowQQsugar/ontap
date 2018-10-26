package com.salesforce.androidsyncengine.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * Util class to check the device network type
 * and checks if the sync should be performed based on the values 
 * stored in preferences 
 * @author usanaga
 *
 */
public class DeviceNetworkUtils {
	
	private static final String TAG = "DeviceNetworkUtils";
	
	private DeviceNetworkUtils() {}
	
	public static boolean shouldPerformSync(Context context) {
		boolean shouldPerformSync = false;
		
		if (isWifi(context)) {
			shouldPerformSync = PreferenceUtils.getWifiSync(context);
		} else {
			
			CellNetworkType type = getCellNetworkType(context);
			switch(type) {
			case EDGE:
				shouldPerformSync = PreferenceUtils.getEdgeSync(context);
				break;
			case _3G:
				shouldPerformSync = PreferenceUtils.get3gSync(context);
				break;
			case _4G:
				shouldPerformSync = PreferenceUtils.get4gSync(context);
				break;
			case LTE:
				shouldPerformSync = PreferenceUtils.getLteSync(context);
				break;
			case UNKNOWN:
				shouldPerformSync = true;
				break;
			}
		}
		
		return shouldPerformSync;
	}

	public static String getNetworkType(Context context) {
		if (isWifi(context)) {
			return "WI-FI";
		}
		else {
			switch (getCellNetworkType(context)) {

				case EDGE:
					return "EDGE";
				case _3G:
					return "3G";
				case _4G:
					return "4G";
				case LTE:
					return "LTE";
				default:
					return "UNKNOWN";
			}
		}
	}

	public static boolean isConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	private static CellNetworkType getCellNetworkType(Context context) {
		CellNetworkType type;
		
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = tm.getNetworkType();
        switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				type = CellNetworkType.EDGE;
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_UMTS:
				type = CellNetworkType._3G;
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				type = CellNetworkType._4G;
				break;
			case TelephonyManager.NETWORK_TYPE_LTE:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				type = CellNetworkType.LTE;
				break;
			default:
				Log.w(TAG, "Uncategorized network type: " + networkType);
				type = CellNetworkType.UNKNOWN;
				break;
					
		}
        
        return type;
	}

	private static boolean isWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
	}

	private enum CellNetworkType {
		EDGE, _3G, _4G, LTE, UNKNOWN;
	}
	
}
