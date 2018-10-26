package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.abinbev.dsa.R;
import com.abinbev.dsa.ui.presenter.UserPerformanceIndicatorsPresenter;
import com.abinbev.dsa.ui.presenter.UserPerformanceIndicatorsPresenter.ViewState;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserPerformanceIndicatorsView extends FrameLayout implements UserPerformanceIndicatorsPresenter.ViewModel, RefreshListener {

    @Bind(R.id.open_tasks)
    CasoView openTasksCard;

    @Nullable
    @Bind(R.id.open_tasks_holder)
    FrameLayout openTasksHolder;

    @Bind(R.id.open_cases)
    CasoView openCasesCard;

    @Nullable
    @Bind(R.id.open_cases_holder)
    FrameLayout openCasesHolder;

    @Bind(R.id.open_surveys)
    CasoView openSurveysCard;

    @Nullable
    @Bind(R.id.open_surveys_holder)
    FrameLayout openSurveysHolder;

    @Bind(R.id.event_planning)
    CasoView eventPlanningCard;

    @Nullable
    @Bind(R.id.event_planning_holder)
    FrameLayout eventPlanningHolder;

    @Bind(R.id.picture_audit)
    CasoView pictureAuditCard;

    @Bind(R.id.rejected_account_changes)
    CasoView rejectedAccountChangesCard;

    @Bind(R.id.my_orders)
    CasoView myOrdersCard;

    @Bind(R.id.my_visit)
    CasoView myVisit;

    private UserPerformanceIndicatorsPresenter performanceIndicatorsPresenter;

    public UserPerformanceIndicatorsView(Context context) {
        this(context, null);
    }

    public UserPerformanceIndicatorsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserPerformanceIndicatorsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        inflate(context, R.layout.merge_user_performance_indicator_view, this);
        ButterKnife.bind(this);
    }

    public void setUserId(String userId) {
        if (performanceIndicatorsPresenter == null) {
            performanceIndicatorsPresenter = new UserPerformanceIndicatorsPresenter(userId);
        }
        performanceIndicatorsPresenter.setViewModel(this);
        performanceIndicatorsPresenter.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.stop();
        }
    }

    @Override
    public void setViewState(ViewState viewState) {
        setupTile(viewState.openTasks, openTasksHolder, openTasksCard);
//        setupTile(viewState.openSurveys, openSurveysHolder, openSurveysCard);//hide open survey
        //? new requirement no need to show openCasesCard in app start --@{
        //setupTile(viewState.openCases, openCasesHolder, openCasesCard);
        //--@}
        setupTile(viewState.eventPlanning, eventPlanningHolder, eventPlanningCard);
        setupTile(viewState.rejectedPictures, null, pictureAuditCard);
        setupTile(viewState.rejectedAccountChanges, null, rejectedAccountChangesCard);
        setupTile(viewState.myOrders, null, myOrdersCard);
        setupTile(0, null, myVisit);
    }

    private void setupTile(Integer count, View cardHolder, CasoView card) {
        View container = cardHolder != null ? cardHolder : card;

        if (count == null) {
            container.setVisibility(GONE);
        } else {
            container.setVisibility(VISIBLE);
            if (card == eventPlanningCard || card == myVisit) {
                card.setCount("");
            } else {
                card.setCount(String.valueOf(count));
            }
        }
    }

    @OnClick(R.id.open_surveys)
    public void onOpenSurveysClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onSurveysClicked(getContext());
        }
    }

    @OnClick(R.id.rejected_account_changes)
    public void onRejectedAccountChangesClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onRejectedAccountChangesClicked(getContext());
        }
    }

    @OnClick(R.id.open_tasks)
    public void onOpenTasksClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onTasksClicked(getContext());
        }
    }

    @OnClick(R.id.open_cases)
    public void onOpenCasesClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onCasesClicked(getContext());
        }
    }

    @OnClick(R.id.event_planning)
    public void onEventCardClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onEventsClicked(getContext());
        }
    }

    @OnClick(R.id.picture_audit)
    public void onAuditClick() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.onPictureAuditClicked(getContext());
        }
    }

    @Override
    public void onRefresh() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.start();
        }
    }

    @OnClick({R.id.my_visit, R.id.my_orders})
    public void onViewClicked(View view) {
        if (performanceIndicatorsPresenter != null) {
            switch (view.getId()) {
                case R.id.my_visit:
                    performanceIndicatorsPresenter.onMyVisitClicked(getContext());
                    break;
                case R.id.my_orders:
                    performanceIndicatorsPresenter.onMyOrderClicked(getContext());
                    break;
            }
        }
    }
}
