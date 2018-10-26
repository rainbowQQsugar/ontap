package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ChSprFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.CONTACT_PHONE;
import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.NAME;
import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.TEMPORARY_PROMO;
import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.WORKING_HOUR_FROM;
import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.WORKING_HOUR_TO;
import static com.abinbev.dsa.utils.AbInBevConstants.ChSprFields.WORK_DATE;

/**
 * Created by Adam Chodera on 7.07.2017.
 */

public class CN_SPR__c extends SFBaseObject {

    private static final String TAG = CN_SPR__c.class.getSimpleName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues.Builder()
            .putTable("CN_SPR", AbInBevObjects.CN_SPR)
                .putColumn("AccountId", ChSprFields.ACCOUNT_ID)
                .putColumn("WorkDate", ChSprFields.WORK_DATE)
            .build();

    public CN_SPR__c(JSONObject json) {
        super(AbInBevObjects.CN_SPR, json);
    }

    public String getName() {
        return getStringValueForKey(NAME);
    }

    public String getWorkingFrom() {
        return getStringValueForKey(WORKING_HOUR_FROM);
    }

    public String getWorkingTo() {
        return getStringValueForKey(WORKING_HOUR_TO);
    }

    public String getWorkDate() {
        return getStringValueForKey(WORK_DATE);
    }

    public String getContactPhone() {
        return getStringValueForKey(CONTACT_PHONE);
    }

    public boolean isTemporaryPromo() {
        return getBooleanValueForKey(TEMPORARY_PROMO);
    }

    public static List<CN_SPR__c> getForCurrentWeek(String accountId) {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; //Monday as 0

        calendar.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
        String weekStart = DateUtils.dateToDateString(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = DateUtils.dateToDateString(calendar.getTime());

        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("accountId", accountId);
        fv.putValue("weekStart", weekStart);
        fv.putValue("weekEnd", weekEnd);

        String sql = "SELECT {CN_SPR:_soup} FROM {CN_SPR} " +
                "WHERE {CN_SPR:AccountId} = '{accountId}' " +
                    "AND {CN_SPR:WorkDate} >= '{weekStart}' " +
                    "AND {CN_SPR:WorkDate} <= '{weekEnd}' " +
                "ORDER BY {CN_SPR:WorkDate}";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, sql, fv, CN_SPR__c.class);
    }

    public static int getCountForCurrentWeek(final String accountId) {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; //Monday as 0

        calendar.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
        String weekStart = DateUtils.dateToDateString(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String weekEnd = DateUtils.dateToDateString(calendar.getTime());

        FormatValues fv = new FormatValues();
        fv.addAll(OBJECT_FORMAT_VALUES);
        fv.putValue("accountId", accountId);
        fv.putValue("weekStart", weekStart);
        fv.putValue("weekEnd", weekEnd);

        String sql = "SELECT count() FROM {CN_SPR} " +
                "WHERE {CN_SPR:AccountId} = '{accountId}' " +
                    "AND {CN_SPR:WorkDate} >= '{weekStart}' " +
                    "AND {CN_SPR:WorkDate} <= '{weekEnd}'";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchInt(dm, sql, fv);
    }
}
