package com.salesforce.dsa.data.model;

import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.CATEGORY__C;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.CONTENTID__C;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.EXTERNAL_ID__C;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.IS_DELETED;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.LAST_ACTIVITY_DATE;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.LAST_REFERENCED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.CatContentJunctionFields.LAST_VIEWED_DATE;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;
import org.json.JSONArray;
import org.json.JSONObject;

public class Cat_Content_Junction__c extends SFBaseObject {

    private static final long serialVersionUID = 2001073955497714058L;

    public Cat_Content_Junction__c() {
        super(DSAObjects.CAT_CONTENT_JUNCTION);
    }

    public Cat_Content_Junction__c(JSONObject json) {
        super(DSAObjects.CAT_CONTENT_JUNCTION, json);
    }

    public boolean isDeleted() {
        return getBooleanValueForKey(IS_DELETED);
    }

    public String getLastActivityDate() {
        return getStringValueForKey(LAST_ACTIVITY_DATE);
    }

    public String getLastReferencedDate() {
        return getStringValueForKey(LAST_REFERENCED_DATE);
    }

    public String getLastViewedDate() {
        return getStringValueForKey(LAST_VIEWED_DATE);
    }

    public String getCategory() {
        return getStringValueForKey(CATEGORY__C);
    }

    public String getContentId() {
        return getStringValueForKey(CONTENTID__C);
    }

    public String getExternalId() {
        return getStringValueForKey(EXTERNAL_ID__C);
    }

    public static Cat_Content_Junction__c getCatContentJunctionForContentDocumentId(String contentDocumentId) {

        String filter = String.format("{Cat_Content_Junction__c:%s} = '%s'",
                DSAConstants.CatContentJunctionFields.CONTENTID__C,
                contentDocumentId);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CAT_CONTENT_JUNCTION, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
            Cat_Content_Junction__c catJunction = new Cat_Content_Junction__c(jsonObject);
            return catJunction;
        } catch (Exception e) {
            return null;
        }
    }
}
