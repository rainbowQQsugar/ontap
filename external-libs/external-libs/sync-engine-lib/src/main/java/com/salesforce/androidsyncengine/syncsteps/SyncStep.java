package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Single synchronization step. Requires {@link SyncControls} to get access to sync related data.
 *
 * Created by Jakub Stefanowski on 13.09.2017.
 */
public interface SyncStep {
    void execute(SyncControls syncControls) throws SyncException;
}
