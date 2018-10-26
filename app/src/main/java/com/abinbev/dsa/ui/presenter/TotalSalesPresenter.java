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
 * Created by wandersonblough on 11/19/15.
 */
public class TotalSalesPresenter implements Presenter<TotalSalesPresenter.ViewModel> {

    private static final String TAG = TotalSalesPresenter.class.getName();

    public interface ViewModel {
        void setData(List<Dato_flexible__c> data);
    }

    private ViewModel viewModel;
    private Subscription subscription;
    private String accountId;

    public TotalSalesPresenter(String accountId) {
        super();
        this.accountId = accountId;
        subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<List<Dato_flexible__c>>() {
            @Override
            public void call(Subscriber<? super List<Dato_flexible__c>> subscriber) {
                List<Dato_flexible__c> datoFlexible = Dato_flexible__c.getDatoFlexibleForParams(accountId, "Venta Acumulada");
                subscriber.onNext(datoFlexible);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<List<Dato_flexible__c>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onNext(List<Dato_flexible__c> datoFlexibles) {
                viewModel.setData(datoFlexibles);
            }
        });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
