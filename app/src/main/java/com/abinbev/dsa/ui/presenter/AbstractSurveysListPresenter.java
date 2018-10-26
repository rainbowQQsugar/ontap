package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.WebViewActivity;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.dsa.data.model.ContentVersion;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;


public abstract class AbstractSurveysListPresenter implements Presenter<AbstractSurveysListPresenter.ViewModel>{
    public static final String TAG = AbstractSurveysListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<SurveyTaker__c> surveys);
        void setSurveyTypes(List<Survey__c> recordTypes);
    }

    private ViewModel viewModel;

    private CompositeSubscription compositeSubscription;


    public AbstractSurveysListPresenter() {
        super();
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.compositeSubscription.clear();

        loadSurveyList();
        loadSurveyRecordTypes();
    }

    private void loadSurveyList() {
        this.compositeSubscription.add(Observable.fromCallable(
                () -> {
                    List<SurveyTaker__c> surveys = getSurveysTakers();
                    Collections.sort(surveys, new SurveyComparator());

                    String objectType = AbInBevConstants.AbInBevObjects.SURVEY_TAKER;
                    String[] fields = {
                            AbInBevConstants.SurveyTakerFields.STATUS
                    };

                    TranslatableSFBaseObject.addTranslations(surveys, objectType, fields);

                    return surveys;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        surveys -> viewModel.setData(surveys),
                        error -> Log.e(TAG, "Error getting all surveys: ", error)
                ));
    }

    private void loadSurveyRecordTypes() {
        this.compositeSubscription.add(Observable.fromCallable(this::getSurveyTypes)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        recordTypes -> viewModel.setSurveyTypes(recordTypes),
                        error -> Log.e(TAG, "Error getting survey RecordTypes: ", error)
                ));
    }

    protected abstract List<SurveyTaker__c> getSurveysTakers();

    protected abstract List<Survey__c> getSurveyTypes();

    @Override
    public void stop() {
        viewModel = null;
        compositeSubscription.clear();
    }

    public void launchSurveyWebViewIntent(Context context, SurveyTaker__c surveyTaker) {

        ContentVersion contentVersion = surveyTaker.getAssociatedContent();
        if (contentVersion == null) {
            Toast.makeText(context, R.string.survey_error_message, Toast.LENGTH_LONG).show();
            return;
        }

        Survey__c survey = surveyTaker.getSurvey__c();
        final Intent webViewIntent = new Intent(context, WebViewActivity.class);
        webViewIntent.putExtra(WebViewActivity.SURVEY_TAKER_ID, surveyTaker.getId());
        webViewIntent.putExtra(WebViewActivity.ACCOUNT_ID_EXTRA, surveyTaker.getAccountId());
        webViewIntent.putExtra(WebViewActivity.TITLE, context.getString(R.string.survey_header_title));
        webViewIntent.putExtra(WebViewActivity.DESCRIPTION, survey.getName());
        webViewIntent.putExtra(WebViewActivity.COMPLETE_FILE_NAME, contentVersion.getCompleteFileName(context));
        webViewIntent.putExtra(WebViewActivity.FILE_EXTENSION, WebViewActivity.ZIP);
        context.startActivity(webViewIntent);
    }

    private static class SurveyComparator implements Comparator<SurveyTaker__c> {

        @Override
        public int compare(SurveyTaker__c lhs, SurveyTaker__c rhs) {
            if (lhs == rhs) return 0;
            if (lhs == null) return -1;
            if (rhs == null) return 1;

            if (AbInBevConstants.SurveyTakerFields.STATUS_OPEN.equalsIgnoreCase(lhs.getState())) {
                if (AbInBevConstants.SurveyTakerFields.STATUS_OPEN.equalsIgnoreCase(rhs.getState())) {
                    String leftDate = lhs.getCreatedDate();
                    String rightDate = rhs.getCreatedDate();

                    // When in offline mode objects won't have creation date.
                    if (TextUtils.isEmpty(leftDate) && TextUtils.isEmpty(rightDate)) {
                        leftDate = lhs.getStringValueForKey(SmartStore.SOUP_LAST_MODIFIED_DATE);
                        rightDate = rhs.getStringValueForKey(SmartStore.SOUP_LAST_MODIFIED_DATE);
                    }

                    return compareDates(leftDate, rightDate);
                }
                else {
                    return -1;
                }
            }
            else {
                if (AbInBevConstants.SurveyTakerFields.STATUS_OPEN.equalsIgnoreCase(rhs.getState())) {
                    return 1;
                }
                else {
                    String leftDate = lhs.getLastModifiedDate();
                    String rightDate = rhs.getLastModifiedDate();

                    // When in offline mode objects won't have last modified date.
                    if (TextUtils.isEmpty(leftDate) && TextUtils.isEmpty(rightDate)) {
                        leftDate = lhs.getStringValueForKey(SmartStore.SOUP_LAST_MODIFIED_DATE);
                        rightDate = rhs.getStringValueForKey(SmartStore.SOUP_LAST_MODIFIED_DATE);
                    }
                    return compareDates(leftDate, rightDate);
                }
            }
        }

        private int compareDates(String obj1, String obj2) {
            if (obj1 != null && TextUtils.isEmpty(obj1)) {
                obj1 = null;
            }
            if (obj2 != null && TextUtils.isEmpty(obj2)) {
                obj2 = null;
            }

            if (obj1 == obj2) {
                return 0;
            }
            if (obj1 == null) {
                return -1;
            }
            if (obj2 == null) {
                return 1;
            }
            return obj2.compareTo(obj1);
        }
    }
}
