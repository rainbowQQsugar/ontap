package com.salesforce.androidsyncengine.datamanager.synchelper;

import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Created by Jakub Stefanowski on 18.05.2017.
 */
public interface CompositeResponseHandler {
    /**
     * If there is a continuation for current subrequest (i.e. next page containing more items) you
     * can return new Subrequest which will be added to current Subrequest queue.
     */
    CompositeSubrequest handleResponse(RestRequest originalRequest, RestResponse originalResponse, CompositeSubrequest subrequest, CompositeSubresponse subresponse) throws SyncException;
}
