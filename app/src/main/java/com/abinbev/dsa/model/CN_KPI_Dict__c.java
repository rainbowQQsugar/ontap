package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.datamanager.DataManagerUtils;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class CN_KPI_Dict__c extends SFBaseObject {


    public CN_KPI_Dict__c(JSONObject json) {
        super(AbInBevConstants.AbInBevObjects.CN_KPI_Dict__c, json);
    }

    protected CN_KPI_Dict__c() {
        super(AbInBevConstants.AbInBevObjects.CN_KPI_Dict__c);
    }

    public String getDesc() {
        return getStringValueForKey(AbInBevConstants.KPIDictFields.CN_Desc__c);
    }

    public String getKpiName() {
        return getStringValueForKey(AbInBevConstants.KPIDictFields.CN_KPI__c);
    }

    public static String getKpiDescription(String kpiName) {
        CN_KPI_Dict__c cn_kpi_dict__c = getCn_kpi_dict__c(AbInBevConstants.KPIDictFields.CN_Mapping_Field__c, kpiName);
        return cn_kpi_dict__c == null ? "" : cn_kpi_dict__c.getDesc();
    }

    public static String getKpiNameByExternalId(String id) {
        CN_KPI_Dict__c cn_kpi_dict__c = getCn_kpi_dict__c(AbInBevConstants.KPIDictFields.CN_External_ID__c, id);
        return cn_kpi_dict__c == null ? id : cn_kpi_dict__c.getKpiName();
    }

    private static CN_KPI_Dict__c getCn_kpi_dict__c(String field, String id) {
        String smartSqlFilter = String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE {%1$s:%2$s} = '%3$s' ",
                AbInBevConstants.AbInBevObjects.CN_KPI_Dict__c, field, id);
        return DataManagerUtils.fetchObject(DataManagerFactory.getDataManager(), smartSqlFilter, CN_KPI_Dict__c.class);
    }

    public static String getDesc1(String kpiName){
        CN_KPI_Dict__c cn_kpi_dict__c = getCn_kpi_dict__c(AbInBevConstants.KPIDictFields.CN_KPI__c, kpiName);
        return cn_kpi_dict__c == null ? "" : cn_kpi_dict__c.getDesc();
    }
}
