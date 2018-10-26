package com.abinbev.dsa.model;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.NegotiationLimitFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.data.model.SFBaseObject;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by usanaga on 5/10/16.
 */
public class Negotiation_Limit__c extends SFBaseObject {

    public Negotiation_Limit__c() {
        super(AbInBevObjects.NEGOTIATION_LIMIT);
    }

    public Negotiation_Limit__c(JSONObject jsonObject) {
        super(AbInBevObjects.NEGOTIATION_LIMIT, jsonObject);
    }

    public int getLimitHigh() {
        return getIntValueForKey(NegotiationLimitFields.LIMIT_HIGH);
    }

    public int getLimitLow() {
        return getIntValueForKey(NegotiationLimitFields.LIMIT_LOW);
    }

    public static Negotiation_Limit__c fetchNegotiationLimitForAccount(Account account) {
        List<Negotiation_Limit__c> limits = new ArrayList<>();


        try {

            String segment = account.getSegmentC();
            if (TextUtils.isEmpty(segment)) segment = null;

            String accountVolume = account.getVolumeCurrentC();
            if (TextUtils.isEmpty(accountVolume)) accountVolume = "0";

            String filter = String.format("{%1$s:%2$s} = '%3$s' AND {%1$s:%4$s} = '%5$s' AND {%1$s:%6$s} = '%7$s' AND {%1$s:%8$s} >= %9$s AND {%1$s:%10$s} <= %11$s",
                    AbInBevObjects.NEGOTIATION_LIMIT, NegotiationLimitFields.BUSINESS_UNIT, account.getBusinessUnit(),
                    NegotiationLimitFields.CLASSIFICATION, account.getClassification(),
                    NegotiationLimitFields.SEGMENT, segment,
                    NegotiationLimitFields.VOLUME_HIGH, accountVolume,
                    NegotiationLimitFields.VOLUME_LOW, accountVolume);
            String smartSQL = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.NEGOTIATION_LIMIT, filter);

            Log.e("Babu", "smartSQL : " + smartSQL);
            
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSQL);

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                Negotiation_Limit__c negotiationItem = new Negotiation_Limit__c(jsonObject);
                limits.add(negotiationItem);
            }

        } catch (Exception e) {
            Log.e("Negotiation_Limit__c", "fetchNegotiationLimitsForAccount: Error fetching negotiation limits", e);
        }

        if (limits.size() > 0) {
            Log.e("Babu", "got limit: " + limits.get(0).toJson().toString());
            return limits.get(0);
        } else {
            Log.e("Babu", "no limit found");
            return null;
        }
    }
}
