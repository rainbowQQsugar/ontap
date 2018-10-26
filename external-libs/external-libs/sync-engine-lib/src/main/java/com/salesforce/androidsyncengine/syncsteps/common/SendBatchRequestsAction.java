package com.salesforce.androidsyncengine.syncsteps.common;

import com.salesforce.androidsyncengine.datamanager.SyncHelper;
import com.salesforce.androidsyncengine.datamanager.exceptions.RuntimeSyncException;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.datamanager.synchelper.Subrequest;

import java.util.List;

import rx.functions.Action1;

/**
 * Action that sends provided list of subrequests.
 *
 * Created by Jakub Stefanowski on 11.10.2017.
 */
public class SendBatchRequestsAction implements Action1<List<Subrequest>> {

    private final SyncHelper syncHelper;

    private final boolean haltOnError;

    public SendBatchRequestsAction(SyncHelper syncHelper, boolean haltOnError) {
        this.syncHelper = syncHelper;
        this.haltOnError = haltOnError;
    }

    @Override
    public void call(List<Subrequest> subrequests) {
        try {
            syncHelper.sendBatchRequests(subrequests, haltOnError);
        } catch (SyncException e) {
            throw RuntimeSyncException.wrap(e);
        }
    }
}
