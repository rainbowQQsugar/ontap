package com.abinbev.dsa.model;


import android.util.Log;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MarketProgramFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class MarketProgram extends SFBaseObject {
    public static final String TAG = MarketProgram.class.getSimpleName();

    public static final String ACTIVE_STATUS = "Active";

    public MarketProgram(JSONObject json) {
        super(AbInBevObjects.MARKET_PROGRAM, json);
    }

    protected MarketProgram() {
        super(AbInBevObjects.MARKET_PROGRAM);
    }

    public String getMarketProgram() {
        return getStringValueForKey(MarketProgramFields.MARKET_PROGRAM);
    }

    public String getStatus() {
        return getStringValueForKey(MarketProgramFields.STATUS);
    }

    public String getStartDate() {
        return getStringValueForKey(MarketProgramFields.START_DATE);
    }

    public String getEndDate() {
        return getStringValueForKey(MarketProgramFields.END_DATE);
    }

    public static int getActiveMarketProgramCountByAccountId(String accountId) {
        int count = 0;
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s' AND {%s:%s} = '%s'",
                    AbInBevObjects.MARKET_PROGRAM, MarketProgramFields.ACCOUNT, accountId,
                    AbInBevObjects.MARKET_PROGRAM, MarketProgramFields.STATUS, ACTIVE_STATUS);

            String smartSql = String.format("SELECT count() FROM {%1$s} WHERE %2$s", AbInBevObjects.MARKET_PROGRAM, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            count = recordsArray.getJSONArray(0).getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting active Market Program by Account ID: " + accountId, e);
        }

        return count;
    }

    public static List<MarketProgram> getMarketProgramsByAccountId(String accountId) {
        List<MarketProgram> marketPrograms = new ArrayList<>();
        try {
            String smartSqlFilter = String.format("{%s:%s} = '%s'",
                    AbInBevObjects.MARKET_PROGRAM, MarketProgramFields.ACCOUNT, accountId);

            String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.MARKET_PROGRAM, smartSqlFilter);
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                marketPrograms.add(new MarketProgram(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting Market Programs by Account ID: " + accountId, e);
        }

        return marketPrograms;
    }

    public static MarketProgram getById(String id) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery(AbInBevObjects.MARKET_PROGRAM, SyncEngineConstants.StdFields.ID, id);
        if (jsonObject != null) {
            return new MarketProgram(jsonObject);
        }
        return null;
    }

}
