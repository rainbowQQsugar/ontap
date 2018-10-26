package com.abinbev.dsa.ui.presenter;


import com.abinbev.dsa.model.Task;

import java.util.List;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public class AccountTasksListPresenter extends AbstractTasksListPresenter  {
    public static final String TAG = AccountTasksListPresenter.class.getSimpleName();

    private String accountId;

    public AccountTasksListPresenter(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected List<Task> getTasks() {
        return Task.TasksByAccountId(accountId);
    }
}
