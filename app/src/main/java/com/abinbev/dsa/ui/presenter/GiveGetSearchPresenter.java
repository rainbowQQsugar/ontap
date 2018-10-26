package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 1/7/16.
 */
public class GiveGetSearchPresenter implements Presenter<GiveGetSearchPresenter.ViewModel> {

    private static final String TAG = GiveGetSearchPresenter.class.getSimpleName();

    public interface ViewModel {
        void setItems(List<Material__c> materialList);
    }

    ViewModel viewModel;
    Subscription subscription;

    public GiveGetSearchPresenter() {
        super();
        subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<List<Material__c>>() {
            @Override
            public void call(Subscriber<? super List<Material__c>> subscriber) {
                List<Material__c> allItems = new ArrayList<>();

                allItems.addAll(Material_Give__c.fetchAll());
                allItems.addAll(Material_Get__c.fetchAll());

                subscriber.onNext(allItems);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<Material__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(List<Material__c> material__cs) {
                        viewModel.setItems(material__cs);
                    }
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
//
//    public void addItems(final List<Material__c> selectedItems, final String startDate, final String endDate) {
//        subscription.clear();
//        subscription.add(Observable.create(new Observable.OnSubscribe<Boolean>() {
//            @Override
//            public void call(Subscriber<? super Boolean> subscriber) {
//                for (Material__c material__c : selectedItems) {
//                    Negotiation_Item__c.createNegotiationItem(material__c, negotiationId, startDate, endDate);
//                }
//                subscriber.onNext(true);
//                subscriber.onCompleted();
//            }
//        }).subscribeOn(AppScheduler.background())
//                .observeOn(AppScheduler.main())
//                .subscribe(new Subscriber<Boolean>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "onError: ", e);
//                    }
//
//                    @Override
//                    public void onNext(Boolean aBoolean) {
//                        viewModel.itemsAdded();
//                    }
//                }));
//    }
//
//    public void fetchCurrentIds() {
//        subscription.clear();
//        subscription.add(Observable.create(new Observable.OnSubscribe<List<String>>() {
//            @Override
//            public void call(Subscriber<? super List<String>> subscriber) {
//                List<String> currentIds = new ArrayList<>();
//
//                for (Negotiation_Item__c item : Negotiation_Item__c.fetchGetsNegotiationItems(negotiationId)) {
//                    currentIds.add(item.fetchGetId());
//                }
//                for (Negotiation_Item__c item : Negotiation_Item__c.fetchGiveNegotiationItems(negotiationId)) {
//                    currentIds.add(item.fetchGiveId());
//                }
//                subscriber.onNext(currentIds);
//                subscriber.onCompleted();
//            }
//        }).subscribeOn(AppScheduler.background())
//                .observeOn(AppScheduler.main())
//                .subscribe(new Subscriber<List<String>>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "onError: ", e);
//                    }
//
//                    @Override
//                    public void onNext(List<String> ids) {
//                        viewModel.setCurrentItem(ids);
//                    }
//                }));
//    }
}
