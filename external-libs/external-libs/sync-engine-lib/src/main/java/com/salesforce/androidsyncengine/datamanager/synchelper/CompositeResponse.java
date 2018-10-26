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
public class CompositeResponse {
    private static final String KEY_RESULTS = "compositeResponse";

    private List<CompositeSubresponse> elements;

    private CompositeResponse(List<CompositeSubresponse> elements) {
        this.elements = elements;
    }

    public List<CompositeSubresponse> getElements() {
        return elements;
    }

    public static CompositeResponse createFrom(RestResponse response) throws IOException, JSONException {
        JSONObject jsonObject = response.asJSONObject();
        JSONArray resultsJson = jsonObject.optJSONArray(KEY_RESULTS);
        List<CompositeSubresponse> results = new ArrayList<>();
        if (resultsJson != null) {
            for (int i = 0; i < resultsJson.length(); i++) {
                results.add(CompositeSubresponse.createFrom(resultsJson.optJSONObject(i)));
            }
        }

        return new CompositeResponse(results);
    }
}
