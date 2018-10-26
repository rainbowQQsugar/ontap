package com.abinbev.dsa.syncmanifest;

import java.util.Map;

/**
 * Created by Jakub Stefanowski on 16.05.2017.
 */

public class DynamicFetchParamsHelper {
    final String objectName;
    final Map<String, String> params;

    DynamicFetchParamsHelper(String objectName, Map<String, String> params) {
        this.objectName = objectName;
        this.params = params;
    }

    public String get(String paramName) {
        if (params == null || !params.containsKey(paramName)) {
            throw new IllegalStateException("Dynamic fetch params map for " + objectName + " doesn't have value for key: " + paramName);
        }
        else {
            return params.get(paramName);
        }
    }
}
