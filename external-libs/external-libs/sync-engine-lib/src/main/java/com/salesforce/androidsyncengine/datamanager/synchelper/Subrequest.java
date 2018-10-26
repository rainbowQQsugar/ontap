package com.salesforce.androidsyncengine.datamanager.synchelper;

import com.salesforce.androidsdk.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class Subrequest {

    private static final String KEY_METHOD = "method";
    private static final String KEY_URL = "url";
    private static final String KEY_RICH_INPUT = "richInput";

    private RestRequest.RestMethod method;

    private String url;

    private Map<String, Object> richInput;

    private BatchResponseHandler responseHandler;

    public Subrequest(RestRequest.RestMethod method, String url) {
        this.method = method;
        this.url = url;
    }

    public Subrequest(RestRequest.RestMethod method, String url, Map<String, Object> richInput) {
        this.method = method;
        this.url = url;
        this.richInput = richInput;
    }

    public BatchResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(BatchResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public RestRequest.RestMethod getMethod() {
        return method;
    }

    public void setMethod(RestRequest.RestMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getRichInput() {
        return richInput;
    }

    public void setRichInput(Map<String, Object> richInput) {
        this.richInput = richInput;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject()
                .put(KEY_METHOD, method)
                .put(KEY_URL, url)
                .put(KEY_RICH_INPUT, richInput == null ? null : new JSONObject(richInput));
    }
}
