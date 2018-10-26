package com.abinbev.dsa.model;

import android.util.Log;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountType;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyFields;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyType;
import com.abinbev.dsa.utils.PicklistUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.ContentVersion;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Survey__c extends SFBaseObject {
    public static final String TAG = Survey__c.class.getName();

    public Survey__c() {
        super(AbInBevObjects.SURVEY_C);
    }

    public Survey__c(JSONObject jsonObject) {
        super(AbInBevObjects.SURVEY_C, jsonObject);
    }

    public boolean hasParent() {
        return !isNullValue(SurveyFields.PARENT);
    }

    public boolean isQuiz() {
        return SurveyType.QUIZ.equals(getRecordTypeName());
    }

    public String getStatus() {
        return getStringValueForKey(SurveyFields.STATUS);
    }

    public String getChannel() {
        return getStringValueForKey(SurveyFields.CHANNEL);
    }

    public boolean hasChannel() {
        return !isNullValue(SurveyFields.CHANNEL);
    }

    public String getCategory() {
        return getStringValueForKey(SurveyFields.CATEGORY);
    }

    public boolean hasCategory() {
        return !isNullValue(SurveyFields.CATEGORY);
    }

    public String getCityRegion() {
        return getStringValueForKey(SurveyFields.CITY_REGION);
    }

    public boolean hasCityRegion() {
        return !isNullValue(SurveyFields.CITY_REGION);
    }

    public String getHTMLBundleKey() {
        return getStringValueForKey(SurveyFields.HTML_BUNDLE);
    }

    public String getRecordTypeName() {
        JSONObject jsonObject = getJsonObject(SurveyFields.RECORD_TYPE);
        return jsonObject == null ? null : jsonObject.optString(AbInBevConstants.AccountFields.NAME, null);
    }

    public boolean isForAccount(Account account) {
        return !AccountType.COMPETITOR.equals(account.getType()) && channelMatch(account) && categoryMatch(account) && cityRegionMatch(account);
    }

    private boolean channelMatch(Account account) {
        return !hasChannel() || Objects.equals(account.getChannel(), getChannel());
    }

    private boolean categoryMatch(Account account) {
        return !hasCategory() || Objects.equals(account.getCategory(), getCategory());
    }

    private boolean cityRegionMatch(Account account) {
        return !hasCityRegion() || PicklistUtils.hasValueInMultipicklist(account.getCityRegion(), getCityRegion());
    }

    public static Survey__c getById(String surveyId) {
        String smartSqlFilter = String.format("{%s:%s} = '%s'", AbInBevObjects.SURVEY_C,
                "Id", surveyId);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_C, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        JSONArray jsonArray = recordsArray.optJSONArray(0);
        JSONObject jsonObject = jsonArray == null ? null : jsonArray.optJSONObject(0);

        return jsonObject == null ? null : new Survey__c(jsonObject);
    }

    public static List<Survey__c> getSurveyTypesForAccountId(String accountId) {
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllRecords(AbInBevObjects.SURVEY_C, "Name");
        // Log.v(TAG, "account survey filter: %s, %s, %s", accountCategory)

        ArrayList<Survey__c> surveyTypes = new ArrayList<>();
        if (recordsArray != null) {
            Account account = Account.getById(accountId);

            for (int i = 0; i < recordsArray.length(); i++) {
                try {
                    Survey__c survey = new Survey__c(recordsArray.getJSONObject(i));
                    if (!survey.hasParent() && "Desplegado".equals(survey.getStatus()) && !survey.isQuiz()) {
                        // match with Account
                        if (survey.isForAccount(account)) {
                            surveyTypes.add(survey);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG,
                            "Error occurred while attempting to get surveys. Please verify validity of JSON data set.", e);
                }
            }
        }
        return surveyTypes;
    }

    public static List<Survey__c> getQuizzes() {
        ArrayList<Survey__c> results = new ArrayList<>();
        RecordType recordType = RecordType.getByName("Quiz");
        if (recordType == null) return results;

        String smartSqlFilter = String.format("{%s:%s} = '%s'", AbInBevObjects.SURVEY_C, SurveyFields.RECORD_TYPE_ID, recordType.getId());
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.SURVEY_C, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        ArrayList<Survey__c> surveyTypes = new ArrayList<>();
        if (recordsArray != null) {
            for (int i = 0; i < recordsArray.length(); i++) {
                try {
                    Survey__c survey = new Survey__c(recordsArray.getJSONArray(i).getJSONObject(0));
                    if ("Desplegado".equals(survey.getStringValueForKey("Status__c"))) {
                        surveyTypes.add(survey);
                    }
                } catch (JSONException e) {
                    Log.e(TAG,
                            "Error occurred while attempting to get quizzes.");
                }
            }
        }
        return surveyTypes;
    }

    public static Survey__c getByName(String name) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.SURVEY_C, "Name", String.valueOf(name));
        if (jsonObject != null) {
            return new Survey__c(jsonObject);
        }
        return null;
    }

    public ContentVersion getAssociatedContent() {
        String contentDocumentId = getHTMLBundleKey();
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
