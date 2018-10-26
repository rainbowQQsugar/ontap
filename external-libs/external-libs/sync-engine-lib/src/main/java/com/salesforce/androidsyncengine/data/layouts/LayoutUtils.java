package com.salesforce.androidsyncengine.data.layouts;

import android.content.Context;

import com.google.gson.Gson;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.exceptions.ServerErrorException;
import com.salesforce.androidsyncengine.datamanager.synchelper.BatchResponse;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subresponse;
import com.salesforce.androidsyncengine.utils.MathUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.salesforce.androidsyncengine.datamanager.SyncHelper.MAX_SUBREQUESTS;

/**
 * Created by bduggirala on 11/17/15.
 */
public class LayoutUtils {

    public static void sendMetadataRequestForLayouts(final Context context, RestClient client, final String apiVersion, final List<String> objectTypes) throws Exception {
        if (objectTypes == null || objectTypes.isEmpty()) return;

        final String requestTag = "layout";
        final Gson gson = new Gson();
        final SyncHelper syncHelper = new SyncHelper(client, apiVersion, context);
        final List<RecordTypeDetails> results = new ArrayList<RecordTypeDetails>();

        final SubrequestFactory<String> objectTypeSubrequestFactory = new SubrequestFactory<String>() {
            @Override
            public Subrequest subrequestFromObject(String objectType) {
                return syncHelper.getLayoutSubrequest(apiVersion, objectType);
            }
        };

        final ObjectResponseHandler<String> objectTypeResponseHandler = new ObjectResponseHandler<String>() {
            @Override
            public void responseForObject(String currentObjectType, String stringResponse) {
                ObjectLayouts objectLayouts = gson.fromJson(stringResponse, ObjectLayouts.class);

                MetaDataProvider.saveMetadataForLayouts(context, currentObjectType, stringResponse);

                List<RecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeMappings();
                for (RecordTypeMapping recordTypeMapping : recordTypeMappingList) {
                    results.add(new RecordTypeDetails(currentObjectType, recordTypeMapping.getRecordTypeId()));
                }
            }
        };

        sendPagedRequest(context, syncHelper, requestTag, objectTypes, objectTypeSubrequestFactory, objectTypeResponseHandler);

        sendMetadataRequestForIndividualLayout(context, apiVersion, results, syncHelper);
    }

    public static void sendMetadataRequestForIndividualLayout(final Context context, final String apiVersion, List<RecordTypeDetails> recordTypesList, final SyncHelper syncHelper) throws Exception {
        final String requestTag = "individual layout";

        final SubrequestFactory<RecordTypeDetails> recordTypeSubrequestFactory = new SubrequestFactory<RecordTypeDetails>() {
            @Override
            public Subrequest subrequestFromObject(RecordTypeDetails recordTypeDetails) {
                return syncHelper.getLayoutSubrequest(apiVersion,
                        recordTypeDetails.getObjectType(), recordTypeDetails.getRecordTypeId());
            }
        };

        final ObjectResponseHandler<RecordTypeDetails> recordTypeResponseHandler = new ObjectResponseHandler<RecordTypeDetails>() {
            @Override
            public void responseForObject(RecordTypeDetails recordTypeDetails, String stringResponse) {
                MetaDataProvider.saveMetadataForIndividualLayout(context,
                        recordTypeDetails.getObjectType(),
                        recordTypeDetails.getRecordTypeId(), stringResponse);
            }
        };

        sendPagedRequest(context, syncHelper, requestTag, recordTypesList, recordTypeSubrequestFactory, recordTypeResponseHandler);
    }

    public static void sendMetadataRequestForCompactLayouts(final Context context, RestClient client, final String apiVersion, final List<String> objectTypes) throws Exception {
        if (objectTypes == null || objectTypes.isEmpty()) return;

        final String requestTag = "compact layout";
        final Gson gson = new Gson();
        final SyncHelper syncHelper = new SyncHelper(client, apiVersion, context);
        final List<RecordTypeDetails> results = new ArrayList<RecordTypeDetails>();

        final SubrequestFactory<String> objectTypeSubrequestFactory = new SubrequestFactory<String>() {
            @Override
            public Subrequest subrequestFromObject(String objectType) {
                return syncHelper.getCompactLayoutSubrequest(apiVersion, objectType);
            }
        };

        final ObjectResponseHandler<String> objectTypeResponseHandler = new ObjectResponseHandler<String>() {
            @Override
            public void responseForObject(String currentObjectType, String stringResponse) {
                ObjectCompactLayouts objectLayouts = gson.fromJson(stringResponse, ObjectCompactLayouts.class);

                MetaDataProvider.saveMetadataForCompactLayouts(context, currentObjectType, stringResponse);

                List<CompactLayoutRecordTypeMapping> recordTypeMappingList = objectLayouts.getRecordTypeCompactLayoutMappings();
                for (CompactLayoutRecordTypeMapping recordTypeMapping : recordTypeMappingList) {
                    results.add(new RecordTypeDetails(currentObjectType, recordTypeMapping.getRecordTypeId()));
                }
            }
        };

        sendPagedRequest(context, syncHelper, requestTag, objectTypes, objectTypeSubrequestFactory, objectTypeResponseHandler);

        sendMetadataRequestForIndividualCompactLayout(context, apiVersion, results, syncHelper);
    }

