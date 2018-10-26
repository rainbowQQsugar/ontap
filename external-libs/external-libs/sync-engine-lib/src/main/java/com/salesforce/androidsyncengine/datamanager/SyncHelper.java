package com.salesforce.androidsyncengine.datamanager;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.R;
import com.salesforce.androidsyncengine.datamanager.exceptions.ConnectionLostException;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SimpleSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.UnsupportedEncodingSyncException;
import com.salesforce.androidsyncengine.datamanager.model.QueueObject;
import com.salesforce.androidsyncengine.datamanager.synchelper.BatchRequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.BatchResponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeRequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeResponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.CompositeSubresponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;
import com.salesforce.androidsyncengine.utils.LongLog;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * SyncHelper: Class to assist in interacting with sync
 *
 * @author Babu Duggirala
 */
public class SyncHelper {
    private static final String TAG = "SyncHelper";

    public static final int MAX_SUBREQUESTS = 25;
    // There is a smaller limit of subrequests for queries.
    public static final int MAX_QUERY_SUBREQUESTS = 9;

    public static final boolean LOG_REQUESTS = false;
    private RestClient client;
    private String apiVersion;
    private Context context;

    private static final Map<String, String> CREATE_UPDATE_HTTP_HEADERS;

    static {
        Map<String, String> h = new HashMap<String, String>();
        h.put("Sforce-Auto-Assign", "FALSE");
        CREATE_UPDATE_HTTP_HEADERS = Collections.unmodifiableMap(h);
    }

    public static SyncHelper createNew(Context context) {
        String apiVersion = context.getString(R.string.api_version);
        ClientManager clientManager = new ClientManager(context,
                SalesforceSDKManager.getInstance().getAccountType(),
                SalesforceSDKManager.getInstance().getLoginOptions(),
                SalesforceSDKManager.getInstance().shouldLogoutWhenTokenRevoked());

        RestClient client = clientManager.peekRestClient();
        return new SyncHelper(client, apiVersion, context);
    }

    public SyncHelper(RestClient client, String apiVersion, Context context) {
        this.client = client;
        this.apiVersion = apiVersion;
        this.context = context.getApplicationContext();
    }

