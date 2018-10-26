package com.salesforce.dsa.data.model;


import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.ACTIVE;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.BUTTON_DEFAULT_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.BUTTON_HIGHLIGHT_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.BUTTON_HIGHLIGHT_TEXT_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.BUTTON_TEXT_ALPHA;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.BUTTON_TEXT_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.CHECK_IN_ENABLED;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.INTRO_TEXT;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.INTRO_TEXT_ALPHA;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.INTRO_TEXT_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.IN_EDIT;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.LANDSCAPE_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.LANGUAGE;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.LINK_TO_EDITOR;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.LOGO_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.PORTRAIT_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.PROFILES;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.PROFILE_NAMES;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.PROFILE_TEXT;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.REPORT_AN_ISSUE;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.TITLE_BG_ALPHA;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.TITLE_BG_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.TITLE_TEXT;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.TITLE_TEXT_ALPHA;
import static com.salesforce.dsa.utils.DSAConstants.MobileAppConfigFields.TITLE_TEXT_COLOR;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;
import org.json.JSONArray;
import org.json.JSONObject;

public class MobileAppConfig__c extends SFBaseObject {

    private static final long serialVersionUID = -5472287987395800930L;

    public MobileAppConfig__c() {
        super(DSAObjects.MOBILE_APP_CONFIG);
    }

    public MobileAppConfig__c(JSONObject json) {
        super(DSAObjects.MOBILE_APP_CONFIG, json);
    }

    public boolean getActive__c() {
        return getBooleanValueForKey(ACTIVE);
    }

    public String getButtonDefaultAttachmentId__c() {
        return getStringValueForKey(BUTTON_DEFAULT_ATTACHMENT_ID);
    }

    public String getButtonHighlightAttachmentId__c() {
        return getStringValueForKey(BUTTON_HIGHLIGHT_ATTACHMENT_ID);
    }

    public String getButtonHighlightTextColor__c() {
        return getStringValueForKey(BUTTON_HIGHLIGHT_TEXT_COLOR);
    }

    public String getButtonTextAlpha__c() {
        return getStringValueForKey(BUTTON_TEXT_ALPHA);
    }

    public String getButtonTextColor__c() {
        return getStringValueForKey(BUTTON_TEXT_COLOR);
    }

    public boolean getCheck_In_Enabled__c() {
        return getBooleanValueForKey(CHECK_IN_ENABLED);
    }

    public String getIntroTextAlpha__c() {
        return getStringValueForKey(INTRO_TEXT_ALPHA);
    }

    public String getIntroTextColor__c() {
        return getStringValueForKey(INTRO_TEXT_COLOR);
    }

    public String getIntroText__c() {
        return getStringValueForKey(INTRO_TEXT);
    }

    public String getLandscapeAttachmentId__c() {
        return getStringValueForKey(LANDSCAPE_ATTACHMENT_ID);
    }

    public String getLanguage__c() {
        return getStringValueForKey(LANGUAGE);
    }

    public String getLinkToEditor__c() {
        return getStringValueForKey(LINK_TO_EDITOR);
    }

    public String getLogoAttachmentId__c() {
        return getStringValueForKey(LOGO_ATTACHMENT_ID);
    }

    public String getPortraitAttachmentId__c() {
        return getStringValueForKey(PORTRAIT_ATTACHMENT_ID);
    }

    public String getProfileText__c() {
        return getStringValueForKey(PROFILE_TEXT);
    }

    public String getProfile_Names__c() {
        return getStringValueForKey(PROFILE_NAMES);
    }

    public String getProfiles__c() {
        return getStringValueForKey(PROFILES);
    }

    public String getReport_an_Issue__c() {
        return getStringValueForKey(REPORT_AN_ISSUE);
    }

    public String getTitleBgAlpha__c() {
        return getStringValueForKey(TITLE_BG_ALPHA);
    }

    public String getTitleBgColor__c() {
        return getStringValueForKey(TITLE_BG_COLOR);
    }

    public String getTitleTextAlpha__c() {
        return getStringValueForKey(TITLE_TEXT_ALPHA);
    }

    public String getTitleTextColor__c() {
        return getStringValueForKey(TITLE_TEXT_COLOR);
    }

    public String getTitleText__c() {
        return getStringValueForKey(TITLE_TEXT);
    }

    public boolean getinEdit__c() {
        return getBooleanValueForKey(IN_EDIT);
    }

    public static MobileAppConfig__c getMobileAppConfigForCatMobileConfig(CategoryMobileConfig__c catMobileConfig) {
        String filter = String.format("{MobileAppConfig__c:%s} IN ('%s')",
                SyncEngineConstants.StdFields.ID,
                catMobileConfig.getMobileAppConfigurationId__c());
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.MOBILE_APP_CONFIG, filter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            JSONObject json = recordsArray.getJSONArray(0).getJSONObject(0);
            MobileAppConfig__c mobileAppConfig = new MobileAppConfig__c(json);
            return mobileAppConfig;
        } catch (Exception e) {
            return null;
        }
    }
}
