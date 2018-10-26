package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.Cat_Content_Junction__c;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CN_SKU_TC_Relationship__c extends TranslatableSFBaseObject {
    public static final String TAG = CN_SKU_TC_Relationship__c.class.getName();

    protected CN_SKU_TC_Relationship__c(String objectName) {
        super(AbInBevConstants.AbInBevObjects.CN_SKU_TC_Relationship__c);
    }

    public CN_SKU_TC_Relationship__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_SKU_TC_Relationship__c, json);
    }

    public String getChannel() {
        return getStringValueForKey(AbInBevConstants.SkuTcRelationshipFields.CHANNEL);
    }

    public String getTerritory() {
        return getStringValueForKey(AbInBevConstants.SkuTcRelationshipFields.TERRITORY);
    }

    public String getProduct() {
        return getStringValueForKey(AbInBevConstants.SkuTcRelationshipFields.PRODUCT);
    }
}
