package com.salesforce.dsa.data.model;

import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONObject;

/**
 * Created by usanaga on 9/28/15.
 */
public class DSA_Playlist__c extends SFBaseObject {

    public DSA_Playlist__c() {
        super(DSAObjects.DSA_PLAYLIST);
    }

    public DSA_Playlist__c(JSONObject json) {
        super(DSAObjects.DSA_PLAYLIST, json);
    }
}
