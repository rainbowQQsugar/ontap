package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Case;

import java.util.List;


public class AccountCasesListPresenter extends AbstractCasesListPresenter {
    public static final String TAG = AccountCasesListPresenter.class.getSimpleName();

    private String accountId;

    public AccountCasesListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected List<Case> getOpenCases() {
        return Case.getOpenCasesByAccountId(accountId);
    }
}
