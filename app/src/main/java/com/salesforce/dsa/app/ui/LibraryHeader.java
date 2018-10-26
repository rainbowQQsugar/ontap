package com.salesforce.dsa.app.ui;

import com.salesforce.dsa.data.model.DSA_Playlist__c;

import org.json.JSONObject;

/**
 * Created by wandersonblough on 10/14/15.
 */
public class LibraryHeader extends DSA_Playlist__c {

    public LibraryHeader(JSONObject json) {
        super(json);
    }
}
