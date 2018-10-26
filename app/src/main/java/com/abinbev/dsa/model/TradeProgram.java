package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MarketProgramFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.List;

public class TradeProgram extends SFBaseObject {

    public static final String TAG = TradeProgram.class.getSimpleName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("MarketProgram", AbInBevObjects.MARKET_PROGRAM)
                .putColumn("AccountId", MarketProgramFields.ACCOUNT)
                .putColumn("ContractId", MarketProgramFields.CN_CONTRACT_ID)
            .build();

    protected TradeProgram() {
        super(AbInBevObjects.MARKET_PROGRAM);
    }

    public TradeProgram(JSONObject json) {
        super(AbInBevObjects.MARKET_PROGRAM, json);
    }

    public static int getTradeProgramCount(String accountId) {
        FormatValues fv = new FormatValues.Builder()
                .addAll(OBJECT_FORMAT_VALUES)
                .putValue("accountId", accountId)
                .build();

        String query = "SELECT count() FROM {MarketProgram} " +
                "WHERE {MarketProgram:AccountId} = '{accountId}' " +
                    "AND {MarketProgram:ContractId} != '' " +
                    "AND {MarketProgram:ContractId} != 'null' " +
                    "AND {MarketProgram:ContractId} IS NOT NULL";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, query, fv);
    }

    public static List<TradeProgram> getAllByAccountId(final String accountId) {
        FormatValues fv = new FormatValues.Builder()
                .addAll(OBJECT_FORMAT_VALUES)
                .putValue("accountId", accountId)
                .build();

        String query = "SELECT {MarketProgram:_soup} FROM {MarketProgram} " +
                "WHERE {MarketProgram:AccountId} = '{accountId}' " +
                    "AND {MarketProgram:ContractId} != '' " +
                    "AND {MarketProgram:ContractId} != 'null' " +
                    "AND {MarketProgram:ContractId} IS NOT NULL";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, query, fv, TradeProgram.class);
    }

    public String getContractId() {
        return getStringValueForKey(MarketProgramFields.CN_CONTRACT_ID);
    }

    public String getName() {
        return getStringValueForKey(MarketProgramFields.CN_TP_NAME);
    }

    public String getValidFrom() {
        return getStringValueForKey(MarketProgramFields.START_DATE);
    }

    public String getValidTo() {
        return getStringValueForKey(MarketProgramFields.END_DATE);
    }

    public String getStatus() {
        return getStringValueForKey(MarketProgramFields.STATUS);
    }
}
