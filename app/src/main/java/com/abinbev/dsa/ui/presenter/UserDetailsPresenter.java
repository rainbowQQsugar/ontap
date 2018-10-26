package com.abinbev.dsa.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.abinbev.dsa.activity.KpiDetailsActivity;
import com.abinbev.dsa.activity.UserVolumeDetailsActivity;
import com.abinbev.dsa.model.CN_KPI_Statistic__c;
import com.abinbev.dsa.model.KPI__c;
import com.abinbev.dsa.model.User;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import rx.Single;

public class UserDetailsPresenter extends AbstractRxPresenter<UserDetailsPresenter.ViewModel> {

    public static final String TAG = UserDetailsPresenter.class.getSimpleName();

    public static class State {
        public User user;
        public List<List<KPI__c>> kpis;
        public List<CN_KPI_Statistic__c> performanceKpis;
        public boolean showTimeZoneWarning;
        public String localTimeZone;
        public String remoteTimeZone;
    }

    public interface ViewModel {
        void setState(State state);
    }

    final Context context;
    boolean isFirstStart;
    State state;
    ViewModel viewModel;

    public UserDetailsPresenter(Context context, boolean isFirstStart) {
        super();
        this.context = context;
        this.isFirstStart = isFirstStart;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        super.setViewModel(viewModel);
        this.viewModel = viewModel;
    }

    @Override
    public void start() {
        super.start();
        loadState(isFirstStart);
        isFirstStart = false;
    }

    @Override
    public void stop() {
        super.stop();
        state = null;
    }

    public void onDestroy() {
        viewModel = null;
    }

    private void loadState(boolean isFirstStart) {
        addSubscription(Single.fromCallable(
                () -> {
                    State state = new State();
                    state.user = User.getCurrentUser();

                    // On first start check if device is using the same time zone as the backend.
                    if (isFirstStart && !TextUtils.isEmpty(state.user.getTimeZone())) {
                        TimeZone remoteTimeZone = TimeZone.getTimeZone(state.user.getTimeZone());
                        TimeZone localTimeZone = TimeZone.getDefault();

                        if (remoteTimeZone != null && localTimeZone != null
                                && !Objects.equals(remoteTimeZone.getID(), localTimeZone.getID())) {

                            state.showTimeZoneWarning = true;
                            state.localTimeZone = localTimeZone.getID();
                            state.remoteTimeZone = remoteTimeZone.getID();
                        }
                    }
                    List<CN_KPI_Statistic__c> kpiList = CN_KPI_Statistic__c.getKPIList();
                    List<KPI__c> allKpis = KPI__c.getParentKpisForUserAndMonths(state.user.getId(),0);
                    state.kpis = groupKpisByCategory(allKpis);
                    state.performanceKpis = kpiList;
                    return state;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        state -> {
                            this.state = state;
                            viewModel.setState(state);
                        },
                        error -> Log.e(TAG, "Error when loading user: ", error)
                ));
    }

    public void onVolumeClicked() {
//        if (state != null && state.user != null && state.userVolume != null) {
//            context.startActivity(new Intent(context, UserVolumeDetailsActivity.class)
//                    .putExtra(UserVolumeDetailsActivity.ARGS_USER_ID, state.user.getId())
//                    .putExtra(UserVolumeDetailsActivity.ARGS_VOLUME_ID, state.userVolume.getKpiNum()));
//        }
    }

    public void onChildVolumeClicked(KPI__c kpi) {
//        if (state != null && state.user != null && state.userVolume != null) {
//            context.startActivity(new Intent(context, UserKpiDetailsActivity.class)
//                    .putExtra(UserKpiDetailsActivity.ARGS_USER_ID, state.user.getId())
//                    .putExtra(UserKpiDetailsActivity.ARGS_SELECTED_KPI_SOUP_ID, kpi.getId())
//                    .putExtra(UserKpiDetailsActivity.ARGS_PARENT_KPI_NUM, state.userVolume.getKpiNum()));
//        }
    }

    public void onKPIClicked(KPI__c kpi) {
        if (state != null && state.user != null) {
            context.startActivity(new Intent(context, KpiDetailsActivity.class)
                    .putExtra(KpiDetailsActivity.ARGS_PARENT_CATEGORY_NAME, kpi.getTranslatedCategory())
                    .putExtra(UserVolumeDetailsActivity.ARGS_USER_ID, state.user.getId())
                    .putExtra(KpiDetailsActivity.ARGS_PARENT_KPI_ID, kpi.getKpiNum()));
        }
    }

    @NonNull
    private List<List<KPI__c>> groupKpisByCategory(final List<KPI__c> allKpis) {
        final List<List<KPI__c>> groupedKpis = new ArrayList<>();
        boolean wasAdded = false;

        for (KPI__c kpi : allKpis) {
            for (int i = 0; i < groupedKpis.size(); i++) {
                if (kpi.getTranslatedCategory().equals(groupedKpis.get(i).get(0).getTranslatedCategory())) {
                    groupedKpis.get(i).add(kpi);
                    wasAdded = true;
                    break;
                } else {
                    wasAdded = false;
                }
            }
            if (!wasAdded) {
                List<KPI__c> tmpList = new ArrayList<>();
                tmpList.add(kpi);
                groupedKpis.add(tmpList);
            }
        }
        return groupedKpis;
    }
}
