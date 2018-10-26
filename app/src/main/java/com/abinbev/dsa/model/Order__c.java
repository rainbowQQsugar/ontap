package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.model.checkoutRules.AccountAssetTracking__c;
import com.abinbev.dsa.ui.presenter.AssetsListPresenter;
import com.abinbev.dsa.ui.presenter.PedidoListPresenter;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PedidoFields;
import com.abinbev.dsa.utils.AbInBevConstants.PedidoStatus;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.PicklistUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.google.gson.Gson;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsyncengine.data.layouts.Details;
import com.salesforce.androidsyncengine.data.layouts.EditLayoutSection;
import com.salesforce.androidsyncengine.data.layouts.IndividualLayouts;
import com.salesforce.androidsyncengine.data.layouts.RecordTypeMapping;
import com.salesforce.androidsyncengine.data.model.PicklistValue;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.MetaDataProvider;
import com.salesforce.androidsyncengine.syncmanifest.ManifestUtils;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;
import com.salesforce.dsa.utils.DSAConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_NOT_THIS_POC;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_QUALIFIED;
import static com.abinbev.dsa.ui.presenter.AssetsListPresenter.Contact.ASSET_TRACKING_STATUS_UNDETECTED;

public class Order__c extends TranslatableSFBaseObject {
    public static final String TAG = Order__c.class.getSimpleName();

    public static final String[] PRODUCT_FIELDS = new String[]{PedidoFields.MATERIALLOOKUP_1__C,
            PedidoFields.MATERIALLOOKUP_2__C, PedidoFields.MATERIALLOOKUP_3__C,
            PedidoFields.MATERIALLOOKUP_4__C, PedidoFields.MATERIALLOOKUP_5__C,
            PedidoFields.MATERIALLOOKUP_6__C, PedidoFields.MATERIALLOOKUP_7__C};
    public static final int MAX_ORDERS = PRODUCT_FIELDS.length;

    public static final String[] MOTIVE_FIELDS = new String[]{PedidoFields.MOTIVE_1__C,
            PedidoFields.MOTIVE_2__C, PedidoFields.MOTIVE_3__C,
            PedidoFields.MOTIVE_4__C, PedidoFields.MOTIVE_5__C,
            PedidoFields.MOTIVE_6__C, PedidoFields.MOTIVE_7__C};

    public static final String[] PRODUCT_CODE_FIELDS = new String[]{PedidoFields.MATERIAL_CODE_1__C,
            PedidoFields.MATERIAL_CODE_2__C, PedidoFields.MATERIAL_CODE_3__C, PedidoFields.MATERIAL_CODE_4__C,
            PedidoFields.MATERIAL_CODE_5__C, PedidoFields.MATERIAL_CODE_6__C, PedidoFields.MATERIAL_CODE_7__C};

    public static final String[] UNIT_OF_MEASURE_FIELDS = new String[]{PedidoFields.UNIT_OF_MEASURE_1__C,
            PedidoFields.UNIT_OF_MEASURE_2__C, PedidoFields.UNIT_OF_MEASURE_3__C,
            PedidoFields.UNIT_OF_MEASURE_4__C, PedidoFields.UNIT_OF_MEASURE_5__C,
            PedidoFields.UNIT_OF_MEASURE_6__C, PedidoFields.UNIT_OF_MEASURE_7__C};

    public static final String[] QUANTITY_FIELDS = new String[]{PedidoFields.QUANTITY_1__C,
            PedidoFields.QUANTITY_2__C, PedidoFields.QUANTITY_3__C,
            PedidoFields.QUANTITY_4__C, PedidoFields.QUANTITY_5__C,
            PedidoFields.QUANTITY_6__C, PedidoFields.QUANTITY_7__C};

    public static final String FIELD_RECORD_TYPE_NAME = AbInBevConstants.AbInBevObjects.RECORD_TYPE +
            "." + AbInBevConstants.RecordTypeFields.NAME;

    public List<PicklistValue> cashPicklist;
    public List<PicklistValue> reasonPicklist;
    /**
     * the set of products on an order and its associated index
     **/
    public LinkedHashMap<Material_Give__c, Integer> products = new LinkedHashMap<>();
    public OrderType_ProductType_Mapping__c orderTypeProductTypeMapping__c;

