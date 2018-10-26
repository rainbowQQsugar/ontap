package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Caso;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class CasosPresenter implements Presenter<CasosPresenter.ViewModel> {

    public interface ViewModel {
        void setData(List<CasoCountWrapper> casos);
    }

    public static class CasoCountWrapper {
        public final String name;
        public final int count;

        public CasoCountWrapper(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    private ViewModel viewModel;
    private String accountId;
    private Subscription subscription;


    public CasosPresenter(String accountId) {
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
        this.subscription = Observable.create(new Observable.OnSubscribe<List<CasoCountWrapper>>() {
            @Override
            public void call(Subscriber<? super List<CasoCountWrapper>> subscriber) {
                List<CasoCountWrapper> casoCountWrappers = Case.getOpenCasesWrapperByAccountId(accountId);
                subscriber.onNext(casoCountWrappers);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<List<CasoCountWrapper>>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(List<CasoCountWrapper> casoCountWrappers) {
                viewModel.setData(casoCountWrappers);
            }
        });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
