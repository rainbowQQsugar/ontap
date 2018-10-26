package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.Paquetes_por_segmento__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 12/10/15.
 */
public class PackagesPresenter implements Presenter<PackagesPresenter.ViewModel> {

    private static final String TAG = "PackagesPresenter";

    public interface ViewModel {
        void setPackages(List<Paquetes_por_segmento__c> packages);
    }

    String accountId;
    ViewModel viewModel;
    Subscription subscription;

    protected PackagesPresenter() {

    }

    public PackagesPresenter(String accountId) {
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
        subscription = Observable.create(new Observable.OnSubscribe<List<Paquetes_por_segmento__c>>() {
            @Override
            public void call(Subscriber<? super List<Paquetes_por_segmento__c>> subscriber) {
                String segment = Account.getSegmentForAccount(accountId);
                List<Paquetes_por_segmento__c> packages = Paquetes_por_segmento__c.fetchPackagesForSegment(segment);
                subscriber.onNext(packages);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Paquetes_por_segmento__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Paquetes_por_segmento__c> packages) {
                        viewModel.setPackages(packages);
                    }
                });

    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        viewModel = null;
    }
}
