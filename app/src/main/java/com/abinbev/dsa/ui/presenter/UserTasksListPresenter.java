package com.abinbev.dsa.ui.presenter;


import com.abinbev.dsa.model.Task;

import java.util.List;

public class UserTasksListPresenter extends AbstractTasksListPresenter {
    public static final String TAG = UserTasksListPresenter.class.getSimpleName();

    private String userId;

    public UserTasksListPresenter(String userId) {
        super();
        this.userId = userId;
    }


    @Override
    protected List<Task> getTasks() {
        return Task.TasksByUserId(userId);
    }
}
