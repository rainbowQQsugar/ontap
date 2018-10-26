package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyQuestionResponseFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants.Formats;

import org.json.JSONArray;
import org.json.JSONObject;

public class SurveyQuestionResponse__c extends SFBaseObject {

    public static final String TAG = SurveyQuestionResponse__c.class.getName();

    public SurveyQuestionResponse__c(JSONObject json) {
        super(AbInBevObjects.SURVEY_Question_Response, json);
    }

    public SurveyQuestionResponse__c() {
        super(AbInBevObjects.SURVEY_Question_Response);
    }

    public String getSurveyQuestionId() {
        return getStringValueForKey(SurveyQuestionResponseFields.SURVEY_QUESTION);
    }

    public String getSurveyTakerId() {
        return getStringValueForKey(SurveyQuestionResponseFields.SURVEY_TAKER);
    }

    public static String getSurveyQuestionResponseId(String surveyTakerId, String surveyQuestionId) {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.SURVEY_Question_Response, SurveyQuestionResponseFields.SURVEY_TAKER, surveyTakerId,
                    AbInBevObjects.SURVEY_Question_Response, SurveyQuestionResponseFields.SURVEY_QUESTION, surveyQuestionId);


            String query = String.format(Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_Question_Response, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
            try {
                for (int i = 0; i < recordsArray.length(); i++) {
                    JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                    SurveyQuestionResponse__c surveyQuestionResponse__c = new SurveyQuestionResponse__c(jsonObject);
                    return surveyQuestionResponse__c.getId();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in getting QuestionResponseId");
            }
            return null;
    }

    public static SurveyQuestionResponse__c getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, id, SurveyQuestionResponse__c.class);
    }
}
