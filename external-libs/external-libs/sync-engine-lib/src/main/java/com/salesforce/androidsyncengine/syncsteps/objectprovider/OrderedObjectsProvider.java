package com.salesforce.androidsyncengine.syncsteps.objectprovider;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;
import com.salesforce.androidsyncengine.utils.PreferenceUtils;

import java.util.List;

/**
 * Provides ordered list of object names.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
public class OrderedObjectsProvider implements ObjectProvider {
    @Override
    public List<String> getObjects(SyncControls syncControls) throws SyncException {
        return PreferenceUtils.getSortedObjects(syncControls.getContext());
    }
}
