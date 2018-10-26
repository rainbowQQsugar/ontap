package com.salesforce.androidsyncengine.syncsteps;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

/**
 * Created by Jakub Stefanowski on 09.10.2017.
 */

public class EmptySyncStep implements SyncStep {

    public static final EmptySyncStep INSTANCE = new EmptySyncStep();

    @Override
    public void execute(SyncControls syncControls) throws SyncException { }
}
