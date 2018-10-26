package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class CN_DSA_Azure_File__c extends SFBaseObject {

    public CN_DSA_Azure_File__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File__c, json);
    }

    protected CN_DSA_Azure_File__c() {
        super(AbInBevConstants.AbInBevObjects.CN_DSA_Azure_File__c);
    }

    //if need setFields ,pls add it self
    public String getName() {
        return getStringValueForKey(AbInBevConstants.ProfileFields.NAME);
    }

    public String getId() {
        return getStringValueForKey(AbInBevConstants.ID);
    }

    public String getDsaFileTpye() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_File_Type__c);
    }

    public String getParentCategoryId() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_CategoryID__c);
    }

    public String getFileExpireDate() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.File_Expire_Date__c);
    }

    public String getDsaFileName() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_File_Name__c);
    }

    public String getFileSize() {
        return getStringValueForKey(AbInBevConstants.CNDsaAzueFields.CN_File_Size__c);
    }

}
