package com.abinbev.dsa.adapter;

import android.view.View;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Task;

/**
 * Created by lukaszwalukiewicz on 12.01.2016.
 */
public class AccountTasksListAdapter extends AbstractTasksListAdapter {

    public AccountTasksListAdapter() {
        super();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.task_list_entry_view;
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    protected void bind(Task task, AbstractTasksListAdapter.ViewHolder viewHolder) {
        viewHolder.subject.setText(task.getTranslatedSubject());
        viewHolder.dueDate.setText(task.getDueDate());
        viewHolder.state.setText(task.getTranslatedState());
    }
}
