package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Date;
import java.util.List;

import rx.Observable;

public class ProspectListPresenter extends AbstractRxPresenter<ProspectListPresenter.ViewModel> {

    public static final String TAG = ProspectListPresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<Account> accounts);
        void setPOCCount(int size);
    }

    public List<String> recordTypeIds;

    public ProspectListPresenter() {
        super();
    }


    @Override
    public void start() {
        super.start();
    }

    public void getProspectsCount(final String searchText, final List<String> recordTypeIds,String prospectStatus, Date prospectCreationDate) {
        addSubscription(Observable.fromCallable(
                () -> {
                    int count = Account.getActiveAccountsCount(searchText, recordTypeIds, prospectStatus, prospectCreationDate);
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

    public void getProspectsForSearchString(final String searchText, final List<String> recordTypeIds,String prospectStatus, Date prospectCreationDate, int pageIndex, int pageSize) {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<Account> accounts = Account.getActiveAccountsForSearchText(searchText, recordTypeIds, prospectStatus, prospectCreationDate, pageIndex, pageSize);

                    String objectType = AbInBevConstants.AbInBevObjects.ACCOUNT;
                    String[] fields = {
                            AbInBevConstants.AccountFields.CN_LEAD_SOURCE__C,
                            AbInBevConstants.AccountFields.PROSPECT_STATUS,
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
