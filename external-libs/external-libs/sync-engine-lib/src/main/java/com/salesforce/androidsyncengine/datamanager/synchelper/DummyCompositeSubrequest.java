package com.salesforce.androidsyncengine.datamanager.synchelper;

import com.salesforce.androidsdk.rest.RestRequest;

import java.util.Map;

/**
 * This class can be used when you don't want to send anything to server but you want to have
 * CompositeResponseHandler called inside SyncHelper.
 *
 * Created by Jakub Stefanowski on 24.05.2017.
 */
public class DummyCompositeSubrequest extends CompositeSubrequest {

    public DummyCompositeSubrequest() {
        super(null, null, null);
    }

    public void setMethod(RestRequest.RestMethod method) {
        throw new UnsupportedOperationException();
    }

    public void setUrl(String url) {
        throw new UnsupportedOperationException();
    }

    public void setBody(Object body) {
        throw new UnsupportedOperationException();
    }

    public void setHeaders(Map<String, String> headers) {
        throw new UnsupportedOperationException();
    }

    public void setReferenceId(String referenceId) {
        throw new UnsupportedOperationException();
    }
}
