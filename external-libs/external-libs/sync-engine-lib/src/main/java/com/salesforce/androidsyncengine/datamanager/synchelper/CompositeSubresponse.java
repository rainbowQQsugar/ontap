package com.salesforce.androidsyncengine.datamanager.synchelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class CompositeSubresponse {

    private static final String KEY_STATUS_CODE = "httpStatusCode";

    private static final String KEY_RESULT = "body";

    private static final String KEY_HEADERS = "httpHeaders";

    private static final String KEY_REFERENCE_ID = "referenceId";

    private int statusCode;

    private Map<String, String> headers;

    private JSONObject response;

    private String referenceId;

    public CompositeSubresponse(JSONObject response) {
        this.response = response;
        this.statusCode = response.optInt(KEY_STATUS_CODE);
        this.referenceId = response.optString(KEY_REFERENCE_ID);
        this.headers = getHeaders(response.optJSONObject(KEY_HEADERS));
    }

    private static Map<String, String> getHeaders(JSONObject headers) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            Iterator<String> iterator = headers.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                result.put(key, headers.optString(key));
            }
        }

        return result;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public JSONObject asJSONObject() {
        return response.optJSONObject(KEY_RESULT);
    }

    public JSONArray asJSONArray() {
        return response.optJSONArray(KEY_RESULT);
    }

    public String asString() {
        return asJSONObject().toString();
    }

    public static CompositeSubresponse createFrom(JSONObject jsonObject) {
        return new CompositeSubresponse(jsonObject);
    }
}
