package com.abinbev.dsa.model;

import android.text.TextUtils;

import com.abinbev.dsa.ui.presenter.ProspectDetailPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ProductFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Product extends SFBaseObject {

    public static final String TAG = Product.class.getSimpleName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("Product", AbInBevObjects.PRODUCT)
            .putColumn("Channel", ProductFields.CHANNEL)
            .putColumn("ProductName", ProductFields.PRODUCT_NAME)
            .build();

    public Product(JSONObject json) {
        super(AbInBevObjects.PRODUCT, json);
    }

    public Product() {
        super(AbInBevObjects.PRODUCT);
    }

    public static List<Product> getAllProducts() {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchAllObjects(dm, AbInBevObjects.PRODUCT, Product.class);
    }

    public static List<Product> getProductsByChannelAndTerritory(String channel, String region) {
        DataManager dm = DataManagerFactory.getDataManager();

        String query = "SELECT {Product:_soup} FROM {Product}";
        String filter;
        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("currentChannel", channel);

        filter = String.format("{%1$s:%2$s} IN (SELECT {%3$s:%4$s} FROM {%3$s} WHERE {%3$s:%5$s} = '%6$s' AND {%3$s:%7$s} = '%8$s' )",
                AbInBevObjects.PRODUCT, ProductFields.ID,
                AbInBevObjects.CN_SKU_TC_Relationship__c, AbInBevConstants.SkuTcRelationshipFields.PRODUCT,
                AbInBevConstants.SkuTcRelationshipFields.CHANNEL, channel,
                AbInBevConstants.SkuTcRelationshipFields.TERRITORY, region);

        query += " WHERE " + filter;
        query += " ORDER BY {Product:ProductName} ASC";

        return DataManagerUtils.fetchObjects(dm, query, fv, Product.class);
    }

    public static List<Product> getProductsByChannel(String channel) {
        DataManager dm = DataManagerFactory.getDataManager();

        String query = "SELECT {Product:_soup} FROM {Product}";
        String filter;

        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("currentChannel", channel);
        filter = "({Product:Channel} = '{currentChannel}'" +
                " OR {Product:Channel} LIKE '{currentChannel};%'" +
                " OR {Product:Channel} LIKE '%;{currentChannel};%'" +
                " OR {Product:Channel} LIKE '%;{currentChannel}')";

        query += " WHERE " + filter;
        query += " ORDER BY {Product:ProductName} ASC";

        return DataManagerUtils.fetchObjects(dm, query, fv, Product.class);
    }


    public static List<Product> filter(String searchText, String channel) {
        if (TextUtils.isEmpty(channel)) return new ArrayList<>();

        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);

        String query = "SELECT {Product:_soup} FROM {Product}";
        String filter;

        // Append channel filter.
        fv.putValue("currentChannel", channel);
        filter = "({Product:Channel} = '{currentChannel}'" +
                " OR {Product:Channel} LIKE '{currentChannel};%'" +
                " OR {Product:Channel} LIKE '%;{currentChannel};%'" +
                " OR {Product:Channel} LIKE '%;{currentChannel}')";

        // Append name filter.
        if (!TextUtils.isEmpty(searchText)) {
            // Replace spaces with '%' for better search.
            searchText = searchText.trim().replace(' ', '%');
            fv.putValue("searchText", searchText);

            filter += " AND {Product:ProductName} LIKE '%{searchText}%'";
        }

        query += " WHERE " + filter;
        query += " ORDER BY {Product:ProductName} ASC";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, query, fv, Product.class);
    }

    public static Product getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, AbInBevObjects.PRODUCT, id, Product.class);
    }

    public String getProductShortName() {
        return getStringValueForKey(ProductFields.PRODUCT_SHORT_NAME);
    }

    public String getProductName() {
        return getStringValueForKey(ProductFields.PRODUCT_NAME);
    }

    public String getProductCode() {
        return getStringValueForKey(ProductFields.PRODUCT_CODE);
    }

    public String getProductUnit() {
        return getStringValueForKey(ProductFields.UNIT_OF_MEASURE);
    }

    public boolean competitorFlag() {
        return getBooleanValueForKey(ProductFields.COMPETITOR_FLAG);
    }

    public String getPackage() {
        return getStringValueForKey(ProductFields.PACKAGE);
    }

    public String getChBrand() {
        return getStringValueForKey(ProductFields.CN_BRAND);
    }

    public String getChannel() {
        return getStringValueForKey(ProductFields.CHANNEL);
    }
}
