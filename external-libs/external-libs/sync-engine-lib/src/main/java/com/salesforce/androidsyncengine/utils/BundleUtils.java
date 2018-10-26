package com.salesforce.androidsyncengine.utils;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jstafanowski on 05.02.18.
 */

public final class BundleUtils {
    private BundleUtils(){}


    public static Map<String, Object> toMap(Bundle bundle) {
        Map<String, Object> result = new HashMap<>();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                result.put(key, bundle.get(key));
            }
        }

        return result;
    }

    public static Map<String, String> toStringMap(Bundle bundle) {
        Map<String, String> result = new HashMap<>();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                result.put(key, bundle.getString(key, null));
            }
        }

        return result;
    }

    public static Bundle toBundle(Map<String, String> map) {
        Bundle bundle = new Bundle();
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                bundle.putString(key, value);
            }
        }

        return bundle;
    }
}
