package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.MarketProgramItemFields;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.List;

public class TradeProgramItem extends SFBaseObject {
    public static final String TAG = TradeProgramItem.class.getSimpleName();

    public static FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("MarketProgramItem", AbInBevObjects.MARKET_PROGRAM_ITEM)
                .putColumn("ItemId", MarketProgramItemFields.CN_ITEM_ID)
                .putColumn("MarketProgramId", MarketProgramItemFields.MARKET_PROGRAM)
                .putColumn("CreatedDate", MarketProgramItemFields.CREATED_DATE)
            .build();

    public TradeProgramItem(JSONObject json) {
        super(AbInBevObjects.MARKET_PROGRAM_ITEM, json);
    }

    public TradeProgramItem() {
        super(AbInBevObjects.MARKET_PROGRAM_ITEM);
    }

    public static List<TradeProgramItem> getByMarketProgram(String marketProgramId) {
        FormatValues fv = new FormatValues.Builder()
                .addAll(OBJECT_FORMAT_VALUES)
                .putValue("marketProgramId", marketProgramId)
                .build();

        String query = "SELECT {MarketProgramItem:_soup} FROM {MarketProgramItem} " +
                "WHERE {MarketProgramItem:MarketProgramId} = '{marketProgramId}' " +
                "ORDER BY {MarketProgramItem:CreatedDate} DESC";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, query, fv, TradeProgramItem.class);
    }

    public String getDate() {
        return getStringValueForKey(MarketProgramItemFields.DATE);
    }

    public String getDescriptionOrder() {
        return getStringValueForKey(MarketProgramItemFields.CN_DESCRIPTION_ORDER);
    }

    public String getDescription() {
        return getStringValueForKey(MarketProgramItemFields.DESCRIPTION);
    }

    public String getItemId() {
        return getStringValueForKey(MarketProgramItemFields.CN_ITEM_ID);
    }

    public String getName() {
        return getStringValueForKey(MarketProgramItemFields.CN_ITEM_NAME);
    }

    public String getProgramItemId() {
        return getStringValueForKey(MarketProgramItemFields.CN_PROGRAM_ITEM_ID);
    }

}
