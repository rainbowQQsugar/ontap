package com.abinbev.dsa.model.checkoutRules;

import static com.salesforce.androidsdk.smartstore.store.SmartStore.SOUP_LAST_MODIFIED_DATE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.abinbev.dsa.activity.SurveysListActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.MandatoryTaskDetail;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.AccountType;
import com.abinbev.dsa.utils.AbInBevConstants.SurveyTakerFields;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Jakub Stefanowski on 04.08.2017.
 */

class SurveyCheckoutRule extends CheckoutRule {

    private static final String TAG = "SurveyCheckoutRule";

    private Account account;
    private String getCompletedSurveysQuery;

    SurveyCheckoutRule(Account account, Event event, MandatoryTaskDetail mandatoryTaskDetail) {
        this.account = account;
        this.getCompletedSurveysQuery = createQuery(account, event, mandatoryTaskDetail);
    }

    private static String createQuery(Account account, Event event, MandatoryTaskDetail mandatoryTaskDetail) {
        String controlStartString = event.getControlStartDateTime();
        long controlStartLong = parseDate(controlStartString);
        return String.format("SELECT count() FROM {%1$s} WHERE " +
                        "{%1$s:%2$s} = '%3$s' AND " +
                        "{%1$s:%4$s} = '%5$s' AND " +
                        "{%1$s:%6$s} = '%7$s' AND " +
                        "({%1$s:%8$s} >= '%9$s' OR ({%1$s:%8$s} IS NULL AND {%1$s:%10$s} >= %11$d))",
                AbInBevObjects.SURVEY_TAKER,
                SurveyTakerFields.ACCOUNT_ID, account.getId(),
                SurveyTakerFields.STATUS, SurveyTakerFields.STATUS_COMPLETE,
                SurveyTakerFields.SURVEY__C, mandatoryTaskDetail.getSurvey(),
                SurveyTakerFields.LAST_MODIFIED_DATE, controlStartString,
                SOUP_LAST_MODIFIED_DATE, controlStartLong);
    }

    @Override
    public boolean isFulfilled() {
        return isCompetitorsAccount() // Competitor account doesn't need any surveys.
                || hasCompletedSurvey();
    }

    private boolean isCompetitorsAccount() {
        return AccountType.COMPETITOR.equals(account.getType());
    }

    private boolean hasCompletedSurvey() {
        JSONArray array = DataManagerFactory.getDataManager().fetchAllSmartSQLQuery(getCompletedSurveysQuery);
        int result = 0;

        try {
            result = array.getJSONArray(0).getInt(0);
        } catch (JSONException e) {
            Log.w(TAG, e);
        }

        return result > 0;
    }

    @Override
    public void openScreen(Context context) {
        Intent intent = new Intent(context, SurveysListActivity.class);
        intent.putExtra(SurveysListActivity.ACCOUNT_ID_EXTRA, account.getId());
        context.startActivity(intent);
    }
}
