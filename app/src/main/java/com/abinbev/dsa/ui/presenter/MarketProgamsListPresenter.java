package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class MarketProgamsListPresenter implements Presenter<MarketProgamsListPresenter.ViewModel> {
    public static final String TAG = MarketProgamsListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<MarketProgram> marketPrograms);
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;

    public MarketProgamsListPresenter(String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        loadMarketProgramList();
    }

    private void loadMarketProgramList() {
        this.subscription = Observable.create(new Observable.OnSubscribe<List<MarketProgram>>() {
            @Override
            public void call(Subscriber<? super List<MarketProgram>> subscriber) {
                List<MarketProgram> marketPrograms = MarketProgram.getMarketProgramsByAccountId(accountId);
                subscriber.onNext(marketPrograms);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<MarketProgram>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error getting all surveys: ", e);
                    }

                    @Override
                    public void onNext(List<MarketProgram> marketPrograms) {
                        viewModel.setData(marketPrograms);
                    }
                });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
