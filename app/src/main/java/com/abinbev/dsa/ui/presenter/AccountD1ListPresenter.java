package com.abinbev.dsa.ui.presenter;

import android.util.Log;

import com.abinbev.dsa.model.CN_Account_Technicians__c;
import com.abinbev.dsa.model.CN_Technicians__c;
import com.abinbev.dsa.utils.AppScheduler;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class AccountD1ListPresenter extends AbstractRxPresenter<AccountD1ListPresenter.ViewModel> implements Presenter<AccountD1ListPresenter.ViewModel> {

    public static final String TAG = AccountD1ListPresenter.class.getSimpleName();

    private String accountId;

    public interface ViewModel {
        void setD1List(List<CN_Technicians__c> technicians);

    }

    public AccountD1ListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    public void start() {
        super.start();
        getD1List();

    }

    private void getD1List() {
        addSubscription(Observable.fromCallable(
                () -> {
                    List<CN_Account_Technicians__c> list= CN_Account_Technicians__c.getD1ListByAccountId(accountId);
                    List<CN_Technicians__c> techniciansList = new ArrayList<>();
                    for(CN_Account_Technicians__c c:list){
                        CN_Technicians__c technician=CN_Technicians__c.getD1ByTechnicianId(c.getD1Id());
                        techniciansList.add(technician);
                    }
                    return techniciansList;
                }
        )
                .subscribeOn(AppScheduler.background())
                .observeOn(AppScheduler.main())
                .subscribe(
                        technicians -> {
                            viewModel().setD1List(technicians);
                        },
                        error -> Log.e(TAG, "Error getting D1 list: ", error)
                ));
    }

    @Override
    public void stop() {
        super.stop();
    }
}
