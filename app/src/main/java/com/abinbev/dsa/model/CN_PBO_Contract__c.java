package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.PboContractFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects.PBO_CONTRACT;

public class CN_PBO_Contract__c extends TranslatableSFBaseObject {

    public static final String TAG = CN_PBO_Contract__c.class.getName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("Contract", PBO_CONTRACT)
                .putColumn("Id", PboContractFields.ID)
                .putColumn("Status", PboContractFields.STATUS)
                .putColumn("Start", PboContractFields.START_MONTH)
                .putColumn("End", PboContractFields.END_MONTH)
            .build();

    /*******This field is added to the identity expiration time*****/
    public void setDateDue(String key, String date) {

        setStringValueForKey(key, date);
    }

    public String getDateDue(String key) {

        return getStringValueForKey(key);
    }

    /******This field is added to the identity expiration time******/

    public CN_PBO_Contract__c(JSONObject json) {
        super(PBO_CONTRACT, json);
    }

    public CN_PBO_Contract__c() {
        super(PBO_CONTRACT);
    }

    public String getStartDateString() {
        return getStringValueForKey(PboContractFields.START_MONTH);
    }

    public String getEndDateString() {
        return getStringValueForKey(PboContractFields.END_MONTH);
    }

    public Date getStartDate() {
        return DateUtils.dateFromString(getStartDateString());
    }

    public Date getEndDate() {
        return DateUtils.dateFromString(getEndDateString());
    }

    public String getSalesManager() {
        return getStringValueForKey(PboContractFields.SALES_MANAGER);
    }

    public String getContractId() {
        return getStringValueForKey(PboContractFields.CONTRACT_ID);
    }

    public String getStatus() {
        return getStringValueForKey(PboContractFields.STATUS);
    }

    public String getTranslatedStatus() {
        return getTranslatedStringValueForKey(PboContractFields.STATUS);
    }


    public static CN_PBO_Contract__c getById(String id) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, id, CN_PBO_Contract__c.class);
    }

    public static List<CN_PBO_Contract__c> getCurrentContracts(String accountId) {
        DataManager dm = DataManagerFactory.getDataManager();

        FormatValues values = new FormatValues();
        values.addAll(OBJECT_FORMAT_VALUES);
        values.addAll(CN_PBO_ContractToAccount__c.OBJECT_FORMAT_VALUES);

        values.putValue("accountId", accountId);
        values.putValue("statusActive", "Active");
        values.putValue("status签署确认", "签署确认");
        values.putValue("currentDate", DateUtils.currentDateString());

        String sql =
                "SELECT {Contract:_soup} FROM {Contract} WHERE " +
                        "({Contract:Status} = '{statusActive}' OR {Contract:Status} = '{status签署确认}') AND " +
                        "{Contract:Start} <= '{currentDate}' AND " +
                        "{Contract:End} >= '{currentDate}' AND " +
                    "{Contract:Id} IN " +
                        "(SELECT {ContractToAccount:ContractId} FROM {ContractToAccount} WHERE {ContractToAccount:AccountId} = '{accountId}') " +
                "ORDER BY {Contract:End} ASC";

        return DataManagerUtils.fetchObjects(dm, sql, values, CN_PBO_Contract__c.class);
    }

    public static int getCurrentContractsCount(String accountId) {
        DataManager dm = DataManagerFactory.getDataManager();

        FormatValues values = new FormatValues();
        values.addAll(OBJECT_FORMAT_VALUES);
        values.addAll(CN_PBO_ContractToAccount__c.OBJECT_FORMAT_VALUES);

        values.putValue("accountId", accountId);
        values.putValue("statusActive", "Active");
        values.putValue("status签署确认", "签署确认");
        values.putValue("currentDate", DateUtils.currentDateString());

        String sql =
                "SELECT count() FROM {Contract} WHERE " +
                        "({Contract:Status} = '{statusActive}' OR {Contract:Status} = '{status签署确认}') AND " +
                        "{Contract:Start} <= '{currentDate}' AND " +
                        "{Contract:End} >= '{currentDate}' AND " +
                    "{Contract:Id} IN " +
                        "(SELECT {ContractToAccount:ContractId} FROM {ContractToAccount} WHERE {ContractToAccount:AccountId} = '{accountId}')";

        return DataManagerUtils.fetchInt(dm, sql, values);
    }
}