    public RestClient getClient() {
        return client;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public RestResponse sendUpdateRequest(QueueObject queueObj, Map<String, Object> updatedFields, String serverName)
            throws ConnectionLostException, UnsupportedEncodingSyncException {
        return sendRawRequest(getUpdateRequest(queueObj, updatedFields, serverName));
    }

    public RestResponse sendUpdateRequest(String objectId, Map<String, Object> updatedFields, String serverName)
            throws ConnectionLostException {
        return sendRawRequest(getUpdateRequest(objectId, updatedFields, serverName));
    }

    public RestRequest getUpdateRequest(QueueObject queueObj, Map<String, Object> updatedFields, String serverName) {
        return getUpdateRequest(queueObj.getId(), updatedFields, serverName);
    }

    public RestRequest getUpdateRequest(String objectId, Map<String, Object> updatedFields, String serverName) {
        return RestRequest.getRequestForUpdate(apiVersion, serverName, objectId, updatedFields);

    }

    public RestResponse sendDeleteRequest(QueueObject queueObj, String serverName)
            throws ConnectionLostException {
        return sendRawRequest(getDeleteRequest(queueObj, serverName));
    }

    public RestRequest getDeleteRequest(QueueObject queueObj, String serverName) {
        return RestRequest.getRequestForDelete(apiVersion, serverName, queueObj.getId());
    }

    public RestResponse sendCreateRequest(Map<String, Object> fields, String serverName)
            throws ConnectionLostException, UnsupportedEncodingSyncException {
        return sendRawRequest(getCreateRequest(fields, serverName));
    }

    public RestRequest getCreateRequest(Map<String, Object> fields, String serverName) {
        return RestRequest.getRequestForCreate(apiVersion, serverName, fields);
    }

    public RestResponse sendFetchRequest(String query)
            throws ConnectionLostException, UnsupportedEncodingSyncException {
        return sendRawRequest(getFetchRequest(query));
    }

    public RestRequest getFetchRequest(String query)
            throws UnsupportedEncodingSyncException {
        try {
            return RestRequest.getRequestForQuery(apiVersion, query);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Error while constructing RestRequest.", e);
            throw new UnsupportedEncodingSyncException(e, "Couldn't create correct request. Encoding is not supported.");
        }
    }

    public RestResponse sendRawRequest(RestRequest request) throws ConnectionLostException {
        try {
            return client.sendSync(request);
        } catch (IOException e) {
            Log.w(TAG, "Error while sending request.", e);
            throw ConnectionLostException.create(e, context);
        }
    }

    public Subrequest getUpdateSubrequest(QueueObject queueObj, Map<String, Object> updatedFields, String serverName) {
        String url = RestAction.UPDATE.getPath(apiVersion, serverName, queueObj.getId());
        return new Subrequest(RestRequest.RestMethod.PATCH, url, updatedFields);
    }

    public CompositeSubrequest getUpdateCompositeRequest(QueueObject queueObj, Object body, String serverName, String referenceId) {
        String url = RestAction.UPDATE.getPath(apiVersion, serverName, queueObj.getId());
        return new CompositeSubrequest(RestRequest.RestMethod.PATCH, url, referenceId, body);
    }

    public Subrequest getDeleteSubrequest(QueueObject queueObj, String serverName) {
        String url = RestAction.DELETE.getPath(apiVersion, serverName, queueObj.getId());
        return new Subrequest(RestRequest.RestMethod.DELETE, url);
    }

    public CompositeSubrequest getDeleteCompositeRequest(QueueObject queueObj, String serverName, String referenceId) {
        String url = RestAction.DELETE.getPath(apiVersion, serverName, queueObj.getId());
        return new CompositeSubrequest(RestRequest.RestMethod.DELETE, url, referenceId);
    }

    public Subrequest getCreateSubrequest(QueueObject queueObj, Map<String, Object> fields, String serverName) {
        String url = RestAction.CREATE.getPath(apiVersion, serverName);
        return new Subrequest(RestRequest.RestMethod.POST, url, fields);
    }

    public CompositeSubrequest getCreateCompositeRequest(QueueObject queueObj, Object data, String serverName, String referenceId) {
        String url = RestAction.CREATE.getPath(apiVersion, serverName);
        return new CompositeSubrequest(RestRequest.RestMethod.POST, url, referenceId, data);
    }

    public Subrequest getFetchSubrequest(String query) throws UnsupportedEncodingSyncException {
        try {
            StringBuilder path = new StringBuilder(RestAction.QUERY.getPath(apiVersion));
            path.append("?q=");
            path.append(URLEncoder.encode(query, HTTP.UTF_8));

            return new Subrequest(RestRequest.RestMethod.GET, path.toString());
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingSyncException(e, "Couldn't create correct request. Encoding is not supported.");
        }
    }

    public CompositeSubrequest getFetchCompositeRequest(String query, String referenceId) throws UnsupportedEncodingSyncException {
        try {
            StringBuilder path = new StringBuilder(RestAction.QUERY.getPath(apiVersion));
            path.append("?q=");
            path.append(encodeExceptReferences(query));

            return new CompositeSubrequest(RestRequest.RestMethod.GET, path.toString(), referenceId);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingSyncException(e, "Couldn't create correct request. Encoding is not supported.");
        }
    }

    /**
     * This method encodes whole string except composite request references.
     */
    private String encodeExceptReferences(String str) throws UnsupportedEncodingException {
        Matcher matcher = Pattern.compile("\\@\\{.*?\\}").matcher(str);
        String result = "";
        int position = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (start > position) {
                result += URLEncoder.encode(str.substring(position, start), HTTP.UTF_8);
            }

            if (end > start) {
                result += str.substring(start, end);
            }

            position = end;
        }

        if (position < str.length()) {
            result += URLEncoder.encode(str.substring(position, str.length()), HTTP.UTF_8);
        }

        return result;
    }

