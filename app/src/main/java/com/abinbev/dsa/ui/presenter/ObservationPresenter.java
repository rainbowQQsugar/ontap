package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Negotiation__c;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by wandersonblough on 12/18/15.
 */
public class ObservationPresenter implements Presenter<ObservationPresenter.ViewModel> {

    private static final String TAG = ObservationPresenter.class.getSimpleName();

    public interface ViewModel {

    }

    String negotiationId;
    ViewModel viewModel;
    Subscription subscription;
    String observation;

    public ObservationPresenter(String negotiationId) {
        this.negotiationId = negotiationId;
        subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.unsubscribe();
        subscription = Observable.just(Negotiation__c.updateObservations(negotiationId, observation))
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
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }

    public void setObservation(String observation) {
        this.observation = observation;
        start();
    }
}
