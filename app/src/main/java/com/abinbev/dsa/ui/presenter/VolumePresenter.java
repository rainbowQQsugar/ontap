package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class VolumePresenter implements Presenter<VolumePresenter.ViewModel> {

    public static final String TAG = VolumePresenter.class.getSimpleName();

    public interface ViewModel {
        void setCurrentKpis(List<KPI__c> kpis);
        void setSaleDays(int currentDay, int maxDays);
    }

    private ViewModel viewModel;
    private String accountId;
    private String parentKpiNum;
    private String userId;
    private Subscription subscription;

    public static VolumePresenter createAccountPresenter(String accountId) {
        return createAccountPresenter(accountId, null);
    }

    public static VolumePresenter createAccountPresenter(String accountId, String parentKpiNum) {
        return new VolumePresenter(accountId, null, parentKpiNum);
    }

    public static VolumePresenter createUserPresenter(String userId) {
        return createUserPresenter(userId, null);
    }

    public static VolumePresenter createUserPresenter(String userId, String parentKpiNum) {
        return new VolumePresenter(null, userId, parentKpiNum);
    }

    private VolumePresenter(String accountId, String userId, String parentKpiNum) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.parentKpiNum = parentKpiNum;
        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        if (!TextUtils.isEmpty(userId)) {
            getUserKpis();
        }

        if (!TextUtils.isEmpty(accountId)) {
            getAccountKpis();
        }
    }

    private void getUserKpis() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(
                new Observable.OnSubscribe<List<KPI__c>>() {
                    @Override
                    public void call(Subscriber<? super List<KPI__c>> subscriber) {
                        List<KPI__c> kpis = KPI__c.getVolumeForUser(userId, new Date(), parentKpiNum);

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(kpis, objectNames, fieldNames);

                        subscriber.onNext(kpis);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<KPI__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error on getting volume kpis: ", e);
                    }

                    @Override
                    public void onNext(List<KPI__c> kpis) {
                        viewModel.setCurrentKpis(kpis);
                        setSaleDays(kpis.isEmpty() ? null : kpis.get(0));
                    }
                });
    }

    private void getAccountKpis() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(
                new Observable.OnSubscribe<List<KPI__c>>() {
                    @Override
                    public void call(Subscriber<? super List<KPI__c>> subscriber) {
                        List<KPI__c> kpis = KPI__c.getVolumeForAccount(accountId, new Date(), parentKpiNum);

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(kpis, objectNames, fieldNames);

                        subscriber.onNext(kpis);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(new Subscriber<List<KPI__c>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error on getting volume kpis: ", e);
                    }

                    @Override
                    public void onNext(List<KPI__c> kpis) {
                        viewModel.setCurrentKpis(kpis);
                        setSaleDays(kpis.isEmpty() ? null : kpis.get(0));
                    }
                });
    }

    void setSaleDays(KPI__c kpi) {
        if (kpi == null) {
            viewModel.setSaleDays(0, 0);
        }
        else {
            viewModel.setSaleDays(kpi.getDaysPassed(), kpi.getTotalDays());
        }
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
