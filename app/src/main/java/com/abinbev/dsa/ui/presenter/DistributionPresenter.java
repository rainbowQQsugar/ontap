package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AppScheduler;
import com.salesforce.androidsyncengine.datamanager.DataManager;
import com.salesforce.androidsyncengine.datamanager.DataManagerFactory;
import com.salesforce.androidsyncengine.datamanager.SyncUtils;

import rx.Observable;

public class DistributionPresenter extends AbstractRxPresenter<DistributionPresenter.ViewModel> {

    private static final String TAG = "DistributionPresenter";

    public interface ViewModel {
        void setDistribution(Distribution distribution);

        void close();
    }

    private final String distributionId;

    private Distribution distribution;

    private boolean isFirstStart = true;

    public DistributionPresenter(String distributionId) {
        super();
        this.distributionId = distributionId;
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
                () -> Distribution.getById(distributionId))
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        distribution -> {
                            this.distribution = distribution;
                            viewModel().setDistribution(distribution);
                        },
                        error -> Log.e(TAG, "Error loading distribution: ", error)
                ));
    }

    public void deleteDistribution() {
        if (distribution == null) return;

        addSubscription(Observable.fromCallable(
                () -> {
                    distribution.setIsActive(false);
                    DataManager dm = DataManagerFactory.getDataManager();
                    dm.updateRecord(AbInBevObjects.CN_DISTRIBUTION, distribution.getId(), distribution.toJson());
                    SyncUtils.TriggerRefresh(ABInBevApp.getAppContext());

                    return true;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        newId -> viewModel().close(),
                        error -> Log.e(TAG, "Error saving distribution: ", error)
                ));
    }
}
