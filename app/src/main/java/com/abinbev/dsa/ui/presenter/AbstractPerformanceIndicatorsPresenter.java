package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.checkoutRules.CheckoutRule;
import com.abinbev.dsa.utils.AppScheduler;
import com.abinbev.dsa.utils.PermissionManager;

import java.util.List;

import rx.Observable;

public abstract class AbstractPerformanceIndicatorsPresenter extends AbstractRxPresenter<AbstractPerformanceIndicatorsPresenter.ViewModel> {

    public static final String TAG = AbstractPerformanceIndicatorsPresenter.class.getSimpleName();

    public interface ViewModel {

        void setItosScore(int score);

        void setOpenTasks(int count);

        void setOpenCases(int count);

        void setOpenSurveys(int count);

        void setActivePromotions(int count);

        void setActiveNegotations(int count);

        void setCustomerAssets(int count);

        void setMarketPrograms(int count);

        void setEventPlanning(int count);

        void setSpr(int count);

        void setTradeProgram(int count);

        void setContracts(int count);

        void setDistribution(int count);

        void setTasksCheckoutRule(CheckoutRule checkoutRule);

        void setSurveysCheckoutRule(CheckoutRule checkoutRule);

        void setNegotiationsCheckoutRule(CheckoutRule checkoutRule);

        void setAssetPoc(int count);

        void setTechnicians(int count);
    }

    private PermissionManager permissionManager;

    public AbstractPerformanceIndicatorsPresenter() {
        super();
        this.permissionManager = PermissionManager.getInstance();
    }

    @Override
    public void start() {
        super.start();

        //kick off each data retrieval
        if (permissionManager.hasPermission(PermissionManager.POCE_TILE)) {
            getLatestItosScore();
        }
        if (permissionManager.hasPermission(PermissionManager.SURVEYS_TILE)) {
            getOpenSurveys();
        }
        if (permissionManager.hasPermission(PermissionManager.TASKS_TILE)) {
            getOpenTasks();
        }
        if (permissionManager.hasPermission(PermissionManager.PROMOTIONS_TILE)) {
            getActivePromotions();
        }
        if (permissionManager.hasPermission(PermissionManager.NEGOTIATIONS_TILE)) {
            getActiveNegotiations();
        }
        if (permissionManager.hasPermission(PermissionManager.ASSETS_TILE)) {
            getCustomerAssets();
        }
        if (permissionManager.hasPermission(PermissionManager.MARKET_PROGRAMS_TILE)) {
            getMarketPrograms();
        }
        if (permissionManager.hasPermission(PermissionManager.CASES_TILE)) {
            getOpenCases();
        }
        if (permissionManager.hasPermission(PermissionManager.CHECKOUT_RULES)) {
            getCheckoutRules();
        }
        if (permissionManager.hasPermission(PermissionManager.ASSET_POC_TITLE)) {
            getAssetPoc();
        }
        if (permissionManager.hasPermission(PermissionManager.TECHNICIANS_TITLE)) {
            getTechnicians();
        }
        getEventPlanning();
        getSpr();
        getTradeProgram();
        getContractsCount();
        getDistribution();
    }

    protected abstract Integer getLatestItosSurveyScore();

    protected abstract int getOpenSurveysCount();

    protected abstract int openTasksCount();

    protected abstract int getOpenCasesCount();

    protected abstract int getActivePromotionsCount();

    protected abstract int getActiveNegotiationCount();

    protected abstract int getCustomerAssetsCount();

    protected abstract int getActiveMarketProgramCount();

    protected abstract int getActiveEventCountWithEventCatalogue();

    protected abstract int getActiveContractsCount();

    protected abstract List<CheckoutRule> getAllCheckoutRules();

    protected abstract int getSprCount();

    protected abstract int getTradeProgramCount();

    protected abstract int getDistributionListCount();

    protected abstract int getAssetPocCount();

    protected abstract int getTechniciansCount();

    public void getLatestItosScore() {
        addSubscription(
                Observable.fromCallable(this::getLatestItosSurveyScore)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                score -> viewModel().setItosScore(score),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getOpenSurveys() {
        addSubscription(
                Observable.fromCallable(this::getOpenSurveysCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setOpenSurveys(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getOpenTasks() {
        addSubscription(
                Observable.fromCallable(this::openTasksCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setOpenTasks(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getOpenCases() {
        addSubscription(
                Observable.fromCallable(this::getOpenCasesCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setOpenCases(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getActivePromotions() {
        addSubscription(
                Observable.fromCallable(this::getActivePromotionsCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setActivePromotions(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getActiveNegotiations() {
        addSubscription(
                Observable.fromCallable(this::getActiveNegotiationCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setActiveNegotations(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getCustomerAssets() {
        addSubscription(
                Observable.fromCallable(this::getCustomerAssetsCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setCustomerAssets(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getMarketPrograms() {
        addSubscription(
                Observable.fromCallable(this::getActiveMarketProgramCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setMarketPrograms(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getEventPlanning() {
        addSubscription(
                Observable.fromCallable(this::getActiveEventCountWithEventCatalogue)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setEventPlanning(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getContractsCount() {
        addSubscription(
                Observable.fromCallable(this::getActiveContractsCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setContracts(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getSpr() {
        addSubscription(
                Observable.fromCallable(this::getSprCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setSpr(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getTradeProgram() {
        addSubscription(
                Observable.fromCallable(this::getTradeProgramCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setTradeProgram(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }

    public void getCheckoutRules() {
        addSubscription(
                Observable.fromCallable(this::getAllCheckoutRules)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                checkoutRules -> processCheckoutRules(checkoutRules),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }


    public void getAssetPoc() {
        addSubscription(
                Observable.fromCallable(this::getAssetPocCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setAssetPoc(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }


    public void getTechnicians() {
        addSubscription(
                Observable.fromCallable(this::getTechniciansCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setTechnicians(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }


    private void processCheckoutRules(final List<CheckoutRule> checkoutRules) {
        for (CheckoutRule checkoutRule : checkoutRules) {
            final CheckoutRule.CheckoutRuleType checkoutRuleType = checkoutRule.getCheckoutRuleType();

            switch (checkoutRuleType) {
                case TASKS:
                    viewModel().setTasksCheckoutRule(checkoutRule);
                    break;
                case SURVEYS:
                    viewModel().setSurveysCheckoutRule(checkoutRule);
                    break;
                case NEGOTIATIONS:
                    viewModel().setNegotiationsCheckoutRule(checkoutRule);
                    break;
            }
        }
    }

    public void getDistribution() {
        addSubscription(
                Observable.fromCallable(this::getDistributionListCount)
                        .subscribeOn(AppScheduler.background())
                        .observeOn(AppScheduler.main())
                        .subscribe(
                                count -> viewModel().setDistribution(count),
                                error -> Log.e(TAG, "Error: ", error)
                        ));
    }
}