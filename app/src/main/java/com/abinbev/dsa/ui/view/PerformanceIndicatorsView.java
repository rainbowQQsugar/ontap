package com.abinbev.dsa.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.abinbev.dsa.R;
import com.abinbev.dsa.activity.AccountCasesListActivity;
import com.abinbev.dsa.activity.AccountD1ListActivity;
import com.abinbev.dsa.activity.AccountDetailsActivity;
import com.abinbev.dsa.activity.AccountEventPlanningActivity;
import com.abinbev.dsa.activity.AccountOverviewActivity;
import com.abinbev.dsa.activity.AssetsInPocListActivity;
import com.abinbev.dsa.activity.AssetsListActivity;
import com.abinbev.dsa.activity.ContractsActivity;
import com.abinbev.dsa.activity.DistributionListActivity;
import com.abinbev.dsa.activity.MarketProgramsListActivity;
import com.abinbev.dsa.activity.NegotiationListActivity;
import com.abinbev.dsa.activity.PromotionsListActivity;
import com.abinbev.dsa.activity.SprListActivity;
import com.abinbev.dsa.activity.SurveysListActivity;
import com.abinbev.dsa.activity.TasksListActivity;
import com.abinbev.dsa.activity.TradeProgramActivity;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;
import com.abinbev.dsa.ui.presenter.AbstractPerformanceIndicatorsPresenter;
import com.abinbev.dsa.ui.presenter.AccountPerformanceIndicatorsPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PerformanceIndicatorsView extends FrameLayout implements AbstractPerformanceIndicatorsPresenter.ViewModel, RefreshListener {

    @Bind(R.id.itos_result)
    CasoView itosResultCard;

    @Nullable
    @Bind(R.id.itos_result_holder)
    FrameLayout itosResultHolder;

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

    @Nullable
    @Bind(R.id.spr_holder)
    FrameLayout sprHolder;

    @Bind(R.id.open_surveys)
    CasoView openSurveysCard;

    @Nullable
    @Bind(R.id.open_surveys_holder)
    FrameLayout openSurveysHolder;

    @Bind(R.id.active_promotions)
    CasoView activePromotionsCard;

    @Nullable
    @Bind(R.id.active_promotions_holder)
    FrameLayout activePromotionsHolder;

    @Bind(R.id.active_negotiations)
    CasoView activeNegotiationsCard;

    @Nullable
    @Bind(R.id.active_negotiations_holder)
    FrameLayout activeNegotiationsHolder;

    @Bind(R.id.customer_assets)
    CasoView customerAssetsCard;

    @Nullable
    @Bind(R.id.customer_assets_holder)
    FrameLayout customerAssetsHolder;

    @Bind(R.id.market_programs)
    CasoView marketProgramsCard;

    @Nullable
    @Bind(R.id.market_programs_holder)
    FrameLayout marketProgramsHolder;

    @Bind(R.id.event_planning)
    CasoView eventPlanningCard;

    @Bind(R.id.spr)
    CasoView sprCard;

    @Nullable
    @Bind(R.id.trade_program_holder)
    FrameLayout tradeProgramHolder;

    @Bind(R.id.trade_program)
    CasoView tradeProgram;

    @Bind(R.id.pbo_contract)
    CasoView pboContract;

    @Nullable
    @Bind(R.id.distribution_holder)
    FrameLayout distributionHolder;

    @Bind(R.id.distribution)
    CasoView distributionCard;

    @Nullable
    @Bind(R.id.asset_poc_holder)
    FrameLayout assetPocHolder;

    @Bind(R.id.asset_poc)
    CasoView assetPocCard;

    @Nullable
    @Bind(R.id.technicians_holder)
    FrameLayout techniciansHolder;

    @Bind(R.id.technicians)
    CasoView techniciansCard;

    @Nullable
    @Bind(R.id.event_planning_holder)
    FrameLayout eventPlanningHolder;

    @Bind(R.id.title_label)
    TextView sectionLabelTextView;

    String sectionLabel;

    private AbstractPerformanceIndicatorsPresenter performanceIndicatorsPresenter;

    public PerformanceIndicatorsView(Context context) {
        this(context, null);
    }

    public PerformanceIndicatorsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PerformanceIndicatorsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        inflate(context, R.layout.merge_performance_indicator_view, this);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PerformanceIndicatorsView,
                    0, 0);

            try {
                sectionLabel = a.getString(R.styleable.PerformanceIndicatorsView_sectionTitle);
            } finally {
                a.recycle();
            }
        }

        if (TextUtils.isEmpty(sectionLabel)) {
            sectionLabelTextView.setVisibility(GONE);
        }
        else {
            sectionLabelTextView.setVisibility(VISIBLE);
            sectionLabelTextView.setText(sectionLabel);
        }
    }

    public void setAccountId(String accountId) {
        if (performanceIndicatorsPresenter == null) {
            performanceIndicatorsPresenter = new AccountPerformanceIndicatorsPresenter(accountId);
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
    public void setItosScore(int score) {
        if (itosResultHolder != null) {
            itosResultHolder.setVisibility(VISIBLE);
        }
        else {
            itosResultCard.setVisibility(VISIBLE);
        }

        if (score >= 0){
            itosResultCard.setCount(String.valueOf(score) + "%");
        } else {
            itosResultCard.setCount("-");
        }
    }

    @Override
    public void setOpenTasks(final int count) {
        if (openTasksHolder != null) {
            openTasksHolder.setVisibility(VISIBLE);
        } else {
            openTasksCard.setVisibility(VISIBLE);
        }

        openTasksCard.setCount(String.valueOf(count));
    }

    @Override
    public void setOpenCases(int count) {
        if (openCasesHolder != null) {
            openCasesHolder.setVisibility(VISIBLE);
        } else {
            openCasesCard.setVisibility(VISIBLE);
        }
        openCasesCard.setCount(String.valueOf(count));
    }

    @Override
    public void setOpenSurveys(int count) {
        if (openSurveysHolder != null) {
            openSurveysHolder.setVisibility(VISIBLE);
        }
        else {
            openSurveysCard.setVisibility(VISIBLE);
        }
        openSurveysCard.setCount(String.valueOf(count));
    }

    @Override
    public void setActivePromotions(int count) {
        if (activePromotionsHolder != null) {
            activePromotionsHolder.setVisibility(VISIBLE);
        }
        else {
            activePromotionsCard.setVisibility(VISIBLE);
        }
        activePromotionsCard.setCount(String.valueOf(count));
    }

    @Override
    public void setActiveNegotations(int count) {
        if (activeNegotiationsHolder != null) {
            activeNegotiationsHolder.setVisibility(VISIBLE);
        }
        else {
            activeNegotiationsCard.setVisibility(VISIBLE);
        }
        activeNegotiationsCard.setCount(String.valueOf(count));
    }

    @Override
    public void setCustomerAssets(int count) {
        if (customerAssetsHolder != null) {
            customerAssetsHolder.setVisibility(VISIBLE);
        }
        else {
            customerAssetsCard.setVisibility(VISIBLE);
        }
        customerAssetsCard.setCount(String.valueOf(count));
    }

    @Override
    public void setMarketPrograms(int count) {
        if (marketProgramsHolder != null) {
            marketProgramsHolder.setVisibility(VISIBLE);
        }
        else {
            marketProgramsCard.setVisibility(VISIBLE);
        }
        marketProgramsCard.setCount(String.valueOf(count));
    }

    @Override
    public void setEventPlanning(int count) {
        if (eventPlanningHolder != null) {
            eventPlanningHolder.setVisibility(VISIBLE);
        }
        else {
            eventPlanningCard.setVisibility(VISIBLE);
        }
        eventPlanningCard.setCount(String.valueOf(count));
    }

    @Override
    public void setSpr(final int count) {
        if (sprHolder != null) {
            sprHolder.setVisibility(VISIBLE);
        } else {
            sprCard.setVisibility(VISIBLE);
        }
        sprCard.setCount(String.valueOf(count));
    }

    @Override
    public void setTradeProgram(final int count) {
        if (tradeProgramHolder != null) {
            tradeProgramHolder.setVisibility(VISIBLE);
        } else {
            tradeProgram.setVisibility(VISIBLE);
        }
        tradeProgram.setCount(String.valueOf(count));
    }

    @Override
    public void setContracts(int count) {
        pboContract.setCount(String.valueOf(count));
        pboContract.setVisibility(VISIBLE);
    }

    @Override
    public void setDistribution(final int count) {
        if (distributionHolder != null) {
            distributionHolder.setVisibility(VISIBLE);
        } else {
            distributionCard.setVisibility(VISIBLE);
        }
        distributionCard.setCount(String.valueOf(count));
    }

    @Override
    public void setTasksCheckoutRule(final CheckoutRule checkoutRule) {
        openTasksCard.setCheckoutRule(checkoutRule);
    }

    @Override
    public void setSurveysCheckoutRule(final CheckoutRule checkoutRule) {
        openSurveysCard.setCheckoutRule(checkoutRule);
    }

    @Override
    public void setNegotiationsCheckoutRule(final CheckoutRule checkoutRule) {
        activeNegotiationsCard.setCheckoutRule(checkoutRule);
    }

    @Override
    public void setAssetPoc(int count) {
        if (assetPocHolder != null) {
            assetPocHolder.setVisibility(VISIBLE);
        } else {
            assetPocCard.setVisibility(VISIBLE);
        }
        assetPocCard.setCount(String.valueOf(count));
    }
    @OnClick(R.id.open_surveys)
    public void onOpenSurveysClick() {
        openSurveysList();
    }

    @OnClick(R.id.itos_result)
    public void onITOSClick() {
        openSurveysList();
    }

    @OnClick(R.id.open_tasks)
    public void onOpenTasksClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), TasksListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            intent.putExtra(TasksListActivity.ARGS_ACCOUNT_ID, accountId);
            getContext().startActivity(intent);
        }
    }
    @Override
    public void setTechnicians(int count) {
        if (techniciansHolder != null) {
            techniciansHolder.setVisibility(VISIBLE);
        } else {
            techniciansCard.setVisibility(VISIBLE);
        }
        techniciansCard.setCount(String.valueOf(count));
    }

    @OnClick(R.id.technicians)
    public void onTechniciansClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), AccountD1ListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            boolean isCheckedIn = ((AccountOverviewActivity) getContext()).isCheckedIn;
            intent.putExtra(AccountD1ListActivity.ARGS_ACCOUNT_ID, accountId);
            intent.putExtra(AccountD1ListActivity.ARGS_IS_CHECKED_IN, isCheckedIn);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.open_cases)
    public void onOpenCasesClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), AccountCasesListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            intent.putExtra(AccountCasesListActivity.ARGS_ACCOUNT_ID, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.active_negotiations)
    @SuppressWarnings("unused")
    public void onActiveNegotiationClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), NegotiationListActivity.class);
            intent.putExtra(AccountDetailsActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.event_planning)
    public void onEventCardClick() {
        openEventsList();
    }

    @OnClick(R.id.active_promotions)
    public void onPromotionsClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), PromotionsListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            intent.putExtra(PromotionsListActivity.ARGS_ACCOUNT_ID, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.customer_assets)
    public void onAssetsClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), AssetsListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            boolean isCheckedIn = ((AccountOverviewActivity) getContext()).isCheckedIn;
            intent.putExtra(AssetsListActivity.ARGS_ACCOUNT_ID, accountId);
            intent.putExtra(AssetsListActivity.ARGS_IS_CHECKED_IN, isCheckedIn);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.market_programs)
    public void onMarketProgramsClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), MarketProgramsListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            intent.putExtra(MarketProgramsListActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.spr)
    public void onSprClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), SprListActivity.class);
            intent.putExtra(SprListActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.trade_program)
    public void onTradeProgramClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), TradeProgramActivity.class);
            intent.putExtra(SprListActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.pbo_contract)
    public void onContractsClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), ContractsActivity.class);
            intent.putExtra(ContractsActivity.ARGS_ACCOUNT_ID, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.distribution)
    public void onDistributionClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), DistributionListActivity.class);
            intent.putExtra(DistributionListActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @OnClick(R.id.asset_poc)
    public void onAssetPocClick() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            Intent intent = new Intent(getContext(), AssetsInPocListActivity.class);
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            intent.putExtra(AssetsInPocListActivity.ARGS_ACCOUNT_ID, accountId);
            getContext().startActivity(intent);
        }
    }
    private void openSurveysList() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), SurveysListActivity.class);
            intent.putExtra(SurveysListActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }


    private void openEventsList() {
        //need to ensure we have an account id
        if (getContext() instanceof AccountOverviewActivity) {
            String accountId = ((AccountOverviewActivity) getContext()).accountId;
            Intent intent = new Intent(getContext(), AccountEventPlanningActivity.class);
            intent.putExtra(AccountEventPlanningActivity.ACCOUNT_ID_EXTRA, accountId);
            getContext().startActivity(intent);
        }
    }

    @Override
    public void onRefresh() {
        if (performanceIndicatorsPresenter != null) {
            performanceIndicatorsPresenter.start();
        }
    }
}
