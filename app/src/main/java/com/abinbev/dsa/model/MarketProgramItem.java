package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MarketProgramItemFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketProgramItem extends SFBaseObject {
    public static final String TAG = MarketProgramItem.class.getSimpleName();

    //specific record types that behave specially
    public static final String LOAN_TYPE = "On Loan Materials";
    public static final String SALES_ACTUALS_TYPE = "Sales Actuals";

    //specific periods that we care about
    public static final String PERIOD_MONTH = "Month";
    public static final String PERIOD_QUARTER = "Quarter";

    public MarketProgramItem(JSONObject json) {
        super(AbInBevObjects.MARKET_PROGRAM_ITEM, json);
    }

    public MarketProgramItem() {
        super(AbInBevObjects.MARKET_PROGRAM_ITEM);
    }

    public static List<MarketProgramItem> getAllMarketProgramItemsByMarketProgramId(String marketProgramId) {
        String smartSqlFilter = String.format("{%s:%s} = ('%s') ORDER BY {%s:%s} DESC",
                AbInBevObjects.MARKET_PROGRAM_ITEM, MarketProgramItemFields.MARKET_PROGRAM, marketProgramId,
                AbInBevObjects.MARKET_PROGRAM_ITEM, MarketProgramItemFields.CREATED_DATE);

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.MARKET_PROGRAM_ITEM, smartSqlFilter);
        JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);

        List<MarketProgramItem> results = new ArrayList<>();
        try {
            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new MarketProgramItem(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting All Market Program Items for Market Program Id: " + marketProgramId, e);
        }
        return results;
    }

    public String getType() {
        return getStringValueForKey(MarketProgramItemFields.TYPE);
    }

    public String getPeriod() {
        return getStringValueForKey(MarketProgramItemFields.PERIOD);
    }

    public String getValue() {
        return getStringValueForKey(MarketProgramItemFields.VALUE);
    }

    public String getDate() {
        return getStringValueForKey(MarketProgramItemFields.DATE);
    }

    public String getDescription() {
        return getStringValueForKey(MarketProgramItemFields.DESCRIPTION);
    }

    public String getRecordType() {
        return getReferencedValueObjectField(AbInBevObjects.RECORD_TYPE, MarketProgramItemFields.RECORD_TYPE, AbInBevConstants.RecordTypeFields.NAME);
    }

}
