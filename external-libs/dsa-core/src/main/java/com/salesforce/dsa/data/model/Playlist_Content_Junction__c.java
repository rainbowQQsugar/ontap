package com.salesforce.dsa.data.model;

import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONObject;

/**
 * @author Jason Harris (jason@akta.com)
 */
public class Playlist_Content_Junction__c extends SFBaseObject {

    private JSONObject delegate;
    private ContentVersion contentVersion;

    public Playlist_Content_Junction__c(JSONObject json) {
        super(DSAObjects.PLAYLIST_CONTENT_JUNCTION, json);
        delegate = json;
    }

    public Playlist_Content_Junction__c(JSONObject json, ContentVersion contentVersion) {
        super(DSAObjects.PLAYLIST_CONTENT_JUNCTION, json);
        delegate = json;
        this.contentVersion = contentVersion;
    }

    public int getOrder() {
        return delegate.optInt(DSAConstants.PlaylistContentJunctionFields.ORDER, -1);
    }

    public ContentVersion getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(ContentVersion contentVersion) {
        this.contentVersion = contentVersion;
    }
}
