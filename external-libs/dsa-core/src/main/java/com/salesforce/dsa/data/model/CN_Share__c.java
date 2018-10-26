package com.salesforce.dsa.data.model;


import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONObject;

public class CN_Share__c extends SFBaseObject {
    public CN_Share__c(JSONObject json) {
        super(DSAConstants.CNShareFields.CN_Share__c, json);
    }


}
