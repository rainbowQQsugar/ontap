package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.OrderItemFields;
import com.abinbev.dsa.utils.AbInBevConstants.ProductFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Order_Item__c extends SFBaseObject {
    public static final String TAG = Order_Item__c.class.getSimpleName();

    public Order_Item__c(JSONObject json) {
        super(AbInBevObjects.ORDER_ITEM, json);
    }

    public Order_Item__c() {
        super(AbInBevObjects.ORDER_ITEM);
    }

    //to handle sorting be created date when note synced, this no created date.
    public String getSoupLastModified() {
        return getStringValueForKey("_soupLastModifiedDate");
    }

    public static List<Order_Item__c> getAllOrderLineItemsByOrderId(String orderId) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s')",
                AbInBevObjects.ORDER_ITEM, OrderItemFields.ORDER, orderId);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.ORDER_ITEM, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Order_Item__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Order_Item__c(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All Order Items for Order Id: " + orderId, e);
        }
        return results;
    }

    public Integer getVolumeHL() {
        return getIntValueForKey(OrderItemFields.VOLUME_HL);
    }

    public Integer getQuantity() {
        return getIntValueForKey(OrderItemFields.QUANTITY);
    }

    public void setQuantity(int quantity) {
        setIntValueForKey(OrderItemFields.QUANTITY, quantity);
    }

    public String getProductDesc() {
        return getStringValueForKey(OrderItemFields.ProductDesc__c);
    }

    public void setProduct(String productId) {
        setStringValueForKey(OrderItemFields.PRODUCT, productId);
    }

    public void setOrder(String orderId) {
        setStringValueForKey(OrderItemFields.ORDER, orderId);
    }

    public String getOrder() {
        return getStringValueForKey(OrderItemFields.ORDER);
    }

    public Product getProduct() {
        return new Product(DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.PRODUCT, "Id", getStringValueForKey(OrderItemFields.PRODUCT)));
    }

    public String getProductId() {
        return getStringValueForKey(OrderItemFields.PRODUCT);
    }

    public String getProductName() {
        return getReferencedValueObjectField(AbInBevObjects.PRODUCT, OrderItemFields.PRODUCT, ProductFields.PRODUCT_NAME);
    }

    public String getProductCode() {
        return getReferencedValueObjectField(AbInBevObjects.PRODUCT, OrderItemFields.PRODUCT, ProductFields.PRODUCT_CODE);
    }

    public String getOrderStartDate() {
        return getReferencedValueObjectField(AbInBevObjects.PEDIDO, OrderItemFields.ORDER, AbInBevConstants.PedidoFields.START_DATE);
    }

    public String getOrderEndDate() {
        return getReferencedValueObjectField(AbInBevObjects.PEDIDO, OrderItemFields.ORDER, AbInBevConstants.PedidoFields.END_DATE);
    }

    public String getProductUnitOfMeasure() {
        return getReferencedValueObjectField(AbInBevObjects.PRODUCT, OrderItemFields.PRODUCT, ProductFields.UNIT_OF_MEASURE);
    }

    public static String saveOrderItem(Order_Item__c orderItem) {
        return DataManagerFactory.getDataManager().createRecord(AbInBevObjects.ORDER_ITEM, orderItem.toJson());
    }

    public static boolean deleteOrderItem(Order_Item__c orderItem) {
        return DataManagerFactory.getDataManager().deleteRecord(AbInBevObjects.ORDER_ITEM, orderItem.getId());
    }

    public String getProductExternalId() {
        return getReferencedValueObjectField(AbInBevObjects.PRODUCT, OrderItemFields.PRODUCT, ProductFields.ExternalKey__c);
    }
    public String B2B_ProductID__c() {
        return getStringValueForKey(OrderItemFields.B2B_ProductID__c);
    }

    public String getBrand() {
        return getStringValueForKey(OrderItemFields.CN_Brand__c);
    }
}
