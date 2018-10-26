package com.salesforce.dsa.data.model;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.BUTTON_TEXT_ALIGN;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.CATEGORY_BUNDLE_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.CATEGORY_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.CONTENT_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.CONTENT_OVER_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.GALLERY_HEADING_TEXT;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.GALLERY_HEADING_TEXT_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.IS_DEFAULT;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.IS_DRAFT;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.LANDSCAPE_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.LANDSCAPE_X;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.LANDSCAPE_Y;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.LAST_REFERENCE_DATE;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.LAST_VIEWED_DATE;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.MAC_IN_EDIT;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.MOBILE_APP_CONFIGURATION_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.OVERLAY_BG_ALPHA;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.OVERLAY_BG_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.OVERLAY_TEXT_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.PORTRAIT_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.PORTRAIT_X;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.PORTRAIT_Y;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.SUB_CATEGORY_BACKGROUND_COLOR;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.TOP_LEVEL_CATEGORY;
import static com.salesforce.dsa.utils.DSAConstants.CategoryMobileConfigFields.USE_CATEGORY_BUNDLE;

public class CategoryMobileConfig__c extends SFBaseObject {

    private static final long serialVersionUID = -5957676194931118974L;

    public CategoryMobileConfig__c() {
        super(DSAObjects.MOBILE_APP_CONFIG);
    }

    public CategoryMobileConfig__c(JSONObject json) {
        super(DSAObjects.MOBILE_APP_CONFIG, json);
    }

    public String getLastReferencedDate() {
        return getStringValueForKey(LAST_REFERENCE_DATE);
    }

    public String getLastViewedDate() {
        return getStringValueForKey(LAST_VIEWED_DATE);
    }

    public String getButton_Text_Align__c() {
        return getStringValueForKey(BUTTON_TEXT_ALIGN);
    }

    public String getCategoryBundleId__c() {
        return getStringValueForKey(CATEGORY_BUNDLE_ID);
    }

    public String getCategoryId__c() {
        return getStringValueForKey(CATEGORY_ID);
    }

    public String getContentAttachmentId__c() {
        return getStringValueForKey(CONTENT_ATTACHMENT_ID);
    }

    public String getContentOverAttachmentId__c() {
        return getStringValueForKey(CONTENT_OVER_ATTACHMENT_ID);
    }

    public String getGalleryHeadingText__c() {
        return getStringValueForKey(GALLERY_HEADING_TEXT);
    }

    public String getGalleryHeadingTextColor__c() {
        return getStringValueForKey(GALLERY_HEADING_TEXT_COLOR);
    }

    public boolean isIsDefault__c() {
        return getBooleanValueForKey(IS_DEFAULT);
    }

    public boolean isIsDraft__c() {
        return getBooleanValueForKey(IS_DRAFT);
    }

    public String getLandscapeAttachmentId__c() {
        return getStringValueForKey(LANDSCAPE_ATTACHMENT_ID);
    }

    public double getLandscapeX__c() {
        return getDoubleValueForKey(LANDSCAPE_X);
    }

    public double getLandscapeY__c() {
        return getDoubleValueForKey(LANDSCAPE_Y);
    }

    public boolean isMAC_in_Edit__c() {
        return getBooleanValueForKey(MAC_IN_EDIT);
    }

    public String getMobileAppConfigurationId__c() {
        return getStringValueForKey(MOBILE_APP_CONFIGURATION_ID);
    }

    public String getOverlayBgAlpha__c() {
        return getStringValueForKey(OVERLAY_BG_ALPHA);
    }

    public String getOverlayBgColor__c() {
        return getStringValueForKey(OVERLAY_BG_COLOR);
    }

    public String getOverlayTextColor__c() {
        return getStringValueForKey(OVERLAY_TEXT_COLOR);
    }

    public String getPortraitAttachmentId__c() {
        return getStringValueForKey(PORTRAIT_ATTACHMENT_ID);
    }

    public double getPortraitX__c() {
        return getDoubleValueForKey(PORTRAIT_X);
    }

    public double getPortraitY__c() {
        return getDoubleValueForKey(PORTRAIT_Y);
    }

    public String getSub_Category_Background_Color__c() {
        return getStringValueForKey(SUB_CATEGORY_BACKGROUND_COLOR);
    }

    public String getTop_Level_Category__c() {
        return getStringValueForKey(TOP_LEVEL_CATEGORY);
    }

    public boolean isUseCategoryBundle__c() {
        return getBooleanValueForKey(USE_CATEGORY_BUNDLE);
    }

    public static List<CategoryMobileConfig__c> fetchAllActiveCategoryMobileConfigs() {

        List<CategoryMobileConfig__c> allCategoryMobileConfigs = new ArrayList<>();

        if (allCategoryMobileConfigs == null)
            return null;

        //get all active MobileAppConfig__c records
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_ALL_FORMAT, DSAObjects.MOBILE_APP_CONFIG);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            List<String> activeMobileAppConfigsIds = new LinkedList<>();
            for (int index = 0; index < recordsArray.length(); index++) {
                JSONObject jsonObject = recordsArray.getJSONArray(index).getJSONObject(0);
                activeMobileAppConfigsIds.add(jsonObject.getString(SyncEngineConstants.StdFields.ID));
            }

            // get all categoryMobileConfig__c records using activeMobileAppConfigs ids
            // invoke the CategoryMobileConfig with a IN clause filter
            String catMobileConfigFilter = String.format("{CategoryMobileConfig__c:%s} IN ('%s')",
                    DSAConstants.CategoryMobileConfigFields.MOBILE_APP_CONFIGURATION_ID, Joiner.on("','").join(activeMobileAppConfigsIds));
            String catMobileSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CATEGORY_MOBILE_CONFIG, catMobileConfigFilter);
            JSONArray jsonRecords = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(catMobileSql);

            try {
                for (int index = 0; index < jsonRecords.length(); index++) {
                    JSONObject json = jsonRecords.getJSONArray(index).getJSONObject(0);
                    CategoryMobileConfig__c catMobileConfig = new CategoryMobileConfig__c(json);
                    allCategoryMobileConfigs.add(catMobileConfig);
                }
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        return allCategoryMobileConfigs;
    }
}