    public Subrequest getDescribeSubrequest(String apiVersion, String objectType) {
        String url = RestAction.DESCRIBE.getPath(apiVersion, objectType);
        return new Subrequest(RestRequest.RestMethod.GET, url);
    }

    public CompositeSubrequest getDescribeCompositeRequest(String apiVersion, String objectType, String referenceId) {
        String url = RestAction.DESCRIBE.getPath(apiVersion, objectType);
        return new CompositeSubrequest(RestRequest.RestMethod.GET, url, referenceId);
    }

    public Subrequest getLayoutSubrequest(String apiVersion, String objectType) {
        String url = RestAction.LAYOUT.getPath(apiVersion, objectType);
        return new Subrequest(RestRequest.RestMethod.GET, url);
    }

    public CompositeSubrequest getLayoutCompositeRequest(String apiVersion, String objectType, String referenceId) {
        String url = RestAction.LAYOUT.getPath(apiVersion, objectType);
        return new CompositeSubrequest(RestRequest.RestMethod.GET, url, referenceId);
    }

    public Subrequest getLayoutSubrequest(String apiVersion, String objectType, String id) {
        String url = RestAction.SPECIFIC_LAYOUT.getPath(apiVersion, objectType, id);
        return new Subrequest(RestRequest.RestMethod.GET, url);
    }

    public CompositeSubrequest getLayoutCompositeRequest(String apiVersion, String objectType, String id, String referenceId) {
        String url = RestAction.SPECIFIC_LAYOUT.getPath(apiVersion, objectType, id);
        return new CompositeSubrequest(RestRequest.RestMethod.GET, url, referenceId);
    }

    public Subrequest getCompactLayoutSubrequest(String apiVersion, String objectType) {
        String url = RestAction.COMPACT_LAYOUT.getPath(apiVersion, objectType);
        return new Subrequest(RestRequest.RestMethod.GET, url);
    }

    public Subrequest getCompactLayoutSubrequest(String apiVersion, String objectType, String id) {
        String url = RestAction.SPECIFIC_COMPACT_LAYOUT.getPath(apiVersion, objectType, id);
        return new Subrequest(RestRequest.RestMethod.GET, url);
    }

    public Subrequest getRawSubrequest(String url) {
        return getRawSubrequest(RestRequest.RestMethod.GET, url);
    }

    public Subrequest getRawSubrequest(RestRequest.RestMethod method, String url) {
        return new Subrequest(method, url);
    }

    public CompositeSubrequest getRawCompositeRequest(String url, String referenceId) {
        return getRawCompositeRequest(RestRequest.RestMethod.GET, url, referenceId);
    }

    public CompositeSubrequest getRawCompositeRequest(RestRequest.RestMethod method, String url, String referenceId) {
        return new CompositeSubrequest(method, url, referenceId);
    }

    public RestResponse sendSubrequests(List<Subrequest> subrequests, boolean haltOnError, Map<String, String> additionalHeaders) throws ConnectionLostException, UnsupportedEncodingSyncException, SimpleSyncException {

        RestRequest restRequest = getBatchRequest(subrequests, haltOnError, additionalHeaders);
        return sendRawRequest(restRequest);
    }

