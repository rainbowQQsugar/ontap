package com.salesforce.androidsyncengine.datamanager.synchelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class CompositeRequest {

    private static final String KEY_SUBREQUESTS = "compositeRequest";
    private static final String KEY_ALL_OR_NONE = "allOrNone";

    private List<CompositeSubrequest> subrequests;

    private boolean allOrNone;

    public CompositeRequest(List<CompositeSubrequest> subrequests, boolean allOrNone) {
        this.subrequests = subrequests;
        this.allOrNone = allOrNone;
    }

    public List<CompositeSubrequest> getSubrequests() {
        return subrequests;
    }

    public void setSubrequests(List<CompositeSubrequest> subrequests) {
        this.subrequests = subrequests;
    }

    public boolean isAllOrNone() {
        return allOrNone;
    }

    public void setAllOrNone(boolean allOrNone) {
        this.allOrNone = allOrNone;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONArray compositeRequests = new JSONArray();
        for (CompositeSubrequest subrequest : subrequests) {
            compositeRequests.put(subrequest.toJSONObject());
        }
        return new JSONObject()
                .put(KEY_SUBREQUESTS, compositeRequests)
                .put(KEY_ALL_OR_NONE, allOrNone);
    }
}
