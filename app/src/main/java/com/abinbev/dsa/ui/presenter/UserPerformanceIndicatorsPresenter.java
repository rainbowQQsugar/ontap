package com.abinbev.dsa.ui.presenter;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abinbev.dsa.activity.MyordersActivity;
import com.abinbev.dsa.activity.PictureAuditActivity;
import com.abinbev.dsa.activity.RejectedAccountChangesActivity;
import com.abinbev.dsa.activity.UserCasesListActivity;
import com.abinbev.dsa.activity.UserEventPlanningActivity;
import com.abinbev.dsa.activity.UserSurveysListActivity;
import com.abinbev.dsa.activity.UserTasksListActivity;
import com.abinbev.dsa.activity.VisitPlanActivity;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.Order__c;
import com.abinbev.dsa.model.Picture_Audit_Status__c;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.PermissionManager;

import rx.Single;


public class UserPerformanceIndicatorsPresenter extends AbstractRxPresenter<UserPerformanceIndicatorsPresenter.ViewModel> {

    public static final String TAG = UserPerformanceIndicatorsPresenter.class.getSimpleName();

    public interface ViewModel {
        void setViewState(ViewState viewState);
    }

    public static class ViewState {
        public Integer openTasks;
        public Integer openCases;
        public Integer openSurveys;
        public Integer eventPlanning;
        public Integer rejectedPictures;
        public Integer rejectedAccountChanges;
        public Integer myOrders;
    }

    private String userId;

    public UserPerformanceIndicatorsPresenter(String userId) {
        super();
        this.userId = userId;
    }

    @Override
    public void start() {
        super.start();

        addSubscription(Single.fromCallable(
                () -> {
                    PermissionManager permissionManager = PermissionManager.getInstance();
                    ViewState viewState = new ViewState();

                    if (permissionManager.hasPermission(PermissionManager.SURVEYS_TILE)) {
                        viewState.openSurveys = SurveyTaker__c.getOpenSurveysCountByUserId(userId);
                    }

                    if (permissionManager.hasPermission(PermissionManager.TASKS_TILE)) {
                        viewState.openTasks = Task.openTasksCountByUserId(userId);
                    }

                    if (permissionManager.hasPermission(PermissionManager.CASES_TILE)) {
                        viewState.openCases = Case.getOpenCasesCountByUser(userId);
                    }

                    viewState.eventPlanning = Event.getActiveEventCountWithEventCatalogueByUserId(userId);

                    viewState.rejectedPictures = Picture_Audit_Status__c.getRejectedCount();

                    viewState.rejectedAccountChanges = Case.getRejectedAccountChangesCount();

                    viewState.myOrders = Order__c.getAllAccountOrdersCount();

                    return viewState;
                })
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        viewState -> viewModel().setViewState(viewState),
                        error -> Log.e(TAG, "Error while loading view state", error)
                ));
    }

    public void onSurveysClicked(Context context) {
        Intent intent = new Intent(context, UserSurveysListActivity.class);
        intent.putExtra(UserSurveysListActivity.USER_ID_EXTRA, userId);
        context.startActivity(intent);
    }

    public void onRejectedAccountChangesClicked(Context context) {
        Intent intent = new Intent(context, RejectedAccountChangesActivity.class);
        context.startActivity(intent);
    }

    public void onTasksClicked(Context context) {
        Intent intent = new Intent(context, UserTasksListActivity.class);
        intent.putExtra(UserTasksListActivity.ARGS_USER_ID, userId);
        context.startActivity(intent);
    }

    public void onCasesClicked(Context context) {
        Intent intent = new Intent(context, UserCasesListActivity.class);
        intent.putExtra(UserCasesListActivity.ARGS_USER_ID, userId);
        context.startActivity(intent);
    }

    public void onEventsClicked(Context context) {
        Intent intent = new Intent(context, UserEventPlanningActivity.class);
        intent.putExtra(UserEventPlanningActivity.USER_ID_EXTRA, userId);
        context.startActivity(intent);
    }

    public void onPictureAuditClicked(Context context) {
        Intent intent = new Intent(context, PictureAuditActivity.class);
        context.startActivity(intent);
    }

    public void onMyVisitClicked(Context context) {
        Intent intent = new Intent(context, VisitPlanActivity.class);
        context.startActivity(intent);
    }

    public void onMyOrderClicked(Context context) {
        Intent intent = new Intent(context, MyordersActivity.class);
        context.startActivity(intent);
    }
}
