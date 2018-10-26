package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.utils.AppScheduler;

import rx.Single;

/**
 * Created by mlangreder on 4/7/16.
 */
public class AdditionalDataDynamicLayoutPresenter extends AbstractRxPresenter<AdditionalDataDynamicLayoutPresenter.ViewModel> {

    public static final String TAG = AdditionalDataDynamicLayoutPresenter.class.getSimpleName();

    public interface ViewModel {
        void onAccountUpdated(Boolean didUpdate);
        void updateAccountInfo(Account account);
    }

    private final String accountId;

    private boolean isInitialLoad = true;

    @Override
    public void start() {
        super.start();
        if (accountId == null || accountId.equals("")) {
            return;
        }

        if (isInitialLoad) {
            getAccount();
            isInitialLoad = false;
        }
    }

    public AdditionalDataDynamicLayoutPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    public void saveAdditionalData(final Account account) {
        clearSubscriptions();
        addSubscription(Single.fromCallable(account::updateRecord)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        didUpdate -> viewModel().onAccountUpdated(didUpdate),
                        error -> Log.e(TAG, "Error while saving account: ", error)
                ));
    }

    public void getAccount() {
        addSubscription(Single.fromCallable(() -> Account.getById(accountId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        account -> viewModel().updateAccountInfo(account),
                        error -> Log.e(TAG, "Error while loading account: ", error)
                ));
    }
}
