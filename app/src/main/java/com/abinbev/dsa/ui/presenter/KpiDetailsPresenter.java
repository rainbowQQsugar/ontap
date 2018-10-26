package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.activity.VolumeDetailsActivity;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AbInBevConstants;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.DateUtils;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class KpiDetailsPresenter implements Presenter<KpiDetailsPresenter.ViewModel> {

    public static final String TAG = KpiDetailsPresenter.class.getSimpleName();
    private Context context;
    private ViewModel viewModel;
    private String userId;
    private String kpiId;
    private Subscription subscription;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public KpiDetailsPresenter(Context context, String userId, String kpiId) {
        super();
        this.context = context;
        this.userId = userId;
        this.kpiId = kpiId;
//        this.subscription = Subscriptions.empty();
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        getPerformanceForMonth();
        getKpisForCurrentMonth();
        getLastSixMonthsKpi();
    }

    private void getPerformanceForMonth() {

        addSubscription(Single.fromCallable(
                () -> {
                    List<CN_KPI_Statistic__c> kpiList = CN_KPI_Statistic__c.getKPIList();
                    return kpiList;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        kpiList -> {
                            viewModel.setPerformanceKpis(kpiList);
                        },
                        error -> Log.e(TAG, "Error when loading user: ", error)
                ));
    }

    private void getLastSixMonthsKpi() {

        addSubscription(Single.fromCallable(
                () -> {
                    FetchResult result = new FetchResult();
                    List<List<KPI__c>> tableData = KPI__c.getLastSixMonthsPerformanceKpi();
                    List<List<KPI__c>> pieChartData = KPI__c.getLastSixMonthsDataForPieChart();
                    result.dataInTable = tableData;
                    result.dataInPieChart = pieChartData;
                    return result;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        result -> {
                            viewModel.setLastSixMonthData(result);
                        },
                        error -> Log.e(TAG, "Error when loading user: ", error)
                ));
    }

    private void addSubscription(Subscription subscribe) {
        subscriptions.add(subscribe);
    }

    public void getPerformanceKpiForDate(String date) {
        addSubscription(Single.fromCallable(
                () -> {
                    List<CN_KPI_Statistic__c> kpiList = CN_KPI_Statistic__c.fetchKpiForDate(date);
                    return kpiList;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        kpiList -> {
                            viewModel.setPerformanceKpis(kpiList);
                        },
                        error -> Log.e(TAG, "Error when loading user: ", error)
                ));
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
        subscriptions.unsubscribe();
    }

    public void getKpis(boolean currentMonth) {
        addSubscription(Observable.fromCallable(
                () -> {
                    FetchResult result = new FetchResult();

                    result.user = User.getUserByUserId(userId);
                    if (currentMonth) {
                        result.kpis = KPI__c.getChildKpisForUserAndCurrentMonth(userId, kpiId);
                    } else {
                        result.kpis = KPI__c.getChildKpisForUserAndPreviousMonth(userId, kpiId);
                    }
                    if (result.kpis == null || result.kpis.isEmpty()) {
                        result.currentSalesDay = 0;
                        result.totalSalesDays = 0;
                    } else {
                        result.currentSalesDay = result.kpis.get(0).getDaysPassed();
                        result.totalSalesDays = result.kpis.get(0).getTotalDays();
                    }
                    String objectNames = AbInBevConstants.AbInBevObjects.KPI;
                    String[] fieldNames = {
                            AbInBevConstants.KpiFields.CATEGORY,
                            AbInBevConstants.KpiFields.UNIT
                    };
                    TranslatableSFBaseObject.addTranslations(result.kpis, objectNames, fieldNames);
                    return result;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        result -> {
                            viewModel.setUser(result.user);
                            viewModel.setData(result.kpis);
                            viewModel.setDays(result.currentSalesDay, result.totalSalesDays);
                        },
                        error -> Log.e(TAG, "Error on getting volume kpis: ", error)));
    }


    public void getKpisForPreviousMonth() {
        getKpis(false);
    }

    public void getKpisForCurrentMonth() {
        getKpis(true);
    }

    public interface ViewModel {
        void setUser(User user);

        void setData(List<KPI__c> volumes);

        void setLastSixMonthData(FetchResult data);

        void setDays(int currentDay, int maxDays);

        void setPerformanceKpis(List<CN_KPI_Statistic__c> kpiList);
    }

    public static class FetchResult {
        List<KPI__c> kpis;
        User user;
        int currentSalesDay;
        int totalSalesDays;
        public List<List<KPI__c>> dataInTable;
        public List<List<KPI__c>> dataInPieChart;
    }
}
