package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyQuestionFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.List;

public class Survey_Question__c extends SFBaseObject {
    public static final String TAG = Survey_Question__c.class.getName();

    public Survey_Question__c() {
        super(AbInBevObjects.SURVEY_Question);
    }

    public Survey_Question__c(JSONObject jsonObject) {
        super(AbInBevObjects.SURVEY_Question, jsonObject);
    }

    public boolean shouldAllowPickingFromLibrary() {
        return getBooleanValueForKey(SurveyQuestionFields.PHOTO_FROM_LIBRARY);
    }

    public String getQuestion() {
        return getStringValueForKey(SurveyQuestionFields.QUESTION);
    }

    public String getSurveyId() {
        return getStringValueForKey(SurveyQuestionFields.SURVEY);
    }

    public static Survey_Question__c getById(String questionId) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, questionId, Survey_Question__c.class);
    }

    public static List<Survey_Question__c> getAll() {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchAllObjects(dm, Survey_Question__c.class);
    }
}
