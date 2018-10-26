package com.abinbev.dsa.ui.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.ABInBevApp;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

public class VolumeDetailsPresenter implements Presenter<VolumeDetailsPresenter.ViewModel> {
    public static final String TAG = VolumeDetailsPresenter.class.getSimpleName();

    public interface ViewModel {
        void setKpi(KPI__c kpi);

        void setHistoricalKpis(List<KPI__c> history);

        void setChildrenKpis(List<KPI__c> detailKpis);

        void setAccountName(String subtitle);
    }

    ViewModel viewModel;

    String kpiId;

    String accountId;

    String userId;

    CompositeSubscription subscription;

    public static VolumeDetailsPresenter createAccountPresenter(String kpiSoupId, String accountId) {
        return new VolumeDetailsPresenter(kpiSoupId, accountId, null);
    }

    public static VolumeDetailsPresenter createUserPresenter(String kpiSoupId, String userId) {
        return new VolumeDetailsPresenter(kpiSoupId, null, userId);
    }

    private VolumeDetailsPresenter(String kpiId, String accountId, String userId) {
        super();
        this.kpiId = kpiId;
        this.accountId = accountId;
        this.userId = userId;
        this.subscription = new CompositeSubscription();
    }

    public void setKpiId(String kpiId) {
        this.kpiId = kpiId;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();

        if (!TextUtils.isEmpty(accountId)) {
            fetchAccount();
            fetchAccountKpi();
        }

        if (!TextUtils.isEmpty(userId)) {
            fetchUser();
            fetchUserKpi();
        }
    }

    private void fetchAccountKpi() {
        this.subscription.add(
                fetchAccountKpiDataObservable(kpiId, accountId)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(new Subscriber<KPIFetchResult>() {
                            @Override
                            public void onCompleted() { }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error on getting kpi: ", e);
                            }

                            @Override
                            public void onNext(KPIFetchResult result) {
                                viewModel.setKpi(result.currentKpi);
                                viewModel.setChildrenKpis(result.childKpis);
                                viewModel.setHistoricalKpis(result.historicalKpis);
                            }
                        }));
    }

    private void fetchUserKpi() {
        this.subscription.add(
                fetchUserKpiDataObservable(kpiId, userId)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(new Subscriber<KPIFetchResult>() {
                            @Override
                            public void onCompleted() { }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error on getting kpi: ", e);
                            }

                            @Override
                            public void onNext(KPIFetchResult result) {
                                viewModel.setKpi(result.currentKpi);
                                viewModel.setChildrenKpis(result.childKpis);
                                viewModel.setHistoricalKpis(result.historicalKpis);
                            }
                        }));
    }

    private void fetchAccount() {
        this.subscription.add(
                fetchAccountObservable(accountId)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(new Subscriber<Account>() {
                            @Override
                            public void onCompleted() { }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error on getting account: ", e);
                            }

                            @Override
                            public void onNext(Account account) {
                                viewModel.setAccountName(account.getName());
                            }
                        }));
    }

    private void fetchUser() {
        this.subscription.add(
                fetchUserObservable(userId)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(new Subscriber<User>() {
                            @Override
                            public void onCompleted() { }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "Error on getting account: ", e);
                            }

                            @Override
                            public void onNext(User user) {
                                viewModel.setAccountName(ABInBevApp.getAppContext().getString(R.string.my_360));
                            }
                        }));
    }

    private static Observable<Account> fetchAccountObservable(final String accountId) {
        return Observable.create(
                new Observable.OnSubscribe<Account>() {
                    @Override
                    public void call(Subscriber<? super Account> subscriber) {
                        Account account = Account.getById(accountId);
                        subscriber.onNext(account);
                        subscriber.onCompleted();
                    }
                });
    }

    private static Observable<User> fetchUserObservable(final String userId) {
        return Observable.create(
                new Observable.OnSubscribe<User>() {
                    @Override
                    public void call(Subscriber<? super User> subscriber) {
                        User user = User.getUserByUserId(userId);
                        subscriber.onNext(user);
                        subscriber.onCompleted();
                    }
                });
    }

    private static Observable<KPIFetchResult> fetchAccountKpiDataObservable(final String kpiId, final String accountId) {
        return Observable.create(
                new Observable.OnSubscribe<KPIFetchResult>() {
                    @Override
                    public void call(Subscriber<? super KPIFetchResult> subscriber) {
                        KPIFetchResult result = new KPIFetchResult();
                        result.currentKpi = KPI__c.getById(kpiId);

                        Calendar calendar = Calendar.getInstance();
                        result.childKpis = KPI__c.getVolumeForAccount(accountId, calendar.getTime(), result.currentKpi.getKpiNum());

                        calendar.add(Calendar.MONTH, -5);               // Take last 6 months.
                        result.historicalKpis = KPI__c.getAccountVolumesSince(accountId, calendar.getTime(), result.currentKpi.getKpiNum());

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(result.currentKpi, objectNames, fieldNames);
                        TranslatableSFBaseObject.addTranslations(result.childKpis, objectNames, fieldNames);
                        TranslatableSFBaseObject.addTranslations(result.historicalKpis, objectNames, fieldNames);

                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                });
    }

    private static Observable<KPIFetchResult> fetchUserKpiDataObservable(final String kpiId, final String userId) {
        return Observable.create(
                new Observable.OnSubscribe<KPIFetchResult>() {
                    @Override
                    public void call(Subscriber<? super KPIFetchResult> subscriber) {
                        KPIFetchResult result = new KPIFetchResult();
                        result.currentKpi = KPI__c.getById(kpiId);

                        Calendar calendar = Calendar.getInstance();
                        result.childKpis = KPI__c.getVolumeForUser(userId, calendar.getTime(), result.currentKpi.getKpiNum());

                        calendar.add(Calendar.MONTH, -5);               // Take last 6 months.
                        result.historicalKpis = KPI__c.getUserVolumesSince(userId, calendar.getTime(), result.currentKpi.getKpiNum());

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(result.currentKpi, objectNames, fieldNames);
                        TranslatableSFBaseObject.addTranslations(result.childKpis, objectNames, fieldNames);
                        TranslatableSFBaseObject.addTranslations(result.historicalKpis, objectNames, fieldNames);

                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                });
    }

    @Override
    public void stop() {
        subscription.clear();
        viewModel = null;
    }

    static class KPIFetchResult {
        List<KPI__c> childKpis;
        List<KPI__c> historicalKpis;
        KPI__c currentKpi;
    }
}
