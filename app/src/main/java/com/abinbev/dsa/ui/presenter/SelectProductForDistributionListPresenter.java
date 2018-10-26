package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Observable;

public class SelectProductForDistributionListPresenter extends AbstractRxPresenter<SelectProductForDistributionListPresenter.ViewModel> {

    private static final String TAG = "SelectProductForDistr";

    public interface ViewModel {
        void onAccountLoaded(Account account);
    }

    String accountId;

    boolean isFirstStart = true;

    public SelectProductForDistributionListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        if (isFirstStart) {
            loadData();
            isFirstStart = false;
        }
    }

    private void loadData() {
        addSubscription(Observable.fromCallable(
                () -> Account.getById(accountId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(account -> viewModel().onAccountLoaded(account),
                        error -> Log.e(TAG, "Error fetching account: ", error)));
    }
}
