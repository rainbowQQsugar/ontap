package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.ResultScaleFields;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

/**
 * Created by lukaszwalukiewicz on 07.01.2016.
 */
public class Resultado_por_escala__c extends SFBaseObject {
    public Resultado_por_escala__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.RESULT_SCALE, json);
    }

    public String getResult() {
        return getStringValueForKey(ResultScaleFields.RESULT);
    }
}
