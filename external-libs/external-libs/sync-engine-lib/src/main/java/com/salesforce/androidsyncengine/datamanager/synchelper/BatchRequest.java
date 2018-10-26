package com.salesforce.androidsyncengine.datamanager.synchelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class BatchRequest {

    private static final String KEY_BATCH_REQUESTS = "batchRequests";
    private static final String KEY_HALT_ON_ERROR = "haltOnError";

    private List<Subrequest> subrequests;

    private boolean haltOnError;

    public BatchRequest(List<Subrequest> subrequests, boolean haltOnError) {
        this.subrequests = subrequests;
        this.haltOnError = haltOnError;
    }

    public List<Subrequest> getSubrequests() {
        return subrequests;
    }

    public void setSubrequests(List<Subrequest> subrequests) {
        this.subrequests = subrequests;
    }

    public boolean isHaltOnError() {
        return haltOnError;
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONArray batchRequests = new JSONArray();
        for (Subrequest subrequest : subrequests) {
            batchRequests.put(subrequest.toJSONObject());
        }
        return new JSONObject()
                .put(KEY_BATCH_REQUESTS, batchRequests)
                .put(KEY_HALT_ON_ERROR, haltOnError);
    }
}
