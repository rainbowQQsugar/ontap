package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.model.TradeProgram;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Single;

public class TradeProgramPresenter extends AbstractRxPresenter<TradeProgramPresenter.ViewModel> implements SyncListener {
    private static final String TAG = TradeProgramPresenter.class.getSimpleName();

    private final String accountId;

    private final RegisteredReceiver<SyncListener> syncReceiver;

    public interface ViewModel {

        void updateAdapter(List<TradeProgram> sprData);

        Context getContext();
    }

    public TradeProgramPresenter(String accountId, RegisteredReceiver<SyncListener> syncReceiver) {
        this.accountId = accountId;
        this.syncReceiver = syncReceiver;
    }

    @Override
    public void start() {
        super.start();

        Context context = ABInBevApp.getAppContext();

        syncReceiver.register(context, this);

        Map<String, String> params = new HashMap<>();
        params.put("accountId", accountId);
        DynamicFetchEngine.fetchInBackground(context, DynamicFetch.TRADE_PROGRAM_OPENED, params); // not sure if it will be needed

        refresh();
    }

    private void refresh() {
        subscriptions.clear();

        addSubscription(getTradeProgramListObservable(accountId)
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

    private Single<List<TradeProgram>> getTradeProgramListObservable(String accountId) {
        return Single.fromCallable(() -> TradeProgram.getAllByAccountId(accountId));
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
