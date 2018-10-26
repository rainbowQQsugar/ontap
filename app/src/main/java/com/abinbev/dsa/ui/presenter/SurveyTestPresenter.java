package com.abinbev.dsa.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.WebViewActivity;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.utils.AppPreferenceUtils;
import com.abinbev.dsa.utils.AppScheduler;

import java.io.File;
import java.util.List;

import rx.Observable;

/**
 * Created by Jakub Stefanowski on 01.09.2017.
 */

public class SurveyTestPresenter extends AbstractRxPresenter<SurveyTestPresenter.ViewModel> {

    private static final String TAG = "SurveyTestPresenter";

    public interface ViewModel {
        void showData(Data data);

        void showSurveyTypes(List<Survey__c> surveys);

        void showToast(String message);

        Data loadData();

        Activity getActivity();
    }

    public static class Data {
        public String accountId;
        public String surveyTakerId;
        public String bundleLocation;
    }

    Data data;

    @Override
    public void start() {
        super.start();

        addSubscription(Observable.fromCallable(
                () -> {
                    Context context = ABInBevApp.getAppContext();
                    Data data = new Data();

                    // Load accountId.
                    data.accountId = AppPreferenceUtils.getAccountId(context);
                    if (TextUtils.isEmpty(data.accountId)) {
                        List<Account> accounts = Account.getAccounts();
                        if (!accounts.isEmpty()) {
                            data.accountId = accounts.get(0).getId();
                        }
                    }

                    // Load surveyTakerId.
                    data.surveyTakerId = AppPreferenceUtils.getSurveyTakerId(context);
                    if (TextUtils.isEmpty(data.surveyTakerId)) {
                        if (!TextUtils.isEmpty(data.accountId)) {
                            List<SurveyTaker__c> surveyTakers = SurveyTaker__c.getSurveysByAccountId(data.accountId);
                            if (!surveyTakers.isEmpty()) {
                                data.surveyTakerId = surveyTakers.get(0).getId();
                            }
                        }
                    }

                    // Load bundle location.
                    data.bundleLocation = AppPreferenceUtils.getBundleLocation(context);
                    if (TextUtils.isEmpty(data.bundleLocation)) {
                        File file = new File(Environment.getExternalStorageDirectory(), "Archive.zip");
                        data.bundleLocation = file.getAbsolutePath();
                    }

                    return data;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        data -> {
                            this.data = data;
                            viewModel().showData(data);
                        },
                        error -> Log.w(TAG, error)
                ));
    }

    public void onNewSurveyClicked() {
        data = viewModel().loadData();
        saveData(data);

        addSubscription(Observable.fromCallable(() -> Survey__c.getSurveyTypesForAccountId(data.accountId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        surveys -> viewModel().showSurveyTypes(surveys),
                        error -> Log.w(TAG, error)
                ));
    }

    public void onLaunchLocalBundleClicked() {
        data = viewModel().loadData();
        if (data.surveyTakerId.equals("") || data.bundleLocation.equals("")) {
            viewModel().showToast("Survey Id and bundleLocation are required!!");
        } else {
            saveData(data);
            openExistingSurvey(data);
        }
    }

    public void onSurveyClicked(Survey__c survey) {
        data = viewModel().loadData();

        if (data.bundleLocation.equals("")) {
            viewModel().showToast("bundleLocation is required!!");
        } else {
            saveData(data);
            openNewSurvey(survey, data);
        }
    }

    private void saveData(Data data) {
        Activity activity = viewModel().getActivity();
        AppPreferenceUtils.putAccountId(activity, data.accountId);
        AppPreferenceUtils.putSurveyTakerId(activity, data.surveyTakerId);
        AppPreferenceUtils.putBundleLocation(activity, data.bundleLocation);
    }

    private void openExistingSurvey(Data data) {
        Activity activity = viewModel().getActivity();

        Intent webViewIntent = new Intent(activity, WebViewActivity.class);
        webViewIntent.putExtra(WebViewActivity.SURVEY_TAKER_ID, data.surveyTakerId);
        webViewIntent.putExtra(WebViewActivity.ACCOUNT_ID_EXTRA, data.accountId);

        webViewIntent.putExtra(WebViewActivity.TITLE, "Local Bundle");
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, "Local Bundle");
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, data.bundleLocation);

        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);

        activity.startActivity(webViewIntent);
    }

    private void openNewSurvey(Survey__c survey, Data data) {
        Activity activity = viewModel().getActivity();
        Intent webViewIntent = new Intent(activity, WebViewActivity.class);

        webViewIntent.putExtra(WebViewActivity.SURVEY_ID, survey.getId());
        webViewIntent.putExtra(WebViewActivity.ACCOUNT_ID_EXTRA, data.accountId);

        webViewIntent.putExtra(WebViewActivity.TITLE, "Local Bundle");
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, "Local Bundle");
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, data.bundleLocation);

        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        activity.startActivity(webViewIntent);
    }
}
