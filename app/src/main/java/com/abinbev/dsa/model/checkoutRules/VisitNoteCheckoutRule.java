package com.abinbev.dsa.model.checkoutRules;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.MandatoryTaskDetail;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.utils.SyncEngineConstants.StdFields;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jakub Stefanowski on 04.08.2017.
 */

class VisitNoteCheckoutRule extends CheckoutRule {

    private static final String TAG = "VisitNoteCheckoutRule";

    private MandatoryTaskDetail mandatoryTaskDetail;
    private String visitNotesQuery;

    VisitNoteCheckoutRule(Account account, Event event, MandatoryTaskDetail mandatoryTaskDetail) {
        this.mandatoryTaskDetail = mandatoryTaskDetail;
        this.visitNotesQuery = createQuery(event);
        setIsCheckoutStep(true);
    }

    private static String createQuery(Event event) {
        return String.format("SELECT {%1$s:_soup} FROM {%1$s} WHERE " +
                        "{%1$s:%2$s} = '%3$s'",
                AbInBevObjects.EVENT,
                StdFields.ID, event.getId());
    }

    private static boolean isEmpty(String string) {
        return TextUtils.isEmpty(string) || "null".equals(string);
    }

    @Override
    public boolean isFulfilled() {
        JSONArray array = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(visitNotesQuery);
        boolean isFulfilled = true;

        try {
            JSONObject jsonObject = array.getJSONArray(0).getJSONObject(0);
            String mandatoryFieldValue = jsonObject.optString(mandatoryTaskDetail.getMandatoryTaskFieldName());
            isFulfilled = !isEmpty(mandatoryFieldValue);
        } catch (JSONException e) {
            Log.w(TAG, e);
        }

        return isFulfilled;
    }

    @Override
    public void openScreen(Context context) {

    }
}
