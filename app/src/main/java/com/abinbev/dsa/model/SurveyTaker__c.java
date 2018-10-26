package com.abinbev.dsa.model;

import android.util.Log;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyTakerFields;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.utils.DSAConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SurveyTaker__c extends TranslatableSFBaseObject {

    public static final String TAG = SurveyTaker__c.class.getName();

    private Account account;

    public SurveyTaker__c(JSONObject json) {
        super(AbInBevObjects.SURVEY_TAKER, json);
    }

    public SurveyTaker__c() {
        super(AbInBevObjects.SURVEY_TAKER);
    }

    public int getTotalScore() {
        double totalScore = getDoubleValueForKey(SurveyTakerFields.TOTAL_SCORE);
        if (Double.isNaN(totalScore)) return 0;
        else return (int)Math.round(totalScore);
    }

    public String getName(){
        String name = getStringValueForKey(SyncEngineConstants.StdFields.NAME);
        if (name == null) {
            return "-";
        }
        return name;
    }

    public String getAccountId() {
        return getStringValueForKey(SurveyTakerFields.ACCOUNT_ID);
    }

    public String getUserName(){
        String userId = getStringValueForKey(SurveyTakerFields.USER__C);
        if (userId != null) {
            User user = User.getUserByUserId(userId);
            if (user != null) return user.getName();
        }
        return "-";
    }

    public Account getAccount() {
        if (account == null) {
            String accountId = getStringValueForKey(SurveyTakerFields.ACCOUNT_ID);
            if (accountId != null) {
                account = Account.getById(accountId);
            }
        }

        return account;
    }

    public void invalidateAccount() {
        account = null;
    }

    public String getSurveyName(){
        return getReferencedValue(AbInBevObjects.SURVEY_C, SurveyTakerFields.SURVEY__C);
//        JSONObject jsonObject = getJsonObject(AbInBevConstants.SurveyTakerFields.SURVEY);
//        return jsonObject == null ? null : jsonObject.optString(SyncEngineConstants.StdFields.NAME, null);
    }

    public ContentVersion getAssociatedContent() {
        Survey__c surveyObject = getSurvey__c();
        if (surveyObject == null) {
            return null;
        } else {
            String contentDocumentId = surveyObject.getHTMLBundleKey();
            if (contentDocumentId == null) {
                return null;
            } else {
                ContentVersion contentVersion = ContentVersion.getContentVersionForContentId(contentDocumentId);
                if (contentVersion == null) {
                    return null;
                } else {
                    return contentVersion;
                }
            }
        }
    }

    public Survey__c getSurvey__c() {
        String surveyId = getStringValueForKey(SurveyTakerFields.SURVEY__C);
        if (surveyId == null) {
            return null;
        } else if (surveyId.equals("")) {
            return null;
        } else {
            Survey__c surveyObject = Survey__c.getById(surveyId);
            if (surveyObject == null) {
                return null;
            } else {
                return surveyObject;
            }
        }
    }

    public String getState(){
        return getStringValueForKey(SurveyTakerFields.STATUS);
    }

    public String getTranslatedState(){
        return getTranslatedStringValueForKey(SurveyTakerFields.STATUS);
    }

    public String getDueDate(){
        return getStringValueForKey(SurveyTakerFields.DUE_DATE);
    }

    public static int getLatestItosSurveyScore(String accountId) {
        int score = -1;

        String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s' AND {%s:%s} = '%s' ORDER BY {%s:%s} DESC LIMIT 1",
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.ACCOUNT_ID, accountId,
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_COMPLETE,
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.TYPE, "POCE",
                AbInBevObjects.SURVEY_TAKER, AbInBevConstants.ID);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        if (recordsArray.length() > 0) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(0).getJSONObject(0);
                score = new SurveyTaker__c(jsonObject).getTotalScore();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return score;
    }

    public static int getOpenSurveysCountByAccountId(String accountId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_OPEN,
                    AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.ACCOUNT_ID, accountId);

            String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting open Surveys by Account ID: " + accountId, e);
        }

        return count;
    }

    public static int getOpenSurveysCountByUserId(String userId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_OPEN,
                    AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.OWNER, userId);

            String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting open Surveys by User ID: " + userId, e);
        }

        return count;
    }

    public static List<SurveyTaker__c> getSurveysByAccountId(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC",
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.ACCOUNT_ID, accountId,
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.SURVEY__C
        );

        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
        List<SurveyTaker__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new SurveyTaker__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Negotiations by Account ID: " + accountId, e);
        }
        return results;
    }

    public static List<SurveyTaker__c> getSurveysByUserId(String userId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC",
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.OWNER, userId,
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.SURVEY__C
        );

        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
        List<SurveyTaker__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new SurveyTaker__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Negotiations by User ID: " + userId, e);
        }
        return results;
    }

    public static SurveyTaker__c getById(String surveyTakerId) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.SURVEY_TAKER, SyncEngineConstants.StdFields.ID, surveyTakerId);
        return new SurveyTaker__c(jsonObject);
    }

    public static List<SurveyTaker__c> getQuizzes() {
        List<SurveyTaker__c> results = new ArrayList<>();

        // this fetches all the temporary Ids
        String smartSqlFilter = String.format("{%s:%s} LIKE '%s%%' ORDER BY {%s:%s} DESC",
                AbInBevObjects.SURVEY_TAKER, AbInBevConstants.ID, "SUR",
                AbInBevObjects.SURVEY_TAKER, SyncEngineConstants.StdFields.NAME
        );

        String query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_TAKER, smartSqlFilter);

        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);

        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                SurveyTaker__c surveyTaker = new SurveyTaker__c(jsonObject);
                Survey__c survey = surveyTaker.getSurvey__c();
                if (survey != null && "Quiz".equals(survey.getRecordTypeName())) {
                    results.add(new SurveyTaker__c(jsonObject));
                } else {
                    Log.e("Babu", "surveyTaker: " + surveyTaker.toJson().toString());
                    Log.e("Babu", "survey:" + (survey == null ? "null" : survey.toJson().toString()));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active quizzes", e);
        }


        // this is the real deal
        smartSqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} DESC",
                AbInBevObjects.SURVEY_TAKER, SurveyTakerFields.TYPE, "Quiz",
                AbInBevObjects.SURVEY_TAKER, SyncEngineConstants.StdFields.NAME
        );

        query = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_TAKER, smartSqlFilter);
        recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);

        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new SurveyTaker__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active quizzes", e);
        }

        return results;
    }

    public static String createNewSurveyTakerRecord(String accountId, String surveyId) {

        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
            json.put(SurveyTakerFields.ACCOUNT_ID, accountId);
            json.put(SurveyTakerFields.SURVEY__C, surveyId);
            json.put(SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_OPEN);
            //TODO: Add lattitude and longitude

        } catch (JSONException e) {
            Log.e(TAG, "Error creating new survey taker record", e);
            return null;
        }
        return dataManager.createRecord(AbInBevObjects.SURVEY_TAKER, json);
    }


    public static String createNewQuizTakerRecord(String surveyId, String userId) {

        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
            json.put(SurveyTakerFields.SURVEY__C, surveyId);
            json.put(SurveyTakerFields.USER__C, userId);
            // json.put(SyncEngineConstants.StdFields.OWNERID, userId);
            json.put(SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_OPEN);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating new survey taker record", e);
            return null;
        }
        return dataManager.createRecord(AbInBevObjects.SURVEY_TAKER, json);
    }
}