    public enum UnitOfMeasure {
        CS, EA;
    }

    public Order__c(Order__c o) {
        super(AbInBevObjects.PEDIDO);
        setStatus(o.getStatus());
        setRecordType(o.getRecordTypeId());
        setStartDate(getStartDate());
        setEndDate(o.getEndDate());
        setName(o.getName());
        setRecordTypeName(o.getRecordTypeName());
    }

    String recordTypeName;

    public Order__c(JSONObject json) {
        super(AbInBevObjects.PEDIDO, json);
    }

    protected Order__c() {
        super(AbInBevObjects.PEDIDO);
    }

    public void setRecordType(String recordTypeId) {
        setStringValueForKey(PedidoFields.RECORD_TYPE_ID, recordTypeId);
    }

    private void setRecordTypeName(String recordTypeName) {
        this.recordTypeName = recordTypeName;
    }

    public String getStatus() {
        return getStringValueForKey(PedidoFields.STATUS);
    }

    public String getTranslatedStatus() {
        return getTranslatedStringValueForKey(PedidoFields.STATUS);
    }

    public void setStatus(String status) {
        setStringValueForKey(PedidoFields.STATUS, status);
    }

    public String getRecordTypeId() {
        return getStringValueForKey(PedidoFields.RECORD_TYPE_ID);
    }

    public String getSource() {
        return getStringValueForKey(PedidoFields.SOURCE);
    }

    public void setSource(String source) {
        setStringValueForKey(PedidoFields.SOURCE, source);
    }

    public String getRecordTypeName() {
        return this.recordTypeName;
    }

    public String getTranslatedRecordTypeName() {
        String translated = getTranslatedStringValueForKey(FIELD_RECORD_TYPE_NAME);
        return TextUtils.isEmpty(translated) ? getRecordTypeName() : translated;
    }

    public boolean getOut_of_Route_Order__c() {
        return getBooleanValueForKey(PedidoFields.OUT_OF_ROUTE_ORDER__C);
    }

    public void setOut_of_Route_Order__c(boolean cash) {
        setBooleanValueForKey(PedidoFields.OUT_OF_ROUTE_ORDER__C, cash);
    }

    public String getCash() {
        return getStringValueForKey(PedidoFields.CASH__C);
    }

    public boolean isCash() {
        return PedidoFields.CASH_PICKLIST_VALUE.equals(getStringValueForKey(PedidoFields.CASH__C));
    }

    public void setCash(String cash) {
        setStringValueForKey(PedidoFields.CASH__C, cash);
    }

