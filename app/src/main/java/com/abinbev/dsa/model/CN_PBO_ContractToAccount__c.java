package com.abinbev.dsa.model;

import static com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects.PBO_CONTRACT_TO_ACCOUNT;

import com.abinbev.dsa.utils.AbInBevConstants.PboContractToAccountFields;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.dsa.data.model.SFBaseObject;
import org.json.JSONObject;

public class CN_PBO_ContractToAccount__c extends SFBaseObject {

    public static final String TAG = CN_PBO_ContractToAccount__c.class.getName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("ContractToAccount", PBO_CONTRACT_TO_ACCOUNT)
                .putColumn("ContractId", PboContractToAccountFields.PBO_CONTRACT)
                .putColumn("AccountId", PboContractToAccountFields.ACCOUNT)
            .build();

    public CN_PBO_ContractToAccount__c(JSONObject json) {
        super(PBO_CONTRACT_TO_ACCOUNT, json);
    }

    public CN_PBO_ContractToAccount__c() {
        super(PBO_CONTRACT_TO_ACCOUNT);
    }

    public String getContractId() {
        return getStringValueForKey(PboContractToAccountFields.PBO_CONTRACT);
    }

    public void setContractId(String contractId) {
        setStringValueForKey(PboContractToAccountFields.PBO_CONTRACT, contractId);
    }
}
