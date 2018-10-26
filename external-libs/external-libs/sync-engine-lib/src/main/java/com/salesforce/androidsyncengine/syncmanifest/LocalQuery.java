package com.salesforce.androidsyncengine.syncmanifest;

import java.util.List;

/**
 * Created by Jakub Stefanowski on 30.03.2017.
 */

public class LocalQuery {

    private String query;

    private boolean excludeClientIds;

    private List<String> args;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isExcludeClientIds() {
        return excludeClientIds;
    }

    public void setExcludeClientIds(boolean excludeClientIds) {
        this.excludeClientIds = excludeClientIds;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