    public void setProductCode(int index, String value) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            setStringValueForKey(PRODUCT_CODE_FIELDS[fieldIndex], value);
        }
    }

    public void setReason(int index, String value) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            setStringValueForKey(MOTIVE_FIELDS[fieldIndex], value);
        }
    }

    public void setQuantity(int index, int value) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            setIntValueForKey(QUANTITY_FIELDS[fieldIndex], value);
        }
    }

    public void setUnitOfMeasure(int index, String value) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            setStringValueForKey(UNIT_OF_MEASURE_FIELDS[fieldIndex], value);
        }
    }

    /**
     * adds a product to the next available spot.
     *
     * @param
     */
    public void addProduct(MaterialGiveAdapter.LineItem lineItem) {

        if (getProducts().size() < MAX_ORDERS) {
            int index = findFirstAvailableProductIndex();
            if (index > -1) {
                //update local object
                setStringValueForKey(PRODUCT_FIELDS[index], lineItem.product.getId());
                setStringValueForKey(MOTIVE_FIELDS[index], lineItem.reason);
                setStringValueForKey(PRODUCT_CODE_FIELDS[index], lineItem.product.getCode());
                setDoubleValueForKey(QUANTITY_FIELDS[index], lineItem.quantity);
                setStringValueForKey(UNIT_OF_MEASURE_FIELDS[index], lineItem.unitOfMeasure);
                products.put(lineItem.product, index);

                //update store
                JSONObject json = new JSONObject();
                try {
                    json.put(PRODUCT_FIELDS[index], lineItem.product.getId());
                    json.put(PRODUCT_CODE_FIELDS[index], lineItem.product.getCode());
                    json.put(MOTIVE_FIELDS[index], lineItem.reason);
                    json.put(QUANTITY_FIELDS[index], lineItem.quantity);
                    json.put(UNIT_OF_MEASURE_FIELDS[index], lineItem.unitOfMeasure);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating new order", e);
                }

                DataManager dataManager = DataManagerFactory.getDataManager();
                dataManager.updateRecord(AbInBevObjects.PEDIDO, this.getId(), json);
            }
        }
    }

    private int findFirstAvailableProductIndex() {
        for (int i = 0; i < MAX_ORDERS; i++) {
            String productId = getStringValueForKey(PRODUCT_FIELDS[i]);
            if (TextUtils.isEmpty(productId) || "null".equals(productId)) {
                return i;
            }
        }
        return -1;
    }

    public void removeProduct(int index) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            setNullValue(PRODUCT_FIELDS[fieldIndex]);
            setNullValue(MOTIVE_FIELDS[fieldIndex]);
            setNullValue(QUANTITY_FIELDS[fieldIndex]);
            setNullValue(UNIT_OF_MEASURE_FIELDS[fieldIndex]);
            Material_Give__c key = getProducts().get(index);
            products.remove(key);

            updateOrder(this.getId(), this.toJson());
        }
    }

    private Integer getFieldIndexForProductIndex(int index) {
        Material_Give__c key = getProducts().get(index);
        return products.get(key);
    }

    private String getStringFieldForProductIndex(int index, String[] fields) {
        Integer fieldIndex = getFieldIndexForProductIndex(index);
        if (fieldIndex != null) {
            return getStringValueForKey(fields[fieldIndex]);
        }
        return null;
    }

    public List<PicklistValue> getCashPicklist() {
        return cashPicklist;
    }

    public void setCashPicklist(List<PicklistValue> cashPicklist) {
        this.cashPicklist = cashPicklist;
    }

    public List<PicklistValue> getReasonPicklist() {
        return reasonPicklist;
    }

    public void setReasonPicklist(List<PicklistValue> reasonPicklist) {
        this.reasonPicklist = reasonPicklist;
    }

    public List<Material_Give__c> getProducts() {
        return new ArrayList(products.keySet());
    }

    public Material_Give__c getProduct(String id) {
        for (Material_Give__c product : getProducts()) {
            if (id.equals(product.getId())) {
                return product;
            }
        }
        return null;
    }

    public String getProductByIndex(int index) {
        return getStringFieldForProductIndex(index, PRODUCT_FIELDS);
    }

    public String getQuantityByIndex(int index) {
        return getStringFieldForProductIndex(index, QUANTITY_FIELDS);
    }

    public String getUnitOfMeasureByIndex(int index) {
        return getStringFieldForProductIndex(index, UNIT_OF_MEASURE_FIELDS);
    }

    public String getMotiveByIndex(int index) {
        return getStringFieldForProductIndex(index, MOTIVE_FIELDS);
    }

    public String getProduct(int position) {
        return getStringValueForKey(PRODUCT_FIELDS[position]);
    }

    public String getProductCode(int position) {
        return getStringValueForKey(PRODUCT_CODE_FIELDS[position]);
    }

    public String getQuantity(int position) {
        return getStringValueForKey(QUANTITY_FIELDS[position]);
    }

    public String getUnitOfMeasure(int position) {
        return getStringValueForKey(UNIT_OF_MEASURE_FIELDS[position]);
    }

    public Double getTotal() {
        return getDoubleValueForKey(PedidoFields.TOTAL);
    }

    public void setStartDate(Date date) {
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String strDate = parseFormat.format(date);
        setStringValueForKey(PedidoFields.START_DATE, strDate);
    }

    public void setEndDate(Date date) {
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String strDate = parseFormat.format(date);
        setStringValueForKey(PedidoFields.END_DATE, strDate);
    }


    public Date getStartDate() {
        return getDateValueForKey(PedidoFields.START_DATE);
    }

    public String getAccount() {
        return getStringValueForKey(AbInBevConstants.PedidoFields.Account__c);
    }

    public Date getEndDate() {
        return getDateValueForKey(PedidoFields.END_DATE);
    }

    public String getMotive(int position) {
        return getStringValueForKey(MOTIVE_FIELDS[position]);
    }

    public OrderType_ProductType_Mapping__c getOrderTypeProductTypeMapping__c() {
        return orderTypeProductTypeMapping__c;
    }

    public void setOrderTypeProductTypeMapping__c(OrderType_ProductType_Mapping__c orderTypeProductTypeMapping__c) {
        this.orderTypeProductTypeMapping__c = orderTypeProductTypeMapping__c;
    }

    // caching last value since this function can get called multiple times
    private static String lastCalculatedReasonPickListRecordTypeId = "";
    private static List<PicklistValue> lastCalculatedReasonPickListValues;

    public static List<PicklistValue> getReasonPicklistMetadata(String recordTypeId) {
        if (lastCalculatedReasonPickListRecordTypeId != null && lastCalculatedReasonPickListRecordTypeId.equals(recordTypeId)) {
            if (lastCalculatedReasonPickListValues != null) {
                for (int i = 0; i < lastCalculatedReasonPickListValues.size(); i++) {
                    if (lastCalculatedReasonPickListValues.get(i).getValue() == null) {
                        lastCalculatedReasonPickListValues.remove(i);
                    }
                }
            }
            return lastCalculatedReasonPickListValues;
        } else {
            Log.v(TAG, "getReasonPicklistMetadata: calculating");
            lastCalculatedReasonPickListValues = getPicklistValues(PedidoFields.MOTIVE_1__C, recordTypeId);
            lastCalculatedReasonPickListRecordTypeId = recordTypeId;
            return lastCalculatedReasonPickListValues;
        }
    }

    private static String lastCalculatedCashPickListRecordTypeId = "";
    private static List<PicklistValue> lastCalculatedCashPickListValues;

    public static List<PicklistValue> getCashPicklistMetadata(String recordTypeId) {
        if (lastCalculatedCashPickListRecordTypeId != null && lastCalculatedCashPickListRecordTypeId.equals(recordTypeId)) {
            return lastCalculatedCashPickListValues;
        } else {
            Log.v(TAG, "getCashPicklistMetadata: calculating");
            lastCalculatedCashPickListValues = getPicklistValues(PedidoFields.CASH__C, recordTypeId);
            lastCalculatedCashPickListRecordTypeId = recordTypeId;
            return lastCalculatedCashPickListValues;
        }
    }

    private static boolean picklistContainsValue(PicklistValue value, List<PicklistValue> picklistValues) {
        for (PicklistValue picklistValue : picklistValues) {
            if (value.getValue().equalsIgnoreCase(picklistValue.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static boolean setField(String orderId, String field, String value) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
            json.put(field, value);
        } catch (JSONException e) {
            Log.e(TAG, "Error setting field", e);
        }
        return dataManager.updateRecord(AbInBevObjects.PEDIDO, orderId, json);
    }

    public static boolean setCash(String orderId, String value) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
            if (value != null) {
                json.put(PedidoFields.CASH__C, value);
            } else {
                json.put(PedidoFields.CASH__C, JSONObject.NULL);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error setting cash value", e);
        }
        return dataManager.updateRecord(AbInBevObjects.PEDIDO, orderId, json);
    }

    public static boolean setOut_of_Route_Order__c(String orderId, boolean outOfRoute) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        JSONObject json = new JSONObject();
        try {
            json.put(PedidoFields.OUT_OF_ROUTE_ORDER__C, outOfRoute);
        } catch (JSONException e) {
            Log.e(TAG, "Error setting out of route order", e);
        }
        return dataManager.updateRecord(AbInBevObjects.PEDIDO, orderId, json);
    }

    public String getAccountId() {
        return getStringValueForKey(AbInBevConstants.PedidoFields.CUSTOMER);
    }

    public static boolean updateOrder(String orderId, JSONObject json) {
        DataManager dataManager = DataManagerFactory.getDataManager();
        return dataManager.updateRecord(AbInBevObjects.PEDIDO, orderId, json);
    }

    public static Order__c getOrderById(String orderId) {
        JSONObject record = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.PEDIDO,
                StdFields.ID, String.valueOf(orderId));
        Order__c order = new Order__c(record == null ? new JSONObject() : record);
        RecordType recordType = RecordType.getById(order.getRecordTypeId());
        order.setRecordTypeName(recordType != null ? recordType.getName() : "");

