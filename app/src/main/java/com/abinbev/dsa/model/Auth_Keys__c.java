package com.abinbev.dsa.model;

import com.abinbev.dsa.utils.AbInBevConstants;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;

import org.json.JSONObject;

public class Auth_Keys__c extends SFBaseObject {
    public static final String TAG = Auth_Keys__c.class.getName();

    //AccountName
    //AccountKey
    //Protocol
    //EndpointSuffix

    public Auth_Keys__c() {
        super("Auth_Keys__c");
    }

    public Auth_Keys__c(JSONObject jsonObject) {
        super("Auth_Keys__c", jsonObject);
    }

    public static String getStorageConnectionString(String name) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery("Auth_Keys__c", "Name", name);
        if (jsonObject != null) {
            String protocol = jsonObject.optString("Protocol__c", null);
            String accountName = jsonObject.optString("AccountName__c", null);
            String accountKey = jsonObject.optString("AccountKey__c", null);
            String endpointSuffix = jsonObject.optString("EndpointSuffix__c", null);
            String connectionString = String.format("DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s;EndpointSuffix=%s;",
                    protocol, accountName, accountKey, endpointSuffix);
            return connectionString;
        } else {
            return null;
        }

    }

    public static String getB2BHtmlUrl(String accessToken, String memberId) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery("Auth_Keys__c", "Name", "CN_B2B");
        if (jsonObject == null) return null;
        String protocol = jsonObject.optString(AbInBevConstants.AuthKeysFielsd.Protocol__c, null);
        String EndpointSuffix__c = jsonObject.optString(AbInBevConstants.AuthKeysFielsd.EndpointSuffix__c, null);
        String suffix = getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.CN_ChinaBackend_Post_Visit_API_URI__c);
        String baseUrl = protocol + "://" + EndpointSuffix__c + suffix + "?redirect=order-create.html&member_id=" + memberId + "&access_token=" + accessToken + "&req_source=SFDC&native=1";
        return baseUrl;
    }

    public static String getAppointFieldValue(String appointField) {
        JSONObject jsonObject = DataManagerFactory.getDataManager().exactQuery("Auth_Keys__c", "Name", "CN_B2B");
        if (jsonObject == null) return null;
        switch (appointField) {
            case AbInBevConstants.AuthKeysFielsd.Protocol__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.Protocol__c, null);
            case AbInBevConstants.AuthKeysFielsd.EndpointSuffix__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.EndpointSuffix__c, null);
            case AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_API_URI__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_API_URI__c, null);
            case AbInBevConstants.AuthKeysFielsd.AccountKey__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.AccountKey__c, null);
            case AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_Method_Name__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_Method_Name__c, null);
            case AbInBevConstants.AuthKeysFielsd.CN_ChinaBackend_Post_Visit_API_URI__c:
                return jsonObject.optString(AbInBevConstants.AuthKeysFielsd.CN_ChinaBackend_Post_Visit_API_URI__c, null);
            default:
                return "";
        }
    }

    public static String getAcessTokenUrl() {
        return getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.Protocol__c) + "://" + getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.EndpointSuffix__c)
                + getAppointFieldValue(AbInBevConstants.AuthKeysFielsd.ChinaBackend_Auth_API_URI__c);
    }

}
