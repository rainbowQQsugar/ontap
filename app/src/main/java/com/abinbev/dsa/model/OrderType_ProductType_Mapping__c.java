package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.OrderTypeProductTypeMappingFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nchangnon on 12/15/15.
 */
public class OrderType_ProductType_Mapping__c extends SFBaseObject {

    public OrderType_ProductType_Mapping__c(JSONObject json) {
        super(AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING, json);
    }

    protected OrderType_ProductType_Mapping__c() {
        super(AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING);
    }

    public String getProductType() {
        return getStringValueForKey(OrderTypeProductTypeMappingFields.PRODUCT_RECORD_TYPE);
    }

    public String getLineItems() {
        return getStringValueForKey(OrderTypeProductTypeMappingFields.LINE_ITEM_FIELDS);
    }

    public static OrderType_ProductType_Mapping__c getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new OrderType_ProductType_Mapping__c(jsonObject);
        }
        return null;
    }

    public static OrderType_ProductType_Mapping__c getByOrderRecordType(String type) {
        if (type == null) return null;
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING, OrderTypeProductTypeMappingFields.ORDER_RECORD_TYPE, type);
        if (jsonObject != null) {
            return new OrderType_ProductType_Mapping__c(jsonObject);
        }
        return null;
    }

    public static List<String> getProductTypeForOrderType(String orderType) {
        List<String> productTypeList = new ArrayList<String>();

        String smartSqlFilter = String.format("{%s:%s} = '%s'",
                AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING, OrderTypeProductTypeMappingFields.ORDER_RECORD_TYPE, orderType);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        for (int i=0; i< recordsArray.length(); i++) {
            try {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                productTypeList.add((new OrderType_ProductType_Mapping__c(jsonObject)).getProductType());
            } catch (JSONException jse) {
                Log.e("Babu", "got exception in getProductTypeForOrder");
            }
        }
        return productTypeList;
    }

    public static String getLineItemsForOrderType(String orderType) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(
                AbInBevObjects.ORDER_TYPE_PRODUCT_TYPE_MAPPING,
                OrderTypeProductTypeMappingFields.ORDER_RECORD_TYPE, orderType);
        if (jsonObject != null) {
            return new OrderType_ProductType_Mapping__c(jsonObject).getLineItems();
        }
        return null;
    }
}
