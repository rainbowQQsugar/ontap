package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.CN_PBO_Contract_Item__c;
import com.abinbev.dsa.model.CN_PBO_Contract__c;
import com.abinbev.dsa.model.OnTapSettings__c;
import com.abinbev.dsa.model.TranslatableSFBaseObject;
import com.abinbev.dsa.utils.AbInBevConstants.AbInBevObjects;
import com.abinbev.dsa.utils.AbInBevConstants.PboContractFields;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import rx.Observable;

/**
 * Created by Jakub Stefanowski
 */
public class ContractsPresenter extends AbstractRxPresenter<ContractsPresenter.ViewModel> {

    public static final String TAG = ContractsPresenter.class.getSimpleName();

    private static final String FORMAT_YEAR_MONTH = "%d%02d";

    public interface ViewModel {
        void setData(List<ContractData> contracts);

        void showExpiringContracts(List<CN_PBO_Contract__c> contracts);
    }

    public static class ContractData {
        public CN_PBO_Contract__c contract;
        public int actualMonthlyVolume;
        public int targetMonthlyVolume;
        public int actualQuarterlyVolume;
        public int targetQuarterlyVolume;
    }

    private static class InitialData {
        List<ContractData> contracts;
        List<CN_PBO_Contract__c> expiringContracts;
    }

    final Calendar calendar;

    final String accountId;

    boolean isFirstStart;

    public ContractsPresenter(String accountId, boolean isFirstStart) {
        super();
        this.accountId = accountId;
        this.calendar = Calendar.getInstance();
        this.isFirstStart = isFirstStart;
    }

    @Override
    public void start() {
        super.start();

        loadInitialData(isFirstStart /* loadExpiringContracts */);

        if (isFirstStart) {
            isFirstStart = false;
        }
    }

    private void loadInitialData(final boolean loadExpiringContracts) {
        addSubscription(Observable.fromCallable(
                () -> {
                    InitialData initialData = new InitialData();
                    initialData.expiringContracts = new ArrayList<>();
                    initialData.contracts = new ArrayList<>();
                    List<CN_PBO_Contract__c> allContracts = CN_PBO_Contract__c.getCurrentContracts(accountId);

                    String objectNames = AbInBevObjects.PBO_CONTRACT;
                    String[] fieldNames = {
                            PboContractFields.STATUS
                    };
                    TranslatableSFBaseObject.addTranslations(allContracts, objectNames, fieldNames);

                    for (CN_PBO_Contract__c contract : allContracts) {
                        initialData.contracts.add(getContractData(contract));

                        OnTapSettings__c settings = OnTapSettings__c.getCurrentSettings();

                        if (loadExpiringContracts && settings.isContractExpiring(contract)) {
                            initialData.expiringContracts.add(contract);
                        }
                    }

                    return initialData;
                })

                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        initialData -> {
                            viewModel().setData(initialData.contracts);
                            if (!initialData.expiringContracts.isEmpty()) {
                                viewModel().showExpiringContracts(initialData.expiringContracts);
                            }
                        },
                        error -> Log.e(TAG, "Error getting all contracts: ", error)
                ));
    }

    private ContractData getContractData(CN_PBO_Contract__c contract) {
        ContractData contractData = new ContractData();
        contractData.contract = contract;

        if (contractData.contract != null) {
            String contractId = contractData.contract.getId();
            List<CN_PBO_Contract_Item__c> contractItems = CN_PBO_Contract_Item__c.getFromCurrentQuarter(contractId);

            for (CN_PBO_Contract_Item__c contractItem : contractItems) {
                contractData.actualQuarterlyVolume += contractItem.getActual();
                contractData.targetQuarterlyVolume += contractItem.getTarget();
                if (isForCurrentMonth(contractItem)) {
                    contractData.actualMonthlyVolume = contractItem.getActual();
                    contractData.targetMonthlyVolume = contractItem.getTarget();
                }
            }
        }

        return contractData;
    }

    private boolean isForCurrentMonth(CN_PBO_Contract_Item__c contractItem) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        String currentDate = String.format(Locale.US, FORMAT_YEAR_MONTH, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        return currentDate.equals(contractItem.getYearMonth());
    }
}
