package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public class CoveragePresenter implements Presenter<CoveragePresenter.ViewModel> {

    public static final String TAG = CoveragePresenter.class.getSimpleName();

    public interface ViewModel {
        void setData(List<ParentKpiDetails> kpis);
    }

    public static class ParentKpiDetails {
        private final KPI__c kpi;
        private final double childrenActualSum;
        private final double childrenTargetSum;

        public ParentKpiDetails(KPI__c kpi, double childrenActualSum, double childrenTargetSum) {
            this.kpi = kpi;
            this.childrenActualSum = childrenActualSum;
            this.childrenTargetSum = childrenTargetSum;
        }

        public KPI__c getKpi() {
            return kpi;
        }

        public double getChildrenActualSum() {
            return childrenActualSum;
        }

        public double getChildrenTargetSum() {
            return childrenTargetSum;
        }
    }

    ViewModel viewModel;
    String accountId;
    Subscription subscription;
    Context context;


    public CoveragePresenter(Context context, String accountId) {
        super();
        this.accountId = accountId;
        this.subscription = Subscriptions.empty();
        this.context = context;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        this.subscription.unsubscribe();
        this.subscription = Observable.create(new Observable.OnSubscribe<List<ParentKpiDetails>>() {
            @Override
            public void call(Subscriber<? super List<ParentKpiDetails>> subscriber) {
                List<ParentKpiDetails> result = new ArrayList<>();

                Date currentDate = new Date();
                List<KPI__c> parentKpis = KPI__c.getCoverageForAccount(accountId, currentDate, null);

                String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                String[] fieldNames = {
                        AbInBevConstants.KpiFields.CATEGORY,
                        AbInBevConstants.KpiFields.UNIT
                };
                TranslatableSFBaseObject.addTranslations(parentKpis, objectNames, fieldNames);

                for (KPI__c kpi : parentKpis) {
                    List<KPI__c> children = KPI__c.getCoverageForAccount(accountId, currentDate, kpi.getKpiNum());
                    double actualSum = 0;
                    double targetSum = 0;

                    for (KPI__c childKpi : children) {
                        actualSum += childKpi.getActual();
                        targetSum += childKpi.getTarget();
                    }


                    TranslatableSFBaseObject.addTranslations(children, objectNames, fieldNames);

                    result.add(new ParentKpiDetails(kpi, actualSum, targetSum));
                }

                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        })
        .subscribeOn(AppScheduler.background())
        .observeOn(AppScheduler.main())
        .subscribe(new Subscriber<List<ParentKpiDetails>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error on getting coverage kpis: ", e);
            }

            @Override
            public void onNext(List<ParentKpiDetails> kpis) {
                viewModel.setData(kpis);
            }
        });
    }

    @Override
    public void stop() {
        viewModel = null;
        subscription.unsubscribe();
    }
}
