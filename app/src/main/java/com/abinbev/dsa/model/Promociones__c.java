package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PromotionFields;
import com.salesforce.dsa.data.model.SFBaseObject;
import org.json.JSONObject;

public class Promociones__c extends SFBaseObject {

    public static final String TAG = Promociones__c.class.getName();

    public Promociones__c(JSONObject json) {
        super(AbInBevObjects.PROMOTIONS, json);
    }

    public Promociones__c() {
        super(AbInBevObjects.PROMOTIONS);
    }

    public String getType() {
        return getStringValueForKey(PromotionFields.TYPE);
    }

    public String getDescription() {
        return getStringValueForKey(PromotionFields.DESCRIPTION);
    }

    public String getStartDate() {
        return getStringValueForKey(PromotionFields.START_DATE);
    }

    public String getEndDate() {
        return getStringValueForKey(PromotionFields.END_DATE);
    }

    public Boolean getObligatory() {
        return getBooleanValueForKey(PromotionFields.OBLIGATORY);
    }

}