    private RestRequest getBatchRequest(List<Subrequest> subrequests, boolean haltOnError, Map<String, String> additionalHeaders) throws UnsupportedEncodingSyncException, SimpleSyncException {
        BatchRequest request = new BatchRequest(subrequests, haltOnError);
        RequestBody requestBody;

        try {
            if (LOG_REQUESTS) {
                Log.i(TAG, "batch request body: " + request.toJSONObject().toString(2));
            }

            String jsonString = request.toJSONObject().toString();
            MediaType type = MediaType.parse("application/json; charset=utf-8");
            requestBody = RequestBody.create(type, jsonString);
        } catch (JSONException e) {
            throw new SimpleSyncException(e);
        }

        String path = RestAction.BATCH.getPath(apiVersion);

        Map<String, String> headers = new HashMap<>();
        headers.putAll(CREATE_UPDATE_HTTP_HEADERS);
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders);
        }

        return new RestRequest(RestRequest.RestMethod.POST, path, requestBody, headers);
    }

    private RestRequest getCompositeRequest(List<CompositeSubrequest> subrequests, boolean allOrNone, Map<String, String> additionalHeaders) throws UnsupportedEncodingSyncException, SimpleSyncException {
        CompositeRequest request = new CompositeRequest(subrequests, allOrNone);
        RequestBody requestBody;

        try {
            if (LOG_REQUESTS) {
                Log.i(TAG, "composite request body: ");
                LongLog.i(TAG, request.toJSONObject().toString(2));
            }

            String jsonString = request.toJSONObject().toString();

            MediaType type = MediaType.parse("application/json; charset=utf-8");
            requestBody = RequestBody.create(type, jsonString);
        } catch (JSONException e) {
            throw new SimpleSyncException(e);
        }

        String path = RestAction.COMPOSITE.getPath(apiVersion);

        Map<String, String> headers = new HashMap<>();
        headers.putAll(CREATE_UPDATE_HTTP_HEADERS);
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders);
        }

        return new RestRequest(RestRequest.RestMethod.POST, path, requestBody, headers);
    }

    protected RestRequest checkLastModify(String jsonObject) {
        RequestBody requestBody;
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        requestBody = RequestBody.create(type, jsonObject);

        String path = RestAction.CHECK_LAST_MODIFY.getPath();

        Map<String, String> headers = new HashMap<>();
        headers.putAll(CREATE_UPDATE_HTTP_HEADERS);
        return new RestRequest(RestRequest.RestMethod.POST, path, requestBody, headers);
    }

    public String getUserId() {
        return client.getClientInfo().userId;
    }

    public void sendBatchRequests(List<Subrequest> subrequests, boolean haltOnError) throws SyncException {
        sendBatchRequests(subrequests, haltOnError, null);
    }

    public void sendBatchRequests(List<Subrequest> subrequests, boolean haltOnError, Map<String, String> additionalHeaders) throws SyncException {
        sendBatchRequests(subrequests, haltOnError, additionalHeaders, MAX_SUBREQUESTS);
    }

    public void sendBatchRequests(List<Subrequest> subrequests, boolean haltOnError, Map<String, String> additionalHeaders, int maxRequestPerBatch) throws SyncException {
        if (subrequests == null || subrequests.isEmpty()) return;

        Log.v(TAG, "Trying to send " + subrequests.size() + " subrequests.");

        int startIndex = 0;
        int endIndex = Math.min(startIndex + maxRequestPerBatch, subrequests.size());

        while (startIndex < subrequests.size()) {
            Log.v(TAG, "Sending subrequests from " + startIndex + " to " + endIndex);

            List<Subrequest> sublistOfRequests = subrequests.subList(startIndex, endIndex);
            RestRequest request = getBatchRequest(sublistOfRequests, haltOnError, additionalHeaders);
            RestResponse response = sendRawRequest(request);

            if (!response.isSuccess()) {
                throw ServerErrorException.createFrom(response, context);
            }

            try {
                BatchResponse batchResponse = BatchResponse.createFrom(response);
                List<Subresponse> responseList = batchResponse.getElements();

                List<Subrequest> followingRequests = handleBatchResponses(request, response, sublistOfRequests, responseList);
                Log.v(TAG, "Adding " + followingRequests.size() + " following requests.");
                subrequests.addAll(endIndex, followingRequests);
            } catch (JSONException e) {
                throw new SimpleSyncException(e, "Unable to read json response.");
            } catch (IOException e) {
                throw ConnectionLostException.create(e, context);
            }

            startIndex = endIndex;
            endIndex = Math.min(startIndex + maxRequestPerBatch, subrequests.size());
        }
    }

    public void sendCompositeRequests(List<CompositeSubrequest> subrequests, boolean allOrNone) throws SyncException {
        sendCompositeRequests(subrequests, allOrNone, null);
    }

    public void sendCompositeRequests(List<CompositeSubrequest> subrequests, boolean allOrNone, Map<String, String> additionalHeaders) throws SyncException {
        if (subrequests == null || subrequests.isEmpty()) return;

        Log.v(TAG, "Trying to send " + subrequests.size() + " subrequests.");

        int startIndex = 0;
        int maxRequestPerBatch = getMaxSubrequestsCount(startIndex, subrequests);
        int endIndex = Math.min(startIndex + maxRequestPerBatch, subrequests.size());

        while (startIndex < subrequests.size()) {
            Log.v(TAG, "Sending subrequests from " + startIndex + " to " + endIndex);

            List<CompositeSubrequest> sublistOfRequests = subrequests.subList(startIndex, endIndex);
            List<CompositeSubrequest> nonEmptyRequests = getNonEmptyRequests(subrequests, startIndex, endIndex);

            RestRequest request = getCompositeRequest(nonEmptyRequests, allOrNone, additionalHeaders);
            RestResponse response = sendRawRequest(request);

            if (!response.isSuccess()) {
                throw ServerErrorException.createFrom(response, context);
            }

            try {
                if (LOG_REQUESTS) {
                    Log.i(TAG, "composite response: ");
                    LongLog.i(TAG, response.asJSONObject().toString(2));
                }
                CompositeResponse compositeResponse = CompositeResponse.createFrom(response);
                List<CompositeSubresponse> responseList = compositeResponse.getElements();

                List<CompositeSubrequest> followingRequests = handleCompositeResponses(request, response, sublistOfRequests, responseList);
                Log.v(TAG, "Adding " + followingRequests.size() + " following requests.");
                subrequests.addAll(endIndex, followingRequests);
            } catch (JSONException e) {
                throw new SimpleSyncException(e, "Unable to read json response.");
            } catch (IOException e) {
                throw ConnectionLostException.create(e, context);
            }

            startIndex = endIndex;
            maxRequestPerBatch = getMaxSubrequestsCount(startIndex, subrequests);
            endIndex = Math.min(startIndex + maxRequestPerBatch, subrequests.size());
        }
    }

    /**
     * Returns true if list of subrequests can fit one composite request.
     */
    public boolean canFitOneRequest(List<CompositeSubrequest> subrequests) {
        return subrequests == null || getMaxSubrequestsCount(0, subrequests) >= subrequests.size();
    }

    private List<CompositeSubrequest> getNonEmptyRequests(List<CompositeSubrequest> subrequests, int startIndex, int endIndex) {
        ArrayList<CompositeSubrequest> result = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            CompositeSubrequest subrequest = subrequests.get(i);
            if (!isEmptyRequest(subrequest)) {
                result.add(subrequest);
            }
        }

        return result;
    }

    /**
     * Calculates how many subrequests we can take for batch.
     */
    private int getMaxSubrequestsCount(int startIndex, List<CompositeSubrequest> subrequests) {
        if (subrequests == null || subrequests.isEmpty() || startIndex >= subrequests.size()) {
            return 0;
        }

        boolean isFinished = false;
        int emptyRequests = 0;
        int queryRequests = 0;
        int maxRequests = 0;

        for (int i = startIndex; i < subrequests.size() && !isFinished; i++) {
            CompositeSubrequest subrequest = subrequests.get(i);
            if (isEmptyRequest(subrequest)) {
                emptyRequests++;
            } else if (isQueryRequest(subrequest)) {
                queryRequests++;
            }

            maxRequests++;

            isFinished = maxRequests >= MAX_SUBREQUESTS || queryRequests >= MAX_QUERY_SUBREQUESTS;
        }

        Log.i(TAG, "Found " + emptyRequests + " empty requests, " + queryRequests
                + " query requests and returned " + maxRequests + " max requests.");

        return maxRequests;
    }

    private boolean isEmptyRequest(CompositeSubrequest subrequest) {
        return subrequest.getUrl() == null || subrequest.getMethod() == null;
    }

    private boolean isQueryRequest(CompositeSubrequest subrequest) {
        return subrequest.getMethod() == RestRequest.RestMethod.GET &&
                (subrequest.getUrl().contains("/query/") || subrequest.getUrl().contains("/queryAll/"));
    }

    private List<Subrequest> handleBatchResponses(RestRequest originalRequest, RestResponse originalResponse, List<Subrequest> subrequests, List<Subresponse> subresponses) throws SyncException {
        List<Subrequest> followingRequests = new ArrayList<>();
        int size = Math.min(subrequests.size(), subresponses.size());

        for (int i = 0; i < size; i++) {
            Subrequest subrequest = subrequests.get(i);
            Subresponse subresponse = subresponses.get(i);

            if (subrequest.getResponseHandler() != null) {
                Subrequest following = subrequest.getResponseHandler()
                        .handleResponse(originalRequest, originalResponse, subrequest, subresponse);
                if (following != null) {
                    followingRequests.add(following);
                }
            } else {
                Log.w(TAG, "Found subrequest without callback.");
            }
        }

        return followingRequests;
    }

    private List<CompositeSubrequest> handleCompositeResponses(RestRequest originalRequest, RestResponse originalResponse, List<CompositeSubrequest> subrequests, List<CompositeSubresponse> subresponses) throws SyncException {
        List<CompositeSubrequest> followingRequests = new ArrayList<>();
        Map<String, CompositeSubresponse> responsePerReferenceId = new HashMap<>();

        for (CompositeSubresponse response : subresponses) {
            responsePerReferenceId.put(response.getReferenceId(), response);
        }

        for (int i = 0; i < subrequests.size(); i++) {
            CompositeSubrequest subrequest = subrequests.get(i);

            if (subrequest.getResponseHandler() != null) {
                String referenceId = subrequest.getReferenceId();
                CompositeSubresponse subresponse = responsePerReferenceId.get(referenceId);

                CompositeSubrequest following = subrequest.getResponseHandler()
                        .handleResponse(originalRequest, originalResponse, subrequest, subresponse);
                if (following != null) {
                    followingRequests.add(following);
                }
            } else {
                Log.w(TAG, "Found subrequest without callback.");
            }
        }

        return followingRequests;
    }

    public enum RestAction {
        BATCH("/services/data/%s/composite/batch/"),
        COMPOSITE("/services/data/%s/composite/"),
        VERSIONS("/services/data/"),
        RESOURCES("/services/data/%s/"),
        DESCRIBE_GLOBAL("/services/data/%s/sobjects/"),
        METADATA("/services/data/%s/sobjects/%s/"),
        DESCRIBE("/services/data/%s/sobjects/%s/describe/"),
        LAYOUT("/services/data/%s/sobjects/%s/describe/layouts"),
        SPECIFIC_LAYOUT("/services/data/%s/sobjects/%s/describe/layouts/%s"),
        COMPACT_LAYOUT("/services/data/%s/sobjects/%s/describe/compactLayouts"),
        SPECIFIC_COMPACT_LAYOUT("/services/data/%s/sobjects/%s/describe/compactLayouts/%s"),
        CREATE("/services/data/%s/sobjects/%s"),
        RETRIEVE("/services/data/%s/sobjects/%s/%s"),
        UPSERT("/services/data/%s/sobjects/%s/%s/%s"),
        UPDATE("/services/data/%s/sobjects/%s/%s"),
        DELETE("/services/data/%s/sobjects/%s/%s"),
        QUERY("/services/data/%s/query"),
        SEARCH("/services/data/%s/search"),
        SEARCH_SCOPE_AND_ORDER("/services/data/%s/search/scopeOrder"),
        SEARCH_RESULT_LAYOUT("/services/data/%s/search/layout"),
        CHECK_LAST_MODIFY("/services/apexrest/CheckLastModify");

        private final String pathTemplate;

        RestAction(String uriTemplate) {
            this.pathTemplate = uriTemplate;
        }

        public String getPath(Object... args) {
            return String.format(pathTemplate, args);
        }
    }
}
