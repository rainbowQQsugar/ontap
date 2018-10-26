package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.activity.SyncListener;
import com.abinbev.dsa.model.CN_SPR__c;
import com.abinbev.dsa.utils.AbInBevConstants.DynamicFetch;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.RegisteredReceiver;
import com.salesforce.androidsyncengine.dynamicfetch.DynamicFetchEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by Adam Chodera on 10/07/17.
 */

public class SprListPresenter extends AbstractRxPresenter<SprListPresenter.ViewModel> implements SyncListener {
    private static final String TAG = SprListPresenter.class.getSimpleName();

    private final String accountId;
    private final RegisteredReceiver<SyncListener> syncReceiver;

    public interface ViewModel {

        void updateAdapter(List<CN_SPR__c> sprData);

        Context getContext();
    }

    public SprListPresenter(String accountId, RegisteredReceiver<SyncListener> syncReceiver) {
        this.accountId = accountId;
        this.syncReceiver = syncReceiver;
    }

    public void start(ViewModel viewModel) {
        setViewModel(viewModel);

        syncReceiver.register(ABInBevApp.getAppContext(), this);

        Map<String, String> params = new HashMap<>();
        params.put("accountId", accountId);
        DynamicFetchEngine.fetchInBackground(ABInBevApp.getAppContext(), DynamicFetch.SPR_LIST_OPENED, params); // not sure if it will be needed

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

        addSubscription(sprObservable(accountId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .takeWhile(a -> viewModel() != null)
                .subscribe(sprData -> viewModel().updateAdapter(sprData))
        );
    }

    public void stop(ViewModel viewModel) {
        stopReceiver(ABInBevApp.getAppContext());
        super.stop();
    }

    @Override
    public void stop() {
        stop(viewModel());
        super.stop();
    }

    private Observable<List<CN_SPR__c>> sprObservable(String accountId) {
        return Observable.just(CN_SPR__c.getForCurrentWeek(accountId));
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
