package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.DateUtils;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.syncmanifest.processors.DateHelper;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

import java.util.List;

public class CN_KPI_Statistic__c extends SFBaseObject {

    public CN_KPI_Statistic__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_KPI_Statistic__c, json);
    }

    protected CN_KPI_Statistic__c() {
        super(AbInBevConstants.AbInBevObjects.CN_KPI_Statistic__c);
    }

    public String getTotalVolume() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_TTL_Volume__c);
    }

    public String getTotalTargetVolume() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_TTL_Volume_Target__c);
    }

    public String getKeySku() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Key_SKU__c);
    }

    public String getTargetKeySku() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Key_SKU_Target__c);
    }

    public String getPocNum() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_POCE_Num__c);
    }

    public String getPocCompliance() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_POCE_Compliance__c);
    }

    public String getDistribution() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Distribution__c);
    }

    public String getTargetDistribution() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Distribution_Target__c);
    }

    public Double getCompletedVisits() {
        return getDoubleValueForKey(AbInBevConstants.KPIStatisticFields.CN_GPS_Corrects__c);
    }

    public Double getCompletedVisits2() {
        return getDoubleValueForKey(AbInBevConstants.KPIStatisticFields.Completed_Visits__c);
    }

    public Double getInPlannedVisits() {
        return getDoubleValueForKey(AbInBevConstants.KPIStatisticFields.CN_In_Planned_Visits__c);
    }

    public String getB2BVolume() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_B2B_Volume__c);
    }

    public String getTargetB2BVolume() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_B2B_Volume_Target__c);
    }

    public String getVisitCompletedRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Visit_Compliance_Rate__c);
    }

    public String getTotalRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_TTL_KPI_Rate__c);
    }

    public String getTotalVolumeRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_TTL_Volume_Rate__c);
    }

    public String geteDateValue() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Date__c);
    }

    public String geteDate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Calculate_Date__c);
    }

    public String getBonusRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Bonus_Rate__c);
    }

    public String getKeySkuRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Key_SKU_Rate__c);
    }

    public String getPoceComplianceRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_POCE_Compliance_Rate__c);
    }

    public String getDistrubtionRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Distribution_Rate__c);
    }

    public String getB2BVolumeRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_B2B_Volume_Rate__c);
    }

    public String getWeeklyVisitRate() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_WTD_Visit_Compliance_Rate__c);
    }

    public String getActualWeeklyVisit() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_WTD_Completed_Visits__c);
    }

    public String getTargetWeeklyVisit() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_WTD_In_Planned_Visits__c);
    }

    public String getThisMonthFirstDay() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_First_Day_Month__c);
    }

    public String getThisMonthEndDay() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.Last_Day_Month__c);
    }

    public String getThisWeekFirstDay() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_First_Day_Week__c);
    }

    public String getThisWeekEndtDay() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_Last_Day_Week__c);
    }

    public static List<CN_KPI_Statistic__c> getKPIList() {
        String thisMonth = new DateHelper(DateUtils.CN_DATE_SHORT_FORMAT).thisMonth().toString();
        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} < '%3$s' ORDER BY {%1$s:%2$s} DESC LIMIT 5", AbInBevConstants.AbInBevObjects.CN_KPI_Statistic__c,
                AbInBevConstants.KPIStatisticFields.CN_Date__c, thisMonth);
        String smartSqlFilter1 = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} >= '%3$s' ORDER BY {%1$s:%2$s} DESC LIMIT 1", AbInBevConstants.AbInBevObjects.CN_KPI_Statistic__c,
                AbInBevConstants.KPIStatisticFields.CN_Calculate_Date__c, thisMonth);
        List<CN_KPI_Statistic__c> cn_kpi_statistic__cs = DataManagerUtils.fetchObjects(DataManagerFactory.getDataManager(), smartSqlFilter, CN_KPI_Statistic__c.class);
        CN_KPI_Statistic__c c = DataManagerUtils.fetchObject(DataManagerFactory.getDataManager(), smartSqlFilter1, CN_KPI_Statistic__c.class);
        cn_kpi_statistic__cs.add(0, c);
        return cn_kpi_statistic__cs;
    }

    public String getYearValue() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_YearValue__c);
    }

    public String getMonthValue() {
        return getStringValueForKey(AbInBevConstants.KPIStatisticFields.CN_MonthValue__c);
    }

    public static List<CN_KPI_Statistic__c> fetchKpiForDate(String date) {
        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} = '%3$s'", AbInBevConstants.AbInBevObjects.CN_KPI_Statistic__c,
                AbInBevConstants.KPIStatisticFields.CN_Date__c, date);
        List<CN_KPI_Statistic__c> cn_kpi_statistic__cs = DataManagerUtils.fetchObjects(DataManagerFactory.getDataManager(), smartSqlFilter, CN_KPI_Statistic__c.class);
        return cn_kpi_statistic__cs;
    }
}
