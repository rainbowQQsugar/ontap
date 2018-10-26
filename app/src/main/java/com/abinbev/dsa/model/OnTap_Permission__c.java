package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PermissionFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class OnTap_Permission__c extends SFBaseObject {

    public static final String TAG = OnTap_Permission__c.class.getName();

    public OnTap_Permission__c() {
        super(AbInBevObjects.PERMISSIONS);
    }

    public OnTap_Permission__c(JSONObject jsonObject) {
        super(AbInBevObjects.PERMISSIONS, jsonObject);
    }

    public static OnTap_Permission__c getPermissionByProfileName(String profileName) {
        JSONObject jsonObject = DataManagerFactory
                .getDataManager().exactQuery(AbInBevObjects.PERMISSIONS, PermissionFields.PROFILE_NAME, profileName);
        return jsonObject == null ? null : new OnTap_Permission__c(jsonObject);
    }

    public boolean hasPermissionForVisitList() {
        return getBooleanValueForKey(PermissionFields.VISIT_LIST);
    }

    public boolean hasPermissionForProspectList() {
        return getBooleanValueForKey(PermissionFields.PROSPECTS);
    }

    public boolean hasPermissionForAccountList() {
        return getBooleanValueForKey(PermissionFields.ACCOUNTS);
    }

    public boolean hasPermissionForQuizzes() {
        return getBooleanValueForKey(PermissionFields.QUIZZES);
    }

    public boolean hasPermissionForDSA() {
        return getBooleanValueForKey(PermissionFields.DSA);
    }

    public boolean hasPermissionForChatter() {
        return getBooleanValueForKey(PermissionFields.CHATTER);
    }

    public boolean hasPermissionForCalculator() {
        return getBooleanValueForKey(PermissionFields.CALCULATOR);
    }

    public boolean hasPermissionForFullSync() {
        return getBooleanValueForKey(PermissionFields.FULL_SYNC);
    }

    public boolean hasPermissionForPOCE() {
        return getBooleanValueForKey(PermissionFields.POCE);
    }

    public boolean hasPermissionForSurveys() {
        return getBooleanValueForKey(PermissionFields.SURVEYS);
    }

    public boolean hasPermissionForTasks() {
        return getBooleanValueForKey(PermissionFields.TASKS);
    }

    public boolean hasPermissionForPromotions() {
        return getBooleanValueForKey(PermissionFields.PROMOTIONS);
    }

    public boolean hasPermissionForCases() {
        return getBooleanValueForKey(PermissionFields.CASES);
    }

    public boolean hasPermissionForNegotiations() {
        return getBooleanValueForKey(PermissionFields.NEGOTIATIONS);
    }

    public boolean hasPermissionForOrders() {
        return getBooleanValueForKey(PermissionFields.ORDERS);
    }

    public boolean hasPermissionForAssets() {
        return getBooleanValueForKey(PermissionFields.ASSETS);
    }

    public boolean hasPermissionForCreateSurvey() {
        return getBooleanValueForKey(PermissionFields.CREATE_SURVEY);
    }

    public boolean hasPermissionForCreateNegotiation() {
        return getBooleanValueForKey(PermissionFields.CREATE_NEGOTIATION);
    }

    public boolean hasPermissionForCreateTasks() {
        return getBooleanValueForKey(PermissionFields.CREATE_TASKS);
    }

    public boolean hasPermissionForCreateAssets() {
        return getBooleanValueForKey(PermissionFields.CREATE_ASSETS);
    }

    public boolean hasPermissionForCreateOrders() {
        return getBooleanValueForKey(PermissionFields.CREATE_ORDERS);
    }

    public boolean hasPermissionForCreateCases() {
        return getBooleanValueForKey(PermissionFields.CREATE_CASE);
    }

    public boolean hasPermissionForMarketPrograms() {
        return getBooleanValueForKey(PermissionFields.MARKET_PROGRAMS);
    }

    public boolean hasPermissionForNegotiationGauge() {
        return getBooleanValueForKey(PermissionFields.NEGOTIATION_GAUGE);
    }

    public boolean hasPermissionForProspectBasicData() {
        return getBooleanValueForKey(PermissionFields.PROSPECT_BASIC_DATA);
    }

    public boolean hasPermissionForProspectAdditionalData() {
        return getBooleanValueForKey(PermissionFields.PROSPECT_ADDITIONAL_DATA);
    }

    public boolean hasPermissionForProspectNegotiations() {
        return getBooleanValueForKey(PermissionFields.PROSPECT_NEGOTIATIONS);
    }

    public boolean hasPermissionForProspectFiles() {
        return getBooleanValueForKey(PermissionFields.PROSPECT_FILES);
    }

    public boolean hasPermissionForUser360HamburgerMenu() {
        return getBooleanValueForKey(PermissionFields.USER_360_HAMBURGER_MENU);
    }

    public boolean hasPermissionForAccountKpis() {
        return getBooleanValueForKey(PermissionFields.ACCOUNT_KPIS);
    }

    public boolean hasPermissionForAssetPhotoRequired() {
        return getBooleanValueForKey(PermissionFields.ASSET_PHOTO_REQUIRED);
    }

    public boolean hasPermissionForCheckoutRules() {
        return true;
        // TODO uncomment after adding to the backend
//        return getBooleanValueForKey(AbInBevConstants.PermissionFields.CHECKOUT_RULES);
    }
    public boolean hasPermissionForAssetPoc() {
        return getBooleanValueForKey(PermissionFields.CN_ASSET_ACCOUNT360);
    }
    public boolean requiresMorningMeetingPictureComment() {
        return getBooleanValueForKey(PermissionFields.MORNING_MEETING_PICTURE_COMMENT_REQUIRED);
    }

    public boolean requiresCheckInPictureComment() {
        return getBooleanValueForKey(PermissionFields.ACCOUNT_CHECK_IN_PICTURE_COMMENT_REQUIRED);
    }
    public boolean hasPermissionForTechnicians() {
        return getBooleanValueForKey(PermissionFields.CN_TECHNICIANS_ACCOUNT360);
    }

    /** In hours. */
    public double getMinimalMorningMeetingInterval() {
        return getDoubleValueForKey(PermissionFields.MINIMAL_MORNING_MEETING_HRS_INTERVAL);
    }
}
