package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.adapter.AssetsListAdapter;
import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.List;

import rx.Observable;

public class AssetsInPocPresenter extends AbstractRxPresenter<AssetsInPocPresenter.ViewModel> implements Presenter<AssetsInPocPresenter.ViewModel> {

    public static final String TAG = AssetsInPocPresenter.class.getSimpleName();

    public interface ViewModel {
        void setAssets(List<Account_Asset__c> assets, String recordName);
    }

    private String accountId;

    public AssetsInPocPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        getCustomerAssets(AssetsListAdapter.SERIALIZED_RECORD_NAME);
        getCustomerAssets(AssetsListAdapter.NO_SERIALIZED_RECORD_NAME);
    }


    private void getCustomerAssets(final String recordName) {
        addSubscription(Observable.fromCallable(
                () -> Account_Asset__c.getCustomerAssets(accountId, recordName)
        )
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        assets -> {
                            viewModel().setAssets(assets, recordName);
                        },
                        error -> Log.e(TAG, "Error getting all assets: ", error)
                ));
    }
}
