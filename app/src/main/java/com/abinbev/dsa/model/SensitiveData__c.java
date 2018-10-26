package com.abinbev.dsa.model;

import android.text.TextUtils;

import com.abinbev.dsa.utils.AbInBevConstants.SensitiveDataFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects.SENSITIVE_DATA;

/**
 * Created by Jakub Stefanowski on 03.06.2017.
 */

public class SensitiveData__c extends SFBaseObject {

    private static final String SEMICOLON = ";";


    public SensitiveData__c(JSONObject json) {
        super(SENSITIVE_DATA, json);
    }

    public SensitiveData__c() {
        super(SENSITIVE_DATA);
    }

    public String getObjectName() {
        return getStringValueForKey(SensitiveDataFields.OBJECT_NAME);
    }

    public Set<String> getFields() {
        Set<String> result = new HashSet<>();

        String fields = getStringValueForKey(SensitiveDataFields.FIELD_NAMES);
        if (!TextUtils.isEmpty(fields)) {
            String[] fieldsArray = fields.split(SEMICOLON);
            Collections.addAll(result, fieldsArray);
        }

        return result;
    }

    public static SensitiveData__c getByObjectName(String objectName) {
        DataManager dataManager = DataManagerFactory.getDataManager();

        JSONObject jsonObject = dataManager.exactQuery(SENSITIVE_DATA, SensitiveDataFields.OBJECT_NAME, objectName);
        if (jsonObject != null) {
            return new SensitiveData__c(jsonObject);
        }
        else {
            return null;
        }
    }

    public static Set<String> getSensitiveFields(String objectName) {
        SensitiveData__c data = getByObjectName(objectName);
        return data != null ? data.getFields() : Collections.<String>emptySet();
    }

    public static List<SensitiveData__c> getAll() {
        return DataManagerUtils.fetchAllObjects(DataManagerFactory.getDataManager(), SensitiveData__c.class);
    }
}
