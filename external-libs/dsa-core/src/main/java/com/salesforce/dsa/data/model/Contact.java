package com.salesforce.dsa.data.model;

import com.salesforce.dsa.utils.DSAConstants.DSAObjects;

import org.json.JSONObject;

import static com.salesforce.dsa.utils.DSAConstants.ContactFields.ACCOUNT;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.ACCOUNT_ID;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.EMAIL;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.FIRST_NAME;
import static com.salesforce.dsa.utils.DSAConstants.ContactFields.LAST_NAME;

public class Contact extends SFBaseObject {

    private static final long serialVersionUID = -2897005430565703626L;

    public Contact() {
        super(DSAObjects.CONTACT);
    }

    public Contact(JSONObject json) {
        super(DSAObjects.CONTACT, json);
    }

    public String getFirstName() {
        return getStringValueForKey(FIRST_NAME);
    }

    public String getLastName() {
        return getStringValueForKey(LAST_NAME);
    }

    public String getEmail() {
        return getStringValueForKey(EMAIL);
    }

    public String getAccountId() {
        return getStringValueForKey(ACCOUNT_ID);
    }

    public String getAccountName() {
        JSONObject json = getJsonObject(ACCOUNT);
        if (json != null) {
            return json.optString("Name");
        }

        return null;
    }

}
