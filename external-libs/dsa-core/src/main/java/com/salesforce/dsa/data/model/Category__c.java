package com.salesforce.dsa.data.model;

import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.Guava.GuavaUtils;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.utils.DSAConstants;
import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.DESCRIPTION;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.GALLERY_ATTACHMENT_ID;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.IS_PARENT;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.IS_PARENT_CATEGORY;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.IS_TOP_LEVEL;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.LANGUAGE;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.ORDER;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.PARENT_CATEGORY;
import static com.salesforce.dsa.utils.DSAConstants.CategoryFields.TODAYS_SPECIAL;

public class Category__c extends SFBaseObject {

    private static final long serialVersionUID = -1144355280437498781L;
    private static final String TAG = "Category__c";

    private List<Category__c> subCategories;

    public Category__c() {
        super(DSAObjects.CATEGORY);
    }

    public Category__c(JSONObject json) {
        super(DSAObjects.CATEGORY, json);
    }

    public String getDescription__c() {
        return getStringValueForKey(DESCRIPTION);
    }

    public String getGalleryAttachmentId__c() {
        return getStringValueForKey(GALLERY_ATTACHMENT_ID);
    }

    public boolean getIs_Parent_Category__c() {
        return getIntValueForKey(IS_PARENT_CATEGORY) == 1;
    }

    public boolean getIs_Parent__c() {
        return getBooleanValueForKey(IS_PARENT);
    }

    public boolean getIs_Top_Level__c() {
        return getIntValueForKey(IS_TOP_LEVEL) == 1;
    }

    public String getLanguage__c() {
        return getStringValueForKey(LANGUAGE);
    }

    public String getOrder__c() {
        return getStringValueForKey(ORDER);
    }

    public String getParent_Category__c() {
        return getStringValueForKey(PARENT_CATEGORY);
    }

    public String getTodays_Special__c() {
        return getStringValueForKey(TODAYS_SPECIAL);
    }

    public static List<Category__c> fetchAllActiveCategories() {

        List<CategoryMobileConfig__c> catMobileConfigs = CategoryMobileConfig__c.fetchAllActiveCategoryMobileConfigs();

        if (catMobileConfigs == null)
            return null;

        List<String> catMobileConfigIds = GuavaUtils.transform(catMobileConfigs, new GuavaUtils.Function<CategoryMobileConfig__c, String>() {
            @Override
            public String apply(CategoryMobileConfig__c categoryMobileConfig__c) {
                return categoryMobileConfig__c.getCategoryId__c();
            }
        });

        Set<Category__c> categories = new HashSet<>();
        String smartSqlFilter = String.format("{Category__c:%s} IN ('%s')",
                SyncEngineConstants.StdFields.ID,
                Joiner.on("','").join(catMobileConfigIds));
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CATEGORY, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        try {
            for (int index = 0; index < recordsArray.length(); index++) {
                JSONObject json = recordsArray.getJSONArray(index).getJSONObject(0);
                Category__c category = new Category__c(json);
                categories.add(category);
                categories.addAll(category.recursiveSubCategories());
            }

        } catch (Exception e) {
            return null;
        }

        return Arrays.asList(categories.toArray(new Category__c[categories.size()]));
    }

    private Set<Category__c> recursiveSubCategories() {

        Set<Category__c> categories = new HashSet<>();
        String subCatFilter = String.format("{Category__c:%s} = '%s'",
                DSAConstants.CategoryFields.PARENT_CATEGORY,
                this.getId());
        String subCatSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CATEGORY, subCatFilter);
        JSONArray records = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(subCatSql);
        try {
            for (int index = 0; index < records.length(); index++) {
                JSONObject jsonCat = records.getJSONArray(index).getJSONObject(0);
                Category__c category = new Category__c(jsonCat);
                categories.add(category);
                categories.addAll(category.recursiveSubCategories());
            }
        } catch (Exception e) {
            return null;
        }

        return categories;
    }

    public List<Category__c> getParentCategories() {
        List<Category__c> parentCategories = new ArrayList<>();
        String parentId = this.getParent_Category__c();
        if (parentId != null) {
            Category__c parent = Category__c.getCategoryForId(parentId);
            if (parent != null)
                parentCategories.add(0, parent);
        }

        return parentCategories;
    }

    public static Category__c getCategoryForId(String categoryId) {

        String filter = String.format("{Category__c:%s} = '%s'",
                SyncEngineConstants.StdFields.ID,
                categoryId);
        String subCatSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, DSAObjects.CATEGORY, filter);
        JSONArray records = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(subCatSql);
        try {
            if (records.length() > 0) {
                JSONObject jsonCat = records.getJSONArray(0).getJSONObject(0);
                Category__c category = new Category__c(jsonCat);
                return category;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}

