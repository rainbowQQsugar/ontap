package com.abinbev.dsa.model;

import android.util.Log;

import static com.abinbev.dsa.utils.DateUtils.SERVER_DATE_FORMAT;
import static com.abinbev.dsa.utils.DateUtils.SERVER_TIME_ZONE;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.KpiCategories;
import com.abinbev.dsa.utils.AbInBevConstants.KpiFields;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.abinbev.dsa.utils.datamanager.FormatValues;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.dsa.utils.DSAConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class KPI__c extends TranslatableSFBaseObject {

    public static final String TAG = KPI__c.class.getName();

    public static final FormatValues OBJECT_FORMAT_VALUES = new FormatValues()
            .putTable("KPI", AbInBevObjects.KPI)
            .putColumn("KPI:_soup", AbInBevObjects.KPI, KpiFields.SOUP)
            .putColumn("KPI:KpiNum", AbInBevObjects.KPI, KpiFields.KPI_NUM)
            .putColumn("KPI:Category", AbInBevObjects.KPI, KpiFields.CATEGORY)
            .putColumn("KPI:AccountId", AbInBevObjects.KPI, KpiFields.ACCOUNT_ID)
            .putColumn("KPI:UserId", AbInBevObjects.KPI, KpiFields.USER_ID)
            .putColumn("KPI:EndDate", AbInBevObjects.KPI, KpiFields.END_DATE);

    public KPI__c(JSONObject json) {
        super(AbInBevObjects.KPI, json);
    }

    public KPI__c() {
        super(AbInBevObjects.KPI);
    }

    public static List<KPI__c> getVolumeForUser(String userId, Date date, String parent) {
        return getKpisForUser(KpiCategories.VOLUME, userId, date, parent);
    }

    public static List<KPI__c> getVolumeForAccount(String accountId, Date date, String parent) {
        return getKpisForAccount(KpiCategories.VOLUME, accountId, date, parent);
    }

    public static List<KPI__c> getCoverageForAccount(String accountId, Date date, String parent) {
        return getKpisForAccount(KpiCategories.COVERAGE, accountId, date, parent);
    }

    private static List<KPI__c> getKpisForUser(String category, String userId, Date date, String parent) {
        String dateString = DateUtils.dateToDateString(date);
        String smartSqlFilter = String.format("{%s:%s} = '%s' " +
                        "AND {%s:%s} = '%s' " +
                        "AND {%s:%s} <= '%s' " +
                        "AND {%s:%s} > '%s'",
                AbInBevObjects.KPI, KpiFields.CATEGORY, category,
                AbInBevObjects.KPI, KpiFields.USER_ID, userId,
                AbInBevObjects.KPI, KpiFields.START_DATE, dateString,
                AbInBevObjects.KPI, KpiFields.END_DATE, dateString);

        if (parent == null) {
            smartSqlFilter = smartSqlFilter.concat(String.format(
                    " AND ({%1$s:%2$s} = '' OR {%1$s:%2$s} = 'null' OR {%1$s:%2$s} IS NULL)", AbInBevObjects.KPI, KpiFields.KPI_PARENT));
        } else {
            smartSqlFilter = smartSqlFilter.concat(String.format(
                    " AND {%s:%s} = '%s'", AbInBevObjects.KPI, KpiFields.KPI_PARENT, parent));
        }

        smartSqlFilter = smartSqlFilter.concat(String.format(
                " ORDER BY {%s:%s} ASC", AbInBevObjects.KPI, KpiFields.END_DATE));

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.KPI, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, KPI__c.class);
    }

    public static List<KPI__c> getParentKpisForUserAndMonths(String userId, int months) {
        DateHelper dateHelper = new DateHelper(SERVER_DATE_FORMAT, SERVER_TIME_ZONE);

        final String monthBeginning = dateHelper.thisMonth().addMonths(months).toString();
        final String monthEnd = dateHelper.thisMonthEnd().toString();

        String smartSqlFilter = getSmartSqlFilterForUserAndDateRange(userId, monthBeginning, monthEnd);

        smartSqlFilter = smartSqlFilter.concat(String.format(
                " AND ({%1$s:%2$s} = '' OR {%1$s:%2$s} = 'null' OR {%1$s:%2$s} IS NULL)",
                AbInBevObjects.KPI,
                KpiFields.KPI_PARENT));

        smartSqlFilter = smartSqlFilter.concat(String.format(
                " ORDER BY {%s:%s} ASC", AbInBevObjects.KPI, KpiFields.END_DATE));

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.KPI, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, KPI__c.class);
    }

    public static List<List<KPI__c>> getLastSixMonthsPerformanceKpi() {
        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} = '%3$s'",
                AbInBevObjects.KPI, KpiFields.CATEGORY, "Performance KPI");
        DataManager dm = DataManagerFactory.getDataManager();
        List<KPI__c> kpi__cs = DataManagerUtils.fetchObjects(dm, smartSqlFilter, KPI__c.class);
        HashMap<String, List<KPI__c>> map = new HashMap<>();
        List<List<KPI__c>> lists = new ArrayList<List<KPI__c>>();
        for (KPI__c kpi__c : kpi__cs) {
            if (map.containsKey(kpi__c.getKpiNum())) map.get(kpi__c.getKpiNum()).add(kpi__c);
            else {
                List<KPI__c> list = new ArrayList<KPI__c>();
                list.add(kpi__c);
                map.put(kpi__c.getKpiNum(), list);
            }
        }
        for (Map.Entry<String, List<KPI__c>> entry : map.entrySet()) {
            lists.add(entry.getValue());
        }
        Collections.sort(lists, new Comparator<List<KPI__c>>() {
            public int compare(List<KPI__c> p1, List<KPI__c> p2) {
                if (Integer.valueOf(p1.get(0).getKpiNum()) < Integer.valueOf(p2.get(0).getKpiNum())) {
                    return 1;
                }
                if (Integer.valueOf(p1.get(0).getKpiNum()) == Integer.valueOf(p2.get(0).getKpiNum())) {
                    return 0;
                }
                return -1;
            }
        });
        return lists;
    }

    public static List<List<KPI__c>> getLastSixMonthsDataForPieChart() {
        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} = '%3$s'",
                AbInBevObjects.KPI, KpiFields.CATEGORY, "Performance KPI Total");
        DataManager dm = DataManagerFactory.getDataManager();
        List<KPI__c> kpi__cs = DataManagerUtils.fetchObjects(dm, smartSqlFilter, KPI__c.class);
        HashMap<String, List<KPI__c>> map = new HashMap<>();
        List<List<KPI__c>> lists = new ArrayList<List<KPI__c>>();
        for (KPI__c kpi__c : kpi__cs) {
            if (map.containsKey(kpi__c.getKpiNum())) map.get(kpi__c.getKpiNum()).add(kpi__c);
            else {
                List<KPI__c> list = new ArrayList<KPI__c>();
                list.add(kpi__c);
                map.put(kpi__c.getKpiNum(), list);
            }
        }
        for (Map.Entry<String, List<KPI__c>> entry : map.entrySet()) {
            lists.add(entry.getValue());
        }
        Collections.sort(lists, new Comparator<List<KPI__c>>() {
            public int compare(List<KPI__c> p1, List<KPI__c> p2) {
                if (Integer.valueOf(p1.get(0).getKpiNum()) < Integer.valueOf(p2.get(0).getKpiNum())) {
                    return 1;
                }
                if (Integer.valueOf(p1.get(0).getKpiNum()) == Integer.valueOf(p2.get(0).getKpiNum())) {
                    return 0;
                }
                return -1;
            }
        });
        return lists;
    }

    public static List<KPI__c> getChildKpisForUserAndPreviousMonth(String userId, String parent) {
        DateHelper dateHelper = new DateHelper(SERVER_DATE_FORMAT, SERVER_TIME_ZONE);
        final String monthBeginning = dateHelper.thisMonth().addMonths(-1).toString();
        final String monthEnd = dateHelper.thisMonthEnd().addMonths(-1).toString();

        final String smartSqlFilter = getSmartSqlFilterForUserAndDateRange(userId, monthBeginning, monthEnd);

        return getChildKpis(parent, smartSqlFilter);
    }

    public static List<KPI__c> getChildKpisForUserAndCurrentMonth(String userId, String parent) {
        DateHelper dateHelper = new DateHelper(SERVER_DATE_FORMAT, SERVER_TIME_ZONE);
        final String monthBeginning = dateHelper.thisMonth().toString();
        final String monthEnd = dateHelper.thisMonthEnd().toString();

        final String smartSqlFilter = getSmartSqlFilterForUserAndDateRange(userId, monthBeginning, monthEnd);

        return getChildKpis(parent, smartSqlFilter);
    }

    private static List<KPI__c> getChildKpis(String parent, String smartSqlFilter) {
        smartSqlFilter = smartSqlFilter.concat(String.format(" AND {%s:%s} = '%s'", AbInBevObjects.KPI, KpiFields.KPI_PARENT, parent));

        smartSqlFilter = smartSqlFilter.concat(String.format(
                " ORDER BY {%s:%s} ASC, {%s:%s} ASC",
                AbInBevObjects.KPI, KpiFields.START_DATE,
                AbInBevObjects.KPI, KpiFields.END_DATE));

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.KPI, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, KPI__c.class);
    }

    private static String getSmartSqlFilterForUserAndDateRange(final String userId, String startDate, String endDate) {
        return String.format(
                "{%s:%s} = '%s' " +
                        "AND {%s:%s} >= '%s' " +
                        "AND {%s:%s} <= '%s'",
                AbInBevObjects.KPI, KpiFields.USER_ID, userId,
                AbInBevObjects.KPI, KpiFields.START_DATE, startDate,
                AbInBevObjects.KPI, KpiFields.END_DATE, endDate
        );
    }

    private static List<KPI__c> getKpisForAccount(String category, String accountId, Date date, String parent) {
        String dateString = DateUtils.dateToDateString(date);
        String smartSqlFilter = String.format("{%s:%s} = '%s' " +
                        "AND {%s:%s} = '%s' " +
                        "AND {%s:%s} <= '%s' " +
                        "AND {%s:%s} > '%s'",
                AbInBevObjects.KPI, KpiFields.CATEGORY, category,
                AbInBevObjects.KPI, KpiFields.ACCOUNT_ID, accountId,
                AbInBevObjects.KPI, KpiFields.START_DATE, dateString,
                AbInBevObjects.KPI, KpiFields.END_DATE, dateString);

        if (parent == null) {
            smartSqlFilter = smartSqlFilter.concat(String.format(
                    " AND ({%1$s:%2$s} = '' OR {%1$s:%2$s} = 'null' OR {%1$s:%2$s} IS NULL)", AbInBevObjects.KPI, KpiFields.KPI_PARENT));
        } else {
            smartSqlFilter = smartSqlFilter.concat(String.format(
                    " AND {%s:%s} = %s", AbInBevObjects.KPI, KpiFields.KPI_PARENT, parent));
        }

        smartSqlFilter = smartSqlFilter.concat(String.format(
                " ORDER BY {%s:%s} ASC", AbInBevObjects.KPI, KpiFields.END_DATE));

        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.KPI, smartSqlFilter);

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, smartSql, KPI__c.class);
    }

    public static List<KPI__c> getAccountVolumesSince(String accountId, Date date, String kpiId) {
        return getAccountKpisSince(KpiCategories.VOLUME, kpiId, accountId, date);
    }

    private static List<KPI__c> getAccountKpisSince(String category, String kpiId, String accountId, Date date) {
        FormatValues fv = new FormatValues()
                .putValue("kpiId", kpiId)
                .putValue("category", category)
                .putValue("accountId", accountId)
                .putValue("date", DateUtils.dateToDateString(date))
                .addAll(OBJECT_FORMAT_VALUES);

        String sql = "SELECT {KPI:_soup} FROM {KPI} " +
                "WHERE {KPI:KpiNum} = '{kpiId}' " +
                "AND {KPI:Category} = '{category}' " +
                "AND {KPI:AccountId} = '{accountId}' " +
                "AND {KPI:EndDate} > '{date}' " +
                "ORDER BY {KPI:EndDate} ASC";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, sql, fv, KPI__c.class);
    }

    public static List<KPI__c> getUserVolumesSince(String userId, Date date, String kpiId) {
        return getUserKpisSince(KpiCategories.VOLUME, kpiId, userId, date);
    }

    private static List<KPI__c> getUserKpisSince(String category, String kpiId, String userId, Date date) {
        FormatValues fv = new FormatValues()
                .putValue("kpiId", kpiId)
                .putValue("category", category)
                .putValue("userId", userId)
                .putValue("date", DateUtils.dateToDateString(date))
                .addAll(OBJECT_FORMAT_VALUES);

        String sql = "SELECT {KPI:_soup} FROM {KPI} " +
                "WHERE {KPI:KpiNum} = '{kpiId}' " +
                "AND {KPI:Category} = '{category}' " +
                "AND {KPI:UserId} = '{userId}' " +
                "AND {KPI:EndDate} > '{date}' " +
                "ORDER BY {KPI:EndDate} ASC";

        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.fetchObjects(dm, sql, fv, KPI__c.class);
    }

    public static KPI__c getById(String kpiId) {
        DataManager dm = DataManagerFactory.getDataManager();
        return DataManagerUtils.getById(dm, kpiId, KPI__c.class);
    }

    public String getAccountId() {
        return getStringValueForKey(KpiFields.ACCOUNT_ID);
    }

    public void setAccountId(String accountId) {
        setStringValueForKey(KpiFields.ACCOUNT_ID, accountId);
    }

    /**
     * This is KPI specific Id. This is not the same as a soup Id.
     */
    public String getKpiNum() {
        return getStringValueForKey(KpiFields.KPI_NUM);
    }

    /**
     * This is KPI specific Id. This is not the same as a soup Id.
     */
    public void setKpiNum(String kpiNum) {
        setStringValueForKey(KpiFields.KPI_NUM, kpiNum);
    }

    public double getTarget() {
        double value = getDoubleValueForKey(KpiFields.TARGET);
        return Double.isNaN(value) ? 0 : value;
    }

    public double getActual() {
        double value = getDoubleValueForKey(KpiFields.ACTUAL);
        return Double.isNaN(value) ? 0 : value;
    }


    public double getPercentageIncrease() {
        double percent = getDoubleValueForKey(KpiFields.PERCENTAGE_INCREASE);
        if (Double.isNaN(percent)) {
            percent = 0;
        }
        return percent;
    }

    public void setPercentageIncrease(double percent) {
        setDoubleValueForKey(KpiFields.PERCENTAGE_INCREASE, percent);
    }

    public String getUnit() {
        return getStringValueForKey(KpiFields.UNIT);
    }

    public String getTranslatedUnit() {
        return getTranslatedStringValueForKey(KpiFields.UNIT);
    }

    public void setUnit(String unit) {
        setStringValueForKey(KpiFields.UNIT, unit);
    }

    public String getCategory() {
        return getStringValueForKey(KpiFields.CATEGORY);
    }

    public String getTranslatedCategory() {
        return getTranslatedStringValueForKey(KpiFields.CATEGORY);
    }

    public void setCategory(String category) {
        setStringValueForKey(KpiFields.CATEGORY, category);
    }

    public String getStartDate() {
        return getStringValueForKey(KpiFields.START_DATE);
    }

    public void setStartDate(String startDate) {
        setStringValueForKey(KpiFields.START_DATE, startDate);
    }

    public String getEndDate() {
        return getStringValueForKey(KpiFields.END_DATE);
    }

    public void setEndDate(String endDate) {
        setStringValueForKey(KpiFields.END_DATE, endDate);
    }

    public String getKpiName() {
        return getStringValueForKey(KpiFields.KPI_NAME);
    }

    public void setKpiName(String kpiName) {
        setStringValueForKey(KpiFields.KPI_NAME, kpiName);
    }

    public int getDaysPassed() {
        return getIntValueForKey(KpiFields.DAYS_PASSED);
    }

    public void setDaysPassed(int daysPassed) {
        setIntValueForKey(KpiFields.DAYS_PASSED, daysPassed);
    }

    public int getTotalDays() {
        return getIntValueForKey(KpiFields.TOTAL_DAYS);
    }

    public String getPercentCompleted() {
        return getStringValueForKey(KpiFields.Percent_Completed);
    }

    public void setTotalDays(int totalDays) {
        setIntValueForKey(KpiFields.TOTAL_DAYS, totalDays);
    }
}
