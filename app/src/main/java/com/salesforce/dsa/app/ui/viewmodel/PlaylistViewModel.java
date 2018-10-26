package com.salesforce.dsa.app.ui.viewmodel;

import com.salesforce.dsa.data.model.DSA_Playlist__c;
import com.salesforce.dsa.data.model.Playlist_Content_Junction__c;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usanaga on 9/28/15.
 */
public class PlaylistViewModel {

    private List<Playlist_Content_Junction__c> contentVersions;
    private DSA_Playlist__c playlist;

    public PlaylistViewModel(DSA_Playlist__c playlist) {
        this.playlist = playlist;
    }

    public DSA_Playlist__c getPlaylist() {
        return this.playlist;
    }

    public List<Playlist_Content_Junction__c> getContent() {
        if (contentVersions == null) {
            contentVersions = new ArrayList<>();
        }
        return contentVersions;
    }
}
