package com.salesforce.androidsyncengine.datamanager;

import android.text.TextUtils;

/**
 * Created by Jakub Stefanowski on 06.03.2017.
 */

abstract class AbstractTempIdContainer {

    interface Visitor {
        void visit(String localId, String salesforceId);
    }

    public abstract void insertSalesforceId(String tempId, String salesforceId);

    public abstract String getSalesforceId(String tempId);

    public abstract int deleteOlderThan(long date);

    public abstract int deleteAll();

    protected abstract void iterate(Visitor visitor);

    public String updateWithServerId(String jsonString) {
        if (TextUtils.isEmpty(jsonString)) return jsonString;

        UpdateIdVisitor visitor = new UpdateIdVisitor(jsonString);
        iterate(visitor);

        return visitor.getJsonString();
    }

    private static class UpdateIdVisitor implements Visitor {

        private String jsonString;

        public UpdateIdVisitor(String jsonString) {
            this.jsonString = jsonString;
        }

        @Override
        public void visit(String localId, String salesforceId) {
            jsonString = jsonString.replace(localId, salesforceId);
        }

        public String getJsonString() {
            return jsonString;
        }
    }
}
