package com.abinbev.dsa.model;

import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class Survey_Question_Dependencies__c extends SFBaseObject {
    public static final String TAG = Survey_Question_Dependencies__c.class.getName();

    public Survey_Question_Dependencies__c() {
        super("Survey_Question_Dependencies__c");
    }

    public Survey_Question_Dependencies__c(JSONObject jsonObject) {
        super("Survey_Question_Dependencies__c", jsonObject);
    }

}
