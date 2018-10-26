package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.PboContractItemFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects.PBO_CONTRACT_ITEM;

public class CN_PBO_Contract_Item__c extends SFBaseObject {

    public static final String TAG = CN_PBO_Contract_Item__c.class.getName();

    private String productName;

    public CN_PBO_Contract_Item__c(JSONObject json) {
        super(PBO_CONTRACT_ITEM, json);
    }

    public CN_PBO_Contract_Item__c() {
        super(PBO_CONTRACT_ITEM);
    }

    public int getActual() {
        return getIntValueForKey(PboContractItemFields.ACTUAL);
    }

    public int getTarget() {
        return getIntValueForKey(PboContractItemFields.TARGET);
    }

    public String getYearMonth() {
        return getStringValueForKey(PboContractItemFields.YEAR_MONTH);
    }

    public String getProductId() {
        return getStringValueForKey(PboContractItemFields.PRODUCT);
    }

    public String getSkuId() {
        return getStringValueForKey(PboContractItemFields.SKU_ID);
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public static List<CN_PBO_Contract_Item__c> getByContract(String contractId) {
        DataManager dm = DataManagerFactory.getDataManager();

        FormatValues values = new FormatValues();
        values.putTable("ContractItem",             PBO_CONTRACT_ITEM);
        values.putColumn("ContractItem:_soup",      PBO_CONTRACT_ITEM, PboContractItemFields.SOUP);
        values.putColumn("ContractItem:ContractId", PBO_CONTRACT_ITEM, PboContractItemFields.PBO_CONTRACT);
        values.putColumn("ContractItem:YearMonth",  PBO_CONTRACT_ITEM, PboContractItemFields.YEAR_MONTH);

        values.putValue("contractId", contractId);

        String sql =
                "SELECT {ContractItem:_soup} FROM {ContractItem} WHERE " +
                    "{ContractItem:ContractId} = '{contractId}' " +
                "ORDER BY {ContractItem:YearMonth} DESC";

        return DataManagerUtils.fetchObjects(dm, sql, values, CN_PBO_Contract_Item__c.class);
    }

    public static List<CN_PBO_Contract_Item__c> getFromCurrentQuarter(String contractId) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int quarter = (calendar.get(Calendar.MONTH) / 3); // Quarter of a year from 0 to 3.
        int startMonth = (quarter * 3) + 1;
        int endMonth = startMonth + 2;

        String startDate = String.valueOf((year * 100) + startMonth);
        String endDate = String.valueOf((year * 100) + endMonth);

        return getFromDates(contractId, startDate, endDate);
    }

    private static List<CN_PBO_Contract_Item__c> getFromDates(String contractId, String startDate, String endDate) {
        DataManager dm = DataManagerFactory.getDataManager();

        FormatValues values = new FormatValues();
        values.putTable("ContractItem",             PBO_CONTRACT_ITEM);
        values.putColumn("ContractItem:_soup",      PBO_CONTRACT_ITEM, PboContractItemFields.SOUP);
        values.putColumn("ContractItem:ContractId", PBO_CONTRACT_ITEM, PboContractItemFields.PBO_CONTRACT);
        values.putColumn("ContractItem:YearMonth",  PBO_CONTRACT_ITEM, PboContractItemFields.YEAR_MONTH);

        values.putValue("contractId", contractId);
        values.putValue("startDate", startDate);
        values.putValue("endDate", endDate);

        String sql = "SELECT {ContractItem:_soup} FROM {ContractItem} WHERE " +
                "{ContractItem:ContractId} = '{contractId}' AND " +
                "{ContractItem:YearMonth} >= '{startDate}' AND " +
                "{ContractItem:YearMonth} <= '{endDate}'";

        return DataManagerUtils.fetchObjects(dm, sql, values, CN_PBO_Contract_Item__c.class);
    }
}
