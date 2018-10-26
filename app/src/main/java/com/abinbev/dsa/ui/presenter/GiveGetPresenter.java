package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 12/11/15.
 */
public class GiveGetPresenter implements Presenter<GiveGetPresenter.ViewModel> {

    private static final String TAG = GiveGetPresenter.class.getSimpleName();

    public interface ViewModel {
        void setGives(List<Material_Give__c> gives);

        void setGets(List<Material_Get__c> gets);

        void givesCheck(List<Negotiation_Item__c> negotiationItems);

        void getsCheck(List<Negotiation_Item__c> negotiationItems);
    }

    String packageId;
    ViewModel viewModel;
    CompositeSubscription subscription;

    public GiveGetPresenter(String packageId) {
        this.packageId = packageId;
        subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.add(Observable.create(new Observable.OnSubscribe<List<Material_Give__c>>() {
            @Override
            public void call(Subscriber<? super List<Material_Give__c>> subscriber) {
                subscriber.onNext(Material_Give__c.fetchGivesByPackageId(packageId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Material_Give__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Material_Give__c> material_give__cs) {
                        viewModel.setGives(material_give__cs);
                    }
                }));

        subscription.add(Observable.create(new Observable.OnSubscribe<List<Material_Get__c>>() {
            @Override
            public void call(Subscriber<? super List<Material_Get__c>> subscriber) {
                subscriber.onNext(Material_Get__c.fetchGetsForPackage(packageId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Material_Get__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Material_Get__c> material_get__cs) {
                        viewModel.setGets(material_get__cs);
                    }
                }));
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }

    public void updateGives(String negotiationId) {
        subscription.add(Observable.just(Negotiation_Item__c.fetchGiveNegotiationItems(negotiationId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Negotiation_Item__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Negotiation_Item__c> negotiationItem___cs) {
                        viewModel.givesCheck(negotiationItem___cs);
                    }
                }));
    }

    public void updateGets(String negotiationId) {
        subscription.add(Observable.just(Negotiation_Item__c.fetchGetsNegotiationItems(negotiationId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Negotiation_Item__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Negotiation_Item__c> negotiationItem___cs) {
                        viewModel.getsCheck(negotiationItem___cs);
                    }
                }));
    }

}
