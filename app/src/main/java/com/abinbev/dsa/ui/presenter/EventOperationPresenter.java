package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Event_Catalog__c;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by parak0 on 04.08.2016.
 */

public class EventOperationPresenter implements Presenter<EventOperationPresenter.ViewModel> {

    public static final String TAG = EventOperationPresenter.class.getSimpleName();
    String eventId;
    ViewModel viewModel;
    private  CompositeSubscription compositeSubscription;

    public EventOperationPresenter(String eventId) {
        super();
        this.eventId = eventId;
        this.compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        loadEvent();
    }

    @Override
    public void stop() {

    }

    void loadEvent() {
        this.compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Event>() {
            @Override
            public void call(Subscriber<? super Event> subscriber) {
                subscriber.onNext(Event.getById(eventId));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Event>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting event: ", e);
                    }

                    @Override
                    public void onNext(Event event) {
                        if (viewModel != null) {
                            viewModel.setEvent(event);
                            loadEventCatalog(event.getEventCatalog());
                        }
                    }
                }));
    }

    private void loadEventCatalog(final String eventCatalogId) {
        this.compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Event_Catalog__c>() {
            @Override
            public void call(Subscriber<? super Event_Catalog__c> subscriber) {
                Event_Catalog__c recordTypes = Event_Catalog__c.getById(eventCatalogId);
                subscriber.onNext(recordTypes);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Event_Catalog__c>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting survey RecordTypes: ", e);
                    }

                    @Override
                    public void onNext(Event_Catalog__c recordTypes) {
                        if (viewModel != null) {
                            viewModel.setEventCatalogRecord(recordTypes);
                        }
                    }
                }));
    }


    public interface ViewModel {
        void setEvent(Event event);
        void setEventCatalogRecord(Event_Catalog__c eventCatalogRecord);
    }

}
