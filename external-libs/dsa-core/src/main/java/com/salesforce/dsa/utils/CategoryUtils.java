package com.salesforce.dsa.utils;

import android.content.Context;
import android.util.Log;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.Guava.Joiner;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.CN_DSA_Folder__c;
import com.salesforce.dsa.data.model.CategoryCacheHolder;
import com.salesforce.dsa.data.model.Category__c;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import static com.salesforce.dsa.utils.DSAConstants.DSAObjects.CATEGORY_MOBILE_CONFIG;

/**
 * Created by bduggirala on 10/21/15.
 */
public class CategoryUtils {

    private CategoryUtils() {
    }

    private static final String TAG = "CategoryUtils";

    private static final String CATEGORY_HOLDER_SERIALIZED_FILE = "categoryHolder.ser";
    private static final Object categoryHolderIOLock = new Object();

    private static CategoryCacheHolder categoryCacheHolder;
    private static ArrayList<String> allVisibleCategories = new ArrayList();

    private static List<String> readSubCategoryIds(final String categoryId) {
        synchronized (categoryHolderIOLock) {
            List<String> subCategoryIds;
            try {
                if (categoryCacheHolder == null) {
                    Context context = SalesforceSDKManager.getInstance().getAppContext();
                    ObjectInputStream in = new ObjectInputStream(context.openFileInput(CATEGORY_HOLDER_SERIALIZED_FILE));
                    categoryCacheHolder = (CategoryCacheHolder) in.readObject();
                    Log.i(TAG, "categoryCacheHolderSize: " + categoryCacheHolder.size());
                }
                subCategoryIds = categoryCacheHolder.get(categoryId);
                if (subCategoryIds == null) subCategoryIds = new ArrayList<String>();
                Log.i(TAG, "read from category file");
            } catch (Exception e) {
                subCategoryIds = new ArrayList<String>();
                Log.e(TAG, "initalized with empty. Should never happen!");
            }
            return subCategoryIds;
        }
    }

    public static List<String> getAllCategories() {
        return allVisibleCategories;
    }

    public static List<CN_DSA_Folder__c> getSubCategories(final String categoryId) {
        List<String> subCategoryIds = readSubCategoryIds(categoryId);
        List<CN_DSA_Folder__c> subCategories = new ArrayList<>();

        //TODO switch to using "IN" clause if it makes sense
        for (String id : subCategoryIds) {
            subCategories.add(CN_DSA_Folder__c.getDsaFolderForId(id));
        }

        return subCategories;
    }


    public static void populateCategoryCacheHolder(final Context context, String mobileAppConfigId) {

        synchronized (categoryHolderIOLock) {

            categoryCacheHolder = new CategoryCacheHolder();

            try {
//                String smartSql = String.format("{CN_DSA_Folder__c:%s} = ('%s')", DSAConstants.CNDSAFolderFields.CN_DSA__c, mobileAppConfigId);
                String smartSql = String.format("select {CN_DSA_Folder__c:Id} from {CN_DSA_Folder__c} where {CN_DSA_Folder__c:%s} = ('%s')", DSAConstants.CNDSAFolderFields.CN_DSA__c, mobileAppConfigId);
                Log.e(TAG, "populateCategoryCacheHolder: " + smartSql);
                JSONArray recordsArray1 = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
                ArrayList<String> categoryIds = new ArrayList<>();

                for (int i = 0; i < recordsArray1.length(); i++) {
                    String categoryId = recordsArray1.getJSONArray(i).getString(0);
                    categoryIds.add(categoryId);
                }
                Log.e(TAG, "populateCategoryCacheHolder: " + categoryIds.toString());
                allVisibleCategories.addAll(categoryIds);
                String query = String.format("select {CN_DSA_Folder__c:_soup} from {CN_DSA_Folder__c} where {CN_DSA_Folder__c:Id} IN ('%s')",
                        Joiner.on("','").join(categoryIds));
                JSONArray recordsArray2 = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(query);
                for (int i = 0; i < recordsArray2.length(); i++) {
                    JSONObject jsonObject = recordsArray2.getJSONArray(i).getJSONObject(0);
                    CN_DSA_Folder__c category = new CN_DSA_Folder__c(jsonObject);
                    List<String> subSategoryIds = categoryCacheHolder.get(category.getCN_Parent_Folder__c());
                    if (subSategoryIds == null) subSategoryIds = new ArrayList<>();
                    if (!subSategoryIds.contains(category.getId())) {
                        subSategoryIds.add(category.getId());
                        categoryCacheHolder.put(category.getCN_Parent_Folder__c(), subSategoryIds);
                    }
//                    Log.e(TAG, "populateCategoryCacheHolder: id" + category.getId() + "   Category__c:Id   " + category.getParent_Category__c());
                }

                ObjectOutputStream out = new ObjectOutputStream(
                        context.openFileOutput(CATEGORY_HOLDER_SERIALIZED_FILE,
                                Context.MODE_PRIVATE));

                out.writeObject(categoryCacheHolder);
                out.close();

            } catch (Exception e2) {
                Log.e(TAG, "got exception in writeSubCategoryIds. " + e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

}

