package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.WebViewActivity;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.salesforce.dsa.data.model.ContentVersion;

import java.util.List;

public class AccountSurveysListPresenter extends AbstractSurveysListPresenter {
    public static final String TAG = AccountSurveysListPresenter.class.getSimpleName();

    String accountId;

    public AccountSurveysListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected List<SurveyTaker__c> getSurveysTakers() {
        return SurveyTaker__c.getSurveysByAccountId(accountId);
    }

    @Override
    protected List<Survey__c> getSurveyTypes() {
        return Survey__c.getSurveyTypesForAccountId(accountId);
    }

    public void goToAddSurvey(Context context, String name) {
        final Survey__c surveyType = Survey__c.getByName(name);
        ContentVersion contentVersion = surveyType.getAssociatedContent();
        if (contentVersion == null) {
            Toast.makeText(context, R.string.survey_error_message, Toast.LENGTH_LONG).show();
            return;
        }
        final Intent webViewIntent = new Intent(context, WebViewActivity.class);
        //New Survey uses SURVEY_ID
        webViewIntent.putExtra(WebViewActivity.SURVEY_ID, surveyType.getId());
        webViewIntent.putExtra(WebViewActivity.ACCOUNT_ID_EXTRA, accountId);
        webViewIntent.putExtra(WebViewActivity.TITLE, context.getString(R.string.survey_header_title));
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, surveyType.getName());
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, contentVersion.getCompleteFileName(context));
        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        context.startActivity(webViewIntent);
    }
}
