package com.salesforce.androidsyncengine.syncsteps.objectprovider;

import com.salesforce.androidsyncengine.syncsteps.SyncControls;

import java.util.Collections;
import java.util.List;

/**
 * Created by Jakub Stefanowski on 09.10.2017.
 */

public class EmptyObjectProvider implements ObjectProvider {

    public static ObjectProvider EMPTY_PROVIDER = new EmptyObjectProvider();

    @Override
    public List<String> getObjects(SyncControls syncControls) {
        return Collections.emptyList();
    }
}
