package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Estandar__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 05.01.2016.
 */
public class OpportunitiesListPresenter implements Presenter<OpportunitiesListPresenter.ViewModel>{
    public interface ViewModel {
        void setData(List<Estandar__c> opportunities);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;

    public OpportunitiesListPresenter(String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = Subscriptions.empty();
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription = Observable.create(new Observable.OnSubscribe<List<Estandar__c>>() {
            @Override
            public void call(Subscriber<? super List<Estandar__c>> subscriber) {
                List<Estandar__c> estandars = Estandar__c.getByAccountId(accountId);
                subscriber.onNext(estandars);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Estandar__c>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<Estandar__c> estandars) {
                        viewModel.setData(estandars);
                    }
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
