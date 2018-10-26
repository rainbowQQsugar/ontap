package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by parak0 on 02.08.2016.
 */

public class EventAddPresenter implements Presenter<EventAddPresenter.ViewModel> {


    public static final String TAG = EventAddPresenter.class.getSimpleName();
    EventAddPresenter.ViewModel viewModel;

    private Subscription subscription;
    private String eventCatalogId;

    public EventAddPresenter(String eventCatalogId) {
        super();
        this.eventCatalogId = eventCatalogId;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        fetchEventCatalog();
    }

    @Override
    public void stop() {

    }

    void fetchEventCatalog() {
        this.subscription.unsubscribe();
        subscription = Observable.create(new Observable.OnSubscribe<Event_Catalog__c>() {
            @Override
            public void call(Subscriber<? super Event_Catalog__c> subscriber) {
                subscriber.onNext(Event_Catalog__c.getById(eventCatalogId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Event_Catalog__c>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting event: ", e);
                    }

                    @Override
                    public void onNext(Event_Catalog__c eventCatalogRecord) {

                        if (viewModel != null) {
                            viewModel.setEventCatalogRecord(eventCatalogRecord);
                        }
                    }
                });

    }

    public interface ViewModel {
       void setChosenDate(Date date);
       void setEventCatalogRecord(Event_Catalog__c eventCatalogRecord);
    }

}
