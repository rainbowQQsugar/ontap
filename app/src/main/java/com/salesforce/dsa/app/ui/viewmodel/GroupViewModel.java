package com.salesforce.dsa.app.ui.viewmodel;

import com.salesforce.dsa.app.ui.LibraryHeader;
import com.salesforce.dsa.data.model.Playlist_Content_Junction__c;
import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 AKTA a SalesForce Company
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class GroupViewModel {

    private SFBaseObject header;
    private List<Playlist_Content_Junction__c> content;

    public GroupViewModel(SFBaseObject header, List<Playlist_Content_Junction__c> content) {
        this.header = header;
        this.content = content;
    }

    public int size() {
        return content == null ? 1 : content.size() + 1;
    }

    public void addContent(int location, Playlist_Content_Junction__c contentJunction) {
        if (!getContent().contains(contentJunction)) {
            getContent().add(location, contentJunction);
        }
    }

    public void removeContent(int location) {
        getContent().remove(location);
    }

    public int indexOf(Playlist_Content_Junction__c contentJunction) {
        return getContent().indexOf(contentJunction);
    }

    public List<Playlist_Content_Junction__c> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return content;
    }

    public SFBaseObject getHeader() {
        return header;
    }

    public boolean isLibrary() {
        return header instanceof LibraryHeader;
    }
}
