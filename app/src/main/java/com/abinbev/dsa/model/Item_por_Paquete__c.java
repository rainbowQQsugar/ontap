package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

/**
 * Created by wandersonblough on 12/9/15.
 */
public class Item_por_Paquete__c extends SFBaseObject {

    protected Item_por_Paquete__c() {
        super(AbInBevObjects.PACKAGE_ITEM);
    }

    public Item_por_Paquete__c(JSONObject jsonObject) {
        super(AbInBevObjects.PACKAGE_ITEM, jsonObject);
    }

}
