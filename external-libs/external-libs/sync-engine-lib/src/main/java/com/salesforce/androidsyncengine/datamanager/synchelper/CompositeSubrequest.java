package com.salesforce.androidsyncengine.datamanager.synchelper;

import com.salesforce.androidsdk.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public class CompositeSubrequest {

    private static final String KEY_METHOD = "method";
    private static final String KEY_URL = "url";
    private static final String KEY_BODY = "body";
    private static final String KEY_HTTP_HEADERS = "httpHeaders";
    private static final String KEY_REFERENCE_ID = "referenceId";

    private RestRequest.RestMethod method;

    private String url;

    private Object body;

    private Map<String, String> headers;

    private String referenceId;

    private CompositeResponseHandler responseHandler;

    public CompositeSubrequest(RestRequest.RestMethod method, String url, String referenceId) {
        this.method = method;
        this.url = url;
        this.referenceId = referenceId;
    }

    public CompositeSubrequest(RestRequest.RestMethod method, String url, String referenceId, Object body) {
        this.method = method;
        this.url = url;
        this.body = body;
        this.referenceId = referenceId;
    }

    public CompositeResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(CompositeResponseHandler responseHandler) {
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

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject()
                .put(KEY_METHOD, method)
                .put(KEY_URL, url)
                .put(KEY_BODY, body)
                .put(KEY_HTTP_HEADERS, headers)
                .put(KEY_REFERENCE_ID, referenceId);
    }
}
