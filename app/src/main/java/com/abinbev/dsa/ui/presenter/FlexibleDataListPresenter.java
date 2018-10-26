package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Dato_flexible__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by lukaszwalukiewicz on 08.01.2016.
 */
public class FlexibleDataListPresenter implements Presenter<FlexibleDataListPresenter.ViewModel>{
    public static final String TAG = FlexibleDataListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Dato_flexible__c> flexibleData);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;


    public FlexibleDataListPresenter(String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(new Observable.OnSubscribe<List<Dato_flexible__c>>() {
            @Override
            public void call(Subscriber<? super List<Dato_flexible__c>> subscriber) {
                List<Dato_flexible__c> flexibleData = Dato_flexible__c.getDatoFlexibleForAccountId(accountId);
                subscriber.onNext(flexibleData);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Dato_flexible__c>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all flexible data: ", e);
                    }

                    @Override
                    public void onNext(List<Dato_flexible__c> flexibleData) {
                        viewModel.setData(flexibleData);
                    }
                });
    }

    @Override
    public void stop() {
        this.viewModel = null;
        this.subscription.unsubscribe();
    }
}
