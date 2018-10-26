package com.salesforce.dsa.data.model;

import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONObject;

import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.BODY;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.BODY_LENGTH;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.CONTENT_TYPE;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.DESCRIPTION;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.IS_PRIVATE;
import static com.salesforce.dsa.utils.DSAConstants.AttachmentFields.PARENT_ID;

public class Attachment extends SFBaseObject {

    private static final long serialVersionUID = -3942039285767043140L;

    public Attachment() {
        super(DSAObjects.ATTACHMENT);
    }

    public Attachment(JSONObject json) {
        super(DSAObjects.ATTACHMENT, json);
    }

    public String getBody() {
        return getStringValueForKey(BODY);
    }

    public String getBodyLength() {
        return getStringValueForKey(BODY_LENGTH);
    }

    public String getContentType() {
        return getStringValueForKey(CONTENT_TYPE);
    }

    public String getDescription() {
        return getStringValueForKey(DESCRIPTION);
    }

    public boolean getIsPrivate() {
        return getBooleanValueForKey(IS_PRIVATE);
    }

    public String getParentId() {
        return getStringValueForKey(PARENT_ID);
    }
}
