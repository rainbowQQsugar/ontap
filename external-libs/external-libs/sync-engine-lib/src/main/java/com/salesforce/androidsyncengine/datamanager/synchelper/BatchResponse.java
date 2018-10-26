package com.salesforce.androidsyncengine.datamanager.synchelper;

import com.salesforce.androidsdk.rest.RestResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class BatchResponse {

    private static final String KEY_HAS_ERRORS = "hasErrors";

    private static final String KEY_RESULTS = "results";

    private boolean hasErrors;

    private List<Subresponse> elements;

    private BatchResponse(boolean hasErrors, List<Subresponse> elements) {
        this.hasErrors = hasErrors;
        this.elements = elements;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public List<Subresponse> getElements() {
        return elements;
    }

    public static BatchResponse createFrom(RestResponse response) throws IOException, JSONException {
        JSONObject jsonObject = response.asJSONObject();
        boolean hasErrors = jsonObject.optBoolean(KEY_HAS_ERRORS, false);
        JSONArray resultsJson = jsonObject.optJSONArray(KEY_RESULTS);
        List<Subresponse> results = new ArrayList<>();
        if (resultsJson != null) {
            for (int i = 0; i < resultsJson.length(); i++) {
                results.add(Subresponse.createFrom(resultsJson.optJSONObject(i)));
            }
        }

        return new BatchResponse(hasErrors, results);
    }
}
