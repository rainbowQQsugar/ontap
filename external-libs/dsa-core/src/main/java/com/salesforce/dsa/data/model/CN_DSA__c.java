package com.salesforce.dsa.data.model;


import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONObject;

public class CN_DSA__c extends SFBaseObject {
    public CN_DSA__c(JSONObject json) {
        super(DSAConstants.CNDSAFolderFields.CN_DSA__c, json);
    }

    public String getOwnerID() {
        return getStringValueForKey(DSAConstants.CNDSAFields.OwnerID);
    }

    public String getCN_IsActive__c() {
        return getStringValueForKey(DSAConstants.CNDSAFields.IsActive__c);
    }
}
