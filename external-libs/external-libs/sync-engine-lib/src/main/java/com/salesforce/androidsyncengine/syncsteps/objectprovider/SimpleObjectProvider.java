package com.salesforce.androidsyncengine.syncsteps.objectprovider;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;
import com.salesforce.androidsyncengine.datamanager.exceptions.SyncException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Object provider that wraps array or collection.
 *
 * Created by Jakub Stefanowski on 09.10.2017.
 */
public class SimpleObjectProvider implements ObjectProvider {

    private final ArrayList<String> objectNames = new ArrayList<>();

    public SimpleObjectProvider(Collection<String> objectNames) {
        this.objectNames.addAll(objectNames);
    }

    public SimpleObjectProvider(String... objectNames) {
        Collections.addAll(this.objectNames, objectNames);
    }

    @Override
    public List<String> getObjects(SyncControls syncControls) throws SyncException {
        return objectNames;
    }
}
