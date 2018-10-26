package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wandersonblough on 2/18/16.
 */
public class CountPresenter implements Presenter<CountPresenter.ViewModel> {

    private static final String TAG = CountPresenter.class.getSimpleName();

    // this has to match the SyncEngine values
    private static String QUEUE_SOUP = "Queue";
    private static String ERROR_SOUP = "ErrorStore";

    public interface ViewModel {
        void setPendingCount(int count);

        void setErrorCount(int count);
    }

    private ViewModel viewModel;
    private CompositeSubscription subscription;

    public CountPresenter() {
        subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();
        getErrors();
        getPendingCount();
    }

    private void getPendingCount() {
        subscription.add(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(DataManagerFactory.getDataManager().getRecordCount(QUEUE_SOUP, "Id"));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (viewModel != null) {
                            viewModel.setPendingCount(integer);
                        }
                    }
                }));
    }

    private void getErrors() {
        subscription.add(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(DataManagerFactory.getDataManager().getRecordCount(ERROR_SOUP, "Id"));
                subscriber.onCompleted();
            }
        }).subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (viewModel != null) {
                            viewModel.setErrorCount(integer);
                        }
                    }
                }));
    }

    @Override
    public void stop() {
        subscription.clear();
        viewModel = null;
    }
}
