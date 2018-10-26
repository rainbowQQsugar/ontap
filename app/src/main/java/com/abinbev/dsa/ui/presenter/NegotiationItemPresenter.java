package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.model.Material_Get__c;
import com.abinbev.dsa.ui.view.negotiation.Material__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 12/16/15.
 */
public class NegotiationItemPresenter implements Presenter<NegotiationItemPresenter.ViewModel> {

    private static final String TAG = NegotiationItemPresenter.class.getSimpleName();

    public interface ViewModel {
        void setNegotiationGives(List<Negotiation_Item__c> gives);

        void setNegotiationGets(List<Negotiation_Item__c> gets);

        void removeItem(Material__c material_c);
    }

    private String negotiationId;
    private CompositeSubscription subscription;
    private ViewModel viewModel;

    protected NegotiationItemPresenter() {

    }

    public NegotiationItemPresenter(String negotiationId) {
        this.negotiationId = negotiationId;
        subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.add(fetchNegotiationGives());
        subscription.add(fetchNegotiationGets());
    }

    @Override
    public void stop() {
        subscription.unsubscribe();
        viewModel = null;
    }

    private Subscription fetchNegotiationGives() {
        return Observable.just(Negotiation_Item__c.fetchGiveNegotiationItems(negotiationId)).subscribeOn(AppScheduler.background())
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
                        viewModel.setNegotiationGives(negotiationItem___cs);
                    }
                });
    }

    private Subscription fetchNegotiationGets() {
        return Observable.just(Negotiation_Item__c.fetchGetsNegotiationItems(negotiationId)).subscribeOn(AppScheduler.background())
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
                        viewModel.setNegotiationGets(negotiationItem___cs);
                    }
                });
    }

//    public void createItem(final Material__c material_c, String negotiationId, String startDate, String endDate) {
//        subscription.add(Observable.just(Negotiation_Item__c.createNegotiationItem(material_c, negotiationId, startDate, endDate)).subscribe(new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.e(TAG, "onError: ", e);
//
//            }
//
//            @Override
//            public void onNext(String id) {
//                if (material_c instanceof Material_Get__c) {
//                    fetchNegotiationGets();
//                } else {
//                    fetchNegotiationGives();
//                }
//            }
//        }));
//    }

    public void removeItem(final Material__c material_c, String negotiationId) {
        subscription.add(Observable.just(Negotiation_Item__c.removeNegotiationItem(Negotiation_Item__c.getIdForNegotiationItem(material_c, negotiationId)))
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);

                    }

                    @Override
                    public void onNext(Boolean removed) {
                        if (material_c instanceof Material_Get__c) {
                            fetchNegotiationGets();
                        } else {
                            fetchNegotiationGives();
                        }
                    }
                }));
    }

    public void updateEndDate(String itemId, String date) {
        subscription.add(Observable.just(Negotiation_Item__c.updateEndDate(itemId, date))
        .subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<Boolean>() {
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

    public void updateStartDate(String itemId, String date) {
        subscription.add(Observable.just(Negotiation_Item__c.updateStartDate(itemId, date))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Boolean>() {
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
