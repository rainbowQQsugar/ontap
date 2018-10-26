package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.Account;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class CoverageDetailsPresenter implements Presenter<CoverageDetailsPresenter.ViewModel> {
    public static final String TAG = CoverageDetailsPresenter.class.getSimpleName();

    public interface ViewModel {
        void setKpi(KPI__c kpi);

        void setDetails(List<KPI__c> detailKpis);

        void setAccount(Account account);
    }

    ViewModel viewModel;

    String kpiId;

    String accountId;

    CompositeSubscription subscription;

    public CoverageDetailsPresenter(String kpiId, String accountId) {
        super();
        this.kpiId = kpiId;
        this.accountId = accountId;
        this.subscription = new CompositeSubscription();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        subscription.clear();
        fetchKpi();
        fetchAccount();
    }

    private void fetchKpi() {

        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
        String[] fieldNames = {
                AbInBevConstants.KpiFields.CATEGORY,
                AbInBevConstants.KpiFields.UNIT
        };

        // Create observable to fetch KPI.
        Observable<KPI__c> fetchKpiObs = fetchKpiObservable(kpiId)
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main());

        // Subscribe to get current KPI.
        subscription.add(fetchKpiObs.subscribe(new Subscriber<KPI__c>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error in kpi: ", e);
            }

            @Override
            public void onNext(KPI__c note) {

                TranslatableSFBaseObject.addTranslations(note, objectNames, fieldNames);
                viewModel.setKpi(note);
            }
        }));

        // Take current KPI and find its children.
        Observable<List<KPI__c>> fetchChildrenObs = fetchKpiObs.flatMap(
                new Func1<KPI__c, Observable<List<KPI__c>>>() {
                    @Override
                    public Observable<List<KPI__c>> call(final KPI__c kpi) {
                        return fetchChildrenObservable(accountId, kpi.getKpiNum())
                                .subscribeOn(AppScheduler.background())
                                .observeOn(AppScheduler.main());
                    }
                });

        // Subscribe to get children KPIs.
        subscription.add(fetchChildrenObs.subscribe(new Subscriber<List<KPI__c>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error on getting children kpis: ", e);
            }

            @Override
            public void onNext(List<KPI__c> kpis) {

                TranslatableSFBaseObject.addTranslations(kpis, objectNames, fieldNames);
                viewModel.setDetails(kpis);
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
                                Log.e(TAG, "Error on getting coverage kpis: ", e);
                            }

                            @Override
                            public void onNext(Account account) {
                                viewModel.setAccount(account);
                            }
                        }));
    }

    private static Observable<KPI__c> fetchKpiObservable(final String kpiId) {
        return Observable.create(
                new Observable.OnSubscribe<KPI__c>() {
                    @Override
                    public void call(Subscriber<? super KPI__c> subscriber) {
                        KPI__c kpi = KPI__c.getById(kpiId);

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(kpi, objectNames, fieldNames);

                        subscriber.onNext(kpi);
                        subscriber.onCompleted();
                    }
                });
    }

    private static Observable<List<KPI__c>> fetchChildrenObservable(final String accountId, final String parentId) {
        return Observable.create(
                new Observable.OnSubscribe<List<KPI__c>>() {
                    @Override
                    public void call(Subscriber<? super List<KPI__c>> subscriber) {
                        Date currentDate = new Date();
                        List<KPI__c> kpis = KPI__c.getCoverageForAccount(accountId, currentDate, parentId);

                        String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                        String[] fieldNames = {
                                AbInBevConstants.KpiFields.CATEGORY,
                                AbInBevConstants.KpiFields.UNIT
                        };
                        TranslatableSFBaseObject.addTranslations(kpis, objectNames, fieldNames);

                        subscriber.onNext(kpis);
                        subscriber.onCompleted();
                    }
                });
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

    @Override
    public void stop() {
        subscription.clear();
        viewModel = null;
    }
}
