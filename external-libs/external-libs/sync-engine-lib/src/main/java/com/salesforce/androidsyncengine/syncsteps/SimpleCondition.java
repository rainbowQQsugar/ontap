package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Condition that depends on the value provided in constructor.
 *
 * Created by Jakub Stefanowski on 11.10.2017.
 */
public class SimpleCondition implements ConditionalSyncStep.Condition {

    private final boolean accept;

    public SimpleCondition(boolean accept) {
        this.accept = accept;
    }

    @Override
    public boolean accept(SyncControls syncControls) throws SyncException {
        return accept;
    }
}