    public static void sendMetadataRequestForIndividualCompactLayout(final Context context, final String apiVersion, List<RecordTypeDetails> recordTypesList, final SyncHelper syncHelper) throws Exception {
        final String requestTag = "individual compact layout";

        final SubrequestFactory<RecordTypeDetails> recordTypeSubrequestFactory = new SubrequestFactory<RecordTypeDetails>() {
            @Override
            public Subrequest subrequestFromObject(RecordTypeDetails recordTypeDetails) {
                return syncHelper.getCompactLayoutSubrequest(apiVersion,
                        recordTypeDetails.getObjectType(), recordTypeDetails.getRecordTypeId());
            }
        };

        final ObjectResponseHandler<RecordTypeDetails> recordTypeResponseHandler = new ObjectResponseHandler<RecordTypeDetails>() {
            @Override
            public void responseForObject(RecordTypeDetails recordTypeDetails, String stringResponse) {
                MetaDataProvider.saveMetadataForIndividualCompactLayout(context,
                        recordTypeDetails.getObjectType(),
                        recordTypeDetails.getRecordTypeId(), stringResponse);
            }
        };

        sendPagedRequest(context, syncHelper, requestTag, recordTypesList, recordTypeSubrequestFactory, recordTypeResponseHandler);
    }

    interface SubrequestFactory<T> {
        Subrequest subrequestFromObject(T object);
    }

    interface ObjectResponseHandler<T> {
        void responseForObject(T object, String response);
    }

    public static class ObjectResponseException extends Exception {
        private final Object parsedObject;

        ObjectResponseException(Object parsedObject, Throwable cause) {
            super(cause);
            this.parsedObject = parsedObject;
        }

        public Object getParsedObject() {
            return parsedObject;
        }
    }

    private static RestResponse getResponseOrThrow(Context context, SyncHelper syncHelper, String tag, List<Subrequest> subrequests) throws Exception {
        try {
            RestResponse response = syncHelper.sendSubrequests(subrequests, true /* haltOnError */, null);

            if (!response.isSuccess()) {
                throw ServerErrorException.createFrom(response, context);
            }
            return response;
        } catch (Exception e) {
            throw new Exception("Error on send " + tag + " requests: " + e.getMessage(), e);
        }
    }

    private static <T> void parseResponse(Context context, RestResponse response, List<T> processedObjects, ObjectResponseHandler<T> objectResponseHandler) throws IOException, JSONException, ObjectResponseException {
        BatchResponse batchResponse = BatchResponse.createFrom(response);
        List<Subresponse> responseList = batchResponse.getElements();

        T currentObjectType;
        for (int i = 0; i < responseList.size(); i++) {
            currentObjectType = processedObjects.get(i);
            Subresponse subresponse = responseList.get(i);

            if (!subresponse.isSuccess()) {
                throw new ObjectResponseException(currentObjectType, ServerErrorException.createFrom(subresponse, context));
            }

            String stringResponse = subresponse.asString();

            objectResponseHandler.responseForObject(currentObjectType, stringResponse);
        }
    }

    /**
     * Sends layout metadata request for given objects paging requests if necessary
     *
     * @param requestTag            Log tag for this request
     * @param objects               List of objects based on which requests should be performed
     * @param subrequestFactory     Factory for generating new subrequests based on objects supplied
     * @param objectResponseHandler Handler used for each subrequest response, defining
     *                              what should be done for each object supplied given its response
     *                              from the server
     * @param <T>                   type of objects
     * @throws Exception
     */
    private static <T> void sendPagedRequest(final Context context, SyncHelper syncHelper, String requestTag,
                                             List<T> objects, SubrequestFactory<T> subrequestFactory,
                                             ObjectResponseHandler<T> objectResponseHandler) throws Exception {
        int pagesCount = MathUtils.ceilInt(objects.size() / (double) MAX_SUBREQUESTS);
        int pageSize = MathUtils.ceilInt(objects.size() / (double) pagesCount);

        for (int pageNum = 0; pageNum < pagesCount; pageNum++) {
            int startIndex = pageNum * pageSize;
            int endIndex = Math.min(startIndex + pageSize, objects.size());
            List<T> sublistOfTypes = objects.subList(startIndex, endIndex);

            List<Subrequest> subrequests = new ArrayList<Subrequest>();
            for (T obj : sublistOfTypes) {
                Subrequest subrequest = subrequestFactory.subrequestFromObject(obj);
                subrequests.add(subrequest);
            }

            final RestResponse response = getResponseOrThrow(context, syncHelper, requestTag, subrequests);

            try {
                parseResponse(context, response, sublistOfTypes, objectResponseHandler);
            } catch (ObjectResponseException e) {
                throw new Exception("Error on object " + e.getParsedObject() + " on send " + requestTag + " requests: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new Exception("Error on " + requestTag + " : " + e.getMessage(), e);
            }
        }
    }

    private static class RecordTypeDetails {
        String objectType;
        String recordTypeId;

        public RecordTypeDetails(String objectType, String recordTypeId) {
            this.objectType = objectType;
            this.recordTypeId = recordTypeId;
        }

        public String getObjectType() {
            return objectType;
        }

        public String getRecordTypeId() {
            return recordTypeId;
        }
    }
}
