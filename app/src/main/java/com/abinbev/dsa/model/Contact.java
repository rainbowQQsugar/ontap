package com.abinbev.dsa.model;

import android.util.Log;

import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.ContactFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.dsa.utils.DSAConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.salesforce.dsa.utils.DSAConstants.ContactFields.ACCOUNT;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.ACCOUNT_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.BIRTHDATE;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.EMAIL;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.FIRST_NAME;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.LAST_NAME;

public class Contact extends TranslatableSFBaseObject {
    public static final String TAG = Contact.class.getName();

    private static final long serialVersionUID = -2897005430565703626L;

    public Contact() {
        super(AbInBevObjects.CONTACT);
    }

    public Contact(JSONObject json) {
        super(AbInBevObjects.CONTACT, json);
    }

    public String getFirstName() {
        return getStringValueForKey(FIRST_NAME);
    }

    public String getLastName() {
        return getStringValueForKey(LAST_NAME);
    }

    public String getPhone() {
        return getStringValueForKey(ContactFields.PHONE);
    }

    public String getFunction() {
        return getStringValueForKey(ContactFields.ROLE);
    }

    public String getEmail() {
        return getStringValueForKey(EMAIL);
    }

    public String getAccountId() {
        return getStringValueForKey(ACCOUNT_ID);
    }

    public String getBirthdate() {
        return getStringValueForKey(BIRTHDATE);
    }

    public static List<Contact> getContactsByAccountId(String accountId) {
        List<Contact> results = new ArrayList<>();
        String sqlFilter = String.format("{%s:%s} = '%s' ORDER BY {%s:%s} ASC", AbInBevObjects.CONTACT,
                ContactFields.ACCOUNT_ID, accountId, AbInBevObjects.CONTACT,
                ContactFields.ROLE);
        String smartSql = String.format(DSAConstants.Formats.SMART_SQL_FORMAT, AbInBevObjects.CONTACT, sqlFilter);
        try {
            JSONArray recordsArray = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(smartSql);
            int recordsCount = recordsArray.length();
            for (int i = 0; i < recordsCount; i++) {
                JSONObject jsonObject = recordsArray.getJSONArray(i).getJSONObject(0);
                results.add(new Contact(jsonObject));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getting  contacts", e);
        }

        return results;
    }

    public String getAccountName() {
        JSONObject json = getJsonObject(ACCOUNT);
        if (json != null) {
            return json.optString("Name");
        }

        return null;
    }

}
