package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;

public class AccountListPresenter extends AbstractRxPresenter<AccountListPresenter.ViewModel> {

    public static final String TAG = AccountListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Account> accounts);
        void setPOCCount(int size);
    }

    public List<String> recordTypeIds;

    public AccountListPresenter() {
        super();
    }


    @Override
    public void start() {
        super.start();
    }

    public void getAccountsCount(final String searchText, final List<String> recordTypeIds) {
        addSubscription(Observable.fromCallable(
                () -> {
                    int count = Account.getActiveAccountsCount(searchText, recordTypeIds);
                    return count;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        count -> {
                            if (viewModel() != null)
                                viewModel().setPOCCount(count);
                        },
                        error -> Log.e(TAG, "Error getting accounts count: ", error)
                ));
    }

    public void getAccountsForSearchString(final String searchText, final List<String> recordTypeIds, int pageIndex, int pageSize) {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<Account> accounts = Account.getActiveAccountsForSearchText(searchText, recordTypeIds, pageIndex, pageSize);

                    String objectType = AbInBevConstants.AbInBevObjects.ACCOUNT;
                    String[] fields = {
                            AbInBevConstants.AccountFields.NEGOTIATION_STATUS__C
                    };
                    TranslatableSFBaseObject.addTranslations(accounts, objectType, fields);

                    return accounts;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        accounts -> {
                            if (viewModel() != null) {
                                viewModel().setData(accounts);
                            }
                        },
                        error -> Log.e(TAG, "Error getting accounts: ", error)
                ));
    }

    @Override
    public void stop() {
        super.stop();
    }
}
