package com.abinbev.dsa.ui.presenter;

import com.abinbev.dsa.model.Case;

import java.util.List;


public class UserCasesListPresenter extends AbstractCasesListPresenter {
    public static final String TAG = UserCasesListPresenter.class.getSimpleName();

    private String userId;

    public UserCasesListPresenter(String userId) {
        super();
        this.userId = userId;
    }

    @Override
    protected List<Case> getOpenCases() {
        return Case.getOpenCasesByUserId(userId);
    }
}