//        String recordTypeId = order.getRecordTypeId();
//        order.setCashPicklist(getCashPicklistMetadata(recordTypeId));
//        order.setReasonPicklist(getReasonPicklistMetadata(recordTypeId));
//        order.setOrderTypeProductTypeMapping__c(OrderType_ProductType_Mapping__c.getByOrderRecordType(order.getRecordTypeName()));
//        for (int i = 0; i < MAX_ORDERS; i++) {
//            String id = order.getStringValueForKey(PRODUCT_FIELDS[i]);
//            if (!GuavaUtils.isNullOrEmpty(id)) {
//                Material_Give__c product = Material_Give__c.getById(id);
//                if (product != null) {
//                    order.products.put(product, i);
//                }
//            }
//        }
        return order;
    }

    public static List<Order__c> getAllOrdersByAccountId(String accountId) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s') ORDER BY {%s:%s} DESC",
                AbInBevObjects.PEDIDO, PedidoFields.CUSTOMER, accountId,
                AbInBevObjects.PEDIDO, PedidoFields.CREATED_DATE);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.PEDIDO, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
        List<Order__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Order__c order = new Order__c(jsonObject);
                RecordType recordType = RecordType.getById(order.getRecordTypeId());
                order.setRecordTypeName(recordType != null ? recordType.getName() : "");
                results.add(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All Pedidos for AccountId: " + accountId, e);
        }
        return results;
    }

    public static List<Order__c> getOpenOrdersByAccountIdSince(String accountId, Date date) {
        String smartSqlFilter = String.format(
                "{%s:%s} = '%s' " +
                        "ORDER BY {%s:%s} DESC " +
                        "LIMIT 2",
                AbInBevObjects.PEDIDO, PedidoFields.CUSTOMER, accountId,
                AbInBevObjects.PEDIDO, PedidoFields.CREATED_DATE);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.PEDIDO, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<Order__c> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Order__c order = new Order__c(jsonObject);
                RecordType recordType = RecordType.getById(order.getRecordTypeId());
                order.setRecordTypeName(recordType != null ? recordType.getName() : "");
                results.add(order);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Open Pedidos for AccountId: " + accountId, e);
        }
        return results;
    }


    public static Order__c createOrder(String accountId, String recordTypeId, String source) {
        if (TextUtils.isEmpty(accountId)) {
            Log.e(TAG, "Unable to create an order without an account.");
            return null;
        }

        if (TextUtils.isEmpty(recordTypeId)) {
            Log.e(TAG, "Unable to create an order without a recordTypeId.");
            return null;
        }

        String id = UserAccountManager.getInstance().getStoredUserId();
        if (TextUtils.isEmpty(id)) {
            Log.e(TAG, "Unable to create an order without a user id.");
            return null;
        }
        DataManager dataManager = DataManagerFactory.getDataManager();
        DateHelper dateHelper = new DateHelper(DateUtils.SERVER_DATE_TIME_FORMAT);
        JSONObject json = new JSONObject();
        try {
            json.put(PedidoFields.CUSTOMER, accountId);
            json.put(PedidoFields.STATUS, PedidoStatus.STATUS_OPEN);
            json.put(PedidoFields.SOURCE, source);
            json.put(PedidoFields.RECORD_TYPE_ID, recordTypeId);
            json.put(PedidoFields.START_DATE, dateHelper.now());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new order", e);
            return null;
        }

        String tempId = dataManager.createRecord(AbInBevObjects.PEDIDO, json);
        Order__c pedido;

        try {
            pedido = getOrderById(tempId);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching pedido", e);
            return null;
        }
        return pedido;
    }


    private static List<PicklistValue> getPicklistValues(String fieldName, String recordTypeId) {

        List<PicklistValue> validPickListValues = new ArrayList<PicklistValue>();

        Map detailsMap;

        String objectType = AbInBevObjects.PEDIDO;
        Gson gson = new Gson();

        RecordTypeMapping recordTypeMapping = PicklistUtils.getRecordTypeMapping(ABInBevApp.getAppContext(), gson, objectType, recordTypeId);

        if (recordTypeMapping == null) {
            Log.v(TAG, "no recordTypeMapping available for RecordType");
            return validPickListValues;
        }

        // Let us ignore the available value for now
        Log.v(TAG, recordTypeMapping.getRecordTypeId() + ":" + recordTypeMapping.getName() + " Available: " + recordTypeMapping.getAvailable());

        IndividualLayouts individualLayouts = MetaDataProvider.getMetaDataForIndividualLayout(ABInBevApp.getAppContext(), gson, objectType, recordTypeMapping.getRecordTypeId());

        if (individualLayouts == null) {
            Log.v(TAG, "no individualLayouts available for RecordType");
            return validPickListValues;
        }
        List<EditLayoutSection> editLayoutSections = individualLayouts.getEditLayoutSections();

        if (editLayoutSections == null) {
            Log.v(TAG, "no editLayoutSections available for RecordType");
            return validPickListValues;
        }

        detailsMap = PicklistUtils.buildDetailsMap(editLayoutSections);

        Details field = (Details) detailsMap.get(ManifestUtils.getNamespaceSupportedFieldName(objectType, fieldName, ABInBevApp.getAppContext()));

        if (field != null) {
            List<PicklistValue> picklistValueList = field.getPicklistValues();

            for (PicklistValue picklistValue : picklistValueList) {
                if (picklistValue.getActive())
                    validPickListValues.add(picklistValue);
            }
        }

        return validPickListValues;

    }

    // TODO: Externalize these so that we can reuse for other queries
    private final static String SMART_SQL_FETCH_ALL_FIELD = "SELECT {%1$s:%2$s} FROM {%1$s}";
    private final static int pageSize = 1000;

    public static HashSet<String> fetchAllOrders() {
        HashSet<String> orders = new HashSet<String>();
        try {
            String query = String.format(SMART_SQL_FETCH_ALL_FIELD, AbInBevObjects.PEDIDO, StdFields.ID);
            int pageIndex = 0;
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchSmartSQLQuery(query, pageIndex, pageSize);

            while (recordsArray != null && recordsArray.length() > 0) {
                for (int i = 0; i < recordsArray.length(); i++) {
                    orders.add(recordsArray.getJSONArray(i).getString(0));
                }
                pageIndex++;
                recordsArray = DataManagerFactory.getDataManager().fetchSmartSQLQuery(query, pageIndex, pageSize);
            }
        } catch (JSONException e) {
            Log.e(TAG, "fetchAll: Error getting data for object: " + AbInBevObjects.PEDIDO, e);
        }
        Log.i(TAG, "orders hashset size: " + orders.size());
        return orders;
    }


    public static int getAllAccountOrdersCount() {
        PedidoListPresenter pedidoListPresenter = new PedidoListPresenter(true);
        pedidoListPresenter.getDefaultTimeFrame();
        String formatDate = DateUtils.CN_DATE_SHORT_FORMAT.format(pedidoListPresenter.getShowOrdersSince());
        String smartSql = String.format("select count() from {%1$s} where {%1$s:%2$s} >= '%3$s' or {%1$s:%2$s} = 'null' or {%1$s:%2$s} is null", AbInBevObjects.PEDIDO, PedidoFields.START_DATE, formatDate);
        int totalCount = DataManagerUtils.fetchInt(DataManagerFactory.getDataManager(), smartSql);
        return totalCount;
    }

    public static List<Order__c> getAllAccountOrders() {
        String smartSql = String.format("select {%1$s:_soup} from {%1$s} ORDER BY {%1$s:%2$s} DESC", AbInBevObjects.PEDIDO, PedidoFields.START_DATE);
        List<Order__c> order__cs = DataManagerUtils.fetchObjects(DataManagerFactory.getDataManager(), smartSql, Order__c.class);
        return order__cs;
    }


}
