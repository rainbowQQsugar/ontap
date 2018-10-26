package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ParametroFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class Parametro__c extends SFBaseObject {

    private static final String TAG = Parametro__c.class.getSimpleName();

    protected Parametro__c() {
        super(AbInBevObjects.PARAMETRO);
    }

    public Parametro__c(JSONObject jsonObject) {
        super(AbInBevObjects.PARAMETRO, jsonObject);
    }

    public String getCodigo() {
        return getStringValueForKey(ParametroFields.CODIGO);
    }

    public String getColor() {
        return getStringValueForKey(ParametroFields.COLOR);
    }

    public static Parametro__c getById(String paremetroId) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.PARAMETRO, SyncEngineConstants.StdFields.ID, paremetroId);
        if (jsonObject != null) {
            return new Parametro__c(jsonObject);
        }
        return null;
    }

}
