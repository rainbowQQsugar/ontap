package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.DistributionFields;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Single;

public class DistributionListPresenter extends AbstractRxPresenter<DistributionListPresenter.ViewModel> implements SyncListener {
    private static final String TAG = DistributionListPresenter.class.getSimpleName();

    private final String accountId;
    private final RegisteredReceiver<SyncListener> syncReceiver;

    public interface ViewModel {

        void updateAdapter(List<Distribution> sprData);

        Context getContext();
    }

    public DistributionListPresenter(String accountId, RegisteredReceiver<SyncListener> syncReceiver) {
        this.accountId = accountId;
        this.syncReceiver = syncReceiver;
    }

    public void start(ViewModel viewModel) {
        setViewModel(viewModel);

        syncReceiver.register(ABInBevApp.getAppContext(), this);

        Map<String, String> params = new HashMap<>();
        params.put("accountId", accountId);
        DynamicFetchEngine.fetchInBackground(ABInBevApp.getAppContext(), "distributionListData", params); // not sure if it will be needed

        refresh();
    }

    @Override
    public void start() {
        super.start();

        ViewModel vm = viewModel();
        if (vm != null) {
            start(vm);
        }
    }

    private void refresh() {
        subscriptions.clear();

        addSubscription(getDistributionListObservable(accountId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(sprData -> viewModel().updateAdapter(sprData))
        );
    }

    @Override
    public void stop() {
        stopReceiver(ABInBevApp.getAppContext());
        super.stop();
    }

    private Single<List<Distribution>> getDistributionListObservable(String accountId) {
        return Single.fromCallable(() -> {
            List<Distribution> distributions = Distribution.getAllByAccountId(accountId);

            String objectName = AbInBevObjects.CN_DISTRIBUTION;
            String[] fieldNames = {
                    DistributionFields.CN_UNIT,
                    DistributionFields.CN_PACKAGE,
            };
            TranslatableSFBaseObject.addTranslations(distributions, objectName, fieldNames);

            return distributions;
        });
    }

    private void stopReceiver(Context context) {
        syncReceiver.unregister(context, this);
    }

    @Override
    public void onSyncCompleted() {
        stopReceiver(ABInBevApp.getAppContext());
        refresh();
    }

    @Override
    public void onSyncError(String message) {
        Log.e(TAG, "Sync error: " + message);
        stopReceiver(ABInBevApp.getAppContext());
    }

    @Override
    public void onSyncFailure(String message) {
        Log.e(TAG, "Sync failure: " + message);
        stopReceiver(ABInBevApp.getAppContext());
    }
}
