package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;


public abstract class AbstractCasesListPresenter implements Presenter<AbstractCasesListPresenter.ViewModel> {
    public static final String TAG = AbstractCasesListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Case> assets);

        void setRecordType(Case caso, RecordType recordType);

        void setNewCaseRecordTypes(List<String> recordTypes);
    }

    private ViewModel viewModel;
    private CompositeSubscription compositeSubscription;

    public AbstractCasesListPresenter() {
        super();
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        getCases();
        getNewCaseRecordTypes();
    }

    private void getCases() {
        compositeSubscription.add(Observable.create(
                new Observable.OnSubscribe<List<Case>>() {
                    @Override
                    public void call(Subscriber<? super List<Case>> subscriber) {
                        List<Case> cases = getOpenCases();

                        String objectType = AbInBevConstants.AbInBevObjects.CASE;
                        String[] fields = {
                                AbInBevConstants.CaseFields.STATUS
                        };

                        TranslatableSFBaseObject.addTranslations(cases, objectType, fields);
                        TranslatableSFBaseObject.addRecordTypeTranslations(cases, objectType, Case.FIELD_RECORD_NAME);

                        subscriber.onNext(cases);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Case>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all negotiations: ", e);
                    }

                    @Override
                    public void onNext(List<Case> cases) {
                        viewModel.setData(cases);
                    }
                }));
    }

    protected abstract List<Case> getOpenCases();

    @Override
    public void stop() {
        compositeSubscription.clear();
        viewModel = null;
    }

    public void getRecordType(final Case caso) {

        compositeSubscription.add(Observable.just(RecordType.getById(caso.getRecordTypeId()))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<RecordType>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting record type: ", e);
                    }

                    @Override
                    public void onNext(RecordType recordType) {
                        viewModel.setRecordType(caso, recordType);
                    }
                }));

    }

    private void getNewCaseRecordTypes() {
        compositeSubscription.add(Observable.just(RecordType.getNewCaseRecordTypes())
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<String>>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting new case record types: ", e);
                    }

                    @Override
                    public void onNext(List<String> recordTypes) {
                        viewModel.setNewCaseRecordTypes(recordTypes);
                    }
                }));
    }
}
