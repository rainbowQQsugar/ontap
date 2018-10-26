package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.activity.VolumeDetailsActivity;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class UserVolumeDetailsPresenter implements Presenter<UserVolumeDetailsPresenter.ViewModel> {

    public static final String TAG = UserVolumeDetailsPresenter.class.getSimpleName();

    public interface ViewModel {
        void setUser(User user);
        void setData(List<KPI__c> volumes);
        void setDays(int currentDay, int maxDays);
    }

    private Context context;
    private ViewModel viewModel;
    private String userId;
    private String kpiId;
    private Subscription subscription;


    public UserVolumeDetailsPresenter(Context context, String userId, String kpiId) {
        super();
        this.context = context;
        this.userId = userId;
        this.kpiId = kpiId;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(new Observable.OnSubscribe<FetchResult>() {
            @Override
            public void call(Subscriber<? super FetchResult> subscriber) {
                FetchResult result = new FetchResult();

                result.user = User.getUserByUserId(userId);

                result.volumes = KPI__c.getVolumeForUser(userId, new Date(), kpiId);

                String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                String[] fieldNames = {
                        AbInBevConstants.KpiFields.CATEGORY,
                        AbInBevConstants.KpiFields.UNIT
                };
                TranslatableSFBaseObject.addTranslations(result.volumes, objectNames, fieldNames);

                if (result.volumes == null || result.volumes.isEmpty()) {
                    result.currentSalesDay = 0;
                    result.totalSalesDays = 0;
                }
                else {
                    result.currentSalesDay = result.volumes.get(0).getDaysPassed();
                    result.totalSalesDays = result.volumes.get(0).getTotalDays();
                }

                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<FetchResult>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error on getting volume kpis: ", e);
            }

            @Override
            public void onNext(FetchResult result) {
                viewModel.setUser(result.user);
                viewModel.setData(result.volumes);
                viewModel.setDays(result.currentSalesDay, result.totalSalesDays);
            }
        });
    }

    public void onVolumeClicked(KPI__c kpi) {
        context.startActivity(
                new Intent(context, VolumeDetailsActivity.class)
                        .putExtra(VolumeDetailsActivity.ARGS_VOLUME_ID, kpi.getId())
                        .putExtra(VolumeDetailsActivity.ARGS_USER_ID, userId));
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }

    static class FetchResult {
        List<KPI__c> volumes;
        User user;
        int currentSalesDay;
        int totalSalesDays;
    }
}
