package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.ARCHIVED_BY_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.ARCHIVED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.IS_ARCHIVED;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.IS_DELETED;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.LAST_REFERENCED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.LAST_VIEWED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.LATEST_PUBLISHED_VERSION_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.PARENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.PUBLISH_STATUS;
import static com.salesforce.dsa.utils.DSAConstants.ContentDocumentFields.TITLE;

public class ContentDocument extends SFBaseObject {

    private static final long serialVersionUID = -4020939132348433061L;

    public ContentDocument() {
        super(AbInBevObjects.CONTENT_DOCUMENT);
    }

    public ContentDocument(JSONObject json) {
        super(AbInBevObjects.CONTENT_DOCUMENT, json);
    }

    public String getArchivedById() {
        return getStringValueForKey(ARCHIVED_BY_ID);
    }

    public String getArchivedDate() {
        return getStringValueForKey(ARCHIVED_DATE);
    }

    public boolean isDeleted() {
        return getBooleanValueForKey(IS_DELETED);
    }

    public boolean isArchived() {
        return getBooleanValueForKey(IS_ARCHIVED);
    }

    public String getLastReferencedDate() {
        return getStringValueForKey(LAST_REFERENCED_DATE);
    }

    public String getLastViewedDate() {
        return getStringValueForKey(LAST_VIEWED_DATE);
    }

    public String getLatestPublishedVersionId() {
        return getStringValueForKey(LATEST_PUBLISHED_VERSION_ID);
    }

    public String getParentId() {
        return getStringValueForKey(PARENT_ID);
    }

    public String getTitle() {
        return getStringValueForKey(TITLE);
    }

    public String getPublishStatus() {
        return getStringValueForKey(PUBLISH_STATUS);
    }
}
