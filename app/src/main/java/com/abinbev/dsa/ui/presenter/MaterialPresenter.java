package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.model.Material_Give__c;
import com.abinbev.dsa.model.RecordType;
import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 12/17/15.
 */
public class MaterialPresenter implements Presenter<MaterialPresenter.ViewModel> {

    private static final String TAG = "MaterialPresenter";

    public interface ViewModel {
        void setMaterial__c(Material__c material__c, Negotiation_Item__c negotiationItem);
    }

    private ViewModel viewModel;
    private Negotiation_Item__c negotiationItem;
    private CompositeSubscription subscription;

    public MaterialPresenter(Negotiation_Item__c negotiationItem) {
        this.negotiationItem = negotiationItem;
        subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();
        subscription.add(Observable.create(new Observable.OnSubscribe<Material__c>() {
            @Override
            public void call(Subscriber<? super Material__c> subscriber) {
                Material__c material__c;
                String name = RecordType.getById(negotiationItem.getRecordId()).getName();
                if (name.equals(Negotiation_Item__c.RECORD_TYPE_GET)) {
                    material__c = Material_Get__c.fetchById(negotiationItem.fetchGetId());
                } else {
                    material__c = Material_Give__c.fetchById(negotiationItem.fetchGiveId());
                }
                subscriber.onNext(material__c);
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Material__c>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Material__c material__c) {
                        viewModel.setMaterial__c(material__c, negotiationItem);
                    }
                }));
    }

    @Override
    public void stop() {
        subscription.clear();
        viewModel = null;
    }

    public void updateAmount(String id, int amount) {
        subscription.add(Observable.just(Negotiation_Item__c.updateAmount(id, amount)).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main()).subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                }));
    }
}
