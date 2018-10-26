package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Survey__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class QuizzesPresenter implements Presenter<QuizzesPresenter.ViewModel>{
    public static final String TAG = QuizzesPresenter.class.getSimpleName();
    private static final String RECORD_TYPE = "SurveyTaker__c";

    public interface ViewModel {
        void setData(List<SurveyTaker__c> quizzes);
        void setSurveyTypes(List<Survey__c> recordTypes);
    }

    private ViewModel viewModel;

    private CompositeSubscription compositeSubscription;

    public QuizzesPresenter() {
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
        this.compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<SurveyTaker__c>>() {
            @Override
            public void call(Subscriber<? super List<SurveyTaker__c>> subscriber) {
                List<SurveyTaker__c> surveys = SurveyTaker__c.getQuizzes();
                subscriber.onNext(surveys);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<SurveyTaker__c>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all quizzes: ", e);
                    }

                    @Override
                    public void onNext(List<SurveyTaker__c> surveys) {
                        viewModel.setData(surveys);
                    }
                }));
    }

    private void loadSurveyRecordTypes() {
        this.compositeSubscription.add(Observable.create(new Observable.OnSubscribe<List<Survey__c>>() {
            @Override
            public void call(Subscriber<? super List<Survey__c>> subscriber) {
                List<Survey__c> recordTypes = Survey__c.getQuizzes();
                subscriber.onNext(recordTypes);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Survey__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting survey RecordTypes: ", e);
                }

                    @Override
                    public void onNext(List<Survey__c> recordTypes) {
                        viewModel.setSurveyTypes(recordTypes);
                    }
                }));
    }


    @Override
    public void stop() {
        viewModel = null;
        compositeSubscription.clear();
    }
}
