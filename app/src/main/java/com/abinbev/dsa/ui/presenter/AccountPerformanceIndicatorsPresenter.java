package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Account_Asset__c;
import com.abinbev.dsa.model.CN_Account_Technicians__c;
import com.abinbev.dsa.model.CN_SPR__c;
import com.abinbev.dsa.model.CN_PBO_Contract__c;
import com.abinbev.dsa.model.CN_Product_Negotiation__c;
import com.abinbev.dsa.model.Case;
import com.abinbev.dsa.model.Distribution;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.MarketProgram;
import com.abinbev.dsa.model.SurveyTaker__c;
import com.abinbev.dsa.model.Task;
import com.abinbev.dsa.model.TradeProgram;
import com.abinbev.dsa.model.checkoutRules.CheckoutRule;

import java.util.Collections;
import java.util.List;

public class AccountPerformanceIndicatorsPresenter extends AbstractPerformanceIndicatorsPresenter {

    public static final String TAG = AccountPerformanceIndicatorsPresenter.class.getSimpleName();

    private String accountId;

    public AccountPerformanceIndicatorsPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected Integer getLatestItosSurveyScore() {
        return SurveyTaker__c.getLatestItosSurveyScore(accountId);
    }

    @Override
    protected int getOpenSurveysCount() {
        return SurveyTaker__c.getOpenSurveysCountByAccountId(accountId);
    }

    @Override
    protected int openTasksCount() {
        return Task.openTasksCountByAccountId(accountId);
    }

    @Override
    protected int getOpenCasesCount() {
        return Case.getOpenCasesCount(accountId);
    }

    @Override
    protected int getActivePromotionsCount() {
        return CN_Product_Negotiation__c.getActivePromotionsCountByAccountId(accountId);
    }

    @Override
    protected int getActiveNegotiationCount() {
        return CN_Product_Negotiation__c.getActiveNegotiationCountByAccountId(accountId);
    }

    @Override
    protected int getCustomerAssetsCount() {
        return Account_Asset__c.getCustomerAssetsCountByAccountId(accountId);
    }

    @Override
    protected int getActiveMarketProgramCount() {
        return MarketProgram.getActiveMarketProgramCountByAccountId(accountId);
    }

    @Override
    protected int getActiveEventCountWithEventCatalogue() {
        return Event.getActiveEventCountWithEventCatalogue(accountId);
    }

    @Override
    protected int getActiveContractsCount() {
        return CN_PBO_Contract__c.getCurrentContractsCount(accountId);
    }

    @Override
    protected List<CheckoutRule> getAllCheckoutRules() {
        return Collections.emptyList();
    }

    @Override
    protected int getSprCount() {
        return CN_SPR__c.getCountForCurrentWeek(accountId);
    }

    @Override
    protected int getTradeProgramCount() {
        return TradeProgram.getTradeProgramCount(accountId);
    }

    @Override
    protected int getDistributionListCount() {
        return Distribution.getActiveDistributionCount(accountId);
    }

    @Override
    public int getAssetPocCount() {
        return Account_Asset__c.getCustomerAssetsCountByAccountId(accountId);
    }

    @Override
    protected int getTechniciansCount() {
        return CN_Account_Technicians__c.getTechniciansCountByAccountId(accountId);
    }
}
