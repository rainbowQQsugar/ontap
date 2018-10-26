package com.salesforce.androidsyncengine.datamanager.synchelper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class Subresponse {

    private static final String KEY_STATUS_CODE = "statusCode";

    private static final String KEY_RESULT = "result";

    private int statusCode;

    private JSONObject response;

    public Subresponse(JSONObject response) {
        this.response = response;
        this.statusCode = response.optInt(KEY_STATUS_CODE);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
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

    public static Subresponse createFrom(JSONObject jsonObject) {
        return new Subresponse(jsonObject);
    }
}
