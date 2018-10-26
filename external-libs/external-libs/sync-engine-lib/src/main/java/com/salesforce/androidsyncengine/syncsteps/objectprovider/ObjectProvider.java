package com.salesforce.androidsyncengine.syncsteps.objectprovider;

import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.syncsteps.SyncControls;

import java.util.List;

/**
 * Provides a list of objects.
 *
 * Created by Jakub Stefanowski on 11.10.2017.
 */
public interface ObjectProvider {
    List<String> getObjects(SyncControls syncControls) throws SyncException;
}
